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
package org.jclouds.ovh.compute.strategy;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceAdapter;
import org.jclouds.compute.domain.Template;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.ovh.OVHComputeClient;

import com.ovh.ws.api.OvhWsException;
import com.ovh.ws.cloud._public.instance.r3.structure.CredentialsStruct;
import com.ovh.ws.cloud._public.instance.r3.structure.DistributionStruct;
import com.ovh.ws.cloud._public.instance.r3.structure.InstanceStruct;
import com.ovh.ws.cloud._public.instance.r3.structure.OfferStruct;
import com.ovh.ws.cloud._public.instance.r3.structure.ZoneStruct;

/**
 * defines the connection between the {@link OVHComputeClient} implementation
 * and the jclouds {@link ComputeService}
 * 
 */
@Singleton
public class OVHComputeServiceAdapter implements
      ComputeServiceAdapter<InstanceStruct, OfferStruct, DistributionStruct, ZoneStruct> {
   private final OVHComputeClient client;

   @Inject
   public OVHComputeServiceAdapter(OVHComputeClient client) {
      this.client = checkNotNull(client, "client");
   }

   @Override
   public NodeAndInitialCredentials<InstanceStruct> createNodeWithGroupEncodedIntoName(String tag, String name,
         Template template) {
      try {
         // create the backend object using parameters from the template.
         InstanceStruct from = client.createServerInDC(template.getLocation().getId(), name, template.getImage()
               .getName(), template.getHardware().getName());

         // request credentials and convert it
         CredentialsStruct cred = client.getCredential(name);
         return new NodeAndInitialCredentials<InstanceStruct>(from, from.getName(), LoginCredentials.builder()
               .user(cred.getLogin()).password(cred.getPassword()).build());
      } catch (OvhWsException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public List<OfferStruct> listHardwareProfiles() {
      try {
         return client.listHardware();
      } catch (OvhWsException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public List<DistributionStruct> listImages() {
      try {
         return client.listImages();
      } catch (OvhWsException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public DistributionStruct getImage(String arg0) {
      try {
         return client.getImage(arg0);
      } catch (OvhWsException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public Iterable<InstanceStruct> listNodes() {
      try {
         return client.listServers();
      } catch (OvhWsException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public Iterable<ZoneStruct> listLocations() {
      try {
         return client.listZones();
      } catch (OvhWsException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public InstanceStruct getNode(String id) {
      try {
         return client.getServer(id);
      } catch (OvhWsException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public void destroyNode(String id) {
      try {
         client.destroyServer(id);
      } catch (OvhWsException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public void rebootNode(String id) {
      try {
         client.rebootServer(id);
      } catch (OvhWsException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public void resumeNode(String id) {
      try {
         client.startServer(id);
      } catch (OvhWsException e) {
         throw new RuntimeException(e);
      }

   }

   @Override
   public void suspendNode(String id) {
      try {
         client.stopServer(id);
      } catch (OvhWsException e) {
         throw new RuntimeException(e);
      }
   }
   
}