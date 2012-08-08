package org.jclouds.ovh.service;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Resource;
import javax.inject.Singleton;

import org.jclouds.logging.Logger;
import org.jclouds.ovh.OVHComputeClient;

import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.ovh.ws.api.OvhWsException;
import com.ovh.ws.cloud._public.instance.r3.structure.TaskStatusEnum;
import com.ovh.ws.cloud._public.instance.r3.structure.TaskStruct;

@Singleton
public class JobComplete  implements Predicate<Long> {

   private final OVHComputeClient client;   
   
   @Resource
   protected Logger logger = Logger.NULL;

   @Inject
   public JobComplete(OVHComputeClient client) {
      this.client = client;
   }

   public boolean apply(Long jobId) {
      logger.trace(">> looking for status on job %s", checkNotNull(jobId, "jobId"));
      TaskStruct task;
      try {
         task = refresh(jobId);
         if (task == null) {
            return false;
         }
         logger.trace("%s: looking for job status %s: currently: %s", task.getId().toString(), 1, task.getStatus().toString());
         if (task.getStatus() != TaskStatusEnum.DONE) {
            throw new OvhWsException(-1, String.format("job %s failed",task.getId().toString()));
         }
      } catch (NumberFormatException e) {
         return false;
      } catch (OvhWsException e) {
         return false;
      }
     
      return task.getStatus() == TaskStatusEnum.DONE;
   }

   private TaskStruct refresh(Long jobId) throws OvhWsException  {
      return client.getTask(jobId);
   }

}
