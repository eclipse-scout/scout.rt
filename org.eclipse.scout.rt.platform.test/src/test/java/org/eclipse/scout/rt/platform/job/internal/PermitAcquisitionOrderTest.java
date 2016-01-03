package org.eclipse.scout.rt.platform.job.internal;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class PermitAcquisitionOrderTest {

  @Test
  public void test() {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.
    final String jobIdentifier = UUID.randomUUID().toString();

    IExecutionSemaphore semaphore = Jobs.newExecutionSemaphore(1);

    final Date date = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(2));

    // Schedule 100 jobs to start at the same time
    for (int i = 0; i < 100; i++) {
      Jobs.schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        }
      }, Jobs.newInput()
          .withName("job-{}", i)
          .withExecutionSemaphore(semaphore)
          .withExecutionTrigger(Jobs.newExecutionTrigger()
              .withStartAt(date))
          .withExecutionHint(jobIdentifier));
    }

    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(jobIdentifier)
        .toFilter(), 10, TimeUnit.SECONDS);

    List<String> expectedProtocol = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      expectedProtocol.add("job-" + i);
    }
    assertEquals(expectedProtocol, protocol);
  }
}
