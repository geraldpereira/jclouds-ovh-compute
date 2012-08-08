package org.jclouds.ovh;

import java.net.URI;
import java.util.Properties;

import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.internal.BaseProviderMetadata;

public class OVHProviderMetadata  extends BaseProviderMetadata {

   public static Builder builder() {
      return new Builder();
   }

   @Override
   public Builder toBuilder() {
      return builder().fromProviderMetadata(this);
   }

   public OVHProviderMetadata() {
      super(builder());
   }

   public OVHProviderMetadata(Builder builder) {
      super(builder);
   }

   public static Properties defaultProperties() {
      return new Properties();
   }

   public static class Builder extends BaseProviderMetadata.Builder {

      protected Builder() {
         id("ovh")
         .name("ovh")
         .apiMetadata(new OVHApiMetadata())
         .homepage(URI.create("http://www.ovh.com"))
         .console(URI.create("https://www.ovh.com/managerv5/"))
         .endpoint("https://ws.ovh.com")
         .defaultProperties(OVHProviderMetadata.defaultProperties());
      }

      @Override
      public OVHProviderMetadata build() {
         return new OVHProviderMetadata(this);
      }

      @Override
      public Builder fromProviderMetadata(ProviderMetadata in) {
         super.fromProviderMetadata(in);
         return this;
      }

   }
}
