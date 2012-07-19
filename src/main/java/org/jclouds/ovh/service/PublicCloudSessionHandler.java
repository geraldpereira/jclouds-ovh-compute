package org.jclouds.ovh.service;

import java.net.URL;

import org.jclouds.ovh.parameters.SessionParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ovh.ws.api.OvhWsException;
import com.ovh.ws.sessionhandler.r3.SessionHandler;
import com.ovh.ws.sessionhandler.r3.structure.SessionWithToken;

//@Singleton
public class PublicCloudSessionHandler {

	private  final Logger log = LoggerFactory.getLogger(PublicCloudSessionHandler.class);
	static private PublicCloudSessionHandler instance=null;
	static public PublicCloudSessionHandler getInstance(){
		if(instance==null){
			instance = new PublicCloudSessionHandler();
		}
		return instance;
	}
	
//	@Inject
	protected SessionHandler sessionHandler = new SessionHandler();
	
	protected SessionWithToken sessionWithToken;
	
	
	public String getSessionId(){
		return sessionWithToken.getSession().getId();
	}
	
	public SessionWithToken getSessionWithToken(){
		return sessionWithToken;
	}
	
	public PublicCloudSessionHandler() {
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
		} catch (OvhWsException e) {
			log.error("login:{}" , e.getMessage());
		}
	}

	public boolean isLoggin(){
		return sessionWithToken!=null;
	}
	

}
