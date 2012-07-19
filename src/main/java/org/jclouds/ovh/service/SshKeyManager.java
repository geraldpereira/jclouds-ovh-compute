package org.jclouds.ovh.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.jclouds.ovh.parameters.SessionParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ovh.ws.api.OvhWsException;
import com.ovh.ws.api.auth.AuthProvider;
import com.ovh.ws.cloud._public.instance.r3.CloudInstance;
import com.ovh.ws.cloud._public.instance.r3.structure.NotificationResultStruct;
import com.ovh.ws.cloud._public.instance.r3.structure.SshKeyStruct;
import com.ovh.ws.jsonizer.api.Jsonizer;
import com.ovh.ws.jsonizer.api.http.HttpClient;

public class SshKeyManager {

	private final Logger log = LoggerFactory.getLogger(SshKeyManager.class);

	private static SshKeyManager instance;

	private List<SshKeyStruct> keys = new ArrayList<SshKeyStruct>();

	// @Inject
	// private PublicCloudSessionHandler sessionHandler;
	private PublicCloudSessionHandler sessionHandler = PublicCloudSessionHandler.getInstance();

	private CloudInstance cloudService;

	public static SshKeyManager getInstance() {
		if (instance == null) {
			instance = new SshKeyManager();
		}
		return instance;
	}

	public SshKeyManager() {
		cloudService = getCloudInstance();
	}

	private CloudInstance getCloudInstance() {
		HttpClient httpClient = Jsonizer.createHttpClient();
		httpClient.setTimeout(3000);
		return new CloudInstance(httpClient, new AuthProvider() {

			@Override
			public String getToken() {
				return sessionHandler.getSessionWithToken().getToken();
			}

			@Override
			public String getSessionId() {
				return sessionHandler.getSessionId();
			}
		});
	}

	public void getLocalPublicKeys() {
		keys.clear();
		File folder = new File(System.getProperty("user.home") + "/.ssh");
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && isPublicKey(listOfFiles[i].getName())) {
				addLocalKey(listOfFiles[i]);
			}
		}
	}

	public SshKeyStruct getSshKeyNamed(String name) {
		getLocalPublicKeys();
		for (SshKeyStruct sshKeyStruct : keys) {
			if (name.equalsIgnoreCase(sshKeyStruct.getName())) {
				return sshKeyStruct;
			}
		}
		return null;
	}

	public void printProjectSshKeys() {
		try {
			print("project ssh keys:");
			keys = cloudService.getSshKeys(SessionParameters.getJcloudsProj());
			for (SshKeyStruct sshKeyStruct : keys) {
				print(sshKeyStruct.getName());
			}
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:printProjectSshKeys:{}", this.getClass().toString(),
					e.getMessage());
		}

	}

	private boolean isPublicKey(String file) {
		if (file.contains(".pub")) {
			return true;
		}
		return false;
	}

	private void addLocalKey(File f) {
		Scanner scanner = null;
		String contents = null;
		SshKeyStruct key = null;
		try {
			scanner = new Scanner(f).useDelimiter("\\Z");
			contents = scanner.next();
			scanner.close();
			if (contents != null) {
				String[] splittedKeys = contents.split(" ");
				if (splittedKeys != null && splittedKeys.length == 3) {
					key = new SshKeyStruct();
					key.setName(splittedKeys[2]);
					key.setKey(splittedKeys[1]);
					keys.add(key);
				}
			}
		}
		catch (FileNotFoundException e) {
			log.error("Exception:{}:addLocalKey:{}", this.getClass().toString(), e.getMessage());
		}

	}

	public void printLocalSshKeys() {
		getLocalPublicKeys();
		print("local ssh keys:");
		for (SshKeyStruct sshKeyStruct : keys) {
			print("key: " + sshKeyStruct.getName() + " " + sshKeyStruct.getKey());
		}
	}

	public void applySshKeys() {
		log.debug("applySshKeys");
		try {
			List<NotificationResultStruct> notifications = cloudService
					.applySshKeys(SessionParameters.getJcloudsProj());
			if (!checkSshKeyStats(notifications)) {
				log.info("Info:" + "sshkey was not apply to whole instances");
				Thread.sleep(5000);
				applySshKeys();
			}
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:applySshKeys:{}", this.getClass().toString(), e.getMessage());
		}
		catch (InterruptedException e) {
			log.error("Exception:{}:setNewSshKey:{}", this.getClass().toString(), e.getMessage());
		}
	}

	public boolean checkSshKeyStats(List<NotificationResultStruct> notifications) {
		for (NotificationResultStruct notificationResultStruct : notifications) {
			if (!notificationResultStruct.getStatus().equalsIgnoreCase("OK")) {
				return false;
			}
		}
		return true;
	}

	private static final int THREAD_TIME_1 = 1000;
	private static final int THREAD_TIME_5 = 1000;

	public void installFromLocalSshKeys(String name, String alias) {
		getLocalPublicKeys();
		try {
			for (SshKeyStruct sshKeyStruct : keys) {
				if (name.equalsIgnoreCase(sshKeyStruct.getName())) {
					List<NotificationResultStruct> notifications = cloudService.newSshKey(
							SessionParameters.getJcloudsProj(), alias,
							"ssh-rsa " + sshKeyStruct.getKey() + " " + sshKeyStruct.getName());
					Thread.sleep(THREAD_TIME_1);
					if (!checkSshKeyStats(notifications)) {
						log.info("Info:" + "sshkey was not apply to whole instances");
						Thread.sleep(THREAD_TIME_5);
						applySshKeys();
					}
				}

			}
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:installFromLocalSshKeys:{}", this.getClass().toString(),
					e.getMessage());
		}
		catch (InterruptedException e) {
			log.error("Exception:{}:installFromLocalSshKeys:{}", this.getClass().toString(),
					e.getMessage());
		}
	}

	public void removeSshKey(String keyName) {
		try {
			String proj = SessionParameters.getJcloudsProj();
			cloudService.deleteSshKey(proj, keyName);
			applySshKeys();
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:removeSshKey:{}", this.getClass().toString(), e.getMessage());
		}
	}

	private void print(String s) {
		System.out.println(s);
	}
}
