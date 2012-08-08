package org.jclouds.ovh;

import org.jclouds.ovh.OVHApiMetadata;
import org.jclouds.ovh.OVHProviderMetadata;
import org.jclouds.providers.internal.BaseProviderMetadataTest;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "OVHProviderMetadataTest")
public class OVHProviderMetadataTest  extends BaseProviderMetadataTest {

   public OVHProviderMetadataTest() {
      super(new OVHProviderMetadata(), new OVHApiMetadata());
   }
}
