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
import org.jclouds.compute.domain.NodeMetadata.Status;
import org.jclouds.compute.domain.NodeMetadataBuilder;
import org.jclouds.compute.functions.GroupNamingConvention;
import org.jclouds.domain.Credentials;
import org.jclouds.domain.Location;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.ovh.parameters.SessionParameters;
import org.jclouds.ovh.service.PublicCloudSessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.ovh.ws.api.OvhWsException;
import com.ovh.ws.api.auth.AuthProvider;
import com.ovh.ws.cloud._public.instance.r3.CloudInstance;
import com.ovh.ws.cloud._public.instance.r3.structure.CredentialsStruct;
import com.ovh.ws.cloud._public.instance.r3.structure.InstanceStatusEnum;
import com.ovh.ws.cloud._public.instance.r3.structure.InstanceStruct;
import com.ovh.ws.jsonizer.api.Jsonizer;
import com.ovh.ws.jsonizer.api.http.HttpClient;

/**
 * @author Adrian Cole
 */
@Singleton
public class OVHServerToNodeMetadata implements Function<InstanceStruct, NodeMetadata> {

	public static final Map<InstanceStatusEnum, Status> serverStatusToNodeState = ImmutableMap
			.<InstanceStatusEnum, Status> builder().put(InstanceStatusEnum.RUNNING, Status.RUNNING)//
			.put(InstanceStatusEnum.PENDING, Status.PENDING)//
			.put(InstanceStatusEnum.STOPPED, Status.SUSPENDED)//
			.put(InstanceStatusEnum.PROVISIONNED, Status.TERMINATED)//
			.put(InstanceStatusEnum.TO_DELETE, Status.UNRECOGNIZED)//
			.build();

	private PublicCloudSessionHandler sessionHandler = PublicCloudSessionHandler.getInstance();
	private CloudInstance cloudService;

	private final Logger log = LoggerFactory.getLogger(OVHServerToNodeMetadata.class);
	private final FindHardwareForServer findHardwareForServer;
	private final FindLocationForServer findLocationForServer;
	private final FindImageForServer findImageForServer;
	private Map<String, CredentialsStruct> credentialStore = new HashMap<String, CredentialsStruct>();
	private final GroupNamingConvention nodeNamingConvention;

	@Inject
	OVHServerToNodeMetadata(Map<String, Credentials> credentialStore,
			FindHardwareForServer findHardwareForServer,
			FindLocationForServer findLocationForServer, FindImageForServer findImageForServer,
			GroupNamingConvention.Factory namingConvention) {
		this.nodeNamingConvention = checkNotNull(namingConvention, "namingConvention")
				.createWithoutPrefix();
		this.findHardwareForServer = checkNotNull(findHardwareForServer, "findHardwareForServer");
		this.findLocationForServer = checkNotNull(findLocationForServer, "findLocationForServer");
		this.findImageForServer = checkNotNull(findImageForServer, "findImageForServer");
	}

	@Override
	public NodeMetadata apply(InstanceStruct from) {
		// convert the result object to a jclouds NodeMetadata
		NodeMetadataBuilder builder = new NodeMetadataBuilder();
		builder.ids(from.getName() + "");
		builder.name(from.getName());
		builder.location(findLocationForServer.apply(from));
		builder.group(nodeNamingConvention.groupInUniqueNameOrNull(from.getName()));
		builder.imageId(from.getDistributionName() + "");
		Image image = findImageForServer.apply(from);
		if (image != null)
			builder.operatingSystem(image.getOperatingSystem());
		builder.hardware(findHardwareForServer.apply(from));
		builder.status(serverStatusToNodeState.get(from.getStatus()));
		builder.publicAddresses(ImmutableSet.<String> of(from.getIpv4()));
		// builder.privateAddresses(ImmutableSet.<String> of(""));

		if (SessionParameters.getLoginSsh() == null) {
			// previous version to execute ssh command
			CredentialsStruct ovhCredendials = credentialStore.get(from.getName());
			if (from != null && ovhCredendials == null) {
				try {
					if (cloudService == null) {
						cloudService = getCloudInstance();
					}
					ovhCredendials = cloudService.getLoginInformations(from.getId());
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
			builder.credentials(SessionParameters.getLoginSsh());
		}

		return builder.build();
	}

	private CloudInstance getCloudInstance() {
		HttpClient httpClient = Jsonizer.createHttpClient();
		httpClient.setTimeout(3000);
		return new CloudInstance(httpClient, new AuthProvider() {

			@Override
			public String getToken() {
				return sessionHandler.getSessionWithToken().getToken();
			}

			@Override
			public String getSessionId() {
				return sessionHandler.getSessionId();
			}
		});
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
			// if (input==null) return true;
			return input.getId().equals(from.getZoneName() + "");
		}
	}
}
