/**
 * Licensed to jclouds, Inc. (jclouds) under one or more
 * contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  jclouds licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jclouds.ovh;

import java.util.List;

import javax.inject.Singleton;

import org.jclouds.ovh.parameters.SessionParameters;
import org.jclouds.ovh.service.PublicCloudSessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.ovh.ws.api.OvhWsException;
import com.ovh.ws.api.auth.AuthProvider;
import com.ovh.ws.cloud._public.instance.r1.CloudInstance;
import com.ovh.ws.cloud._public.instance.r1.structure.CredentialsStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.DistributionStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.InstanceStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.OfferStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.ProjectStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.TaskStatusEnum;
import com.ovh.ws.cloud._public.instance.r1.structure.TaskStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.ZoneStruct;
import com.ovh.ws.definitions.ovhgenerictype.r2.structure.CommandStatus;
import com.ovh.ws.jsonizer.api.Jsonizer;
import com.ovh.ws.jsonizer.api.http.HttpClient;

/**
 * This would be replaced with the real connection to the service that can
 * create/list/reboot/get/destroy things
 * 
 * @author David Krolak
 */
@Singleton
public class OVHComputeClient {

	private final Logger log = LoggerFactory.getLogger(OVHComputeClient.class);

//	@Inject
//	private PublicCloudSessionHandler sessionHandler;
	private PublicCloudSessionHandler sessionHandler = PublicCloudSessionHandler.getInstance();

//	@Inject
//	private CloudInstance cloudService;
	private CloudInstance cloudService = null;

	protected ProjectStruct currentProject = null;


	private void checkSession() {
		if (sessionHandler.getSessionWithToken() == null)
			login();
	}

	/**
	 * login methods
	 */
	private void login() {
		log.debug("login");
		if (!sessionHandler.isLoggin()) {
			sessionHandler.login();
			if (cloudService == null) {
				cloudService = getCloudInstance();
			}
		}
	}
	
	 private CloudInstance getCloudInstance(){
		   HttpClient httpClient = Jsonizer.createHttpClient();
			httpClient.setTimeout(3000);
			return new CloudInstance(httpClient,sessionHandler.getSessionWithToken());
	   }

	private void setCurrentProjectNamed(final String name) throws Exception {
		log.debug("setCurrentProjectNamed");

		if (!sessionHandler.isLoggin())
			login();

		currentProject = Iterables.find(cloudService.getProjects(), new Predicate<ProjectStruct>() {

			@Override
			public boolean apply(ProjectStruct input) {
				return input.getName().equalsIgnoreCase(name);
			}
		});

	}

	public void waitForCommandDone(CommandStatus command) throws OvhWsException {
		log.debug("getCommandStatus");
		if (command == null)
			return;

		TaskStruct task = null;
		do {
			task = cloudService.getTask(currentProject.getId(), command.getTasks().get(0).getId());
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


	/**
	 * simulate creating a server, as this is really going to happen with the
	 * api underneath
	 * 
	 * @param name
	 * @param name
	 * @param imageId
	 * @param hardwareId
	 * @return new server
	 */
	public InstanceStruct createServerInDC(String datacenter, String name, String imageId,
			String hardwareId)  throws Exception {
		if (currentProject == null)
			setCurrentProjectNamed(SessionParameters.getJcloudsProj());
		CommandStatus status = cloudService.newInstance(currentProject.getId(),
				currentProject.getName(), name, datacenter, "", 0l, hardwareId, imageId);
		waitForCommandDone(status);
		return getServer(name);
	}

	public Iterable<InstanceStruct> listServers() throws Exception {
		checkSession();
		if (currentProject == null)
			setCurrentProjectNamed(SessionParameters.getJcloudsProj());
		return cloudService.getInstances(currentProject.getId(), currentProject.getName());
	}
	
	public InstanceStruct getServer(final String name) throws Exception {
		return Iterables.find(listServers(), new Predicate<InstanceStruct>() {

			@Override
			public boolean apply(InstanceStruct input) {
				return input.getName().equalsIgnoreCase(name);
			}
		});
	}

	public List<DistributionStruct> listImages() throws Exception {
		checkSession();
		return cloudService.getDistributions();
	}
	
	public DistributionStruct getImage(final String name) throws Exception {
		listServers();
		return Iterables.find(listImages(), new Predicate<DistributionStruct>() {

			@Override
			public boolean apply(DistributionStruct input) {
				return input.getName().equalsIgnoreCase(name);
			}
		});
	}

	public List<OfferStruct> listHardware() throws Exception {
		checkSession();
		return cloudService.getOffers();
	}
	
	public OfferStruct getHardware(final String name) throws Exception {
		return Iterables.find(listHardware(), new Predicate<OfferStruct>() {
			@Override
			public boolean apply(OfferStruct offer) {
				return offer.getName().equalsIgnoreCase(name);
			}
		});
	}

	public List<ZoneStruct> listZones() throws Exception {
		checkSession();
		return cloudService.getZones();
	}

	public void destroyServer(String name) throws Exception {
		CommandStatus command = cloudService.deleteInstance(getServer(name).getId());
		waitForCommandDone(command);
	}

	public void rebootServer(String name) throws Exception {
		cloudService.rebootInstance(getServer(name).getId());
	}

	public void stopServer(String name) throws Exception {
		cloudService.stopInstance(getServer(name).getId());
	}

	public void startServer(String name) throws Exception {
		cloudService.startInstance(getServer(name).getId());
	}

	public CredentialsStruct getCredential(String name) throws Exception {
		return cloudService.getLoginInformations(getServer(name).getId());
	}
}