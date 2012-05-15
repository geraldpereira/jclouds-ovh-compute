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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ovh.ws.cloud._public.instance.r1.structure.CredentialsStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.DistributionStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.InstanceStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.OfferStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.ZoneStruct;
import com.ovh.ws.common.OvhWsException;

/**
 * defines the connection between the {@link OVHComputeClient} implementation
 * and the jclouds {@link ComputeService}
 * 
 */
@Singleton
public class OVHComputeServiceAdapter implements
		ComputeServiceAdapter<InstanceStruct, OfferStruct, DistributionStruct, ZoneStruct> {
	private final OVHComputeClient client;

	private final Logger log = LoggerFactory.getLogger(OVHComputeServiceAdapter.class);

	@Inject
	public OVHComputeServiceAdapter(OVHComputeClient client) {
		this.client = checkNotNull(client, "client");
	}

	@Override
	public NodeAndInitialCredentials<InstanceStruct> createNodeWithGroupEncodedIntoName(String tag,
			String name, Template template) {
		// create the backend object using parameters from the template.
		InstanceStruct from = client.createServerInDC(template.getLocation().getId(), name,
				template.getImage().getName(), template.getHardware().getName());
		// request credentials and convert it
		CredentialsStruct cred = null;
		NodeAndInitialCredentials<InstanceStruct> nodeCred = null;
		try {
			cred = client.getCredential(name);
			nodeCred = new NodeAndInitialCredentials<InstanceStruct>(from, from.getName() + "",
					LoginCredentials.builder().user(cred.getLogin()).password(cred.getPassword())
							.build());
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:listNodes:{}", this.getClass().toString(), e.getMessage());
		}
		return nodeCred;
	}

	@Override
	public List<OfferStruct> listHardwareProfiles() {
		return client.listHardware();
	}

	@Override
	public List<DistributionStruct> listImages() {
		return client.listImages();
	}

	@Override
	public Iterable<InstanceStruct> listNodes() {
		Iterable<InstanceStruct> nodes = null;
		try {
			nodes = client.listServers();
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:listNodes:{}", this.getClass().toString(), e.getMessage());
		}
		return nodes;
	}

	@Override
	public Iterable<ZoneStruct> listLocations() {
		return client.listZones();
	}

	@Override
	public InstanceStruct getNode(String id) {
		return client.getServer(id);
	}

	@Override
	public void destroyNode(String id) {
		try {
			client.destroyServer(id);
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:destroyServer:{}", this.getClass().toString(), e.getMessage());
		}
	}

	@Override
	public void rebootNode(String id) {
		try {
			client.rebootServer(id);
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:destroyServer:{}", this.getClass().toString(), e.getMessage());
		}
	}

	@Override
	public void resumeNode(String id) {
		try {
			client.startServer(id);
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:resumeNode:{}", this.getClass().toString(), e.getMessage());
		}

	}

	@Override
	public void suspendNode(String id) {
		try {
			client.stopServer(id);
		}
		catch (OvhWsException e) {
			log.error("Exception:{}:suspendNode:{}", this.getClass().toString(), e.getMessage());
		}
	}
}