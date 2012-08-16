package org.jclouds.ovh.service;

import com.google.common.base.Supplier;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ovh.ws.api.auth.AuthProvider;
import com.ovh.ws.sessionhandler.r3.SessionHandler;
import com.ovh.ws.sessionhandler.r3.structure.SessionWithToken;

@Singleton
public class PublicCloudSessionHandler extends SessionHandler implements AuthProvider{
   
   @Inject
   protected Supplier<SessionWithToken> sessionWithToken;

   @Override
   public String getSessionId() {
      if (sessionWithToken == null) {
         return null;
      }
      return sessionWithToken.get().getSession().getId();
   }

   @Override
   public String getToken() {
      if (sessionWithToken == null) {
         return null;
      }
      return sessionWithToken.get().getToken();
   }

}
