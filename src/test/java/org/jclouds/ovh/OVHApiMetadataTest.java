package org.jclouds.ovh;

import org.jclouds.View;
import org.jclouds.apis.internal.BaseApiMetadataTest;
import org.jclouds.ovh.OVHApiMetadata;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

@Test(groups = "unit", testName = "OVHApiMetadataTest")
public class OVHApiMetadataTest  extends BaseApiMetadataTest {
   public OVHApiMetadataTest() {
      super(new OVHApiMetadata(), ImmutableSet.<TypeToken<? extends View>> of());
   }
}
