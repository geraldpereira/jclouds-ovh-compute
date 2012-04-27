package org.jclouds.ovh.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ovh.ws.cloud._public.instance.r1.structure.SshKeyStruct;
import com.ovh.ws.common.OvhWsException;

public class SshKeyManager {

	private final Logger log = LoggerFactory.getLogger(SshKeyManager.class);
	
	private static SshKeyManager instance;
	
	private List<SshKeyStruct> keys = new ArrayList<SshKeyStruct>();
	
	private PublicCloudService publicCloudService = PublicCloudService.getInstance();
	
	public static SshKeyManager getInstance(){
		if (instance == null) {
			instance = new SshKeyManager();
		}
		return instance;
	}
	
	public void getLocalPublicKeys(){
		keys.clear();
		File folder = new File(System.getProperty("user.home") + "/.ssh");
	    File[] listOfFiles = folder.listFiles();

	    for (int i = 0; i < listOfFiles.length; i++) {
	      if (listOfFiles[i].isFile()) {
	        if (isPublicKey(listOfFiles[i].getName())) {
				addLocalKey(listOfFiles[i]);
			}
	      }
	    }
	}
	
	
	public SshKeyStruct getSshKeyNamed(String name){
		getLocalPublicKeys();
		for (SshKeyStruct sshKeyStruct : keys) {
		      if (name.equalsIgnoreCase(sshKeyStruct.getName())) {
		    	  return sshKeyStruct;
		      }
		}
		return null;
	}
	
	public void printProjectSshKeys(){
		List<SshKeyStruct> keys;
		try {
			System.out.println("project ssh keys:");
			keys = publicCloudService.getSshKeys();
			for (SshKeyStruct sshKeyStruct : keys) {
				System.out.println(sshKeyStruct.getName());
			}
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:printProjectSshKeys:{}" ,this.getClass().toString(),
					 e.getMessage());
		}
		
	}
	
	
	private boolean isPublicKey(String file){
		if (file.contains(".pub")) {
			return true;
		}
		return false;
	}
	
	private void addLocalKey(File f){ 
		Scanner scanner = null;
		String contents = null;
		SshKeyStruct key = null;
		try {
			scanner = new Scanner(f).useDelimiter("\\Z");
			contents = scanner.next();
		    scanner.close();
		    if (contents != null) {
				String [] splittedKeys = contents.split(" ");
				if (splittedKeys!=null && splittedKeys.length==3) {
					key = new SshKeyStruct(splittedKeys[2], splittedKeys[1]);
					keys.add(key);
				}
			}
		}
		catch (FileNotFoundException e) {
			log.error("Exception:{}:addLocalKey:{}" ,this.getClass().toString(),
					 e.getMessage());
		}
		
	}
	
	public void printLocalSshKeys(){
		getLocalPublicKeys();
		System.out.println("local ssh keys:");
		for (SshKeyStruct sshKeyStruct : keys) {
			System.out.println("key: " + sshKeyStruct.getName() + " " + sshKeyStruct.getKey());
		}
	}
	
	public void installFromLocalSshKeys(String name, String alias)throws OvhWsException, InterruptedException{
		getLocalPublicKeys();
		try {
		for (SshKeyStruct sshKeyStruct : keys) {
			if (name.equalsIgnoreCase(sshKeyStruct.getName())) {
					publicCloudService.setNewSshKey(alias, "ssh-rsa " + sshKeyStruct.getKey() + " " + sshKeyStruct.getName());
				}
				
			}
		}catch (OvhWsException e) {
			log.error("Exception:{}:installFromLocalSshKeys:{}" ,this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}
	
	public void removeSshKey(String keyName)throws OvhWsException{
		try {
			publicCloudService.removeSshKey(keyName);
		}catch (OvhWsException e) {
			log.error("Exception:{}:removeSshKey:{}" ,this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}
}
