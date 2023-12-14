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

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.jms.Message;
import javax.naming.NamingException;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoEntityBuilder;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.value.StringValueDo;
import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IDestination.DestinationType;
import org.eclipse.scout.rt.mom.api.IDestination.ResolveMethod;
import org.eclipse.scout.rt.mom.api.IMessage;
import org.eclipse.scout.rt.mom.api.IMessageListener;
import org.eclipse.scout.rt.mom.api.ISubscription;
import org.eclipse.scout.rt.mom.api.MOM;
import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.mom.api.marshaller.BytesMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.JsonDataObjectMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.JsonMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.ObjectMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.TextMarshaller;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.util.IDisposable;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.eclipse.scout.rt.testing.platform.runner.Times;
import org.eclipse.scout.rt.testing.platform.runner.parameterized.NonParameterized;
import org.eclipse.scout.rt.testing.platform.testcategory.SlowTest;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.hamcrest.MatcherAssert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(SlowTest.class)
public class JmsMomPubSubTest extends AbstractJmsMomTest {

  public JmsMomPubSubTest(AbstractJmsMomTestParameter parameter) {
    super(parameter);
  }

  @Test
  @NonParameterized
  public void testInstanceScoped() {
    installMom();
    JmsMomImplementor mom1 = BEANS.get(JmsMomImplementor.class);
    JmsMomImplementor mom2 = BEANS.get(JmsMomImplementor.class);
    assertNotSame(mom1, mom2);
  }

  @Test
  @NonParameterized
  public void testCreateContextNullMap() throws NamingException {
    installMom();
    new JmsMomImplementor().createContextEnvironment(null);
  }

  @Test
  @NonParameterized
  public void testCreateContextEmptyMap() throws NamingException {
    installMom();
    new JmsMomImplementor().createContextEnvironment(Collections.emptyMap());
  }

  @Test
  @NonParameterized
  public void testCreateContextOrdinaryMap() throws NamingException {
    installMom();
    new JmsMomImplementor().createContextEnvironment(Collections.singletonMap("key", "value"));
  }

  @Test
  @NonParameterized
  public void testCreateContextMapWithNullEntries() throws NamingException {
    installMom();
    new JmsMomImplementor().createContextEnvironment(Collections.singletonMap("key", null));
  }

  @Test
  public void testPublishObject() throws InterruptedException {
    installMom();
    final Capturer<Person> capturer = new Capturer<>();

    Person person = new Person();
    person.setLastname("smith");
    person.setFirstname("anna");

    IDestination<Person> queue = MOM.newDestination("test/mom/testPublishObject", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    ObjectMarshaller marshaller = BEANS.get(ObjectMarshaller.class);
    m_disposables.add(MOM.registerMarshaller(FixtureMom.class, queue, marshaller));
    m_disposables.add(MOM.subscribe(FixtureMom.class, queue, message -> capturer.set(message.getTransferObject())));
    MOM.publish(FixtureMom.class, queue, person);

    // Verify
    Person testee = capturer.get();
    assertEquals("smith", testee.getLastname());
    assertEquals("anna", testee.getFirstname());
    verifyMessageHandlerHandleOutgoingCalled(queue, marshaller, testee);
    verifyMessageHandlerHandleIncomingCalled(queue, marshaller, person);
  }

  @Test
  public void testPublishBytes() throws InterruptedException {
    installMom();
    final Capturer<byte[]> capturer = new Capturer<>();

    IDestination<byte[]> queue = MOM.newDestination("test/mom/testPublishBytes", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    BytesMarshaller marshaller = BEANS.get(BytesMarshaller.class);
    m_disposables.add(MOM.registerMarshaller(FixtureMom.class, queue, marshaller));
    m_disposables.add(MOM.subscribe(FixtureMom.class, queue, message -> capturer.set(message.getTransferObject())));
    byte[] bytes = "hello world".getBytes(StandardCharsets.UTF_8);
    MOM.publish(FixtureMom.class, queue, bytes);

    // Verify
    byte[] testee = capturer.get();
    assertEquals("hello world", new String(testee, StandardCharsets.UTF_8));
    assertArrayEquals(bytes, testee);
    verifyMessageHandlerHandleOutgoingCalled(queue, marshaller, bytes);
    verifyMessageHandlerHandleIncomingCalled(queue, marshaller, testee);
  }

  @Test
  public void testPublishText() throws InterruptedException {
    installMom();
    final Capturer<String> capturer = new Capturer<>();

    IDestination<String> queue = MOM.newDestination("test/mom/testPublishText", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    TextMarshaller marshaller = BEANS.get(TextMarshaller.class);
    m_disposables.add(MOM.registerMarshaller(FixtureMom.class, queue, marshaller));
    m_disposables.add(MOM.subscribe(FixtureMom.class, queue, message -> capturer.set(message.getTransferObject())));
    String string = "hello world";
    MOM.publish(FixtureMom.class, queue, string);

    // Verify
    String testee = capturer.get();
    assertEquals(string, testee);
    verifyMessageHandlerHandleOutgoingCalled(queue, marshaller, string);
    verifyMessageHandlerHandleIncomingCalled(queue, marshaller, testee);
  }

  @Test
  public void testPublishSubscribe() throws InterruptedException {
    installMom();
    assertEquals("Hello World", testPublishAndConsumeInternal("Hello World", BEANS.get(TextMarshaller.class)));
    assertEquals("Hello World", testPublishAndConsumeInternal("Hello World", BEANS.get(ObjectMarshaller.class)));
    assertEquals("Hello World", testPublishAndConsumeInternal("Hello World", BEANS.get(JsonMarshaller.class)));
    IDoEntity entity = BEANS.get(DoEntityBuilder.class).put("message", "Hello World").build();
    assertEquals(entity, testPublishAndConsumeInternal(entity, BEANS.get(JsonDataObjectMarshaller.class)));
    assertArrayEquals("Hello World".getBytes(StandardCharsets.UTF_8), (byte[]) testPublishAndConsumeInternal("Hello World".getBytes(StandardCharsets.UTF_8), BEANS.get(BytesMarshaller.class)));

    assertEquals("Hello World", testPublishAndConsumeInternal("Hello World", BEANS.get(TextMarshaller.class)));
    assertEquals("Hello World", testPublishAndConsumeInternal("Hello World", BEANS.get(ObjectMarshaller.class)));
    assertEquals("Hello World", testPublishAndConsumeInternal("Hello World", BEANS.get(JsonMarshaller.class)));
    assertEquals(entity, testPublishAndConsumeInternal(entity, BEANS.get(JsonDataObjectMarshaller.class)));
    assertArrayEquals("Hello World".getBytes(StandardCharsets.UTF_8), (byte[]) testPublishAndConsumeInternal("Hello World".getBytes(StandardCharsets.UTF_8), BEANS.get(BytesMarshaller.class)));
  }

  @Test
  public void testSubscriptionSingleThreadedDisposeSynchronized() throws InterruptedException {
    installMom();
    testSubscriptionDisposeSynchronized(MOM.newSubscribeInput().withAcknowledgementMode(SubscribeInput.ACKNOWLEDGE_AUTO_SINGLE_THREADED));
    testSubscriptionDisposeSynchronized(MOM.newSubscribeInput().withAcknowledgementMode(SubscribeInput.ACKNOWLEDGE_AUTO));
  }

  /**
   * In case of single threaded subscription, {@link ISubscription#dispose()} waits for any ongoing processing of this
   * subscription to finish.
   */
  protected void testSubscriptionDisposeSynchronized(SubscribeInput subscribeInput) throws InterruptedException {
    final Capturer<String> capturer = new Capturer<>();
    final CountDownLatch latch = new CountDownLatch(1);

    IDestination<String> queue = MOM.newDestination("test/mom/testPublishText", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    m_disposables.add(MOM.registerMarshaller(FixtureMom.class, queue, BEANS.get(TextMarshaller.class)));

    final ISubscription subscription = MOM.subscribe(FixtureMom.class, queue, message -> {
      capturer.set(message.getTransferObject());
      try {
        latch.await();
      }
      catch (InterruptedException e) {
        throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
      }
    }, subscribeInput);
    MOM.publish(FixtureMom.class, queue, "hello world");

    assertEquals("hello world", capturer.get());

    List<ISubscription> subscriptions = BEANS.get(FixtureMom.class).getSubscriptions();
    assertEquals(1, subscriptions.size());
    assertEquals(subscription, subscriptions.get(0));

    IFuture<Void> disposeFuture = Jobs.schedule(subscription::dispose, Jobs.newInput()
        .withName("dispose subscription")
        .withExecutionHint(FixtureJobInput.EXPLICIT_HINT)
        .withExceptionHandling(null, false));

    // Verify
    try {
      disposeFuture.awaitDoneAndGet(1, TimeUnit.SECONDS);
      assertEquals(SubscribeInput.ACKNOWLEDGE_AUTO, subscribeInput.getAcknowledgementMode());
    }
    catch (TimedOutError e) {
      assertEquals(SubscribeInput.ACKNOWLEDGE_AUTO_SINGLE_THREADED, subscribeInput.getAcknowledgementMode());
    }
    finally {
      latch.countDown();
    }

    disposeFuture.awaitDoneAndGet(1, TimeUnit.SECONDS);
    assertTrue(BEANS.get(FixtureMom.class).getSubscriptions().isEmpty());
  }

  @Test
  public void testPublishEmpty() throws InterruptedException {
    installMom();
    assertEquals("", testPublishAndConsumeInternal("", BEANS.get(TextMarshaller.class)));
    assertEquals("", testPublishAndConsumeInternal("", BEANS.get(ObjectMarshaller.class)));
    assertEquals("", testPublishAndConsumeInternal("", BEANS.get(JsonMarshaller.class)));
    assertEquals(BEANS.get(DoEntity.class), testPublishAndConsumeInternal(BEANS.get(DoEntity.class), BEANS.get(JsonMarshaller.class)));
    assertArrayEquals(new byte[0], (byte[]) testPublishAndConsumeInternal(new byte[0], BEANS.get(BytesMarshaller.class)));

    assertEquals("", testPublishAndConsumeInternal("", BEANS.get(TextMarshaller.class)));
    assertEquals("", testPublishAndConsumeInternal("", BEANS.get(ObjectMarshaller.class)));
    assertEquals("", testPublishAndConsumeInternal("", BEANS.get(JsonMarshaller.class)));
    assertEquals(BEANS.get(DoEntity.class), testPublishAndConsumeInternal(BEANS.get(DoEntity.class), BEANS.get(JsonMarshaller.class)));
    assertArrayEquals(new byte[0], (byte[]) testPublishAndConsumeInternal(new byte[0], BEANS.get(BytesMarshaller.class)));
  }

  @Test
  public void testPublishNull() throws InterruptedException {
    installMom();
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(TextMarshaller.class)));
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(ObjectMarshaller.class)));
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(JsonMarshaller.class)));
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(JsonDataObjectMarshaller.class)));
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(BytesMarshaller.class)));

    assertNull(testPublishAndConsumeInternal(null, BEANS.get(TextMarshaller.class)));
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(ObjectMarshaller.class)));
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(JsonMarshaller.class)));
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(JsonDataObjectMarshaller.class)));
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(BytesMarshaller.class)));
  }

  @Test
  public void testPublishJsonData() throws InterruptedException {
    installMom();
    final Capturer<Person> capturer = new Capturer<>();

    IDestination<Person> queue = MOM.newDestination("test/mom/testPublishJsonData", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    JsonMarshaller marshaller = BEANS.get(JsonMarshaller.class);
    m_disposables.add(MOM.registerMarshaller(FixtureMom.class, queue, marshaller));

    Person person = new Person();
    person.setFirstname("anna");
    person.setLastname("smith");

    m_disposables.add(MOM.subscribe(FixtureMom.class, queue, message -> capturer.set(message.getTransferObject())));
    MOM.publish(FixtureMom.class, queue, person);

    // Verify
    Person testee = capturer.get();
    assertEquals("smith", testee.getLastname());
    assertEquals("anna", testee.getFirstname());
    verifyMessageHandlerHandleOutgoingCalled(queue, marshaller, person);
    verifyMessageHandlerHandleIncomingCalled(queue, marshaller, testee);
  }

  @Test
  public void testPublishDataObjectJson() throws InterruptedException {
    installMom();
    final Capturer<StringValueDo> capturer = new Capturer<>();

    IDestination<StringValueDo> queue = MOM.newDestination("test/mom/testPublishDataObjectJson", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    IMarshaller marshaller = BEANS.get(JsonDataObjectMarshaller.class);
    m_disposables.add(MOM.registerMarshaller(FixtureMom.class, queue, marshaller));

    StringValueDo value = StringValueDo.of("foo");

    m_disposables.add(MOM.subscribe(FixtureMom.class, queue, message -> capturer.set(message.getTransferObject())));
    MOM.publish(FixtureMom.class, queue, value);

    // Verify
    StringValueDo testee = capturer.get();
    assertEquals("foo", testee.getValue());
    verifyMessageHandlerHandleOutgoingCalled(queue, marshaller, value);
    verifyMessageHandlerHandleIncomingCalled(queue, marshaller, testee);
  }

  @Test
  public void testTopicPublishSubscribe() throws InterruptedException {
    installMom();
    IDestination<String> topic = MOM.newDestination("test/mom/testTopicPublishSubscribe", DestinationType.TOPIC, ResolveMethod.DEFINE, null);

    final Capturer<String> capturer = new Capturer<>();

    // Subscribe for the destination
    m_disposables.add(MOM.subscribe(FixtureMom.class, topic, message -> capturer.set(message.getTransferObject())));

    // Publish a message
    MOM.publish(FixtureMom.class, topic, "hello world");

    // Verify
    assertEquals("hello world", capturer.get());
  }

  @Test
  public void testTopicPublishSubscribeMultipleSubscriptions() throws InterruptedException {
    installMom();
    IDestination<String> topic = MOM.newDestination("test/mom/testTopicPublishSubscribeMultipleSubscriptions", DestinationType.TOPIC, ResolveMethod.DEFINE, null);

    final CountDownLatch latch = new CountDownLatch(2);

    // Subscribe for the destination
    m_disposables.add(MOM.subscribe(FixtureMom.class, topic, message -> latch.countDown()));

    // Subscribe for the destination
    m_disposables.add(MOM.subscribe(FixtureMom.class, topic, message -> latch.countDown()));

    // Publish a message
    MOM.publish(FixtureMom.class, topic, "hello world");

    // Verify
    assertTrue(latch.await(30, TimeUnit.SECONDS));
  }

  @Test
  @Times(10) // regression
  public void testTopicPublishFirst() throws InterruptedException {
    installMom();
    IDestination<String> topic = MOM.newDestination("test/mom/testTopicPublishFirst", DestinationType.TOPIC, ResolveMethod.DEFINE, null);

    // Publish a message
    MOM.publish(FixtureMom.class, topic, "hello world");

    // Subscribe too late for the destination
    final CountDownLatch latch = new CountDownLatch(1);
    m_disposables.add(MOM.subscribe(FixtureMom.class, topic, message -> latch.countDown()));

    // Verify
    assertFalse(latch.await(200, TimeUnit.MILLISECONDS));
  }

  @Test
  public void testQueuePublishSubscribe() throws InterruptedException {
    installMom();
    IDestination<String> queue = MOM.newDestination("test/mom/testQueuePublishSubscribe", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    final Capturer<String> capturer = new Capturer<>();

    // Subscribe for the destination
    m_disposables.add(MOM.subscribe(FixtureMom.class, queue, message -> capturer.set(message.getTransferObject())));

    // Publish a message
    MOM.publish(FixtureMom.class, queue, "hello world");

    // Verify
    assertEquals("hello world", capturer.get());
  }

  @Test
  @Times(10) // regression
  public void testQueuePublishSubscribeMultipleSubscriptions() throws InterruptedException {
    installMom();
    IDestination<String> queue = MOM.newDestination("test/mom/testQueuePublishSubscribeMultipleSubscriptions", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    final AtomicInteger msgCounter = new AtomicInteger();
    final CountDownLatch latch = new CountDownLatch(1);

    // Subscribe for the destination
    m_disposables.add(MOM.subscribe(FixtureMom.class, queue, message -> {
      msgCounter.incrementAndGet();
      latch.countDown();
    }));

    // Subscribe for the destination
    m_disposables.add(MOM.subscribe(FixtureMom.class, queue, message -> {
      msgCounter.incrementAndGet();
      latch.countDown();
    }));

    // Publish a message
    MOM.publish(FixtureMom.class, queue, "hello world");

    // Verify
    assertTrue(latch.await(5, TimeUnit.SECONDS));
    Thread.sleep(50);
    assertEquals(1, msgCounter.get());
  }

  @Test
  @Times(10) // regression
  public void testQueuePublishFirst() throws InterruptedException {
    installMom();
    IDestination<String> queue = MOM.newDestination("test/mom/testQueuePublishFirst", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    // Publish a message
    MOM.publish(FixtureMom.class, queue, "hello world");

    // Subscribe for the destination
    final CountDownLatch latch = new CountDownLatch(1);
    m_disposables.add(MOM.subscribe(FixtureMom.class, queue, message -> latch.countDown()));

    // Verify
    assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));
  }

  @Test
  public void testQueuePublishSubscribeCorrelationId() throws InterruptedException {
    installMom();
    final IDestination<String> queue = MOM.newDestination("test/mom/testQueuePublishSubscribeCorrelationId", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    testPublishSubscribeCorrelationIdInternal(queue);
  }

  @Test
  public void testTopicPublishSubscribeCorrelationId() throws InterruptedException {
    installMom();
    final IDestination<String> topic = MOM.newDestination("test/mom/testTopicPublishSubscribeCorrelationId", DestinationType.TOPIC, ResolveMethod.DEFINE, null);
    testPublishSubscribeCorrelationIdInternal(topic);
  }

  @Test
  public void testProperties() throws InterruptedException {
    installMom();
    IDestination<String> topic = MOM.newDestination("test/mom/testProperties", DestinationType.TOPIC, ResolveMethod.DEFINE, null);

    final Capturer<String> capturer1 = new Capturer<>();

    // Subscribe for the destination
    m_disposables.add(MOM.subscribe(FixtureMom.class, topic, message -> capturer1.set(message.getProperty("prop"))));

    // Publish a message
    MOM.publish(FixtureMom.class, topic, "hello world", MOM.newPublishInput()
        .withProperty("prop", "propValue"));

    // Verify
    assertEquals("propValue", capturer1.get());
  }

  @Test
  @Times(10)
  public void testTimeToLive() throws InterruptedException {
    installMom();
    IDestination<String> queue = MOM.newDestination("test/mom/testTimeToLive", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    final CountDownLatch latch = new CountDownLatch(1);

    // Publish a message
    MOM.publish(FixtureMom.class, queue, "hello world", MOM.newPublishInput()
        .withTimeToLive(1, TimeUnit.MILLISECONDS));

    Thread.sleep(100);

    // Subscribe for the destination
    m_disposables.add(MOM.subscribe(FixtureMom.class, queue, message -> latch.countDown()));

    // Verify
    assertFalse(latch.await(50, TimeUnit.MILLISECONDS)); // expect the message not to be received
  }

  private void testPublishSubscribeCorrelationIdInternal(final IDestination<String> destination) throws InterruptedException {
    final Capturer<String> cid = new Capturer<>();

    m_disposables.add(MOM.subscribe(FixtureMom.class, destination, message -> cid.set(CorrelationId.CURRENT.get()), MOM.newSubscribeInput()
        .withRunContext(RunContexts.copyCurrent()
            .withCorrelationId("cid:xyz"))));

    RunContexts.copyCurrent()
        .withCorrelationId("cid:abc")
        .run(() -> MOM.publish(FixtureMom.class, destination, "hello world"));

    assertEquals("cid:abc", cid.get());
  }

  public static class Person implements Serializable {

    private static final long serialVersionUID = 1L;

    private long m_id;
    private String m_firstname;
    private String m_lastname;

    public long getId() {
      return m_id;
    }

    public void setId(long id) {
      m_id = id;
    }

    public String getFirstname() {
      return m_firstname;
    }

    public void setFirstname(String firstname) {
      m_firstname = firstname;
    }

    public String getLastname() {
      return m_lastname;
    }

    public void setLastname(String lastname) {
      m_lastname = lastname;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((m_firstname == null) ? 0 : m_firstname.hashCode());
      result = prime * result + (int) (m_id ^ (m_id >>> 32));
      result = prime * result + ((m_lastname == null) ? 0 : m_lastname.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      Person other = (Person) obj;
      if (m_firstname == null) {
        if (other.m_firstname != null) {
          return false;
        }
      }
      else if (!m_firstname.equals(other.m_firstname)) {
        return false;
      }
      if (m_id != other.m_id) {
        return false;
      }
      if (m_lastname == null) {
        if (other.m_lastname != null) {
          return false;
        }
      }
      else if (!m_lastname.equals(other.m_lastname)) {
        return false;
      }
      return true;
    }
  }

  @Test
  public void testPublishJsonDataSecure() throws InterruptedException {
    installMom();
    final Capturer<Person> capturer = new Capturer<>();

    IDestination<Person> queue = MOM.newDestination("test/mom/testPublishJsonDataSecure", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    m_disposables.add(MOM.registerMarshaller(FixtureMom.class, queue, BEANS.get(JsonMarshaller.class)));

    Person person = new Person();
    person.setFirstname("anna");
    person.setLastname("smith");

    MOM.publish(FixtureMom.class, queue, person);
    m_disposables.add(MOM.subscribe(FixtureMom.class, queue, message -> capturer.set(message.getTransferObject())));

    // Verify
    Person testee = capturer.get();
    assertEquals("smith", testee.getLastname());
    assertEquals("anna", testee.getFirstname());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCurrentMessagePubSub() throws InterruptedException {
    installMom();
    CorrelationId.CURRENT.set("cid_test");
    try {
      final Capturer<IMessage<?>> capturer = new Capturer<>();

      Person person = new Person();
      person.setLastname("smith");
      person.setFirstname("anna");

      IDestination<Person> queue = MOM.newDestination("test/mom/testCurrentMessagePubSub", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
      m_disposables.add(MOM.registerMarshaller(FixtureMom.class, queue, BEANS.get(ObjectMarshaller.class)));

      MOM.publish(FixtureMom.class, queue, person);
      m_disposables.add(MOM.subscribe(FixtureMom.class, queue, message -> capturer.set(IMessage.CURRENT.get())));

      // Verify
      IMessage<Person> testee = (IMessage<Person>) capturer.get();
      assertNotNull(testee);
      assertEquals("smith", testee.getTransferObject().getLastname());
      assertEquals("anna", testee.getTransferObject().getFirstname());
      MatcherAssert.assertThat(testee.getAdapter(Message.class), instanceOf(Message.class));
    }
    finally {
      CorrelationId.CURRENT.remove();
    }
  }

  @Test
  public void testPublishTransactional() throws InterruptedException {
    installMom();
    final Capturer<Person> capturer = new Capturer<>();

    Person person = new Person();
    person.setLastname("smith");
    person.setFirstname("anna");

    ITransaction tx = BEANS.get(ITransaction.class);
    ITransaction.CURRENT.set(tx);

    IDestination<Person> queue = MOM.newDestination("test/mom/testPublishTransactional", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    m_disposables.add(MOM.registerMarshaller(FixtureMom.class, queue, BEANS.get(ObjectMarshaller.class)));

    MOM.publish(FixtureMom.class, queue, person, MOM.newPublishInput().withTransactional(true));
    m_disposables.add(MOM.subscribe(FixtureMom.class, queue, message -> capturer.set(message.getTransferObject())));

    try {
      capturer.get(2, TimeUnit.SECONDS);
      fail();
    }
    catch (TimedOutError e) {
      // expected
    }

    tx.commitPhase1();
    tx.commitPhase2();

    try {
      capturer.get(5, TimeUnit.SECONDS);
    }
    catch (TimedOutError e) {
      fail();
    }
    finally {
      tx.release();
    }

    // Verify
    Person testee = capturer.get();
    assertEquals("smith", testee.getLastname());
    assertEquals("anna", testee.getFirstname());
  }

  @Test
  public void testSubscribeTransactional() throws InterruptedException {
    installMom();
    IDestination<String> queue = MOM.newDestination("test/mom/testSubscribeTransactional", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    m_disposables.add(MOM.registerMarshaller(FixtureMom.class, queue, BEANS.get(ObjectMarshaller.class)));

    MOM.publish(FixtureMom.class, queue, "message-1", MOM.newPublishInput());

    final AtomicInteger messageCounter = new AtomicInteger();

    // 1. Receive message, but reject it (rollback)
    final BlockingCountDownLatch message1Latch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch message2Latch = new BlockingCountDownLatch(1);

    IMessageListener<String> listener = message -> {
      switch (message.getTransferObject()) {
        case "message-1":
          switch (messageCounter.incrementAndGet()) {
            case 1:
            case 2:
            case 3:
              ITransaction.CURRENT.get().rollback();
              break;
            default:
              message1Latch.countDown();
              break;
          }
          return;
        case "message-2":
          message2Latch.countDown();
          break;
        default:
          throw new IllegalArgumentException("illegal message");
      }
    };

    // Register transactional subscriber
    m_disposables.add(MOM.subscribe(FixtureMom.class, queue, listener, MOM.newSubscribeInput().withAcknowledgementMode(SubscribeInput.ACKNOWLEDGE_TRANSACTED)));
    assertTrue("message expected to be rejected 3 times", message1Latch.await());

    // Publish a next message
    MOM.publish(FixtureMom.class, queue, "message-2", MOM.newPublishInput());

    // Check that the message was received
    assertTrue("message expected to be received", message2Latch.await());

    Thread.sleep(1000); // Wait some time to verify that 'message-1' is no longer received.
    assertEquals(messageCounter.get(), 4);

    List<ISubscription> subscriptions = BEANS.get(FixtureMom.class).getSubscriptions();
    assertEquals(1, subscriptions.size());
    assertEquals(queue, subscriptions.get(0).getDestination());
  }

  @Test
  public void testConcurrentMessageConsumption() throws InterruptedException {
    installMom();
    IDestination<Object> queue = MOM.newDestination("test/mom/testConcurrentMessageConsumption", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    // 1. Publish some messages
    int msgCount = 10;
    for (int i = 0; i < msgCount; i++) {
      MOM.publish(FixtureMom.class, queue, "hello");
    }

    // 1. Publish some messages
    final BlockingCountDownLatch latch = new BlockingCountDownLatch(msgCount, 3, TimeUnit.SECONDS);
    m_disposables.add(MOM.subscribe(FixtureMom.class, queue, message -> {
      try {
        latch.countDownAndBlock(1, TimeUnit.MINUTES); // timeout must be greater than the default latch timeout
      }
      catch (InterruptedException e) {
        throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
      }
    }));

    try {
      assertTrue("messages expected to be consumed concurrently", latch.await());
    }
    finally {
      latch.unblock();
    }
  }

  @Test
  public void testSerialMessageConsumption() throws InterruptedException {
    installMom();
    IDestination<Object> queue = MOM.newDestination("test/mom/testSerialMessageConsumption", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    // 1. Publish some messages
    int msgCount = 10;
    for (int i = 0; i < msgCount; i++) {
      MOM.publish(FixtureMom.class, queue, "hello");
    }

    // 2. Consume the messages
    final BlockingCountDownLatch latch = new BlockingCountDownLatch(msgCount, 3, TimeUnit.SECONDS);
    m_disposables.add(MOM.subscribe(FixtureMom.class, queue, message -> {
      try {
        latch.countDownAndBlock(1, TimeUnit.MINUTES); // timeout must be greater than the default latch timeout
      }
      catch (InterruptedException e) {
        throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
      }
    }, MOM.newSubscribeInput().withAcknowledgementMode(SubscribeInput.ACKNOWLEDGE_AUTO_SINGLE_THREADED)));

    try {
      assertFalse("messages not expected to be consumed concurrently", latch.await());
      assertEquals("one message expected to be consumed", msgCount - 1, latch.getCount());
    }
    finally {
      latch.unblock();
    }
  }

  @Test
  public void testMessageSelector() throws InterruptedException {
    installMom();
    final Capturer<String> allCapturer = new Capturer<>();
    final Capturer<String> johnCapturer = new Capturer<>();
    final Capturer<String> annaCapturer = new Capturer<>();

    IDestination<String> topic = MOM.newDestination("test/mom/testMessageSelector", DestinationType.TOPIC, ResolveMethod.DEFINE, null);
    // register subscriber without selector
    m_disposables.add(MOM.subscribe(FixtureMom.class, topic, message -> allCapturer.set(message.getTransferObject())));
    // register subscriber with selector user = 'john'
    m_disposables.add(MOM.subscribe(FixtureMom.class, topic, message -> johnCapturer.set(message.getTransferObject()), MOM.newSubscribeInput().withSelector("user = 'john'")));

    // register subscriber with selector user = 'anna'
    m_disposables.add(MOM.subscribe(FixtureMom.class, topic, message -> annaCapturer.set(message.getTransferObject()), MOM.newSubscribeInput().withSelector("user = 'anna'")));

    // Publish the message for anna
    MOM.publish(FixtureMom.class, topic, "message-for-anna", MOM.newPublishInput().withProperty("user", "anna"));

    // Verify
    try {
      johnCapturer.get(2, TimeUnit.SECONDS);
      fail("timeout expected");
    }
    catch (TimedOutError e) {
      // NOOP
    }

    assertEquals("message-for-anna", allCapturer.get(5, TimeUnit.SECONDS));
    assertEquals("message-for-anna", annaCapturer.get(5, TimeUnit.SECONDS));

    assertEquals(3, BEANS.get(FixtureMom.class).getSubscriptions().size());
  }

  private Object testPublishAndConsumeInternal(Object transferObject, IMarshaller marshaller) throws InterruptedException {
    reset(BEANS.get(IJmsMessageHandler.class));

    final Capturer<Object> capturer = new Capturer<>();

    IDestination<Object> queue = MOM.newDestination("test/mom/testPublishAndConsumeInternal", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    List<IDisposable> disposables = new ArrayList<>();
    if (marshaller != null) {
      disposables.add(MOM.registerMarshaller(FixtureMom.class, queue, marshaller));
    }
    disposables.add(MOM.subscribe(FixtureMom.class, queue, msg -> capturer.set(msg.getTransferObject()), MOM.newSubscribeInput()
        // use single threaded in order to block dispose until subscription is completely released
        .withAcknowledgementMode(SubscribeInput.ACKNOWLEDGE_AUTO_SINGLE_THREADED)));
    MOM.publish(FixtureMom.class, queue, transferObject);

    // Verify
    try {
      Object object = capturer.get();
      verifyMessageHandlerHandleIncomingCalled(queue, marshaller, transferObject);
      verifyMessageHandlerHandleOutgoingCalled(queue, marshaller, object);
      return object;
    }
    finally {
      dispose(disposables);
    }
  }

  @Test
  public void testTopicDurableSubscription() throws InterruptedException {
    installMom();
    // J2EE implementor does not set any client id; therefore no durable subscription is possible
    Assume.assumeFalse(J2eeJmsMomImplementor.class.isAssignableFrom(m_testParameter.getImplementor()));

    final IDestination<String> topic = MOM.newDestination("test/mom/testTopicPublishSubscribe", DestinationType.TOPIC, ResolveMethod.DEFINE, null);
    final String durableSubscriptionName = "Durable-Test-Subscription";

    // 1. Subscribe (non-durable)
    final Capturer<String> capturer1 = new Capturer<>();
    final ISubscription subscription1 = MOM.subscribe(FixtureMom.class, topic, new CapturerListener<>(capturer1), MOM.newSubscribeInput());
    m_disposables.add(subscription1);

    assertEquals(1, BEANS.get(FixtureMom.class).getSubscriptions().size());
    assertEquals(subscription1, BEANS.get(FixtureMom.class).getSubscriptions().get(0));

    // 2. Disconnect
    subscription1.dispose();

    assertTrue(BEANS.get(FixtureMom.class).getSubscriptions().isEmpty());

    // 3. Publish a message
    MOM.publish(FixtureMom.class, topic, "lost message");
    capturer1.assertEmpty(1, TimeUnit.SECONDS); // no one is listening

    // 4. Subscribe again (durable)
    final Capturer<String> capturer2 = new Capturer<>();
    final ISubscription subscription2 = MOM.subscribe(FixtureMom.class, topic, new CapturerListener<>(capturer2), MOM.newSubscribeInput()
        .withDurableSubscription(durableSubscriptionName));
    m_disposables.add(subscription2);

    // 5. Assert that message is lost
    capturer2.assertEmpty(1, TimeUnit.SECONDS);

    assertEquals(1, BEANS.get(FixtureMom.class).getSubscriptions().size());
    assertEquals(subscription2, BEANS.get(FixtureMom.class).getSubscriptions().get(0));

    // 6. Disconnect
    subscription2.dispose();

    assertTrue(BEANS.get(FixtureMom.class).getSubscriptions().isEmpty());

    // 7. Publish another message
    MOM.publish(FixtureMom.class, topic, "hello world");
    capturer2.assertEmpty(1, TimeUnit.SECONDS); // not yet

    // 8. Subscribe again (durable, same name)
    final Capturer<String> capturer3 = new Capturer<>();
    final ISubscription subscription3 = MOM.subscribe(FixtureMom.class, topic, new CapturerListener<>(capturer3), MOM.newSubscribeInput()
        .withDurableSubscription(durableSubscriptionName));
    m_disposables.add(subscription3);

    // 9. Assert that the message is received
    assertEquals("hello world", capturer3.get(1, TimeUnit.SECONDS));

    assertEquals(1, BEANS.get(FixtureMom.class).getSubscriptions().size());
    assertEquals(subscription3, BEANS.get(FixtureMom.class).getSubscriptions().get(0));

    // 10. Disconnect and cancel the durable subscription
    subscription3.dispose();
    MOM.cancelDurableSubscription(FixtureMom.class, durableSubscriptionName);

    assertTrue(BEANS.get(FixtureMom.class).getSubscriptions().isEmpty());

    // 11. Publish another message
    MOM.publish(FixtureMom.class, topic, "hello universe");
    assertEquals("hello world", capturer3.get(1, TimeUnit.SECONDS)); // still the same old message

    // 12. Subscribe again (durable, same name)
    final Capturer<String> capturer4 = new Capturer<>();
    final ISubscription subscription4 = MOM.subscribe(FixtureMom.class, topic, new CapturerListener<>(capturer4), MOM.newSubscribeInput()
        .withDurableSubscription(durableSubscriptionName));
    m_disposables.add(subscription4);

    // 13. Assert that message is still lost, even if the same name was used (because the previous subscription was cancelled explicitly)
    capturer4.assertEmpty(1, TimeUnit.SECONDS);

    assertEquals(1, BEANS.get(FixtureMom.class).getSubscriptions().size());
    assertEquals(subscription4, BEANS.get(FixtureMom.class).getSubscriptions().get(0));
  }
}
