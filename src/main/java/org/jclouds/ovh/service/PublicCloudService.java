package org.jclouds.ovh.service;

import java.util.ArrayList;
import java.util.List;

import org.jclouds.ovh.parameters.SessionParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.ovh.ws.jsonizer.common.http.HttpClient;

public class PublicCloudService extends PublicCloudSessionHandler {

	private final Logger log = LoggerFactory.getLogger(PublicCloudService.class);

	private static PublicCloudService instance = null;

	public static PublicCloudService getInstance() {
		if (instance == null) {
			instance = new PublicCloudService();
		}
		return instance;
	}

	private CloudInstance cloudService = new CloudInstance();

	public PublicCloudService() {
		super();
	}

	public List<InstanceStruct> getInstances() throws OvhWsException {
		try {
			if (projectInstances == null)
				this.setCurrentInstances();
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:getInstances:" ,this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
		return projectInstances;
	}

	/**
	 * init local variables with the current parameters of OVH
	 * @throws OvhWsException 
	 */
	public void initCloudServiceParameters() throws OvhWsException {
		log.debug("initCloudServiceParameters");
		try {
			initOffers();
			initDistributions();
			initZones();
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:initCloudServiceParameters:{}" ,this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public void initOffers() throws OvhWsException {
		log.debug("initOffers");

		if (offers != null)
			return;

		try {
			offers = cloudService.getOffers(session.getSession().getId());
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:initOffers:{}",this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public List<OfferStruct> getOffers() throws OvhWsException {
		log.debug("getOffers");

		try {
			if (session == null)
				login();
			return cloudService.getOffers(session.getSession().getId());
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:getOffers:{}" ,this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public OfferStruct getOfferNamed(String name) throws OvhWsException {
		log.debug("getOfferNamed");

		try {
			if (offers == null)
				initOffers();
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:getOfferNamed:{}" ,this.getClass().toString(),
					 e.getMessage());
			throw e;
		}

		for (OfferStruct offer : offers) {
			if (offer.getName().equalsIgnoreCase(name)) {
				return offer;
			}
		}
		return null;
	}

	public void initDistributions() throws OvhWsException {
		log.debug("initDistributions");

		if (distributions != null)
			return;

		try {
			distributions = getDistributions();
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:initDistributions:{}",this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public List<DistributionStruct> getDistributions() throws OvhWsException {
		log.debug("getDistributions");

		try {
			if (session == null)
				login();
			return cloudService.getDistributions(session.getSession().getId());
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:getDistributions:{}" ,this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public DistributionStruct getDistributionNamed(String name) {
		log.debug("getDistributionNamed");

		try {
			if (distributions == null)
				initDistributions();
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:getDistributionNamed:{}",this.getClass().toString(),
					 e.getMessage());
		}

		for (DistributionStruct distib : distributions) {
			if (distib.getName().equalsIgnoreCase(name)) {
				return distib;
			}
		}
		return null;
	}

	public void initZones() throws OvhWsException {
		log.debug("initZones");

		if (zones != null)
			return;

		try {
			zones = getZones();
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:initZones:{}" ,this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public List<ZoneStruct> getZones() throws OvhWsException {
		log.debug("getZones");

		try {
			if (session == null)
				login();
			return cloudService.getZones(session.getSession().getId());
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:getZones:" ,this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public ZoneStruct getZoneNamed(String name) {
		log.debug("getZoneNamed");

		try {
			if (zones == null)
				initZones();
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:getZoneNamed:" ,this.getClass().toString(),
					 e.getMessage());
		}

		for (ZoneStruct zone : zones) {
			if (zone.getName().equalsIgnoreCase(name)) {
				return zone;
			}
		}
		return null;
	}

	/**
	 * login methods
	 */
	public void login() {
		log.debug("login");
		if (!isLoggin()) {
			super.login(SessionParameters.getJcloudsLogin(), SessionParameters.getJcloudsPwd(),
					SessionParameters.CLOUD_LANG, SessionParameters.CLOUD_MULT);
			HttpClient httpClient = cloudService.getHttpClient();
			httpClient.setToken(session.getToken());
			httpClient.setRequestSigned(true);
			cloudService.setHttpClient(httpClient);
		}
	}

	public void logout() {
		log.debug("logout");
		if (isLoggin()) {
			super.logout();
		}
	}

	public void setCurrentProjectNamed(String name) throws OvhWsException {
		log.debug("setCurrentProjectNamed");

		if (!isLoggin())
			login();

		try {
			List<ProjectStruct> projectList = cloudService
					.getProjects(session.getSession().getId());
			for (ProjectStruct projectStruct : projectList) {
				if (projectStruct.getName().equalsIgnoreCase(name)) {
					currentProject = projectStruct;
				}
			}
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:setCurrentProjectNamed:{}",this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public void setCurrentInstances() throws OvhWsException {
		log.debug("setCurrentInstances");

		if (!isLoggin())
			login();
		if (currentProject == null)
			setCurrentProjectNamed(SessionParameters.getJcloudsProj());

		try {
			if (projectInstances == null)
				projectInstances = new ArrayList<InstanceStruct>();
			if (projectInstances.size() > 0)
				projectInstances.clear();

			List<InstanceStruct> inst = cloudService.getInstances(session.getSession().getId(),
					currentProject.getId(), currentProject.getName());
			projectInstances.addAll(inst);

		}
		catch (OvhWsException e) {
			log.error("Exception:{}:setCurrentInstances:{}",this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public void newInstance(String zoneId, String name, String offerId, String distributionId)
			throws OvhWsException {
		log.debug("newInstance");

		try {
			if (currentProject == null)
				setCurrentProjectNamed(SessionParameters.getJcloudsProj());
			CommandStatus status = cloudService.newInstance(session.getSession().getId(),
					currentProject.getId(), currentProject.getName(), name, zoneId, "", 0l,
					offerId, distributionId);
			waitForCommandDone(status);
			setCurrentInstances();
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:newInstance:{}" ,this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public void newInstanceNamed(String name) throws OvhWsException {
		log.debug("newInstanceNamed");

		try {
			if (zones == null)
				initZones();
			if (offers == null)
				initOffers();
			if (distributions == null)
				initDistributions();
			newInstance(zones.get(0).getName(), name, offers.get(0).getName(), distributions.get(0)
					.getName());
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:newInstanceNamed:{}",this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public InstanceStruct getInstanceNamed(String name) {

		try {
			// if (projectInstances == null)
			setCurrentInstances(); // refresh all node with stats

			log.debug("getInstanceNamed");
			for (InstanceStruct instanceStruct : projectInstances) {
				if (instanceStruct.getName().equalsIgnoreCase(name)) {
					return instanceStruct;
				}
			}
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:getInstanceNamed:{}",this.getClass().toString(),
					 e.getMessage());
		}
		return null;
	}

	public void deleteInstanceNamed(String name) throws OvhWsException {
		log.debug("deleteInstanceNamed");

		setCurrentInstances();
		InstanceStruct deletedInstance = getInstanceNamed(name);

		if (deletedInstance == null)
			return;

		try {
			CommandStatus command = cloudService.deleteInstance(session.getSession().getId(),
					deletedInstance.getId());
			waitForCommandDone(command);
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:deleteInstanceNamed:{}",this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public void rebootInstanceNamed(String name) throws OvhWsException {
		log.debug("rebootInstanceNamed:" + name);

		try {
			setCurrentInstances();
			InstanceStruct i = getInstanceNamed(name);

			if (i == null)
				return;
			
			cloudService.rebootInstance(session.getSession().getId(), i.getId());
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:rebootInstanceNamed:{}",this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public void startInstanceNamed(String name) throws OvhWsException {
		log.debug("startInstanceNamed");

		try {
			setCurrentInstances();
			InstanceStruct i = getInstanceNamed(name);

			if (i == null)
				return;
			cloudService.startInstance(session.getSession().getId(), i.getId());
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:startInstanceNamed:{}",this.getClass().toString(),
					 e.getMessage());
			throw e;
		}
	}

	public void stopInstanceNamed(String name) throws OvhWsException {
		log.debug("stopInstanceNamed");

		try {
			setCurrentInstances();
			InstanceStruct i = getInstanceNamed(name);

			if (i == null)
				return;
			cloudService.stopInstance(session.getSession().getId(), i.getId());
		}
		catch (OvhWsException e) {
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
			credential = cloudService.getLoginInformations(session.getSession().getId(),
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
			credential = cloudService.getLoginInformations(session.getSession().getId(), i.getId());
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
			List<NotificationResultStruct> notifications = cloudService.newSshKey(session
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
			List<NotificationResultStruct> notifications = cloudService.applySshKeys(session
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
			keys = cloudService.getSshKeys(session.getSession().getId(),
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
					cloudService.deleteSshKey(session
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
				task = cloudService.getTask(session.getSession().getId(), currentProject.getId(),
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
