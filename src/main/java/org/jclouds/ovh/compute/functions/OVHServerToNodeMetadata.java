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
package org.jclouds.ovh.compute.functions;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.jclouds.compute.util.ComputeServiceUtils.parseGroupFromName;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jclouds.collect.FindResourceInSet;
import org.jclouds.collect.Memoized;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeMetadataBuilder;
import org.jclouds.compute.domain.NodeState;
import org.jclouds.domain.Credentials;
import org.jclouds.domain.Location;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.ovh.parameters.SessionParameters;
import org.jclouds.ovh.service.PublicCloudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.ovh.ws.cloud._public.instance.r1.structure.CredentialsStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.InstanceStatusEnum;
import com.ovh.ws.cloud._public.instance.r1.structure.InstanceStruct;
import com.ovh.ws.common.OvhWsException;

/**
 * @author Adrian Cole
 */
@Singleton
public class OVHServerToNodeMetadata implements Function<InstanceStruct, NodeMetadata> {

   public static final Map<InstanceStatusEnum, NodeState> serverStatusToNodeState = ImmutableMap
         .<InstanceStatusEnum, NodeState> builder().put(InstanceStatusEnum.RUNNING, NodeState.RUNNING)//
         .put(InstanceStatusEnum.PENDING, NodeState.PENDING)//
         .put(InstanceStatusEnum.STOPPED, NodeState.SUSPENDED)//
         .put(InstanceStatusEnum.PROVISIONNED, NodeState.TERMINATED)//
         .put(InstanceStatusEnum.TO_DELETE, NodeState.UNRECOGNIZED)//
         .build();


	private final Logger log = LoggerFactory.getLogger(OVHServerToNodeMetadata.class);
   private PublicCloudService publicCloudService = null;
   private final FindHardwareForServer findHardwareForServer;
   private final FindLocationForServer findLocationForServer;
   private final FindImageForServer findImageForServer;
   private Map<String, CredentialsStruct> credentialStore = new HashMap<String, CredentialsStruct>();

   @Inject
   OVHServerToNodeMetadata(Map<String, Credentials> credentialStore, FindHardwareForServer findHardwareForServer,
         FindLocationForServer findLocationForServer, FindImageForServer findImageForServer) {
      this.findHardwareForServer = checkNotNull(findHardwareForServer, "findHardwareForServer");
      this.findLocationForServer = checkNotNull(findLocationForServer, "findLocationForServer");
      this.findImageForServer = checkNotNull(findImageForServer, "findImageForServer");
      publicCloudService = PublicCloudService.getInstance();
   }

   @Override
   public NodeMetadata apply(InstanceStruct from)  {
      // convert the result object to a jclouds NodeMetadata
      NodeMetadataBuilder builder = new NodeMetadataBuilder();
      builder.ids(from.getName() + "");
      builder.name(from.getName());
      builder.location(findLocationForServer.apply(from));
      builder.group(parseGroupFromName(from.getName()));
      builder.imageId(from.getDistributionName() + "");
      Image image = findImageForServer.apply(from);
      if (image != null)
         builder.operatingSystem(image.getOperatingSystem());
      builder.hardware(findHardwareForServer.apply(from));
      builder.state(serverStatusToNodeState.get(from.getStatus()));
      builder.publicAddresses(ImmutableSet.<String> of(from.getIpv4()));
      //builder.privateAddresses(ImmutableSet.<String> of(""));

      
		if (SessionParameters.loginSsh == null) {
			// previous version to execute ssh command
			CredentialsStruct ovhCredendials = credentialStore.get(from.getName());
			if (from != null && ovhCredendials == null) {
				try {
					ovhCredendials = publicCloudService.getCrendential(from);
				}
				catch (OvhWsException e) {
					log.error("Exception:{}:apply:{}", this.getClass().toString(), e.getMessage());
				}
				credentialStore.put(from.getName(), ovhCredendials);
			}
			if (ovhCredendials != null) {
				builder.credentials(LoginCredentials.fromCredentials(new Credentials(ovhCredendials
						.getLogin(), ovhCredendials.getPassword())));
			}
		}
		else {
			builder.credentials(SessionParameters.loginSsh);
		}

      return builder.build();
   }

   @Singleton
   public static class FindHardwareForServer extends FindResourceInSet<InstanceStruct, Hardware> {

      @Inject
      public FindHardwareForServer(@Memoized Supplier<Set<? extends Hardware>> hardware) {
         super(hardware);
      }

      @Override
      public boolean matches(InstanceStruct from, Hardware input) {
         return input.getProviderId().equals(from.getOfferName() + "");
      }
   }

   @Singleton
   public static class FindImageForServer extends FindResourceInSet<InstanceStruct, Image> {

      @Inject
      public FindImageForServer(@Memoized Supplier<Set<? extends Image>> hardware) {
         super(hardware);
      }

      @Override
      public boolean matches(InstanceStruct from, Image input) {
         return input.getProviderId().equals(from.getDistributionName() + "");
      }
   }

   @Singleton
   public static class FindLocationForServer extends FindResourceInSet<InstanceStruct, Location> {

      @Inject
      public FindLocationForServer(@Memoized Supplier<Set<? extends Location>> hardware) {
         super(hardware);
      }

      @Override
      public boolean matches(InstanceStruct from, Location input) {
    	  //if (input==null) return true;
         return input.getId().equals(from.getZoneName() + "");
      }
   }
}