/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.jms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;

import org.apache.activemq.broker.BrokerRegistry;
import org.apache.activemq.broker.BrokerService;
import org.eclipse.scout.rt.mom.api.AbstractMomTransport;
import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IDestination.DestinationType;
import org.eclipse.scout.rt.mom.api.IDestination.ResolveMethod;
import org.eclipse.scout.rt.mom.api.IMessage;
import org.eclipse.scout.rt.mom.api.IMessageListener;
import org.eclipse.scout.rt.mom.api.IMomImplementor;
import org.eclipse.scout.rt.mom.api.MOM;
import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.mom.api.marshaller.ObjectMarshaller;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.BeanUtility;
import org.eclipse.scout.rt.platform.util.IDisposable;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PlatformTestRunner.class)
public class JmsMomManualTest {
  private static final Logger LOG = LoggerFactory.getLogger(JmsMomManualTest.class);

  private FixtureJmsMom m_mom;
  private final List<IBean<?>> m_beans = new ArrayList<>();
  private final List<IDisposable> m_disposables = new ArrayList<>();

  private String m_testJobExecutionHint;

  @Rule
  public TestName m_testName = new TestName();
  public long m_t0;

  @Before
  public void before() {
    installTestMom(FixtureJmsMom.class);

    LOG.info("---------------------------------------------------");
    LOG.info("<{}>", m_testName.getMethodName());
    m_t0 = System.nanoTime();
    m_testJobExecutionHint = UUID.randomUUID().toString();
  }

  @After
  public void after() throws Exception {
    // Dispose resources
    dispose(m_disposables);

    // Cancel regular jobs
    IFilter<IFuture<?>> testJobsFilter = Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(m_testJobExecutionHint)
        .toFilter();
    Set<IFuture<?>> futures = Jobs.getJobManager().getFutures(testJobsFilter);
    if (futures.size() > 0) {
      LOG.info("Cancelling {} regular jobs: {}", futures.size(), futures);
      Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
          .andMatchFuture(futures)
          .andMatchNotState(JobState.DONE)
          .toFilter(), true);
      long t0 = System.nanoTime();
      try {
        Jobs.getJobManager().awaitDone(testJobsFilter, 30, TimeUnit.SECONDS);
        LOG.info("All regular jobs have finished after {} ms", StringUtility.formatNanos(System.nanoTime() - t0));
      }
      catch (TimedOutError e) {
        LOG.warn("Some cancelled regular jobs are still running after {} ms! Please check their implementation.", StringUtility.formatNanos(System.nanoTime() - t0));
      }
    }
    // Cancel jms subscriber jobs
    IFilter<IFuture<?>> jmsJobsFilter = Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(FixtureJmsJobInput.HINT)
        .toFilter();
    futures = Jobs.getJobManager().getFutures(jmsJobsFilter);
    if (futures.size() > 0) {
      LOG.info("Cancelling {} subscriber jobs: {}", futures.size(), futures);
      Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
          .andMatchFuture(futures)
          .andMatchNotState(JobState.DONE)
          .toFilter(), false);
      long t0 = System.nanoTime();
      try {
        Jobs.getJobManager().awaitDone(testJobsFilter, 30, TimeUnit.SECONDS);
        LOG.info("All subscriber jobs have finished after {} ms", StringUtility.formatNanos(System.nanoTime() - t0));
      }
      catch (TimedOutError e) {
        LOG.warn("Some cancelled subscriber jobs are still running after {} ms! Please check their implementation.", StringUtility.formatNanos(System.nanoTime() - t0));
      }
    }

    uninstallTestMom();

    // ensure activeMQ is stopped
    BrokerService brokerService = BrokerRegistry.getInstance().findFirst();
    if (brokerService != null) {
      brokerService.stop();
      brokerService.waitUntilStopped();
    }

    LOG.info("Finished test in {} ms", StringUtility.formatNanos(System.nanoTime() - m_t0));
    LOG.info("</{}>", m_testName.getMethodName());
  }

  protected void installTestMom(Class<? extends FixtureJmsMom> transportType) {
    m_beans.add(BEANS.getBeanManager().registerBean(new BeanMetaData(FixtureJmsJobInput.class)));

    FixtureJmsMom transport = BeanUtility.createInstance(transportType);
    m_beans.add(BEANS.getBeanManager().registerBean(new BeanMetaData(transportType, transport)));
    m_mom = BEANS.get(transportType);
  }

  protected void uninstallTestMom() {
    m_mom.destroy();
    m_mom = null;
    for (IBean<?> bean : m_beans) {
      BEANS.getBeanManager().unregisterBean(bean);
    }
    m_beans.clear();
  }

  private void dispose(Collection<IDisposable> disposables) {
    if (!disposables.isEmpty()) {
      LOG.info("Disposing {} objects: {}", disposables.size(), disposables);
      for (IDisposable disposable : disposables) {
        disposable.dispose();
      }
      disposables.clear();
    }
  }

  @Ignore
  @Test
  public void test() throws InterruptedException {
    //retryCount=3, retryInterval=1s, sesionRetryInterval=2s

    final IDestination<String> queue = MOM.newDestination("test/mom/testSubscribeFailover", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    m_disposables.add(MOM.registerMarshaller(FixtureJmsMom.class, queue, BEANS.get(ObjectMarshaller.class)));

    // Register subscriber
    m_disposables.add(MOM.subscribe(FixtureJmsMom.class, queue,
        new IMessageListener<String>() {
          @Override
          public void onMessage(IMessage<String> message) {
            System.out.println("RECEIVED " + message.getTransferObject());
          }
        },
        MOM.newSubscribeInput().withAcknowledgementMode(SubscribeInput.ACKNOWLEDGE_AUTO)));

    // Publish messages
    IFuture<?> publishJob = Jobs.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        int i = 0;
        while (true) {
          i++;
          try {
            MOM.publish(FixtureJmsMom.class, queue, "message-" + i, MOM.newPublishInput());
            System.out.println("SEND " + i + " OK");
          }
          catch (Exception | ThreadInterruptedError e) {
            System.out.println("SEND " + i + " FAILED");
          }
          SleepUtil.sleepSafe(1, TimeUnit.SECONDS);
          Thread.interrupted();
        }
      }
    }, Jobs.newInput());

    publishJob.awaitDone();
  }

  /**
   * Encapsulates {@link JmsMomImplementor} for testing purpose.
   */
  @IgnoreBean
  public static class FixtureJmsMom extends AbstractMomTransport {
    @Override
    protected Class<? extends IMomImplementor> getConfiguredImplementor() {
      return JmsMomImplementor.class;
    }

    @Override
    protected Map<String, String> getConfiguredEnvironment() {
      // We do not need jmx for unit testing. Also we must disable watchTopicAdvisories else some concurrent issues with broker recreation will happen
      final Map<String, String> activeMQEnvironment = new HashMap<>();
      activeMQEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, org.apache.activemq.jndi.ActiveMQInitialContextFactory.class.getName());
      activeMQEnvironment.put("connectionFactoryNames", "JUnitConnectionFactory"); // Active MQ specific
      activeMQEnvironment.put(IMomImplementor.CONNECTION_FACTORY, "JUnitConnectionFactory");
      activeMQEnvironment.put(IMomImplementor.SYMBOLIC_NAME, "Scout JUnit MOM");

      //Server embedded in vm
      //activeMQEnvironment.put(Context.PROVIDER_URL, "vm://mom" + MOM_COUNTER.incrementAndGet() + "/junit" + activeMQUrlOptions);

      //Server external with activemq failover
      //activeMQEnvironment.put(Context.PROVIDER_URL, "failover:(tcp://localhost:61616?keepAlive=true)/junit?randomize=false&jms.prefetchPolicy.queuePrefetch=1");

      //Server external without activemq failover
      activeMQEnvironment.put(Context.PROVIDER_URL, "tcp://localhost:61616?jms.prefetchPolicy.all=100&jms.redeliveryPolicy.maximumRedeliveries=5");

      return activeMQEnvironment;
    }

    @Override
    protected Map<Object, Object> lookupEnvironment() {
      Map<Object, Object> env = super.lookupEnvironment();
      env.put(IMomImplementor.CONNECTION_RETRY_COUNT, 5);
      env.put(IMomImplementor.CONNECTION_RETRY_INTERVAL_MILLIS, 1000);
      env.put(IMomImplementor.SESSION_RETRY_INTERVAL_MILLIS, 2000);
      return env;
    }
  }
}
