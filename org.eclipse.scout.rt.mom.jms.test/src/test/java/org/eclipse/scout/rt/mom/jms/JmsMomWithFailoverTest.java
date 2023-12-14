/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.jms;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.BooleanSupplier;

import jakarta.jms.JMSException;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IDestination.DestinationType;
import org.eclipse.scout.rt.mom.api.IDestination.ResolveMethod;
import org.eclipse.scout.rt.mom.api.IMomImplementor;
import org.eclipse.scout.rt.mom.api.ISubscription;
import org.eclipse.scout.rt.mom.api.MOM;
import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.mom.api.marshaller.ObjectMarshaller;
import org.eclipse.scout.rt.mom.jms.internal.ISubscriptionStats;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.Replace;
import org.junit.Assume;
import org.junit.Test;

public class JmsMomWithFailoverTest extends AbstractJmsMomTest {

  public JmsMomWithFailoverTest(AbstractJmsMomTestParameter parameter) {
    super(parameter);
  }

  @Test
  public void testSubscribeFailover() throws InterruptedException {
    Assume.assumeFalse(J2eeJmsMomImplementor.class.isAssignableFrom(m_testParameter.getImplementor()));

    //retryCount=3, retryInterval=1s, sessionRetryInterval=2s
    FixtureMomWithFailover mom = installMom(FixtureMomWithFailover.class);

    IDestination<String> queue = MOM.newDestination("test/mom/testSubscribeFailover", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    m_disposables.add(MOM.registerMarshaller(FixtureMom.class, queue, BEANS.get(ObjectMarshaller.class)));

    final CountDownLatch message1CountDown = new CountDownLatch(1);
    final CountDownLatch message2CountDown = new CountDownLatch(2);

    // Register subscriber
    ISubscription subs;
    m_disposables.add(subs = MOM.subscribe(FixtureMom.class, queue,
        message -> {
          message1CountDown.countDown();
          message2CountDown.countDown();
        },
        MOM.newSubscribeInput().withAcknowledgementMode(SubscribeInput.ACKNOWLEDGE_AUTO)));

    // Publish first message
    MOM.publish(FixtureMom.class, queue, "message-1", MOM.newPublishInput());
    message1CountDown.await();

    //3*2+1 failures triggers one error log message after 3*2 retries but continues trying
    CountDownLatch failureCount = new CountDownLatch(3 * 2 + 1);
    mom.simulateConnectionDown(failureCount);
    failureCount.await();

    // Publish second message after connection failover
    MOM.publish(FixtureMom.class, queue, "message-2", MOM.newPublishInput());
    message2CountDown.await();

    //the stats reflect events after reconnect
    ISubscriptionStats stats = ((JmsSubscription) subs).getStats();
    assertEquals(0, stats.receivedErrors());
    assertEquals(1, stats.receivedMessages());
    assertEquals(1, stats.receivedNonNullMessages());
  }

  @IgnoreBean
  @Replace
  public static class FixtureMomWithFailover extends FixtureMom {
    private ICreateJmsConnection m_oldCF;

    public FixtureMomWithFailover(AbstractJmsMomTestParameter parameter) {
      super(parameter);
    }

    @Override
    protected IMomImplementor initDelegate() throws Exception {
      IMomImplementor impl = super.initDelegate();
      m_oldCF = ((JmsMomImplementor) impl).m_connectionWrapper.getConnectionFunction();

      return impl;
    }

    @Override
    protected Map<Object, Object> lookupEnvironment() {
      Map<Object, Object> env = super.lookupEnvironment();
      env.put(IMomImplementor.CONNECTION_RETRY_COUNT, 5);
      env.put(IMomImplementor.CONNECTION_RETRY_INTERVAL_MILLIS, 1000);
      env.put(IMomImplementor.SESSION_RETRY_INTERVAL_MILLIS, 2000);
      return env;
    }

    public void simulateConnectionDown(final CountDownLatch failingCount) {
      System.out.println("simulateConnectionDown");
      final JmsMomImplementor impl = (JmsMomImplementor) getImplementor();
      impl.m_connectionWrapper.withConnectionFunction(() -> {
        failingCount.countDown();
        if (failingCount.getCount() > 0) {
          throw new JMSException("JUnit fixture: no connection");
        }
        if (failingCount.getCount() == 0) {
          System.out.println("simulateConnectionUp");
        }
        return m_oldCF.create();
      });
      impl.m_connectionWrapper.invalidate(impl.getConnection(), new JMSException("JUnit fixture: connection failed"));
    }

    public void simulateConnectionDown(BooleanSupplier active) {
      final JmsMomImplementor impl = (JmsMomImplementor) getImplementor();
      impl.m_connectionWrapper.withConnectionFunction(() -> {
        if (active.getAsBoolean()) {
          System.out.println("simulateConnectionUp");
          return m_oldCF.create();
        }
        else {
          System.out.println("simulateConnectionDown");
          throw new JMSException("JUnit fixture: no connection");
        }
      });
      impl.m_connectionWrapper.invalidate(impl.getConnection(), new JMSException("JUnit fixture: connection failed"));
    }
  }
}
