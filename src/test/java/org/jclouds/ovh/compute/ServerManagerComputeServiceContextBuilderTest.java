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

import static org.testng.Assert.assertEquals;

import java.util.Properties;

import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.compute.StandaloneComputeServiceContextSpec;
import org.jclouds.ovh.OVHComputeClient;
import org.jclouds.ovh.compute.OVHComputeServiceContextBuilder;
import org.jclouds.ovh.parameters.SessionParameters;
import org.jclouds.rest.RestContext;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

import com.ovh.ws.cloud._public.instance.r1.structure.DistributionStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.InstanceStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.ZoneStruct;
import com.ovh.ws.cloud._public.instance.r1.structure.OfferStruct;

/**
 * 
 * @author Adrian Cole
 * 
 */
@Test(groups = "unit")
public class ServerManagerComputeServiceContextBuilderTest {

	private String LOGIN = "jj35216";
	private String PWD = "jcloudstest";
	
   @Test
   public void testCanBuildDirectly() {
      ComputeServiceContext context = new OVHComputeServiceContextBuilder(new Properties())
               .buildComputeServiceContext();
      context.close();
   }

   @Test
   public void testCanBuildWithContextSpec() {
	   SessionParameters.setJcloudsLogin(LOGIN);
	   SessionParameters.setJcloudsPwd(PWD);
      ComputeServiceContext context = new ComputeServiceContextFactory()
               .createContext(new StandaloneComputeServiceContextSpec<OVHComputeClient, InstanceStruct, OfferStruct, DistributionStruct, ZoneStruct>(
                        "ovh", "http://www.ovh.com", "1", "", "", SessionParameters.getJcloudsLogin()
                        , SessionParameters.getJcloudsPwd(), OVHComputeClient.class,
                        OVHComputeServiceContextBuilder.class, ImmutableSet.<Module> of()));

      context.close();
   }

   @Test
   public void testCanBuildWithRestProperties() {
	   SessionParameters.setJcloudsLogin(LOGIN);
	   SessionParameters.setJcloudsPwd(PWD);
	   
      Properties restProperties = new Properties();
      restProperties.setProperty("ovh.contextbuilder", OVHComputeServiceContextBuilder.class
               .getName());
      restProperties.setProperty("ovh.endpoint", "http://www.ovh.com");
      restProperties.setProperty("ovh.apiversion", "1");

      ComputeServiceContext context = new ComputeServiceContextFactory(restProperties).createContext("ovh", SessionParameters.getJcloudsLogin()
               , SessionParameters.getJcloudsPwd());
      
      context.close();
   }

   @Test
   public void testProviderSpecificContextIsCorrectType() {
      ComputeServiceContext context = new OVHComputeServiceContextBuilder(new Properties())
               .buildComputeServiceContext();
      RestContext<OVHComputeClient, OVHComputeClient> providerContext = context.getProviderSpecificContext();

      assertEquals(providerContext.getApi().getClass(), OVHComputeClient.class);

      context.close();
   }
}
