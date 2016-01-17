package org.eclipse.scout.rt.platform.job.internal;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.job.FixedDelayScheduleBuilder;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobListenerRegistration;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SimpleScheduleBuilder;

@RunWith(PlatformTestRunner.class)
public class TemporalValueComputationTest {

  private String m_jobIdentifier;
  private List<IJobListenerRegistration> m_listeners;
  private List<String> m_protocol;

  @Before
  public void before() {
    m_jobIdentifier = UUID.randomUUID().toString();
    m_listeners = new ArrayList<>();
    m_protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    // scheduled listener
    m_listeners.add(Jobs.getJobManager().addListener(Jobs.newEventFilterBuilder()
        .andMatchState(JobState.SCHEDULED)
        .andMatchExecutionHint(m_jobIdentifier)
        .toFilter(), new IJobListener() {

          @Override
          public void changed(JobEvent event) {
            protocolCurrentState("SCHEDULED", (JobFutureTask<?>) event.getData().getFuture(), m_protocol);
          }
        }));

    // pending listener
    m_listeners.add(Jobs.getJobManager().addListener(Jobs.newEventFilterBuilder()
        .andMatchState(JobState.PENDING)
        .andMatchExecutionHint(m_jobIdentifier)
        .toFilter(), new IJobListener() {

          @Override
          public void changed(JobEvent event) {
            protocolCurrentState("PENDING", (JobFutureTask<?>) event.getData().getFuture(), m_protocol);
          }
        }));

    // done listener
    m_listeners.add(Jobs.getJobManager().addListener(Jobs.newEventFilterBuilder()
        .andMatchState(JobState.DONE)
        .andMatchExecutionHint(m_jobIdentifier)
        .toFilter(), new IJobListener() {

          @Override
          public void changed(JobEvent event) {
            protocolCurrentState("DONE", (JobFutureTask<?>) event.getData().getFuture(), m_protocol);
          }
        }));
  }

  @After
  public void after() {
    for (IJobListenerRegistration listener : m_listeners) {
      listener.dispose();
    }
  }

  @Test
  public void testSingleExecution() {
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocolCurrentState("JOB", (JobFutureTask<?>) IFuture.CURRENT.get(), m_protocol);
      }
    }, Jobs.newInput()
        .withExecutionHint(m_jobIdentifier));
    future.awaitDone(10, TimeUnit.SECONDS);

    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("SCHEDULED:single-execution");
    expectedProtocol.add("SCHEDULED:with-next-execution");
    expectedProtocol.add("JOB:single-execution");
    expectedProtocol.add("DONE:single-execution");
    assertEquals(expectedProtocol, m_protocol);
  }

  @Test
  public void testSingleExecutionWithDelay() {
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocolCurrentState("JOB", (JobFutureTask<?>) IFuture.CURRENT.get(), m_protocol);
      }
    }, Jobs.newInput()
        .withExecutionHint(m_jobIdentifier)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.MILLISECONDS)));
    future.awaitDone(10, TimeUnit.SECONDS);

    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("SCHEDULED:single-execution");
    expectedProtocol.add("SCHEDULED:with-next-execution");
    expectedProtocol.add("SCHEDULED:delayed-execution");
    expectedProtocol.add("PENDING:single-execution");
    expectedProtocol.add("PENDING:with-next-execution");
    expectedProtocol.add("PENDING:delayed-execution");
    expectedProtocol.add("JOB:single-execution");
    expectedProtocol.add("JOB:delayed-execution");
    expectedProtocol.add("DONE:single-execution");
    expectedProtocol.add("DONE:delayed-execution");
    assertEquals(expectedProtocol, m_protocol);
  }

  @Test
  public void testSingleExecutionWithFutureExecution() {
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocolCurrentState("JOB", (JobFutureTask<?>) IFuture.CURRENT.get(), m_protocol);
      }
    }, Jobs.newInput()
        .withExecutionHint(m_jobIdentifier)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartAt(new Date(System.currentTimeMillis() + 1))));
    future.awaitDone(10, TimeUnit.SECONDS);

    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("SCHEDULED:single-execution");
    expectedProtocol.add("SCHEDULED:with-next-execution");
    expectedProtocol.add("SCHEDULED:delayed-execution");
    expectedProtocol.add("PENDING:single-execution");
    expectedProtocol.add("PENDING:with-next-execution");
    expectedProtocol.add("PENDING:delayed-execution");
    expectedProtocol.add("JOB:single-execution");
    expectedProtocol.add("JOB:delayed-execution");
    expectedProtocol.add("DONE:single-execution");
    expectedProtocol.add("DONE:delayed-execution");
    assertEquals(expectedProtocol, m_protocol);
  }

  @Test
  public void testSingleExecutionWithPastExecution() {
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocolCurrentState("JOB", (JobFutureTask<?>) IFuture.CURRENT.get(), m_protocol);
      }
    }, Jobs.newInput()
        .withExecutionHint(m_jobIdentifier)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartAt(new Date(0))));
    future.awaitDone(10, TimeUnit.SECONDS);

    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("SCHEDULED:single-execution");
    expectedProtocol.add("SCHEDULED:with-next-execution");
    expectedProtocol.add("JOB:single-execution");
    expectedProtocol.add("DONE:single-execution");
    assertEquals(expectedProtocol, m_protocol);
  }

  @Test
  public void testFixedDelayExecution() {
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocolCurrentState("JOB", (JobFutureTask<?>) IFuture.CURRENT.get(), m_protocol);
      }
    }, Jobs.newInput()
        .withExecutionHint(m_jobIdentifier)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(FixedDelayScheduleBuilder.repeatForTotalCount(3, 1, TimeUnit.MILLISECONDS))));
    future.awaitDone(10, TimeUnit.SECONDS);

    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("SCHEDULED:repetitively-execution");
    expectedProtocol.add("SCHEDULED:with-next-execution");
    // 1st round
    expectedProtocol.add("JOB:repetitively-execution");
    expectedProtocol.add("JOB:with-next-execution");
    expectedProtocol.add("PENDING:repetitively-execution");
    expectedProtocol.add("PENDING:with-next-execution");
    // 2nd round
    expectedProtocol.add("JOB:repetitively-execution");
    expectedProtocol.add("JOB:with-next-execution");
    expectedProtocol.add("PENDING:repetitively-execution");
    expectedProtocol.add("PENDING:with-next-execution");
    // 3th round
    expectedProtocol.add("JOB:repetitively-execution");

    expectedProtocol.add("DONE:repetitively-execution");
    assertEquals(expectedProtocol, m_protocol);
  }

  @Test
  public void testFixedDelayExecutionWithDelay() {
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocolCurrentState("JOB", (JobFutureTask<?>) IFuture.CURRENT.get(), m_protocol);
      }
    }, Jobs.newInput()
        .withExecutionHint(m_jobIdentifier)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.MILLISECONDS)
            .withSchedule(FixedDelayScheduleBuilder.repeatForTotalCount(3, 1, TimeUnit.MILLISECONDS))));
    future.awaitDone(10, TimeUnit.SECONDS);

    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("SCHEDULED:repetitively-execution");
    expectedProtocol.add("SCHEDULED:with-next-execution");
    expectedProtocol.add("SCHEDULED:delayed-execution");
    expectedProtocol.add("PENDING:repetitively-execution");
    expectedProtocol.add("PENDING:with-next-execution");
    expectedProtocol.add("PENDING:delayed-execution");

    // 1st round
    expectedProtocol.add("JOB:repetitively-execution");
    expectedProtocol.add("JOB:with-next-execution");
    expectedProtocol.add("JOB:delayed-execution");
    expectedProtocol.add("PENDING:repetitively-execution");
    expectedProtocol.add("PENDING:with-next-execution");
    expectedProtocol.add("PENDING:delayed-execution");
    // 2nd round
    expectedProtocol.add("JOB:repetitively-execution");
    expectedProtocol.add("JOB:with-next-execution");
    expectedProtocol.add("JOB:delayed-execution");
    expectedProtocol.add("PENDING:repetitively-execution");
    expectedProtocol.add("PENDING:with-next-execution");
    expectedProtocol.add("PENDING:delayed-execution");
    // 3th round
    expectedProtocol.add("JOB:repetitively-execution");
    expectedProtocol.add("JOB:delayed-execution");

    expectedProtocol.add("DONE:repetitively-execution");
    expectedProtocol.add("DONE:delayed-execution");
    assertEquals(expectedProtocol, m_protocol);
  }

  @Test
  public void testFixedDelayExecutionWithFutureExecution() {
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocolCurrentState("JOB", (JobFutureTask<?>) IFuture.CURRENT.get(), m_protocol);
      }
    }, Jobs.newInput()
        .withExecutionHint(m_jobIdentifier)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartAt(new Date(System.currentTimeMillis() + 1))
            .withSchedule(FixedDelayScheduleBuilder.repeatForTotalCount(3, 1, TimeUnit.MILLISECONDS))));
    future.awaitDone(10, TimeUnit.SECONDS);

    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("SCHEDULED:repetitively-execution");
    expectedProtocol.add("SCHEDULED:with-next-execution");
    expectedProtocol.add("SCHEDULED:delayed-execution");
    expectedProtocol.add("PENDING:repetitively-execution");
    expectedProtocol.add("PENDING:with-next-execution");
    expectedProtocol.add("PENDING:delayed-execution");

    // 1st round
    expectedProtocol.add("JOB:repetitively-execution");
    expectedProtocol.add("JOB:with-next-execution");
    expectedProtocol.add("JOB:delayed-execution");
    expectedProtocol.add("PENDING:repetitively-execution");
    expectedProtocol.add("PENDING:with-next-execution");
    expectedProtocol.add("PENDING:delayed-execution");
    // 2nd round
    expectedProtocol.add("JOB:repetitively-execution");
    expectedProtocol.add("JOB:with-next-execution");
    expectedProtocol.add("JOB:delayed-execution");
    expectedProtocol.add("PENDING:repetitively-execution");
    expectedProtocol.add("PENDING:with-next-execution");
    expectedProtocol.add("PENDING:delayed-execution");
    // 3th round
    expectedProtocol.add("JOB:repetitively-execution");
    expectedProtocol.add("JOB:delayed-execution");

    expectedProtocol.add("DONE:repetitively-execution");
    expectedProtocol.add("DONE:delayed-execution");
    assertEquals(expectedProtocol, m_protocol);
  }

  @Test
  public void testFixedDelayExecutionWithPastExecution() {
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocolCurrentState("JOB", (JobFutureTask<?>) IFuture.CURRENT.get(), m_protocol);
      }
    }, Jobs.newInput()
        .withExecutionHint(m_jobIdentifier)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartAt(new Date(0))
            .withSchedule(FixedDelayScheduleBuilder.repeatForTotalCount(3, 1, TimeUnit.MILLISECONDS))));
    future.awaitDone(10, TimeUnit.SECONDS);

    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("SCHEDULED:repetitively-execution");
    expectedProtocol.add("SCHEDULED:with-next-execution");

    // 1st round
    expectedProtocol.add("JOB:repetitively-execution");
    expectedProtocol.add("JOB:with-next-execution");
    expectedProtocol.add("PENDING:repetitively-execution");
    expectedProtocol.add("PENDING:with-next-execution");
    // 2nd round
    expectedProtocol.add("JOB:repetitively-execution");
    expectedProtocol.add("JOB:with-next-execution");
    expectedProtocol.add("PENDING:repetitively-execution");
    expectedProtocol.add("PENDING:with-next-execution");
    // 3th round
    expectedProtocol.add("JOB:repetitively-execution");

    expectedProtocol.add("DONE:repetitively-execution");
    assertEquals(expectedProtocol, m_protocol);
  }

  @Test
  public void testFixedRateExecution() {
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocolCurrentState("JOB", (JobFutureTask<?>) IFuture.CURRENT.get(), m_protocol);
      }
    }, Jobs.newInput()
        .withExecutionHint(m_jobIdentifier)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(2) // With a fixed rate at every 2s, concurrent runs should not occur, meaning runs should not be consolidated.
                .withRepeatCount(2))));
    future.awaitDone(20, TimeUnit.SECONDS);

    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("SCHEDULED:repetitively-execution");
    expectedProtocol.add("SCHEDULED:with-next-execution");
    // 1st round
    expectedProtocol.add("JOB:repetitively-execution");
    expectedProtocol.add("JOB:with-next-execution");
    expectedProtocol.add("PENDING:repetitively-execution");
    expectedProtocol.add("PENDING:with-next-execution");
    // 2nd round
    expectedProtocol.add("JOB:repetitively-execution");
    expectedProtocol.add("JOB:with-next-execution");
    expectedProtocol.add("PENDING:repetitively-execution");
    expectedProtocol.add("PENDING:with-next-execution");
    // 3th round
    expectedProtocol.add("JOB:repetitively-execution");

    expectedProtocol.add("DONE:repetitively-execution");
    assertEquals(expectedProtocol, m_protocol);
  }

  @Test
  public void testFixedRateExecutionWithDelay() {
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocolCurrentState("JOB", (JobFutureTask<?>) IFuture.CURRENT.get(), m_protocol);
      }
    }, Jobs.newInput()
        .withExecutionHint(m_jobIdentifier)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.MILLISECONDS)
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(2) // With a fixed rate at every 2s, concurrent runs should not occur, meaning runs should not be consolidated.
                .withRepeatCount(2))));
    future.awaitDone(20, TimeUnit.SECONDS);

    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("SCHEDULED:repetitively-execution");
    expectedProtocol.add("SCHEDULED:with-next-execution");
    expectedProtocol.add("SCHEDULED:delayed-execution");
    expectedProtocol.add("PENDING:repetitively-execution");
    expectedProtocol.add("PENDING:with-next-execution");
    expectedProtocol.add("PENDING:delayed-execution");

    // 1st round
    expectedProtocol.add("JOB:repetitively-execution");
    expectedProtocol.add("JOB:with-next-execution");
    expectedProtocol.add("JOB:delayed-execution");
    expectedProtocol.add("PENDING:repetitively-execution");
    expectedProtocol.add("PENDING:with-next-execution");
    expectedProtocol.add("PENDING:delayed-execution");
    // 2nd round
    expectedProtocol.add("JOB:repetitively-execution");
    expectedProtocol.add("JOB:with-next-execution");
    expectedProtocol.add("JOB:delayed-execution");
    expectedProtocol.add("PENDING:repetitively-execution");
    expectedProtocol.add("PENDING:with-next-execution");
    expectedProtocol.add("PENDING:delayed-execution");
    // 3th round
    expectedProtocol.add("JOB:repetitively-execution");
    expectedProtocol.add("JOB:delayed-execution");

    expectedProtocol.add("DONE:repetitively-execution");
    expectedProtocol.add("DONE:delayed-execution");
    assertEquals(expectedProtocol, m_protocol);
  }

  @Test
  public void testFixedRateExecutionWithFutureExecution() {
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocolCurrentState("JOB", (JobFutureTask<?>) IFuture.CURRENT.get(), m_protocol);
      }
    }, Jobs.newInput()
        .withExecutionHint(m_jobIdentifier)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartAt(new Date(System.currentTimeMillis() + 1))
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(2) // With a fixed rate at every 2s, concurrent runs should not occur, meaning runs should not be consolidated.
                .withRepeatCount(2))));
    future.awaitDone(20, TimeUnit.SECONDS);

    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("SCHEDULED:repetitively-execution");
    expectedProtocol.add("SCHEDULED:with-next-execution");
    expectedProtocol.add("SCHEDULED:delayed-execution");
    expectedProtocol.add("PENDING:repetitively-execution");
    expectedProtocol.add("PENDING:with-next-execution");
    expectedProtocol.add("PENDING:delayed-execution");

    // 1st round
    expectedProtocol.add("JOB:repetitively-execution");
    expectedProtocol.add("JOB:with-next-execution");
    expectedProtocol.add("JOB:delayed-execution");
    expectedProtocol.add("PENDING:repetitively-execution");
    expectedProtocol.add("PENDING:with-next-execution");
    expectedProtocol.add("PENDING:delayed-execution");
    // 2nd round
    expectedProtocol.add("JOB:repetitively-execution");
    expectedProtocol.add("JOB:with-next-execution");
    expectedProtocol.add("JOB:delayed-execution");
    expectedProtocol.add("PENDING:repetitively-execution");
    expectedProtocol.add("PENDING:with-next-execution");
    expectedProtocol.add("PENDING:delayed-execution");
    // 3th round
    expectedProtocol.add("JOB:repetitively-execution");
    expectedProtocol.add("JOB:delayed-execution");

    expectedProtocol.add("DONE:repetitively-execution");
    expectedProtocol.add("DONE:delayed-execution");
    assertEquals(expectedProtocol, m_protocol);
  }

  @Test
  public void testFixedRateExecutionWithPastExecution() {
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocolCurrentState("JOB", (JobFutureTask<?>) IFuture.CURRENT.get(), m_protocol);
      }
    }, Jobs.newInput()
        .withExecutionHint(m_jobIdentifier)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartAt(new Date(0))
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(2) // With a fixed rate at every 2s, concurrent runs should not occur, meaning runs should not be consolidated.
                .withRepeatCount(2))));
    future.awaitDone(20, TimeUnit.SECONDS);

    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("SCHEDULED:repetitively-execution");
    expectedProtocol.add("SCHEDULED:with-next-execution");

    // 1st round
    expectedProtocol.add("JOB:repetitively-execution");
    expectedProtocol.add("JOB:with-next-execution");
    expectedProtocol.add("PENDING:repetitively-execution");
    expectedProtocol.add("PENDING:with-next-execution");
    // 2nd round
    expectedProtocol.add("JOB:repetitively-execution");
    expectedProtocol.add("JOB:with-next-execution");
    expectedProtocol.add("PENDING:repetitively-execution");
    expectedProtocol.add("PENDING:with-next-execution");
    // 3th round
    expectedProtocol.add("JOB:repetitively-execution");

    expectedProtocol.add("DONE:repetitively-execution");
    assertEquals(expectedProtocol, m_protocol);
  }

  private static void protocolCurrentState(String prefix, JobFutureTask<?> future, List<String> protocol) {
    if (future.isSingleExecution()) {
      protocol.add(prefix + ":single-execution");
    }
    else {
      protocol.add(prefix + ":repetitively-execution");
    }

    if (future.hasNextExecution()) {
      protocol.add(prefix + ":with-next-execution");
    }

    if (future.isDelayedExecution()) {
      protocol.add(prefix + ":delayed-execution");
    }
  }
}
