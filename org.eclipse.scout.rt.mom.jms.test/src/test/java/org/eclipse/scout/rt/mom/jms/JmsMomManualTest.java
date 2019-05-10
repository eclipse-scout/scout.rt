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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;

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
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.junit.Ignore;
import org.junit.Test;

public class JmsMomManualTest extends AbstractJmsMomTest {

  public JmsMomManualTest(AbstractJmsMomTestParameter parameter) {
    super(parameter);
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
