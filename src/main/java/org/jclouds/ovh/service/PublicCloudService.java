package org.jclouds.ovh.service;

import java.util.ArrayList;
import java.util.List;

import org.jclouds.ovh.parameters.SessionParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.ovh.ws.cloud._public.instance.r1.CloudInstance;
import com.ovh.ws.cloud._public.instance.r1.structure.CredentialsStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.DistributionStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.InstanceStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.NotificationResultStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.OfferStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.ProjectStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.SshKeyStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.TaskStatusEnum;
import com.ovh.ws.cloud._public.instance.r1.structure.TaskStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.ZoneStruct;
import com.ovh.ws.common.OvhWsException;
import com.ovh.ws.definitions.ovhgenerictype.r2.structure.CommandStatus;
import com.ovh.ws.jsonizer.Jsonizer;
import com.ovh.ws.jsonizer.common.http.HttpClient;

public class PublicCloudService  {

	private final Logger log = LoggerFactory.getLogger(PublicCloudService.class);

	private static PublicCloudService instance = null;

	public static PublicCloudService getInstance() {
		if (instance == null) {
			instance = new PublicCloudService();
		}
		return instance;
	}
	
	private PublicCloudSessionHandler sessionHandler = new PublicCloudSessionHandler();

	private CloudInstance cloudService = new CloudInstance();
	
	protected ProjectStruct currentProject = null;
	protected List<InstanceStruct> projectInstances = null;
	protected InstanceStruct currentInstance = null;
	protected CredentialsStruct currentCredential = null;
		
	
	
	public List<InstanceStruct> getInstances() throws Exception {
		try {
			if (projectInstances == null)
				this.setCurrentInstances();
		}
		catch (Exception e) {
			log.error("Exception:{}:getInstances:" ,this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
		return projectInstances;
	}

	private void checkSession(){
		if (sessionHandler.getSessionWithToken() == null)
			login();
	}
	

	
	public List<OfferStruct> getOffers() throws OvhWsException {
		log.debug("getOffers");

		try {
			checkSession();
			return cloudService.getOffers(sessionHandler.getSessionId());
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:getOffers:{}" ,this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	
	public OfferStruct getOfferNamed(final String name) {
		log.debug("getOfferNamed");

		try {
			return Iterables.find(getOffers(), new Predicate<OfferStruct>() {
		         @Override
		         public boolean apply(OfferStruct offer) {
		            return offer.getName().equalsIgnoreCase(name);
		         }
		      });
			
		}
		catch (Exception e) {
			log.error("Exception:{}:getOfferNamed:{}" ,this.getClass().toString(),
					 e.getMessage());
		}
		return null;
	}

	public List<DistributionStruct> getDistributions() throws OvhWsException {
		log.debug("getDistributions");

		try {
			checkSession();
			return cloudService.getDistributions(sessionHandler.getSessionId());
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:getDistributions:{}" ,this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public DistributionStruct getDistributionNamed(final String name) {
		log.debug("getDistributionNamed");

		try {
			return Iterables.find(getDistributions(), new Predicate<DistributionStruct>() {

				@Override
				public boolean apply(DistributionStruct input) {
					return input.getName().equalsIgnoreCase(name);
				}
			});
		}
		catch (Exception e) {
			log.error("Exception:{}:getDistributionNamed:{}",this.getClass().toString(),
					 e.getMessage());
		}	
		return null;
	}


	public List<ZoneStruct> getZones() throws OvhWsException {
		log.debug("getZones");

		try {
			checkSession();
			return cloudService.getZones(sessionHandler.getSessionId());
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:getZones:" ,this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public ZoneStruct getZoneNamed(final String name) {
		log.debug("getZoneNamed");

		try {
			return Iterables.find(getZones(), new Predicate<ZoneStruct>() {

				@Override
				public boolean apply(ZoneStruct input) {
					return input.getName().equalsIgnoreCase(name);
				}
			});
		}
		catch (Exception e) {
			log.error("Exception:{}:getZoneNamed:" ,this.getClass().toString(),
					 e.getMessage());
		}
		return null;

	}

	
	/**
	 * login methods
	 */
	public void login() {
		log.debug("login");
		if (!sessionHandler.isLoggin()) {
			sessionHandler.login(SessionParameters.getJcloudsLogin(), SessionParameters.getJcloudsPwd(),
					SessionParameters.CLOUD_LANG, SessionParameters.CLOUD_MULT);
			HttpClient httpClient = Jsonizer.createHttpClient();
	        httpClient.setTimeout(3000);
			httpClient.setRequestSigned(true);
			httpClient.setToken(sessionHandler.getSessionWithToken().getToken());
			cloudService = new CloudInstance(httpClient);
		}
	}

	public void logout() {
		log.debug("logout");
		if (sessionHandler.isLoggin()) {
			sessionHandler.logout();
		}
	}

	public void setCurrentProjectNamed(final String name) throws Exception {
		log.debug("setCurrentProjectNamed");

		if (!sessionHandler.isLoggin())
			login();

		try {
			currentProject = Iterables.find(cloudService
					.getProjects(sessionHandler.getSessionId()), new Predicate<ProjectStruct>() {

						@Override
						public boolean apply(ProjectStruct input) {
							return input.getName().equalsIgnoreCase(name);
						}
					});
		}
		catch (Exception e) {
			log.error("Exception:{}:setCurrentProjectNamed:{}",this.getClass().toString(),
					 e.getMessage());
		}
	}

	public void setCurrentInstances() throws Exception {
		log.debug("setCurrentInstances");

		if (!sessionHandler.isLoggin())
			login();

		try {
			if (currentProject == null)
				setCurrentProjectNamed(SessionParameters.getJcloudsProj());

			if (projectInstances == null)
				projectInstances = new ArrayList<InstanceStruct>();
			if (projectInstances.size() > 0)
				projectInstances.clear();

			List<InstanceStruct> inst = cloudService.getInstances(sessionHandler.getSessionId(),
					currentProject.getId(), currentProject.getName());
			projectInstances.addAll(inst);

		}
		catch (Exception e) {
			log.error("Exception:{}:setCurrentInstances:{}",this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public void newInstance(String zoneId, String name, String offerId, String distributionId)
			throws Exception {
		log.debug("newInstance");

		try {
			if (currentProject == null)
				setCurrentProjectNamed(SessionParameters.getJcloudsProj());
			CommandStatus status = cloudService.newInstance(sessionHandler.getSessionId(),
					currentProject.getId(), currentProject.getName(), name, zoneId, "", 0l,
					offerId, distributionId);
			waitForCommandDone(status);
			setCurrentInstances();
		}
		catch (Exception e) {
			log.error("Exception:{}:newInstance:{}" ,this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public void newInstanceNamed(String name) throws Exception {
		log.debug("newInstanceNamed");

		try {
			List<ZoneStruct> zones = getZones();
			List<OfferStruct> offers = getOffers();
			List<DistributionStruct> distributions = getDistributions();
			
			newInstance(zones.get(0).getName(), name, offers.get(0).getName(), distributions.get(0)
					.getName());
		}
		catch (Exception e) {
			log.error("Exception:{}:newInstanceNamed:{}",this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public InstanceStruct getInstanceNamed(final String name) {

		try {
			// if (projectInstances == null)
			setCurrentInstances(); // refresh all node with stats

			log.debug("getInstanceNamed");
			return Iterables.find(projectInstances, new Predicate<InstanceStruct>() {

				@Override
				public boolean apply(InstanceStruct input) {
					return input.getName().equalsIgnoreCase(name);
				}
			});
		}
		catch (Exception e) {
			log.error("Exception:{}:getInstanceNamed:{}",this.getClass().toString(),
					 e.getMessage());
		}
		return null;
	}

	public void deleteInstanceNamed(String name) throws Exception {
		log.debug("deleteInstanceNamed");

		setCurrentInstances();
		InstanceStruct deletedInstance = getInstanceNamed(name);

		if (deletedInstance == null)
			return;

		try {
			CommandStatus command = cloudService.deleteInstance(sessionHandler.getSessionId(),
					deletedInstance.getId());
			waitForCommandDone(command);
		}
		catch (Exception e) {
			log.error("Exception:{}:deleteInstanceNamed:{}",this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public void rebootInstanceNamed(String name) throws Exception {
		log.debug("rebootInstanceNamed:" + name);

		try {
			setCurrentInstances();
			InstanceStruct i = getInstanceNamed(name);

			if (i == null)
				return;
			
			cloudService.rebootInstance(sessionHandler.getSessionId(), i.getId());
		}
		catch (Exception e) {
			log.error("Exception:{}:rebootInstanceNamed:{}",this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public void startInstanceNamed(String name) throws Exception {
		log.debug("startInstanceNamed");

		try {
			setCurrentInstances();
			InstanceStruct i = getInstanceNamed(name);

			if (i == null)
				return;
			cloudService.startInstance(sessionHandler.getSessionId(), i.getId());
		}
		catch (Exception e) {
			log.error("Exception:{}:startInstanceNamed:{}",this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public void stopInstanceNamed(String name) throws Exception {
		log.debug("stopInstanceNamed");

		try {
			setCurrentInstances();
			InstanceStruct i = getInstanceNamed(name);

			if (i == null)
				return;
			cloudService.stopInstance(sessionHandler.getSessionId(), i.getId());
		}
		catch (Exception e) {
			log.error("Exception:{}:stopInstanceNamed:{}",this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public void setCurrentCrendential() throws OvhWsException {
		log.debug("setCurrentCrendential");
		currentCredential = getCrendential(currentInstance);
	}

	public CredentialsStruct getCrendential(InstanceStruct instance) throws OvhWsException {
		log.debug("getCrendential");
		CredentialsStruct credential = null;
		try {
			credential = cloudService.getLoginInformations(sessionHandler.getSessionId(),
					instance.getId());
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:getCrendential:" ,this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
		return credential;
	}

	public CredentialsStruct getCrendentialOfInstanceNamed(String name) throws OvhWsException {
		CredentialsStruct credential = null;
		try {
			InstanceStruct i = getInstanceNamed(name);
			if (i == null)
				return null;
			log.debug("getCrendentialOfInstanceNamed");
			credential = cloudService.getLoginInformations(sessionHandler.getSessionId(), i.getId());
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:getCrendentialOfInstanceNamed:{}"
					,this.getClass().toString(), e.getMessage());
			throw e;
		}
		return credential;
	}

	public void setNewDefaultSshKey() throws OvhWsException {
		String user = System.getProperty("user.name");
		try {
			setNewSshKey(user);
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:setNewDefaultSshKey:{}" ,this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	private static int THREAD_TIME_1 = 1000;
	private static int THREAD_TIME_5 = 1000;
	
	public void setNewSshKey(String name) throws OvhWsException {
		if (SessionParameters.loginSsh == null) {
			return;
		}
		try {
			String cred = SessionParameters.loginSsh.getPassword();
			setNewSshKey(name, cred);
		}
		catch (OvhWsException e) {throw e;}
		catch (InterruptedException e) {
			log.error("Exception:{}:setNewSshKey:{}" ,this.getClass().toString(),
				 e.getMessage());}
	}
	
	public void setNewSshKey(String name, String credential)throws OvhWsException, InterruptedException {
		
		log.debug("setNewSshKey");
		try {
			String proj = SessionParameters.getJcloudsProj();
			List<NotificationResultStruct> notifications = cloudService.newSshKey(sessionHandler.getSessionWithToken()
					.getSession().getId(), proj, name, credential);
			Thread.sleep(THREAD_TIME_1);
			if (!checkSshKeyStats(notifications)) {
				log.info("Info:" + "sshkey was not apply to whole instances");
				Thread.sleep(THREAD_TIME_5);
				applySshKeys();
			}
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:setNewSshKey:{}" ,this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
		catch (InterruptedException e) {
			log.error("Exception:{}:setNewSshKey:{}" ,this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public void applySshKeys() throws OvhWsException {
		log.debug("applySshKeys");
		try {
			List<NotificationResultStruct> notifications = cloudService.applySshKeys(sessionHandler.getSessionWithToken()
					.getSession().getId(), SessionParameters.getJcloudsProj());
			if (!checkSshKeyStats(notifications)) {
				log.info("Info:" + "sshkey was not apply to whole instances");
				Thread.sleep(5000);
				applySshKeys();
			}
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:applySshKeys:{}" ,this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
		catch (InterruptedException e) {
			log.error("Exception:{}:setNewSshKey:{}" ,this.getClass().toString(),
					 e.getMessage());
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

	public List<SshKeyStruct> getSshKeys() throws OvhWsException{
		List<SshKeyStruct> keys = null;
		try {
			keys = cloudService.getSshKeys(sessionHandler.getSessionId(),
					SessionParameters.getJcloudsProj());

		}
		catch (OvhWsException e) {
			log.error("Exception:{}:needNewSshKey:{}" ,this.getClass().toString() , e.getMessage());
			throw e;
		}
		return keys;
	}
	
	public boolean needNewSshKey() throws OvhWsException {
		List<SshKeyStruct> keys;
		try {
			keys = getSshKeys();

			String user = System.getProperty("user.name");
			for (SshKeyStruct sshKeyStruct : keys) {
				if (user.equalsIgnoreCase(sshKeyStruct.getName())) {
					return false;
				}
			}
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:needNewSshKey:{}" ,this.getClass().toString() , e.getMessage());
			throw e;
		}
		return true;
	}

	public void removeSshKey(String sshKeyName) throws OvhWsException{
		log.debug("removeSshKey");
		try {
			String proj = SessionParameters.getJcloudsProj();
//			SshKeyStruct sshkey = 
					cloudService.deleteSshKey(sessionHandler.getSessionWithToken()
					.getSession().getId(), proj, sshKeyName);
			applySshKeys();
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:removeSshKey:{}" ,this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}
	
	public void waitForCommandDone(CommandStatus command) throws OvhWsException {
		log.debug("getCommandStatus");
		if (command == null)
			return;
		try {
			TaskStruct task = null;
			do {
				task = cloudService.getTask(sessionHandler.getSessionId(), currentProject.getId(),
						command.getTasks().get(0).getId());
				try {
					if (task.getStatus() != TaskStatusEnum.DONE) {
						Thread.currentThread();
						Thread.sleep(10000l);
					}
				}
				catch (InterruptedException ie) {
				}
			} while (task.getStatus() != TaskStatusEnum.DONE);

		}
		catch (OvhWsException e) {
			log.error("Exception:{}:getCommandStatus:{}",this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}
}
