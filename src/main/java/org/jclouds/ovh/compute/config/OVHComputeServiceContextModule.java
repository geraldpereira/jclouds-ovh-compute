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

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.jclouds.compute.ComputeServiceAdapter;
import org.jclouds.compute.config.ComputeServiceAdapterContextModule;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.domain.Location;
import org.jclouds.ovh.compute.functions.DistributionStructToImage;
import org.jclouds.ovh.compute.functions.InstanceStructToNodeMetadata;
import org.jclouds.ovh.compute.functions.OfferStructToHardware;
import org.jclouds.ovh.compute.functions.ZoneStructToLocation;
import org.jclouds.ovh.compute.strategy.OVHComputeServiceAdapter;
import org.jclouds.ovh.parameters.SessionParameters;
import org.jclouds.ovh.service.JobComplete;
import org.jclouds.ovh.service.PublicCloudSessionHandler;
import org.jclouds.predicates.RetryablePredicate;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.ovh.ws.api.OvhWsException;
import com.ovh.ws.api.auth.AuthProvider;
import com.ovh.ws.cloud._public.instance.r3.structure.DistributionStruct;
import com.ovh.ws.cloud._public.instance.r3.structure.InstanceStruct;
import com.ovh.ws.cloud._public.instance.r3.structure.OfferStruct;
import com.ovh.ws.cloud._public.instance.r3.structure.ZoneStruct;
import com.ovh.ws.jsonizer.api.http.HttpClient;
import com.ovh.ws.jsonizer.api.http.JerseyHttpClient;
import com.ovh.ws.sessionhandler.r3.SessionHandler;
import com.ovh.ws.sessionhandler.r3.structure.SessionWithToken;

/**
 * 
 * @author David Krolak
 */
public class OVHComputeServiceContextModule extends
      ComputeServiceAdapterContextModule<InstanceStruct, OfferStruct, DistributionStruct, ZoneStruct> {

   @Override
   protected void configure() {
      super.configure();

      bind(HttpClient.class).to(JerseyHttpClient.class);
      bind(AuthProvider.class).to(PublicCloudSessionHandler.class);
      bind(SessionHandler.class).to(PublicCloudSessionHandler.class);
      bind(new TypeLiteral<ComputeServiceAdapter<InstanceStruct, OfferStruct, DistributionStruct, ZoneStruct>>() {
      }).to(OVHComputeServiceAdapter.class);
      bind(new TypeLiteral<Function<InstanceStruct, NodeMetadata>>() {
      }).to(InstanceStructToNodeMetadata.class);
      bind(new TypeLiteral<Function<DistributionStruct, Image>>() {
      }).to(DistributionStructToImage.class);
      bind(new TypeLiteral<Function<OfferStruct, Hardware>>() {
      }).to(OfferStructToHardware.class);
      bind(new TypeLiteral<Function<ZoneStruct, Location>>() {
      }).to(ZoneStructToLocation.class);
      install(new LocationsFromComputeServiceAdapterModule<InstanceStruct, OfferStruct, DistributionStruct, ZoneStruct>() {
      });
      
   }

   @Provides
   @Singleton
   protected Predicate<Long> jobComplete(JobComplete jobComplete) {
      return new RetryablePredicate<Long>(jobComplete, 300, 10, TimeUnit.SECONDS);
   }

   @Provides
   @Singleton
   protected Supplier<SessionWithToken> sessionWithToken(final SessionHandler sessionHandler) {
      return Suppliers.memoize(new Supplier<SessionWithToken>(){
         @Override
         public SessionWithToken get(){
            SessionWithToken session;
            try {
               session = sessionHandler.login(SessionParameters.getSessionParameters().getJcloudsLogin(),
                     SessionParameters.getSessionParameters().getJcloudsPwd(), SessionParameters.CLOUD_LANG, null);
            } catch (OvhWsException e) {
               session = new SessionWithToken();
               session.setSession(null);
               session.setToken(null);
            }
            return session;
         }
      });
      
   }
}