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

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoEntityBuilder;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.mom.api.IBiDestination;
import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IDestination.DestinationType;
import org.eclipse.scout.rt.mom.api.IDestination.ResolveMethod;
import org.eclipse.scout.rt.mom.api.IMessage;
import org.eclipse.scout.rt.mom.api.IMom;
import org.eclipse.scout.rt.mom.api.IMomImplementor;
import org.eclipse.scout.rt.mom.api.MOM;
import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.mom.api.marshaller.BytesMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.JsonDataObjectMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.JsonMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.ObjectMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.TextMarshaller;
import org.eclipse.scout.rt.mom.jms.JmsMomPubSubTest.Person;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.platform.util.IDisposable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.eclipse.scout.rt.testing.platform.runner.JUnitExceptionHandler;
import org.eclipse.scout.rt.testing.platform.runner.Times;
import org.eclipse.scout.rt.testing.platform.testcategory.SlowTest;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(SlowTest.class)
public class JmsMomWithRequestReplyTest extends AbstractJmsMomTest {

  public JmsMomWithRequestReplyTest(AbstractJmsMomTestParameter parameter) {
    super(parameter);
  }

  @Test(timeout = 200_000)
  public void testRequestReply() {
    installMom();
    assertEquals("Hello World", testRequestReplyInternal("Hello World", BEANS.get(TextMarshaller.class)));
    assertEquals("Hello World", testRequestReplyInternal("Hello World", BEANS.get(ObjectMarshaller.class)));
    assertEquals("Hello World", testRequestReplyInternal("Hello World", BEANS.get(JsonMarshaller.class)));
    IDoEntity entity = BEANS.get(DoEntityBuilder.class).put("message", "Hello World").build();
    assertEquals(entity, testRequestReplyInternal(entity, BEANS.get(JsonDataObjectMarshaller.class)));
    assertArrayEquals("Hello World".getBytes(StandardCharsets.UTF_8), (byte[]) testRequestReplyInternal("Hello World".getBytes(StandardCharsets.UTF_8), BEANS.get(BytesMarshaller.class)));

    assertEquals("Hello World", testRequestReplyInternal("Hello World", BEANS.get(TextMarshaller.class)));
    assertEquals("Hello World", testRequestReplyInternal("Hello World", BEANS.get(ObjectMarshaller.class)));
    assertEquals("Hello World", testRequestReplyInternal("Hello World", BEANS.get(JsonMarshaller.class)));
    assertEquals(entity, testRequestReplyInternal(entity, BEANS.get(JsonDataObjectMarshaller.class)));
    assertArrayEquals("Hello World".getBytes(StandardCharsets.UTF_8), (byte[]) testRequestReplyInternal("Hello World".getBytes(StandardCharsets.UTF_8), BEANS.get(BytesMarshaller.class)));
  }

  @Test(timeout = 200_000)
  public void testRequestReplyEmpty() {
    installMom();
    assertEquals("", testRequestReplyInternal("", BEANS.get(TextMarshaller.class)));
    assertEquals("", testRequestReplyInternal("", BEANS.get(ObjectMarshaller.class)));
    assertEquals("", testRequestReplyInternal("", BEANS.get(JsonMarshaller.class)));
    assertEquals(BEANS.get(DoEntity.class), testRequestReplyInternal(BEANS.get(DoEntity.class), BEANS.get(JsonDataObjectMarshaller.class)));
    assertArrayEquals(new byte[0], (byte[]) testRequestReplyInternal(new byte[0], BEANS.get(BytesMarshaller.class)));

    assertEquals("", testRequestReplyInternal("", BEANS.get(TextMarshaller.class)));
    assertEquals("", testRequestReplyInternal("", BEANS.get(ObjectMarshaller.class)));
    assertEquals("", testRequestReplyInternal("", BEANS.get(JsonMarshaller.class)));
    assertEquals(BEANS.get(DoEntity.class), testRequestReplyInternal(BEANS.get(DoEntity.class), BEANS.get(JsonDataObjectMarshaller.class)));
    assertArrayEquals(new byte[0], (byte[]) testRequestReplyInternal(new byte[0], BEANS.get(BytesMarshaller.class)));
  }

  @Test(timeout = 200_000)
  public void testRequestReplyNull() {
    installMom();
    assertNull(testRequestReplyInternal(null, BEANS.get(TextMarshaller.class)));
    assertNull(testRequestReplyInternal(null, BEANS.get(ObjectMarshaller.class)));
    assertNull(testRequestReplyInternal(null, BEANS.get(JsonMarshaller.class)));
    assertNull(testRequestReplyInternal(null, BEANS.get(JsonDataObjectMarshaller.class)));
    assertNull(testRequestReplyInternal(null, BEANS.get(BytesMarshaller.class)));

    assertNull(testRequestReplyInternal(null, BEANS.get(TextMarshaller.class)));
    assertNull(testRequestReplyInternal(null, BEANS.get(ObjectMarshaller.class)));
    assertNull(testRequestReplyInternal(null, BEANS.get(JsonMarshaller.class)));
    assertNull(testRequestReplyInternal(null, BEANS.get(JsonDataObjectMarshaller.class)));
    assertNull(testRequestReplyInternal(null, BEANS.get(BytesMarshaller.class)));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCurrentMessageRequestReply() throws InterruptedException, JMSException {
    installMom();
    CorrelationId.CURRENT.set("cid_test");
    try {

      final Capturer<IMessage<?>> capturer = new Capturer<>();

      Person person = new Person();
      person.setLastname("smith");
      person.setFirstname("anna");

      IBiDestination<Person, Void> queue = MOM.newBiDestination("test/mom/testCurrentMessageRequestReply", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
      m_disposables.add(MOM.registerMarshaller(FixtureMom.class, queue, BEANS.get(ObjectMarshaller.class)));

      m_disposables.add(MOM.reply(FixtureMom.class, queue, request -> {
        capturer.set(IMessage.CURRENT.get());
        return null;
      }));
      MOM.request(FixtureMom.class, queue, person);

      // Verify
      IMessage<Person> testee = (IMessage<Person>) capturer.get();
      assertNotNull(testee);
      assertEquals("smith", testee.getTransferObject().getLastname());
      assertEquals("anna", testee.getTransferObject().getFirstname());
      MatcherAssert.assertThat(testee.getAdapter(Message.class), instanceOf(Message.class));
      assertEquals("cid_test", testee.getAdapter(Message.class).getJMSCorrelationID());
    }
    finally {
      CorrelationId.CURRENT.remove();
    }
  }

  @Test(expected = AssertionException.class)
  public void testMomEnvironmentWithoutRequestReply() {
    installMom(FixtureMomWithoutRequestReply.class);
    testRequestReplyInternal("Hello World", null);
  }

  private Object testRequestReplyInternal(Object request, IMarshaller marshaller) {
    reset(BEANS.get(IJmsMessageHandler.class));

    IBiDestination<Object, Object> queue = MOM.newBiDestination("test/mom/testRequestReplyInternal", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    List<IDisposable> disposables = new ArrayList<>();
    if (marshaller != null) {
      disposables.add(MOM.registerMarshaller(FixtureMom.class, queue, marshaller));
    }

    try {
      disposables.add(MOM.reply(FixtureMom.class, queue, IMessage::getTransferObject, MOM.newSubscribeInput()
          // use single threaded in order to block dispose until subscription is completely released
          .withAcknowledgementMode(SubscribeInput.ACKNOWLEDGE_AUTO_SINGLE_THREADED)));

      Object reply = MOM.request(FixtureMom.class, queue, request);
      // Verify
      verifyRequestReplyMessageHandler(queue, marshaller, request, reply);
      return reply;
    }
    finally {
      dispose(disposables);
    }
  }

  @Test(timeout = 200_000)
  public void testQueueRequestReply() {
    installMom();
    IBiDestination<String, String> queue = MOM.newBiDestination("test/momtestQueueRequestReply", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    // Subscribe for the destination
    m_disposables.add(MOM.reply(FixtureMom.class, queue, request -> request.getTransferObject().toUpperCase()));

    // Initiate 'request-reply' communication
    final String request = "hello world";
    String testee = MOM.request(FixtureMom.class, queue, request);

    // Verify
    final String expectedReply = "HELLO WORLD";
    assertEquals(expectedReply, testee);
    IMarshaller marshaller = BEANS.get(JsonMarshaller.class);
    verifyRequestReplyMessageHandler(queue, marshaller, request, expectedReply);
    assertEquals(1, BEANS.get(FixtureMom.class).getSubscriptions().size());
    assertEquals(queue, BEANS.get(FixtureMom.class).getSubscriptions().get(0).getDestination());
  }

  @Test(timeout = 200_000)
  public void testQueueRequestReplyCorrelationId() {
    installMom();
    IBiDestination<String, String> queue = MOM.newBiDestination("test/mom/testQueueRequestReplyCorrelationId", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    testRequestReplyCorrelationIdInternal(queue);
  }

  @Test(timeout = 200_000)
  public void testTopicRequestReplyCorrelationId() {
    installMom();
    IBiDestination<String, String> topic = MOM.newBiDestination("test/mom/testTopicRequestReplyCorrelationId", DestinationType.TOPIC, ResolveMethod.DEFINE, null);
    testRequestReplyCorrelationIdInternal(topic);
  }

  private void testRequestReplyCorrelationIdInternal(final IBiDestination<String, String> destination) {
    m_disposables.add(MOM.reply(FixtureMom.class, destination, request -> CorrelationId.CURRENT.get(), MOM.newSubscribeInput()
        .withRunContext(RunContexts.copyCurrent()
            .withCorrelationId("cid:xyz"))));

    // Initiate 'request-reply' communication
    RunContexts.empty()
        .withCorrelationId("cid:abc")
        .run(() -> {
          final String request = "hello world";
          String testee = MOM.request(FixtureMom.class, destination, request);
          // Verify
          final String expectedReply = "cid:abc";
          assertEquals(expectedReply, testee);
          IMarshaller marshaller = BEANS.get(JsonMarshaller.class);
          verifyRequestReplyMessageHandler(destination, marshaller, request, expectedReply);
        });
  }

  @Test(timeout = 200_000)
  public void testTopicRequestReply() {
    installMom();
    IBiDestination<String, String> topic = MOM.newBiDestination("test/mom/testTopicRequestReply", DestinationType.TOPIC, ResolveMethod.DEFINE, null);

    // Subscribe for the destination
    m_disposables.add(MOM.reply(FixtureMom.class, topic, request -> request.getTransferObject().toUpperCase()));

    // Initiate 'request-reply' communication
    String testee = MOM.request(FixtureMom.class, topic, "hello world");

    // Verify
    assertEquals("HELLO WORLD", testee);
    assertEquals(1, BEANS.get(FixtureMom.class).getSubscriptions().size());
    assertEquals(topic, BEANS.get(FixtureMom.class).getSubscriptions().get(0).getDestination());
  }

  @Test(timeout = 200_000)
  @Times(10) // regression
  public void testQueueRequestReplyMultipleSubscriptions() throws InterruptedException {
    installMom();
    IBiDestination<String, String> queue = MOM.newBiDestination("test/mom/testQueueRequestReplyMultipleSubscriptions", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    final CountDownLatch msgLatch = new CountDownLatch(2);

    // Subscribe for the destination
    m_disposables.add(MOM.reply(FixtureMom.class, queue, request -> {
      msgLatch.countDown();
      return request.getTransferObject().toUpperCase();
    }));

    // Subscribe for the destination
    m_disposables.add(MOM.reply(FixtureMom.class, queue, request -> {
      msgLatch.countDown();
      return request.getTransferObject().toUpperCase();
    }));

    // Initiate 'request-reply' communication
    String testee = MOM.request(FixtureMom.class, queue, "hello world");

    // Verify
    assertEquals("HELLO WORLD", testee);
    assertFalse(msgLatch.await(50, TimeUnit.MILLISECONDS));
  }

  @Test(timeout = 200_000)
  public void testTopicRequestReplyMultipleSubscriptions() throws InterruptedException {
    installMom();
    IBiDestination<String, String> topic = MOM.newBiDestination("test/mom/testTopicRequestReplyMultipleSubscriptions", DestinationType.TOPIC, ResolveMethod.DEFINE, null);

    final CountDownLatch msgLatch = new CountDownLatch(2);

    // Subscribe for the destination
    m_disposables.add(MOM.reply(FixtureMom.class, topic, request -> {
      msgLatch.countDown();
      return request.getTransferObject().toUpperCase();
    }));

    // Subscribe for the destination
    m_disposables.add(MOM.reply(FixtureMom.class, topic, request -> {
      msgLatch.countDown();
      return request.getTransferObject().toUpperCase();
    }));

    // Initiate 'request-reply' communication
    String testee = MOM.request(FixtureMom.class, topic, "hello world");

    // Verify
    assertEquals("HELLO WORLD", testee);
    assertTrue(msgLatch.await(5, TimeUnit.SECONDS));
  }

  @Test(timeout = 200_000)
  @Times(10) // regression
  public void testQueueRequestReplyRequestFirst() throws InterruptedException {
    installMom();
    final IBiDestination<String, String> queue = MOM.newBiDestination("test/mom/testQueueRequestReplyRequestFirst", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    // 1. Initiate 'request-reply' communication
    IFuture<String> requestFuture = Jobs.schedule(() -> MOM.request(FixtureMom.class, queue, "hello world"), Jobs.newInput()
        .withName("requester (Q)")
        .withExecutionHint(FixtureJobInput.EXPLICIT_HINT));

    // Wait some time to give request message publisher time to send message
    // If we wait not long enough, we get a false positive
    Thread.sleep(300);

    // 2. Subscribe for reply
    Jobs.schedule(() -> {
      // Subscribe replier
      m_disposables.add(MOM.reply(FixtureMom.class, queue, request -> request.getTransferObject().toUpperCase()));
    }, Jobs.newInput()
        .withName("replier (Q)")
        .withExecutionHint(FixtureJobInput.EXPLICIT_HINT));

    String testee = requestFuture.awaitDoneAndGet(10, TimeUnit.SECONDS);

    // Verify
    assertEquals("HELLO WORLD", testee);
  }

  @Test(timeout = 200_000)
  public void testRequestReplyWithBlockingCondition() {
    installMom();
    final IBiDestination<String, String> queue = MOM.newBiDestination("test/mom/testRequestReplyWithBlockingCondition", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    //prepare the reply
    final CountDownLatch replyLatch = new CountDownLatch(1);
    m_disposables.add(MOM.reply(FixtureMom.class, queue, request -> {
      try {
        replyLatch.await();
      }
      catch (InterruptedException e) {
        throw new ThreadInterruptedError("Interrupted", e);
      }
      return "TheReply";
    }));

    // semaphore for jobs
    IExecutionSemaphore mutex = Jobs.newExecutionSemaphore(1);

    // request should block with IBlockingCondition.waitFor in order to release semaphore
    IFuture<String> requestFuture = Jobs.schedule(() -> {
      String reply = MOM.request(FixtureMom.class, queue, "hello world");
      System.out.println("Reply: " + reply);
      return reply;
    }, Jobs.newInput()
        .withName("requester (T)")
        .withExecutionHint(FixtureJobInput.EXPLICIT_HINT)
        .withExceptionHandling(null, false)
        .withExecutionSemaphore(mutex));

    // test if semaphore was released (with waitFor)
    IFuture<Void> otherFuture = Jobs.schedule(() -> {
      // nop
    }, Jobs.newInput()
        .withName("null job")
        .withExecutionHint(FixtureJobInput.EXPLICIT_HINT)
        .withExecutionSemaphore(mutex));

    // Verify
    try {
      otherFuture.awaitDone(10, TimeUnit.SECONDS);
      assertSame(JobState.WAITING_FOR_BLOCKING_CONDITION, requestFuture.getState());
      //now send reply
      replyLatch.countDown();
      requestFuture.awaitDone(10, TimeUnit.SECONDS);
    }
    catch (TimedOutError e) {
      fail();
    }
    finally {
      replyLatch.countDown();
    }
  }

  @Test(timeout = 200_000)
  public void testQueueRequestReplyTimeout() throws InterruptedException {
    installMom();
    final IBiDestination<String, String> queue = MOM.newBiDestination("test/mom/testQueueRequestReplyTimeout", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    testRequestReplyTimeoutInternal(queue);
  }

  @Test(timeout = 200_000)
  public void testTopicRequestReplyTimeout() throws InterruptedException {
    installMom();
    final IBiDestination<String, String> topic = MOM.newBiDestination("test/mom/testTopicRequestReplyTimeout", DestinationType.TOPIC, ResolveMethod.DEFINE, null);
    testRequestReplyTimeoutInternal(topic);
  }

  private void testRequestReplyTimeoutInternal(final IBiDestination<String, String> destination) throws InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(2);

    final AtomicBoolean requestorTimedOut = new AtomicBoolean();
    final AtomicBoolean replierInterrupted = new AtomicBoolean();

    // Subscribe for the destination
    m_disposables.add(MOM.reply(FixtureMom.class, destination, request -> {
      try {
        /*
         * MARKER
         */
        setupLatch.countDownAndBlock();
      }
      catch (InterruptedException e) {
        replierInterrupted.set(true);
      }
      finally {
        verifyLatch.countDown();
      }
      return request.getTransferObject().toUpperCase();
    }));

    // Initiate 'request-reply' communication
    try {
      MOM.request(FixtureMom.class, destination, "hello world", MOM.newPublishInput()
          /*
           * wait 5 seconds
           * 1 second is too low, if the test runner takes 1 second until it enters the MARKER code in onRequest
           * then some jms implementations cancel the jms request completeley, so MARKER is never called.
           */
          .withRequestReplyTimeout(5, TimeUnit.SECONDS));
    }
    catch (TimedOutError e) {
      requestorTimedOut.set(true);
    }
    finally {
      verifyLatch.countDown();
    }

    assertTrue(verifyLatch.await());

    // Verify
    assertTrue(requestorTimedOut.get());
    assertTrue(replierInterrupted.get());
  }

  @Test(timeout = 200_000)
  public void testQueueRequestReplyCancellation() throws InterruptedException {
    installMom();
    final IBiDestination<String, String> queue = MOM.newBiDestination("test/mom/testQueueRequestReplyCancellation", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    testRequestReplyCancellationInternal(queue);
  }

  @Test(timeout = 200_000)
  public void testTopicRequestReplyCancellation() throws InterruptedException {
    installMom();
    final IBiDestination<String, String> topic = MOM.newBiDestination("test/mom/testTopicRequestReplyCancellation", DestinationType.TOPIC, ResolveMethod.DEFINE, null);
    testRequestReplyCancellationInternal(topic);
  }

  @Test(timeout = 200_000)
  public void testMomEnvironmentWithCustomCancellationTopicAsString() throws InterruptedException {
    installMom(FixtureMomWithCustomRequestReplyCancellationTopicAsString.class);
    IDestination<String> defaultTopic = CONFIG.getPropertyValue(IMom.RequestReplyCancellationTopicProperty.class);
    IDestination<String> differentTopic = MOM.newDestination("differentTopic", IDestination.DestinationType.TOPIC, IDestination.ResolveMethod.DEFINE, null);
    final Capturer<String> capturer1 = new Capturer<>();
    final Capturer<String> capturer2 = new Capturer<>();
    m_disposables.add(MOM.subscribe(FixtureMom.class, defaultTopic, message -> {
      capturer1.set("cancelled!"); // should not be called
    }));
    m_disposables.add(MOM.subscribe(FixtureMom.class, differentTopic, message -> {
      capturer2.set("cancelled!"); // should be called
    }));

    // Run test
    final IBiDestination<String, String> queue = MOM.newBiDestination("test/mom/testQueueRequestReplyCancellation", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    testRequestReplyCancellationInternal(queue);

    // Verify
    capturer1.assertEmpty(1, TimeUnit.SECONDS);
    assertNotNull(capturer2.get(1, TimeUnit.SECONDS));
  }

  @Test(timeout = 200_000)
  public void testMomEnvironmentWithCustomCancellationTopic() throws InterruptedException {
    installMom(FixtureMomWithCustomRequestReplyCancellationTopic.class);
    IDestination<String> differentTopic = MOM.newDestination("UnitTestTopic", IDestination.DestinationType.TOPIC, IDestination.ResolveMethod.JNDI, null);
    final Capturer<String> capturer = new Capturer<>();
    m_disposables.add(MOM.subscribe(FixtureMom.class, differentTopic, message -> {
      capturer.set("cancelled!"); // should be called
    }));

    // Run test
    final IBiDestination<String, String> queue = MOM.newBiDestination("test/mom/testQueueRequestReplyCancellation", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    testRequestReplyCancellationInternal(queue);

    // Verify
    assertNotNull(capturer.get(1, TimeUnit.SECONDS));
  }

  private void testRequestReplyCancellationInternal(final IBiDestination<String, String> destination) throws InterruptedException {
    final CountDownLatch neverLatch = new CountDownLatch(1);
    final CountDownLatch setupLatch = new CountDownLatch(1);
    final CountDownLatch verifyLatch = new CountDownLatch(2);

    final AtomicBoolean requestorInterrupted = new AtomicBoolean();
    final AtomicBoolean replierInterrupted = new AtomicBoolean();
    final AtomicBoolean replierCancelled = new AtomicBoolean();

    // Subscribe for the destination
    m_disposables.add(MOM.reply(FixtureMom.class, destination, request -> {
      setupLatch.countDown();
      try {
        neverLatch.await();
      }
      catch (InterruptedException e) {
        replierInterrupted.set(true);
      }
      finally {
        replierCancelled.set(RunMonitor.CURRENT.get().isCancelled());
        verifyLatch.countDown();
      }
      return request.getTransferObject().toUpperCase();
    }));

    // Initiate 'request-reply' communication
    final FinalValue<String> testee = new FinalValue<>();
    IFuture<Void> requestFuture = Jobs.schedule(() -> {
      try {
        String result = MOM.request(FixtureMom.class, destination, "hello world");
        testee.set(result);
      }
      catch (ThreadInterruptedError e) {
        requestorInterrupted.set(true);
      }
      finally {
        verifyLatch.countDown();
      }
    }, Jobs.newInput()
        .withName("initiator")
        .withExecutionHint(FixtureJobInput.EXPLICIT_HINT));

    // Wait until reply message processing started
    setupLatch.await();

    // Cancel the publishing thread
    requestFuture.cancel(true);

    // wait for request / reply interrupted
    verifyLatch.await();

    // Verify
    assertTrue(requestorInterrupted.get());
    assertTrue(replierInterrupted.get());
    assertTrue(replierCancelled.get());
    assertFalse(testee.isSet());
  }

  @Test(timeout = 200_000)
  public void testTopicRequestReplyJsonObjectMarshaller() {
    installMom();
    IBiDestination<Person, Person> queue = MOM.newBiDestination("test/mom/testTopicRequestReplyJsonObjectMarshaller", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    testRequestReplyJsonObjectMarshallerInternal(queue);
  }

  @Test(timeout = 200_000)
  public void testQueueRequestReplyJsonObjectMarshaller() {
    installMom();
    IBiDestination<Person, Person> topic = MOM.newBiDestination("test/mom/testQueueRequestReplyJsonObjectMarshaller", DestinationType.TOPIC, ResolveMethod.DEFINE, null);
    testRequestReplyJsonObjectMarshallerInternal(topic);
  }

  private void testRequestReplyJsonObjectMarshallerInternal(IBiDestination<Person, Person> destination) {
    m_disposables.add(MOM.registerMarshaller(FixtureMom.class, destination, BEANS.get(JsonMarshaller.class)));

    Person person = new Person();
    person.setLastname("smith");
    person.setFirstname("anna");

    // Subscribe for the destination
    m_disposables.add(MOM.reply(FixtureMom.class, destination, request -> {
      Person result = new Person();
      result.setLastname(request.getTransferObject().getLastname().toUpperCase());
      result.setFirstname(request.getTransferObject().getFirstname().toUpperCase());
      return result;
    }));

    // Initiate 'request-reply' communication
    Person testee = MOM.request(FixtureMom.class, destination, person);

    // Verify
    assertEquals("ANNA", testee.getFirstname());
    assertEquals("SMITH", testee.getLastname());
  }

  @Test(timeout = 200_000)
  public void testRequestReply_ObjectMarshaller_Exception() {
    installMom();
    // Unregister JUnit exception handler
    BEANS.getBeanManager().unregisterBean(BEANS.getBeanManager().getBean(JUnitExceptionHandler.class));

    IBiDestination<Void, String> destination = MOM.newBiDestination("test/mom/testRequestReply_ObjectMarshaller_Exception", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    final RuntimeException runtimeException = new SomethingWrongException("expected-expected-junit-exception");

    // Subscribe for the destination
    m_disposables.add(MOM.reply(FixtureMom.class, destination, request -> {
      throw runtimeException;
    }));

    // Initiate 'request-reply' communication
    try {
      MOM.request(FixtureMom.class, destination, null);
      fail("SomethingWrongException expected");
    }
    catch (SomethingWrongException e) {
      assertEquals(0, e.getStackTrace().length); // security
    }
  }

  @Test(timeout = 200_000)
  public void testRequestReply_JsonMarshaller_Exception1() {
    installMom();
    // Unregister JUnit exception handler
    BEANS.getBeanManager().unregisterBean(BEANS.getBeanManager().getBean(JUnitExceptionHandler.class));

    IBiDestination<Void, String> destination = MOM.newBiDestination("test/mom/testRequestReply_JsonMarshaller_Exception1", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    MOM.registerMarshaller(FixtureMom.class, destination, BEANS.get(JsonMarshaller.class));

    final RuntimeException runtimeException = new SomethingWrongException("expected-expected-junit-exception");

    // Subscribe for the destination
    m_disposables.add(MOM.reply(FixtureMom.class, destination, request -> {
      throw runtimeException;
    }));

    // Initiate 'request-reply' communication
    try {
      MOM.request(FixtureMom.class, destination, null);
      fail("SomethingWrongException expected");
    }
    catch (SomethingWrongException e) {
      assertEquals(0, e.getStackTrace().length); // security
    }
  }

  @Test(timeout = 200_000)
  public void testRequestReply_JsonMarshaller_Exception2() {
    installMom();
    // Unregister JUnit exception handler
    BEANS.getBeanManager().unregisterBean(BEANS.getBeanManager().getBean(JUnitExceptionHandler.class));

    IBiDestination<Void, String> destination = MOM.newBiDestination("test/mom/testRequestReply_JsonMarshaller_Exception2", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    MOM.registerMarshaller(FixtureMom.class, destination, BEANS.get(JsonMarshaller.class));

    final RuntimeException runtimeException = new VetoException();

    // Subscribe for the destination
    m_disposables.add(MOM.reply(FixtureMom.class, destination, request -> {
      throw runtimeException;
    }));

    // Initiate 'request-reply' communication
    try {
      MOM.request(FixtureMom.class, destination, null);
      fail("RuntimeException expected");
    }
    catch (RuntimeException e) {
      assertEquals(0, e.getStackTrace().length); // security
    }
  }

  @Test(timeout = 200_000)
  public void testRequestReply_StringMarshaller_Exception() {
    installMom();
    // Unregister JUnit exception handler
    BEANS.getBeanManager().unregisterBean(BEANS.getBeanManager().getBean(JUnitExceptionHandler.class));

    IBiDestination<Void, String> destination = MOM.newBiDestination("test/mom/testRequestReply_StringMarshaller_Exception", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    MOM.registerMarshaller(FixtureMom.class, destination, BEANS.get(TextMarshaller.class));

    final RuntimeException runtimeException = new SomethingWrongException("expected-expected-junit-exception");

    // Subscribe for the destination
    m_disposables.add(MOM.reply(FixtureMom.class, destination, request -> {
      throw runtimeException;
    }));

    // Initiate 'request-reply' communication
    try {
      MOM.request(FixtureMom.class, destination, null);
      fail("ProcessingException expected");
    }
    catch (ProcessingException e) {
      assertTrue(e.getContextInfos().isEmpty()); // security
      assertFalse(e.getDisplayMessage().contains("expected-junit-exception"));
    }
  }

  private static class SomethingWrongException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    public SomethingWrongException() {
    }

    public SomethingWrongException(String message) {
      super(message);
    }
  }

  @IgnoreBean
  @Replace
  public static class FixtureMomWithoutRequestReply extends FixtureMom {

    public FixtureMomWithoutRequestReply(AbstractJmsMomTestParameter parameter) {
      super(parameter);
    }

    @Override
    protected Map<String, String> getConfiguredEnvironment() {
      final Map<String, String> env = super.getConfiguredEnvironment();
      env.put(IMomImplementor.REQUEST_REPLY_ENABLED, "false");
      return env;
    }
  }

  @IgnoreBean
  @Replace
  public static class FixtureMomWithCustomRequestReplyCancellationTopicAsString extends FixtureMom {

    public FixtureMomWithCustomRequestReplyCancellationTopicAsString(AbstractJmsMomTestParameter parameter) {
      super(parameter);
    }

    @Override
    protected Map<String, String> getConfiguredEnvironment() {
      final Map<String, String> env = super.getConfiguredEnvironment();
      env.put(IMomImplementor.REQUEST_REPLY_ENABLED, null); // should yield "true"
      env.put(IMomImplementor.REQUEST_REPLY_CANCELLATION_TOPIC, "define:///differentTopic");
      return env;
    }
  }

  @IgnoreBean
  @Replace
  public static class FixtureMomWithCustomRequestReplyCancellationTopic extends FixtureMom {

    public FixtureMomWithCustomRequestReplyCancellationTopic(AbstractJmsMomTestParameter parameter) {
      super(parameter);
    }

    @Override
    protected Map<Object, Object> lookupEnvironment() {
      Map<Object, Object> env = super.lookupEnvironment();
      env.put("topic.UnitTestTopic", "scout.physical.UnitTestTopic");
      env.put(IMomImplementor.REQUEST_REPLY_CANCELLATION_TOPIC, MOM.newDestination("UnitTestTopic", IDestination.DestinationType.TOPIC, IDestination.ResolveMethod.JNDI, null));
      return env;
    }
  }
}
