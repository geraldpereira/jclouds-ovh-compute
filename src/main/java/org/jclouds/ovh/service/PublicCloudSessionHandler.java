package org.jclouds.ovh.service;

import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ovh.ws.cloud._public.instance.r1.structure.CredentialsStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.DistributionStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.InstanceStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.OfferStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.ProjectStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.ZoneStruct;
import com.ovh.ws.common.OvhWsException;
import com.ovh.ws.sessionhandler.r3.SessionHandler;
import com.ovh.ws.sessionhandler.r3.structure.SessionWithToken;;

public class PublicCloudSessionHandler {

	private  final Logger log = LoggerFactory.getLogger(PublicCloudSessionHandler.class);
	static private PublicCloudSessionHandler instance=null;
	static public PublicCloudSessionHandler getInstance(){
		if(instance==null){
			instance = new PublicCloudSessionHandler();
		}
		return instance;
	}
	
	
	
	protected ProjectStruct currentProject = null;
	protected List<InstanceStruct> projectInstances = null;
	protected InstanceStruct currentInstance = null;
	protected CredentialsStruct currentCredential = null;
	protected SessionWithToken session = null;
	
	protected List<OfferStruct> offers = null ;
	protected List<DistributionStruct> distributions = null ;
	protected List<ZoneStruct> zones = null ;
	
	protected SessionHandler sessionHandler;

	
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
				sessionHandler.logout(session.getSession().getId());
			}
		} catch (OvhWsException e) {
			log.error("xception:{}:logout:{}" ,this.getClass().toString(),
					 e.getMessage());
		}
	}

	public void login(String login, String password, String language, Boolean multisession) {
		try {
			session = sessionHandler.login(login, password, language);
		} catch (OvhWsException e) {
			log.error("Exception:{}:login:{}" ,this.getClass().toString(),
					 e.getMessage());
		}
	}

	public boolean isLoggin(){
		return session!=null?true:false;
	}
	

}
