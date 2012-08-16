package org.jclouds.ovh;

import java.net.URI;

import org.jclouds.apis.ApiMetadata;
import org.jclouds.apis.internal.BaseApiMetadata;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.ovh.compute.config.OVHComputeServiceContextModule;

/**
 * Implementation of {@link ApiMetadata} for an example of library integration
 * (ServerManager)
 * 
 * @author Adrian Cole
 */
public class OVHApiMetadata extends BaseApiMetadata {

   /** The serialVersionUID */
   private static final long serialVersionUID = 3606170564482119304L;

   public static Builder builder() {
      return new Builder();
   }

   @Override
   public Builder toBuilder() {
      return Builder.class.cast(builder().fromApiMetadata(this));
   }

   public OVHApiMetadata() {
      super(builder());
   }

   protected OVHApiMetadata(Builder builder) {
      super(builder);
   }

   public static class Builder extends BaseApiMetadata.Builder {

      protected Builder() {
         id("ovh").name("ovh")
               .identityName("${userName}").credentialName("${password}")
               .documentation(URI.create("http://www.jclouds.org/documentation/userguide/compute"))
               .view(ComputeServiceContext.class).defaultModule(OVHComputeServiceContextModule.class);
      }

      @Override
      public OVHApiMetadata build() {
         return new OVHApiMetadata(this);
      }

   }
}