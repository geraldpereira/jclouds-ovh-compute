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
package org.jclouds.ovh.compute.config;

import org.jclouds.compute.ComputeServiceAdapter;
import org.jclouds.compute.config.ComputeServiceAdapterContextModule;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.domain.Location;
//import org.jclouds.location.suppliers.OnlyLocationOrFirstZone;
import org.jclouds.ovh.OVHComputeClient;
import org.jclouds.ovh.compute.functions.OVHDatacenterToLocation;
import org.jclouds.ovh.compute.functions.OVHHardwareToHardware;
import org.jclouds.ovh.compute.functions.OVHImageToImage;
import org.jclouds.ovh.compute.functions.OVHServerToNodeMetadata;
import org.jclouds.ovh.compute.strategy.OVHComputeServiceAdapter;

import com.google.common.base.Function;
//import com.google.common.base.Supplier;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.ovh.ws.cloud._public.instance.r1.structure.DistributionStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.InstanceStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.OfferStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.ZoneStruct;

/**
 * 
 * @author David Krolak
 */
public class OVHComputeServiceContextModule extends
      ComputeServiceAdapterContextModule<OVHComputeClient, OVHComputeClient, InstanceStruct, OfferStruct, DistributionStruct, ZoneStruct> {

   public OVHComputeServiceContextModule() {
      super(OVHComputeClient.class, OVHComputeClient.class);
   }

   @Override
   protected void configure() {
	   super.configure();
	  
      bind(new TypeLiteral<ComputeServiceAdapter<InstanceStruct, OfferStruct, DistributionStruct, ZoneStruct>>() {
      }).to(OVHComputeServiceAdapter.class);
      bind(new TypeLiteral<Function<InstanceStruct, NodeMetadata>>() {
      }).to(OVHServerToNodeMetadata.class);
      bind(new TypeLiteral<Function<DistributionStruct, Image>>() {
      }).to(OVHImageToImage.class);
      bind(new TypeLiteral<Function<OfferStruct, Hardware>>() {
      }).to(OVHHardwareToHardware.class);
      bind(new TypeLiteral<Function<ZoneStruct, Location>>() {
      }).to(OVHDatacenterToLocation.class);
//      bind(new TypeLiteral<Supplier<Location>>() {
//      }).to(OnlyLocationOrFirstZone.class);
      install(new LocationsFromComputeServiceAdapterModule<InstanceStruct, OfferStruct, DistributionStruct, ZoneStruct>(){});
   }
   
	@Override
	protected TemplateBuilder provideTemplate(Injector injector, TemplateBuilder template) {
		return template.osFamily(OsFamily.UBUNTU);
	}
}