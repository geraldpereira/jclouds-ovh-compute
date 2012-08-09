package org.jclouds.ovh.service;

import java.net.URL;

import javax.annotation.Resource;
import javax.inject.Named;

import org.jclouds.compute.reference.ComputeServiceConstants;
import org.jclouds.logging.Logger;
import org.jclouds.ovh.parameters.SessionParameters;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ovh.ws.api.OvhWsException;
import com.ovh.ws.sessionhandler.r3.SessionHandler;
import com.ovh.ws.sessionhandler.r3.structure.SessionWithToken;

@Singleton
public class PublicCloudSessionHandler {

   @Resource
   @Named(ComputeServiceConstants.COMPUTE_LOGGER)
   private Logger log = Logger.NULL;
   
   @Inject
   protected SessionHandler sessionHandler;

   protected SessionWithToken sessionWithToken;

   public SessionWithToken getSessionWithToken() {
      return sessionWithToken;
   }

   public void setUrl(URL url) {
      sessionHandler.setUrl(url);
   }

   public void password(final String login, final String password) {
      try {
         sessionHandler.password(login, password);
      } catch (OvhWsException e) {
         log.error("password:{}", e.getMessage());
      }
   }

   public void logout() {
      try {
         if (isLoggin()) {
            sessionHandler.logout();
         }
      } catch (OvhWsException e) {
         log.error("logout:{}", e.getMessage());
      }
   }

   public void login() {
      try {
         sessionWithToken = sessionHandler.login(SessionParameters.getSessionParameters().getJcloudsLogin(),
               SessionParameters.getSessionParameters().getJcloudsPwd(), SessionParameters.CLOUD_LANG, null);
      } catch (OvhWsException e) {
         log.error("login:{}", e.getMessage());
      }
   }

   public boolean isLoggin() {
      return sessionWithToken != null;
   }

   public String getSessionId() {
      if (sessionWithToken == null) {
         return null;
      }
      return sessionWithToken.getSession().getId();
   }

   public String getToken() {
      if (sessionWithToken == null) {
         return null;
      }
      return sessionWithToken.getToken();
   }

}
