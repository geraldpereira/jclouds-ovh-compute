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
package org.jclouds.ovh.compute;

import static com.google.common.base.Preconditions.checkNotNull;

import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.compute.StandaloneComputeServiceContextSpec;
import org.jclouds.ovh.OVHComputeClient;
import org.jclouds.ovh.parameters.SessionParameters;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.ovh.ws.cloud._public.instance.r1.structure.DistributionStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.InstanceStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.OfferStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.ZoneStruct;

/**
 * 
 * @author Adrian Cole
 */
@Test(groups = "live")
public class ServerManagerExperimentLiveTest {
   protected String provider = "ovh";
   protected String identity = "";
   protected String credential = "";
   protected String endpoint = "http://www.ovh.com";
   protected String apiversion = "1";

   @BeforeClass
   protected void setupCredentials() {
      identity = checkNotNull(System.getProperty("test." + provider + ".identity"), "test." + provider + ".identity");
      credential = System.getProperty("test." + provider + ".credential");
      endpoint = System.getProperty("test." + provider + ".endpoint");
      apiversion = System.getProperty("test." + provider + ".apiversion");
   }

   @Test
   public void testAndExperiment() {
      ComputeServiceContext context = null;
      try {
   	   SessionParameters.setJcloudsLogin(identity);
   	   SessionParameters.setJcloudsPwd(credential);
         context = new ComputeServiceContextFactory()
                  .createContext(new StandaloneComputeServiceContextSpec<OVHComputeClient, InstanceStruct, OfferStruct, DistributionStruct, ZoneStruct>(
                           "ovh", endpoint, apiversion, "", "", SessionParameters.getJcloudsLogin(), 
                           SessionParameters.getJcloudsPwd(), OVHComputeClient.class,
                           OVHComputeServiceContextBuilder.class, ImmutableSet.<Module> of()));

         context.getComputeService().listNodes();

      } finally {
         if (context != null)
            context.close();
      }
   }

}