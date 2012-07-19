package org.jclouds.ovh.service;

import java.net.URL;

import org.jclouds.ovh.parameters.SessionParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ovh.ws.api.OvhWsException;
import com.ovh.ws.sessionhandler.r3.SessionHandler;
import com.ovh.ws.sessionhandler.r3.structure.SessionWithToken;

@Singleton
public class PublicCloudSessionHandler{

	private  final Logger log = LoggerFactory.getLogger(PublicCloudSessionHandler.class);
//	private static PublicCloudSessionHandler instance=null;
//	public static PublicCloudSessionHandler getInstance(){
//		if(instance==null){
//			instance = new PublicCloudSessionHandler();
//		}
//		return instance;
//	}
	
	@Inject
	protected SessionHandler sessionHandler;
	
	protected SessionWithToken sessionWithToken;
	
	
	public SessionWithToken getSessionWithToken(){
		return sessionWithToken;
	}
	
	
	public void setUrl(URL url) {
		sessionHandler.setUrl(url);
	}
	
	public void password(final String login,final String password) {
		try {
			sessionHandler.password( login, password);
		} catch (OvhWsException e) {
			log.error("password:{}" ,e.getMessage());
		}
	}

	public void logout() {
		try {
			if (isLoggin()) {
				sessionHandler.logout();
			}
		} catch (OvhWsException e) {
			log.error("logout:{}" , e.getMessage());
		}
	}

	public void login() {
		try {
			sessionWithToken = sessionHandler.login(SessionParameters.getJcloudsLogin(),
					SessionParameters.getJcloudsPwd(), SessionParameters.CLOUD_LANG,null);
			System.out.println(sessionWithToken);
		} catch (OvhWsException e) {
			log.error("login:{}" , e.getMessage());
		}
	}

	public boolean isLoggin(){
		return sessionWithToken!=null;
	}

	public String getSessionId() {
		if (sessionWithToken==null) {
			return null;
		}
		return sessionWithToken.getSession().getId();
	}
	
	public String getToken(){
		if (sessionWithToken==null) {
			return null;
		}
		return sessionWithToken.getToken();
	}
	
}
