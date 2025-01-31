/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.mom.jms;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IDestination.DestinationType;
import org.eclipse.scout.rt.mom.api.IDestination.ResolveMethod;
import org.eclipse.scout.rt.mom.api.MOM;
import org.eclipse.scout.rt.mom.api.marshaller.ObjectMarshaller;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.testing.platform.testcategory.SlowTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Category(SlowTest.class)
public class JmsMomPubSemaphoreTest extends AbstractJmsMomTest {
  private static final Logger LOG = LoggerFactory.getLogger(JmsMomPubSemaphoreTest.class);

  public JmsMomPubSemaphoreTest(AbstractJmsMomTestParameter parameter) {
    super(parameter);
  }

  /**
   * Job should consume 1 message (serial) when {@code withMaxConcurrentConsumerJobs(1)}
   */
  @Test
  public void testMessageConsumerJobWith1ConsumerJob() throws InterruptedException {
    IBlockingCondition condStart = Jobs.newBlockingCondition(true);
    IBlockingCondition condFinished = Jobs.newBlockingCondition(true);
    SerializableObject obj = new SerializableObject();
    AtomicInteger consumed = new AtomicInteger(0);
    CountDownLatch latch = new CountDownLatch(1);
    LOG.info("test works");
    installMom();

    IDestination<SerializableObject> queue = MOM.newDestination("test/mom/testPublishObject", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    ObjectMarshaller marshaller = BEANS.get(ObjectMarshaller.class);
    m_disposables.add(MOM.registerMarshaller(FixtureMom.class, queue, marshaller));
    m_disposables.add(MOM.subscribe(FixtureMom.class,
        queue,
        message -> {
          latch.countDown();
          condStart.waitFor();

          consumed.incrementAndGet();

          condStart.setBlocking(true);
          condFinished.setBlocking(false);
        },
        MOM.newSubscribeInput().withMaxConcurrentConsumerJobs(1)));
    LOG.info("test works");
    // flood queue
    IntStream.range(0, 99).forEach(i -> MOM.publish(FixtureMom.class, queue, obj));

    LOG.info("test works");
    latch.await();
    assertEquals(0, consumed.get());
    condStart.setBlocking(false);
    condFinished.waitFor();
    condFinished.setBlocking(true);
    assertEquals(1, consumed.get());
    assertTrue(condStart.isBlocking());
    condStart.setBlocking(false);
    condFinished.waitFor();
    assertEquals(2, consumed.get());
  }

  /**
   * Job should consume 6 messages in parallel when {@code withMaxConcurrentConsumerJobs(6)}
   */
  @Test
  public void testMessageConsumerJobWith6ConsumerJob() throws InterruptedException {
    IBlockingCondition condStart = Jobs.newBlockingCondition(true);
    IBlockingCondition condFinished = Jobs.newBlockingCondition(true);
    SerializableObject obj = new SerializableObject();
    AtomicInteger consumed = new AtomicInteger(0);
    CountDownLatch latch = new CountDownLatch(6);
    CountDownLatch latch2 = new CountDownLatch(6);

    installMom();

    IDestination<SerializableObject> queue = MOM.newDestination("test/mom/testPublishObject2", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    ObjectMarshaller marshaller = BEANS.get(ObjectMarshaller.class);
    m_disposables.add(MOM.registerMarshaller(FixtureMom.class, queue, marshaller));
    m_disposables.add(MOM.subscribe(FixtureMom.class,
        queue,
        message -> {
          latch.countDown();
          condStart.waitFor();

          consumed.incrementAndGet();
          latch2.countDown();
          condFinished.waitFor();
        },
        MOM.newSubscribeInput().withMaxConcurrentConsumerJobs(6)));

    // flood queue
    IntStream.range(0, 99).forEach(i -> MOM.publish(FixtureMom.class, queue, obj));

    latch.await(); // wait until counted down to 6
    assertEquals(0, consumed.get());
    condStart.setBlocking(false);
    latch2.await();
    assertEquals(6, consumed.get());
    condFinished.setBlocking(false);
  }

  /**
   * If {@code withMaxConcurrentConsumerJobs(int)} not called, job should consume messages in parallel
   */
  @Test
  public void testMessageConsumerJobWithUnlimitedConsumerJobs() throws InterruptedException {
    IBlockingCondition condStart = Jobs.newBlockingCondition(true);
    IBlockingCondition condFinished = Jobs.newBlockingCondition(true);
    SerializableObject obj = new SerializableObject();
    AtomicInteger consumed = new AtomicInteger(0);
    CountDownLatch latch = new CountDownLatch(10);
    CountDownLatch latch2 = new CountDownLatch(10);

    installMom();

    IDestination<SerializableObject> queue = MOM.newDestination("test/mom/testPublishObjectU", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    ObjectMarshaller marshaller = BEANS.get(ObjectMarshaller.class);
    m_disposables.add(MOM.registerMarshaller(FixtureMom.class, queue, marshaller));
    m_disposables.add(MOM.subscribe(FixtureMom.class,
        queue,
        message -> {
          latch.countDown();
          condStart.waitFor();

          consumed.incrementAndGet();
          latch2.countDown();
          condFinished.waitFor();
        },
        MOM.newSubscribeInput()));

    // flood queue
    IntStream.range(0, 10).forEach(i -> MOM.publish(FixtureMom.class, queue, obj));

    latch.await();
    assertEquals(0, consumed.get());
    condStart.setBlocking(false);
    latch2.await();
    assertEquals(10, consumed.get());
    condFinished.setBlocking(false);
  }

  protected static class SerializableObject implements Serializable {
    private static final long serialVersionUID = 2903932396188258477L;
  }
}
