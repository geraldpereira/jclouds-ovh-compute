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
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.inject.Named;
import javax.inject.Singleton;

import org.jclouds.compute.reference.ComputeServiceConstants;
import org.jclouds.logging.Logger;
import org.jclouds.ovh.parameters.SessionParameters;
import org.jclouds.ovh.service.PublicCloudSessionHandler;
import org.jclouds.predicates.RetryablePredicate;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.ovh.ws.api.OvhWsException;
import com.ovh.ws.api.auth.AuthProvider;
import com.ovh.ws.cloud._public.instance.r3.CloudInstance;
import com.ovh.ws.cloud._public.instance.r3.structure.CredentialsStruct;
import com.ovh.ws.cloud._public.instance.r3.structure.DistributionStruct;
import com.ovh.ws.cloud._public.instance.r3.structure.InstanceStruct;
import com.ovh.ws.cloud._public.instance.r3.structure.OfferStruct;
import com.ovh.ws.cloud._public.instance.r3.structure.ProjectStruct;
import com.ovh.ws.cloud._public.instance.r3.structure.TaskStruct;
import com.ovh.ws.cloud._public.instance.r3.structure.ZoneStruct;
import com.ovh.ws.definitions.ovhgenerictype.r2.structure.CommandStatus;

/**
 * This would be replaced with the real connection to the service that can
 * create/list/reboot/get/destroy instances
 * 
 * @author David Krolak
 */
@Singleton
public class OVHComputeClient implements AuthProvider {

   @Resource
   @Named(ComputeServiceConstants.COMPUTE_LOGGER)
   private Logger log = Logger.NULL;

   private PublicCloudSessionHandler sessionHandler;

   private CloudInstance cloudService;

   @Inject
   private Predicate<Long> jobComplete;

   protected ProjectStruct currentProject = null;

   @Inject
   public OVHComputeClient(PublicCloudSessionHandler sessionHandler, CloudInstance cloudService){
      this.sessionHandler = sessionHandler;
      this.cloudService = cloudService;
   }
   
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
      }
   }

   private void setCurrentProjectNamed(final String name) throws OvhWsException {
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

   public TaskStruct getTask(Long taskId) throws OvhWsException {
      return cloudService.getTask(currentProject.getName(), taskId);
   }

   /**
    * simulate creating a server, as this is really going to happen with the api
    * underneath
    * 
    * @param name
    * @param name
    * @param imageId
    * @param hardwareId
    * @return new server
    */
   public InstanceStruct createServerInDC(String datacenter, String name, String imageId, String hardwareId)
         throws OvhWsException {
      if (currentProject == null)
         setCurrentProjectNamed(SessionParameters.getSessionParameters().getJcloudsProj());
      CommandStatus status = cloudService.newInstance(currentProject.getName(), name, datacenter, "", Long.valueOf(0l),
            hardwareId, imageId);
      RetryablePredicate<Long> taskTester = new RetryablePredicate<Long>(jobComplete, 300, 10,
            TimeUnit.SECONDS);
      if (!taskTester.apply(status.getTasks().get(0).getId()))
         throw new OvhWsException(-1, "could not create server named " + name);
      return getServer(name);
   }

   public Iterable<InstanceStruct> listServers() throws OvhWsException {
      checkSession();
      if (currentProject == null)
         setCurrentProjectNamed(SessionParameters.getSessionParameters().getJcloudsProj());
      return cloudService.getInstances(currentProject.getName());
   }

   public InstanceStruct getServer(final String name) throws OvhWsException {
      return Iterables.find(listServers(), new Predicate<InstanceStruct>() {

         @Override
         public boolean apply(InstanceStruct input) {
            return input.getName().equalsIgnoreCase(name);
         }
      });
   }

   public List<DistributionStruct> listImages() throws OvhWsException {
      checkSession();
      return cloudService.getDistributions();
   }

   public DistributionStruct getImage(final String name) throws OvhWsException {
      listServers();
      return Iterables.find(listImages(), new Predicate<DistributionStruct>() {

         @Override
         public boolean apply(DistributionStruct input) {
            return input.getName().equalsIgnoreCase(name);
         }
      });
   }

   public List<OfferStruct> listHardware() throws OvhWsException {
      checkSession();
      return cloudService.getOffers();
   }

   public OfferStruct getHardware(final String name) throws OvhWsException {
      return Iterables.find(listHardware(), new Predicate<OfferStruct>() {
         @Override
         public boolean apply(OfferStruct offer) {
            return offer.getName().equalsIgnoreCase(name);
         }
      });
   }

   public List<ZoneStruct> listZones() throws OvhWsException {
      checkSession();
      return cloudService.getZones();
   }

   public void destroyServer(String name) throws OvhWsException {
      CommandStatus command = cloudService.deleteInstance(getServer(name).getId());
      RetryablePredicate<Long> taskTester = new RetryablePredicate<Long>(jobComplete, 300, 10, TimeUnit.SECONDS);
      if (!taskTester.apply(command.getTasks().get(0).getId()))
         throw new OvhWsException(-1, "could not destroy server named " + name);
   }

   public void rebootServer(String name) throws OvhWsException {
      cloudService.rebootInstance(getServer(name).getId());
   }

   public void stopServer(String name) throws OvhWsException {
      cloudService.stopInstance(getServer(name).getId());
   }

   public void startServer(String name) throws OvhWsException {
      cloudService.startInstance(getServer(name).getId(), "");
   }

   public CredentialsStruct getCredential(String name) throws OvhWsException {
      return cloudService.getLoginInformations(getServer(name).getId());
   }

   @Override
   public String getToken() {
      return sessionHandler.getToken();
   }

   @Override
   public String getSessionId() {
      return sessionHandler.getSessionId();
   }
}