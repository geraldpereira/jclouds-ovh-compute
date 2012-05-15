package org.jclouds.ovh.service;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ovh.ws.common.OvhWsException;
import com.ovh.ws.sessionhandler.r3.SessionHandler;
import com.ovh.ws.sessionhandler.r3.structure.SessionWithToken;

public class PublicCloudSessionHandler {

	private  final Logger log = LoggerFactory.getLogger(PublicCloudSessionHandler.class);
	static private PublicCloudSessionHandler instance=null;
	static public PublicCloudSessionHandler getInstance(){
		if(instance==null){
			instance = new PublicCloudSessionHandler();
		}
		return instance;
	}
	
	
	
	protected SessionHandler sessionHandler;

	protected SessionWithToken sessionWithToken = null;
	
	
	public String getSessionId(){
		return sessionWithToken.getSession().getId();
	}
	
	public SessionWithToken getSessionWithToken(){
		return sessionWithToken;
	}
	
	public PublicCloudSessionHandler() {
		sessionHandler = new SessionHandler();
	}
	
	
	
	public void setUrl(URL url) {
		sessionHandler.setUrl(url);
	}
	
	public void password(final String sessionId, final String login,final String password) {
		try {
			sessionHandler.password(sessionId, login, password);
		} catch (OvhWsException e) {
			log.error("Exception:{}:password:{}" ,this.getClass().toString(),
					 e.getMessage());
		}
	}

	public void logout() {
		try {
			if (isLoggin()) {
				sessionHandler.logout(getSessionId());
			}
		} catch (OvhWsException e) {
			log.error("xception:{}:logout:{}" ,this.getClass().toString(),
					 e.getMessage());
		}
	}

	public void login(String login, String password, String language, Boolean multisession) {
		try {
			sessionWithToken = sessionHandler.login(login, password, language);
		} catch (OvhWsException e) {
			log.error("Exception:{}:login:{}" ,this.getClass().toString(),
					 e.getMessage());
		}
	}

	public boolean isLoggin(){
		return sessionWithToken!=null;
	}
	

}
