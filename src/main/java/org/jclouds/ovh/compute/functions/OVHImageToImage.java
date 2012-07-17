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

import javax.annotation.Resource;
import javax.inject.Named;
import javax.inject.Singleton;

import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.Image.Status;
import org.jclouds.compute.domain.ImageBuilder;
import org.jclouds.compute.domain.OperatingSystem;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.reference.ComputeServiceConstants;
import org.jclouds.logging.Logger;

import com.google.common.base.Function;
import com.ovh.ws.cloud._public.instance.r1.structure.DistributionStruct;

/**
 * @author David Krolak
 */
@Singleton
public class OVHImageToImage implements Function<DistributionStruct, Image> {
	@Resource
	@Named(ComputeServiceConstants.COMPUTE_LOGGER)
	protected Logger logger = Logger.NULL;

	@Override
	public Image apply(DistributionStruct from) {

		ImageBuilder builder = new ImageBuilder().providerId("ovh").ids(from.getName())
				.name(from.getName()).description(from.getDescription()).status(Status.AVAILABLE);
		
		OsFamily family = null;

		try {
			family = OsFamily.valueOf(from.getBase().toUpperCase());

			builder.operatingSystem(new OperatingSystem.Builder().name(from.getName())
					.description(from.getDescription()).family(family).version(null)
					.is64Bit("64".equalsIgnoreCase(from.getPlatform())).arch(null).build());
		}
		catch (IllegalArgumentException e) {
			logger.debug("<< didn't match os(%s)", from);
		}
		
		return builder.build();
	}

}
