package org.jclouds.ovh.service;

import java.util.List;

import javax.annotation.Resource;
import javax.inject.Named;
import javax.inject.Singleton;

import org.jclouds.compute.reference.ComputeServiceConstants;
import org.jclouds.logging.Logger;
import org.jclouds.ovh.parameters.SessionParameters;

import com.google.inject.Inject;
import com.ovh.ws.api.OvhWsException;
import com.ovh.ws.cloud._public.instance.r3.CloudInstance;
import com.ovh.ws.cloud._public.instance.r3.structure.NotificationResultStruct;
import com.ovh.ws.cloud._public.instance.r3.structure.SshKeyStruct;

@Singleton
public class SshKeyManager{

   @Resource
   @Named(ComputeServiceConstants.COMPUTE_LOGGER)
   private Logger log = Logger.NULL;

   @Inject
   private CloudInstance cloudService;
   
   @Inject
   private PublicCloudSessionHandler sessionHandler;

   private void checkSession() {
      if (sessionHandler.getSessionWithToken() == null)
         login();
   }
   
   /**
    * login methods
    */
   private void login() {
      log.debug("login");
      if (!sessionHandler.isLoggin()) {
         sessionHandler.login();
      }
   }
   
   public List<SshKeyStruct> getProjectSshKeys() throws OvhWsException {
      checkSession();
      return cloudService.getSshKeys(SessionParameters.sessionParameters.getJcloudsProj());
   }

   public void applySshKeys() {

      checkSession();
      
      log.debug("applySshKeys");
      try {
         List<NotificationResultStruct> notifications = cloudService.applySshKeys(SessionParameters.sessionParameters.getJcloudsProj());
         if (!checkSshKeyStats(notifications)) {
            log.info("Info:" + "sshkey was not apply to whole instances");
         }
      } catch (OvhWsException e) {
         log.error("Exception:applySshKeys:{}", e.getMessage());
      }
   }

   public boolean checkSshKeyStats(List<NotificationResultStruct> notifications) {
      for (NotificationResultStruct notificationResultStruct : notifications) {
         if (!notificationResultStruct.getStatus().equalsIgnoreCase("OK")) {
            log.info("Info:" + "sshkey was not apply to instance" + notificationResultStruct.getVmName());
            return false;
         }
      }
      return true;
   }

   public void installSshKey(String key, String alias) {
      checkSession();
      try {
         List<NotificationResultStruct> notifications = cloudService.newSshKey(SessionParameters.sessionParameters.getJcloudsProj(),
               alias, key);
         if (!checkSshKeyStats(notifications)) {
            log.info("Info:" + "sshkey was not apply to whole instances");
         }
      } catch (OvhWsException e) {
         log.error("Exception:installFromLocalSshKeys:{}", e.getMessage());
      }
   }

   public void removeSshKey(String keyName) {
      checkSession();
      try {
         String proj = SessionParameters.sessionParameters.getJcloudsProj();
         cloudService.deleteSshKey(proj, keyName);
         applySshKeys();
      } catch (OvhWsException e) {
         log.error("Exception:removeSshKey:{}", e.getMessage());
      }
   }

}
