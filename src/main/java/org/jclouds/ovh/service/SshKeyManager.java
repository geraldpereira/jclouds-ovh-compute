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

   private void checkSession() throws OvhWsException {
      if (sessionHandler.getToken() == null)
         throw new OvhWsException(0, "Could not log in on OVH services");
   }
      
   public List<SshKeyStruct> getProjectSshKeys() throws OvhWsException {
      checkSession();
      return cloudService.getSshKeys(SessionParameters.getSessionParameters().getJcloudsProj());
   }

   public void applySshKeys() {
      try {
         checkSession();
         List<NotificationResultStruct> notifications = cloudService.applySshKeys(SessionParameters.getSessionParameters().getJcloudsProj());
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
      try {
         checkSession();
         List<NotificationResultStruct> notifications = cloudService.newSshKey(SessionParameters.getSessionParameters().getJcloudsProj(),
               alias, key);
         if (!checkSshKeyStats(notifications)) {
            log.info("Info:" + "sshkey was not apply to whole instances");
         }
      } catch (OvhWsException e) {
         log.error("Exception:installFromLocalSshKeys:{}", e.getMessage());
      }
   }

   public void removeSshKey(String keyName) {
      try {
         checkSession();
         String proj = SessionParameters.getSessionParameters().getJcloudsProj();
         cloudService.deleteSshKey(proj, keyName);
         applySshKeys();
      } catch (OvhWsException e) {
         log.error("Exception:removeSshKey:{}", e.getMessage());
      }
   }

}
