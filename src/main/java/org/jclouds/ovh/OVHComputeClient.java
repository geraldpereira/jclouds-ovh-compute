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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import org.jclouds.ovh.service.PublicCloudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ovh.ws.cloud._public.instance.r1.structure.CredentialsStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.DistributionStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.InstanceStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.OfferStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.ZoneStruct;

/**
 * This would be replaced with the real connection to the service that can
 * create/list/reboot/get/destroy things
 * 
 * @author David Krolak
 */
@Singleton
public class OVHComputeClient {

	private final Logger log = LoggerFactory.getLogger(OVHComputeClient.class);

	private PublicCloudService publicCloudService = PublicCloudService.getInstance();

	public OVHComputeClient() {
	}
	
	
   /**
    * simulate creating a server, as this is really going to happen with the api underneath
    * 
    * @param name
    * @param name
    * @param imageId
    * @param hardwareId
    * @return new server
    */
   public InstanceStruct createServerInDC(String datacenter, String name, String imageId, String hardwareId) {
		try {
			publicCloudService.newInstance(datacenter, name, hardwareId, imageId );
		}
		catch (Exception e) {
			log.error("Exception:createServerInDC:" + e.getMessage());
		}
      return publicCloudService.getInstanceNamed(name);
   }

   public InstanceStruct getServer(String name) {
	    return publicCloudService.getInstanceNamed(name);
   }

   public Iterable<InstanceStruct> listServers() throws Exception {
		try {
			return publicCloudService.getInstances();
		}
		catch (Exception e) {
			log.error("Exception:{}:listServers:{}", this.getClass().toString(), e.getMessage());
			throw e;
		}
   }

   public DistributionStruct getImage(String name) {
      return publicCloudService.getDistributionNamed(name);
   }

   public List<DistributionStruct> listImages() {
		try {
			return publicCloudService.getDistributions();
		}
		catch (Exception e) {
			log.error("Exception:{}:listImages:{}", this.getClass().toString(), e.getMessage());
		}
		return new ArrayList<DistributionStruct>();
   }

   public OfferStruct getHardware(String name) throws Exception {
	      try {
			return publicCloudService.getOfferNamed(name);
		}
		catch (Exception e) {
			log.error("Exception:{}:getHardware:{}", this.getClass().toString(), e.getMessage());
			throw e;
		}
   }

   public List<OfferStruct> listHardware() {
		try {
			return publicCloudService.getOffers();
		}
		catch (Exception e) {
			log.error("Exception:{}:listHardware:{}", this.getClass().toString(), e.getMessage());
		}
		return new ArrayList<OfferStruct>();
   }
   
   public List<ZoneStruct> listZones(){
		try {
			return publicCloudService.getZones();
		}
		catch (Exception e) {
			log.error("Exception:{}:listZones:{}", this.getClass().toString(), e.getMessage());
		}
		return new ArrayList<ZoneStruct>();
   }

   public void destroyServer(String name) throws Exception {
	   try {
			publicCloudService.deleteInstanceNamed(name);
		}
		catch (Exception e1) {
			log.error("Exception:{}:destroyServer:{}",this.getClass().toString(),
					 e1.getMessage());
			throw e1;
		}
   }

   public void rebootServer(String name) throws Exception {
		try {
			publicCloudService.rebootInstanceNamed(name);
		}
		catch (Exception e1) {
			log.error("Exception:{}:rebootServer:{}" ,this.getClass().toString(),
					 e1.getMessage());
			throw e1;
		}
   }

   public void stopServer(String name) throws Exception {
	   try {
			publicCloudService.stopInstanceNamed(name);
		}
		catch (Exception e1) {
			log.error("Exception:{}:stopServer:{}",this.getClass().toString(),
					 e1.getMessage());
			throw e1;
		}
   }
   
   public void startServer(String name) throws Exception {
	   try {
			publicCloudService.startInstanceNamed(name);
		}
		catch (Exception e1) {
			log.error("Exception:{}:startServer:{}",this.getClass().toString(),
					 e1.getMessage());
			throw e1;
		}
   }
   
	public CredentialsStruct getCredential(String name) throws Exception {
		CredentialsStruct cred = null;
		try {
			cred = publicCloudService.getCrendentialOfInstanceNamed(name);
		}
		catch (Exception e) {
			log.error("Exception:{}:getCredential:{}", this.getClass().toString(), e.getMessage());
			throw e;
		}
		return cred;
	}
}