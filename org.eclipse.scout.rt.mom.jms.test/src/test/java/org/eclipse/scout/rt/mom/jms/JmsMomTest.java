package org.eclipse.scout.rt.mom.jms;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.naming.Context;

import org.apache.activemq.jndi.ActiveMQInitialContextFactory;
import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IMessage;
import org.eclipse.scout.rt.mom.api.IMessageListener;
import org.eclipse.scout.rt.mom.api.IMom;
import org.eclipse.scout.rt.mom.api.IMom.EncrypterProperty;
import org.eclipse.scout.rt.mom.api.IMomInitializer;
import org.eclipse.scout.rt.mom.api.IRequestListener;
import org.eclipse.scout.rt.mom.api.encrypter.ClusterEncrypter;
import org.eclipse.scout.rt.mom.api.encrypter.ClusterEncrypter.PbePasswordProperty;
import org.eclipse.scout.rt.mom.api.encrypter.ClusterEncrypter.PbeSaltProperty;
import org.eclipse.scout.rt.mom.api.encrypter.IEncrypter;
import org.eclipse.scout.rt.mom.api.marshaller.BytesMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.JsonMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.ObjectMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.TextMarshaller;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.platform.util.IDisposable;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedException;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutException;
import org.eclipse.scout.rt.testing.platform.runner.JUnitExceptionHandler;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.Times;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JmsMomTest {

  private static JmsMom s_mom;

  private List<IDisposable> m_disposables;

  private String m_testJobExecutionHint;

  private IBean<EncrypterProperty> m_nullEncrypterProperty;
  private IBean<ClusterEncrypter.PbeSaltProperty> m_junitPbeSaltProperty;
  private IBean<ClusterEncrypter.PbePasswordProperty> m_junitPbePasswordProperty;

  @BeforeClass
  public static void beforeClass() throws Exception {
    final Map<Object, Object> env = new HashMap<>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, ActiveMQInitialContextFactory.class.getName());
    env.put(Context.PROVIDER_URL, "vm://junitBroker?broker.persistent=false");
    env.put("connectionFactoryNames", "JUnitConnectionFactory"); // Active MQ specific
    env.put(IMomInitializer.CONNECTION_FACTORY, "JUnitConnectionFactory");

    s_mom = BEANS.get(JmsMom.class);
    s_mom.init(env);
  }

  @AfterClass
  public static void afterClass() {
    s_mom.destroy();
  }

  @Before
  public void before() {
    m_testJobExecutionHint = UUID.randomUUID().toString();
    m_disposables = new ArrayList<>();

    // Unregister default encrypter
    m_nullEncrypterProperty = BEANS.getBeanManager().registerBean(new BeanMetaData(NullEncrypterProperty.class)
        .withInitialInstance(new NullEncrypterProperty())
        .withReplace(true));

    // Replace default PBE_SALT property
    m_junitPbeSaltProperty = BEANS.getBeanManager().registerBean(new BeanMetaData(JUnitPbeSaltProperty.class)
        .withInitialInstance(new JUnitPbeSaltProperty())
        .withReplace(true));

    // Replace default PBE_PASSWORD property
    m_junitPbePasswordProperty = BEANS.getBeanManager().registerBean(new BeanMetaData(JUnitPbePasswordProperty.class)
        .withInitialInstance(new JUnitPbePasswordProperty())
        .withReplace(true));
  }

  @After
  public void after() {
    for (IDisposable disposable : m_disposables) {
      disposable.dispose();
    }
    Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(m_testJobExecutionHint)
        .toFilter(), true);

    // Restore default encrypter
    BEANS.getBeanManager().unregisterBean(m_nullEncrypterProperty);
    BEANS.getBeanManager().unregisterBean(m_junitPbeSaltProperty);
    BEANS.getBeanManager().unregisterBean(m_junitPbePasswordProperty);
  }

  @Test
  public void testInstanceScoped() {
    JmsMom mom1 = BEANS.get(JmsMom.class);
    JmsMom mom2 = BEANS.get(JmsMom.class);
    assertNotSame(mom1, mom2);
  }

  @Test
  public void testPublishObject() throws InterruptedException {
    final Capturer<Person> capturer = new Capturer<>();

    Person person = new Person();
    person.setLastname("smith");
    person.setFirstname("anna");

    IDestination queue = s_mom.newQueue("test/mom");
    m_disposables.add(s_mom.registerMarshaller(queue, BEANS.get(ObjectMarshaller.class)));

    s_mom.publish(queue, person);
    m_disposables.add(s_mom.subscribe(queue, new IMessageListener<Person>() {

      @Override
      public void onMessage(IMessage<Person> message) {
        capturer.set(message.getTransferObject());
      }
    }, null));

    // Verify
    Person testee = capturer.get();
    assertEquals("smith", testee.getLastname());
    assertEquals("anna", testee.getFirstname());
  }

  @Test
  public void testPublishBytes() throws InterruptedException {
    final Capturer<byte[]> capturer = new Capturer<>();

    IDestination queue = s_mom.newQueue("test/mom");
    m_disposables.add(s_mom.registerMarshaller(queue, BEANS.get(BytesMarshaller.class)));

    s_mom.publish(queue, "hello world".getBytes(StandardCharsets.UTF_8));
    m_disposables.add(s_mom.subscribe(queue, new IMessageListener<byte[]>() {

      @Override
      public void onMessage(IMessage<byte[]> message) {
        capturer.set(message.getTransferObject());
      }
    }, null));

    // Verify
    byte[] testee = capturer.get();
    assertEquals("hello world", new String(testee, StandardCharsets.UTF_8));
  }

  @Test
  public void testPublishText() throws InterruptedException {
    final Capturer<String> capturer = new Capturer<>();

    IDestination queue = s_mom.newQueue("test/mom");
    m_disposables.add(s_mom.registerMarshaller(queue, BEANS.get(TextMarshaller.class)));

    s_mom.publish(queue, "hello world");
    m_disposables.add(s_mom.subscribe(queue, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        capturer.set(message.getTransferObject());
      }
    }, null));

    // Verify
    String testee = capturer.get();
    assertEquals("hello world", testee);
  }

  @Test
  public void testPublishSubscribe() throws InterruptedException {
    assertEquals("Hello World", testPublishAndConsumeInternal("Hello World", BEANS.get(TextMarshaller.class), BEANS.get(ClusterEncrypter.class)));
    assertEquals("Hello World", testPublishAndConsumeInternal("Hello World", BEANS.get(ObjectMarshaller.class), BEANS.get(ClusterEncrypter.class)));
    assertEquals("Hello World", testPublishAndConsumeInternal("Hello World", BEANS.get(JsonMarshaller.class), BEANS.get(ClusterEncrypter.class)));
    assertArrayEquals("Hello World".getBytes(StandardCharsets.UTF_8), (byte[]) testPublishAndConsumeInternal("Hello World".getBytes(StandardCharsets.UTF_8), BEANS.get(BytesMarshaller.class), BEANS.get(ClusterEncrypter.class)));

    assertEquals("Hello World", testPublishAndConsumeInternal("Hello World", BEANS.get(TextMarshaller.class), null));
    assertEquals("Hello World", testPublishAndConsumeInternal("Hello World", BEANS.get(ObjectMarshaller.class), null));
    assertEquals("Hello World", testPublishAndConsumeInternal("Hello World", BEANS.get(JsonMarshaller.class), null));
    assertArrayEquals("Hello World".getBytes(StandardCharsets.UTF_8), (byte[]) testPublishAndConsumeInternal("Hello World".getBytes(StandardCharsets.UTF_8), BEANS.get(BytesMarshaller.class), null));
  }

  @Test(timeout = 15_000)
  public void testRequestReply() throws InterruptedException {
    assertEquals("Hello World", testRequestReplyInternal("Hello World", BEANS.get(TextMarshaller.class), BEANS.get(ClusterEncrypter.class)));
    assertEquals("Hello World", testRequestReplyInternal("Hello World", BEANS.get(ObjectMarshaller.class), BEANS.get(ClusterEncrypter.class)));
    assertEquals("Hello World", testRequestReplyInternal("Hello World", BEANS.get(JsonMarshaller.class), BEANS.get(ClusterEncrypter.class)));
    assertArrayEquals("Hello World".getBytes(StandardCharsets.UTF_8), (byte[]) testRequestReplyInternal("Hello World".getBytes(StandardCharsets.UTF_8), BEANS.get(BytesMarshaller.class), BEANS.get(ClusterEncrypter.class)));

    assertEquals("Hello World", testRequestReplyInternal("Hello World", BEANS.get(TextMarshaller.class), null));
    assertEquals("Hello World", testRequestReplyInternal("Hello World", BEANS.get(ObjectMarshaller.class), null));
    assertEquals("Hello World", testRequestReplyInternal("Hello World", BEANS.get(JsonMarshaller.class), null));
    assertArrayEquals("Hello World".getBytes(StandardCharsets.UTF_8), (byte[]) testRequestReplyInternal("Hello World".getBytes(StandardCharsets.UTF_8), BEANS.get(BytesMarshaller.class), null));
  }

  @Test
  public void testPublishEmpty() throws InterruptedException {
    assertEquals("", testPublishAndConsumeInternal("", BEANS.get(TextMarshaller.class), BEANS.get(ClusterEncrypter.class)));
    assertEquals("", testPublishAndConsumeInternal("", BEANS.get(ObjectMarshaller.class), BEANS.get(ClusterEncrypter.class)));
    assertEquals("", testPublishAndConsumeInternal("", BEANS.get(JsonMarshaller.class), BEANS.get(ClusterEncrypter.class)));
    assertArrayEquals(new byte[0], (byte[]) testPublishAndConsumeInternal(new byte[0], BEANS.get(BytesMarshaller.class), BEANS.get(ClusterEncrypter.class)));

    assertEquals("", testPublishAndConsumeInternal("", BEANS.get(TextMarshaller.class), null));
    assertEquals("", testPublishAndConsumeInternal("", BEANS.get(ObjectMarshaller.class), null));
    assertEquals("", testPublishAndConsumeInternal("", BEANS.get(JsonMarshaller.class), null));
    assertArrayEquals(new byte[0], (byte[]) testPublishAndConsumeInternal(new byte[0], BEANS.get(BytesMarshaller.class), null));
  }

  @Test
  public void testPublishNull() throws InterruptedException {
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(TextMarshaller.class), BEANS.get(ClusterEncrypter.class)));
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(ObjectMarshaller.class), BEANS.get(ClusterEncrypter.class)));
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(JsonMarshaller.class), BEANS.get(ClusterEncrypter.class)));
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(BytesMarshaller.class), BEANS.get(ClusterEncrypter.class)));

    assertNull(testPublishAndConsumeInternal(null, BEANS.get(TextMarshaller.class), null));
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(ObjectMarshaller.class), null));
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(JsonMarshaller.class), null));
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(BytesMarshaller.class), null));
  }

  @Test(timeout = 15_000)
  public void testRequestReplyEmpty() throws InterruptedException {
    assertEquals("", testRequestReplyInternal("", BEANS.get(TextMarshaller.class), null));
    assertEquals("", testRequestReplyInternal("", BEANS.get(ObjectMarshaller.class), null));
    assertEquals("", testRequestReplyInternal("", BEANS.get(JsonMarshaller.class), null));
    assertArrayEquals(new byte[0], (byte[]) testRequestReplyInternal(new byte[0], BEANS.get(BytesMarshaller.class), BEANS.get(ClusterEncrypter.class)));

    assertEquals("", testRequestReplyInternal("", BEANS.get(TextMarshaller.class), BEANS.get(ClusterEncrypter.class)));
    assertEquals("", testRequestReplyInternal("", BEANS.get(ObjectMarshaller.class), BEANS.get(ClusterEncrypter.class)));
    assertEquals("", testRequestReplyInternal("", BEANS.get(JsonMarshaller.class), BEANS.get(ClusterEncrypter.class)));
    assertArrayEquals(new byte[0], (byte[]) testRequestReplyInternal(new byte[0], BEANS.get(BytesMarshaller.class), BEANS.get(ClusterEncrypter.class)));
  }

  @Test(timeout = 15_000)
  public void testRequestReplyNull() throws InterruptedException {
    assertNull(testRequestReplyInternal(null, BEANS.get(TextMarshaller.class), BEANS.get(ClusterEncrypter.class)));
    assertNull(testRequestReplyInternal(null, BEANS.get(ObjectMarshaller.class), BEANS.get(ClusterEncrypter.class)));
    assertNull(testRequestReplyInternal(null, BEANS.get(JsonMarshaller.class), BEANS.get(ClusterEncrypter.class)));
    assertNull(testRequestReplyInternal(null, BEANS.get(BytesMarshaller.class), BEANS.get(ClusterEncrypter.class)));

    assertNull(testRequestReplyInternal(null, BEANS.get(TextMarshaller.class), null));
    assertNull(testRequestReplyInternal(null, BEANS.get(ObjectMarshaller.class), null));
    assertNull(testRequestReplyInternal(null, BEANS.get(JsonMarshaller.class), null));
    assertNull(testRequestReplyInternal(null, BEANS.get(BytesMarshaller.class), null));
  }

  @Test
  public void testPublishJsonData() throws InterruptedException {
    final Capturer<Person> capturer = new Capturer<>();

    IDestination queue = s_mom.newQueue("test/mom");
    m_disposables.add(s_mom.registerMarshaller(queue, BEANS.get(JsonMarshaller.class)));

    Person person = new Person();
    person.setFirstname("anna");
    person.setLastname("smith");

    s_mom.publish(queue, person);
    m_disposables.add(s_mom.subscribe(queue, new IMessageListener<Person>() {

      @Override
      public void onMessage(IMessage<Person> message) {
        capturer.set(message.getTransferObject());
      }
    }, null));

    // Verify
    Person testee = capturer.get();
    assertEquals("smith", testee.getLastname());
    assertEquals("anna", testee.getFirstname());
  }

  @Test
  public void testTopicPublishSubscribe() throws InterruptedException {
    IDestination topic = s_mom.newTopic("test/mom");

    final Capturer<String> capturer = new Capturer<>();

    // Subscribe for the destination
    m_disposables.add(s_mom.subscribe(topic, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        capturer.set(message.getTransferObject());
      }
    }, null));

    // Publish a message
    s_mom.publish(topic, "hello world");

    // Verify
    assertEquals("hello world", capturer.get());
  }

  @Test
  public void testTopicPublishSubscribeMultipleSubscriptions() throws InterruptedException {
    IDestination topic = s_mom.newTopic("test/mom");

    final CountDownLatch latch = new CountDownLatch(2);

    // Subscribe for the destination
    m_disposables.add(s_mom.subscribe(topic, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        latch.countDown();
      }
    }, null));

    // Subscribe for the destination
    m_disposables.add(s_mom.subscribe(topic, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        latch.countDown();
      }
    }, null));

    // Publish a message
    s_mom.publish(topic, "hello world");

    // Verify
    assertTrue(latch.await(5, TimeUnit.SECONDS));
  }

  @Test
  @Times(10) // regression
  public void testTopicPublishFirst() throws InterruptedException {
    IDestination topic = s_mom.newTopic("test/mom");

    // Publish a message
    s_mom.publish(topic, "hello world");

    // Subscribe for the destination
    final CountDownLatch latch = new CountDownLatch(1);
    m_disposables.add(s_mom.subscribe(topic, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        latch.countDown();
      }
    }, null));

    // Verify
    assertFalse(latch.await(200, TimeUnit.MILLISECONDS));
  }

  @Test
  public void testQueuePublishSubscribe() throws InterruptedException {
    IDestination queue = s_mom.newQueue("test/mom");

    final Capturer<String> capturer = new Capturer<>();

    // Subscribe for the destination
    m_disposables.add(s_mom.subscribe(queue, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        capturer.set(message.getTransferObject());
      }
    }, null));

    // Publish a message
    s_mom.publish(queue, "hello world");

    // Verify
    assertEquals("hello world", capturer.get());
  }

  @Test
  @Times(10) // regression
  public void testQueuePublishSubscribeMultipleSubscriptions() throws InterruptedException {
    IDestination queue = s_mom.newQueue("test/mom");

    final AtomicInteger msgCounter = new AtomicInteger();
    final CountDownLatch latch = new CountDownLatch(1);

    // Subscribe for the destination
    m_disposables.add(s_mom.subscribe(queue, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        msgCounter.incrementAndGet();
        latch.countDown();
      }
    }, null));

    // Subscribe for the destination
    m_disposables.add(s_mom.subscribe(queue, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        msgCounter.incrementAndGet();
        latch.countDown();
      }
    }, null));

    // Publish a message
    s_mom.publish(queue, "hello world");

    // Verify
    latch.await(5, TimeUnit.SECONDS);
    Thread.sleep(50);
    assertEquals(1, msgCounter.get());
  }

  @Test
  @Times(10) // regression
  public void testQueuePublishFirst() throws InterruptedException {
    IDestination queue = s_mom.newQueue("test/mom");

    // Publish a message
    s_mom.publish(queue, "hello world");

    // Subscribe for the destination
    final CountDownLatch latch = new CountDownLatch(1);
    m_disposables.add(s_mom.subscribe(queue, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        latch.countDown();
      }
    }, null));

    // Verify
    assertTrue(latch.await(200, TimeUnit.MILLISECONDS));
  }

  @Test
  public void testQueuePublishSubscribeCorrelationId() throws InterruptedException {
    final IDestination queue = s_mom.newQueue("test/mom");
    testPublishSubscribeCorrelationIdInternal(queue);
  }

  @Test
  public void testTopicPublishSubscribeCorrelationId() throws InterruptedException {
    final IDestination topic = s_mom.newTopic("test/mom");
    testPublishSubscribeCorrelationIdInternal(topic);
  }

  @Test(timeout = 15_000)
  public void testQueueRequestReply() throws InterruptedException {
    IDestination queue = s_mom.newQueue("test/mom");

    // Subscribe for the destination
    m_disposables.add(s_mom.reply(queue, new IRequestListener<String, String>() {

      @Override
      public String onRequest(IMessage<String> request) {
        return request.getTransferObject().toUpperCase();
      }
    }, null));

    // Initiate 'request-reply' communication
    String testee = s_mom.request(queue, "hello world");

    // Verify
    assertEquals("HELLO WORLD", testee);
  }

  @Test(timeout = 15_000)
  public void testQueueRequestReplyCorrelationId() throws InterruptedException {
    IDestination queue = s_mom.newQueue("test/mom");
    testRequestReplyCorrelationIdInternal(queue);
  }

  @Test(timeout = 15_000)
  public void testTopicRequestReplyCorrelationId() throws InterruptedException {
    IDestination topic = s_mom.newTopic("test/mom");
    testRequestReplyCorrelationIdInternal(topic);
  }

  @Test
  public void testProperties() throws InterruptedException {
    testProperties(null);
    testProperties(BEANS.get(ClusterEncrypter.class));
  }

  private void testProperties(IEncrypter encrypter) throws InterruptedException {
    IDestination topic = s_mom.newTopic("test/mom");

    List<IDisposable> disposables = new ArrayList<>();
    if (encrypter != null) {
      disposables.add(s_mom.registerEncrypter(topic, encrypter));
    }

    final Capturer<String> capturer1 = new Capturer<>();

    // Subscribe for the destination
    disposables.add(s_mom.subscribe(topic, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        capturer1.set((String) message.getProperty("prop"));
      }
    }, null));

    // Publish a message
    s_mom.publish(topic, "hello world", s_mom.newPublishInput()
        .withProperty("prop", "propValue"));

    // Verify
    assertEquals("propValue", capturer1.get());

    dispose(disposables);
  }

  @Test
  @Times(10)
  public void testTimeToLive() throws InterruptedException {
    IDestination queue = s_mom.newQueue("test/mom");

    final CountDownLatch latch = new CountDownLatch(1);

    // Publish a message
    s_mom.publish(queue, "hello world", s_mom.newPublishInput()
        .withTimeToLive(1, TimeUnit.MILLISECONDS));

    Thread.sleep(100);

    // Subscribe for the destination
    m_disposables.add(s_mom.subscribe(queue, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        latch.countDown();
      }
    }, null));

    // Verify
    assertFalse(latch.await(50, TimeUnit.MILLISECONDS)); // expect the message not to be received
  }

  private void testRequestReplyCorrelationIdInternal(final IDestination destination) throws InterruptedException {
    m_disposables.add(s_mom.reply(destination, new IRequestListener<String, String>() {

      @Override
      public String onRequest(IMessage<String> request) {
        return CorrelationId.CURRENT.get();
      }
    }, RunContexts.copyCurrent()
        .withCorrelationId("cid:xyz")));

    // Initiate 'request-reply' communication
    RunContexts.empty()
        .withCorrelationId("cid:abc")
        .run(new IRunnable() {

          @Override
          public void run() throws Exception {
            String testee = s_mom.request(destination, "hello world");
            // Verify
            assertEquals("cid:abc", testee);
          }
        });
  }

  @Test(timeout = 15_000)
  public void testTopicRequestReply() throws InterruptedException {
    IDestination topic = s_mom.newTopic("test/mom");

    // Subscribe for the destination
    m_disposables.add(s_mom.reply(topic, new IRequestListener<String, String>() {

      @Override
      public String onRequest(IMessage<String> request) {
        return request.getTransferObject().toUpperCase();
      }
    }, null));

    // Initiate 'request-reply' communication
    String testee = s_mom.request(topic, "hello world");

    // Verify
    assertEquals("HELLO WORLD", testee);
  }

  @Test(timeout = 15_000)
  @Times(10) // regression
  public void testQueueRequestReplyMultipleSubscriptions() throws InterruptedException {
    IDestination queue = s_mom.newQueue("test/mom");

    final CountDownLatch msgLatch = new CountDownLatch(2);

    // Subscribe for the destination
    m_disposables.add(s_mom.reply(queue, new IRequestListener<String, String>() {

      @Override
      public String onRequest(IMessage<String> request) {
        msgLatch.countDown();
        return request.getTransferObject().toUpperCase();
      }
    }, null));

    // Subscribe for the destination
    m_disposables.add(s_mom.reply(queue, new IRequestListener<String, String>() {

      @Override
      public String onRequest(IMessage<String> request) {
        msgLatch.countDown();
        return request.getTransferObject().toUpperCase();
      }
    }, null));

    // Initiate 'request-reply' communication
    String testee = s_mom.request(queue, "hello world");

    // Verify
    assertEquals("HELLO WORLD", testee);
    assertFalse(msgLatch.await(50, TimeUnit.MILLISECONDS));
  }

  @Test(timeout = 15_000)
  public void testTopicRequestReplyMultipleSubscriptions() throws InterruptedException {
    IDestination topic = s_mom.newTopic("test/mom");

    final CountDownLatch msgLatch = new CountDownLatch(2);

    // Subscribe for the destination
    m_disposables.add(s_mom.reply(topic, new IRequestListener<String, String>() {

      @Override
      public String onRequest(IMessage<String> request) {
        msgLatch.countDown();
        return request.getTransferObject().toUpperCase();
      }
    }, null));

    // Subscribe for the destination
    m_disposables.add(s_mom.reply(topic, new IRequestListener<String, String>() {

      @Override
      public String onRequest(IMessage<String> request) {
        msgLatch.countDown();
        return request.getTransferObject().toUpperCase();
      }
    }, null));

    // Initiate 'request-reply' communication
    String testee = s_mom.request(topic, "hello world");

    // Verify
    assertEquals("HELLO WORLD", testee);
    assertTrue(msgLatch.await(5, TimeUnit.SECONDS));
  }

  @Test(timeout = 15_000)
  public void testQueueRequestReplyRequestFirst() throws InterruptedException {
    final IDestination queue = s_mom.newQueue("test/mom");

    // Test setup: simulate to initiate 'request-reply' before subscription
    IExecutionSemaphore mutex = Jobs.newExecutionSemaphore(1);

    // 1. Initiate 'request-reply' communication
    IFuture<String> requestFuture = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        return s_mom.request(queue, "hello world");
      }
    }, Jobs.newInput()
        .withExecutionHint(m_testJobExecutionHint)
        .withExecutionSemaphore(mutex));

    // 2. Subscribe for reply
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        // Subscribe replier
        m_disposables.add(s_mom.reply(queue, new IRequestListener<String, String>() {

          @Override
          public String onRequest(IMessage<String> request) {
            return request.getTransferObject().toUpperCase();
          }
        }, null));
      }
    }, Jobs.newInput()
        .withExecutionHint(m_testJobExecutionHint)
        .withExecutionSemaphore(mutex));

    String testee = requestFuture.awaitDoneAndGet(10, TimeUnit.SECONDS);

    // Verify
    assertEquals("HELLO WORLD", testee);
  }

  @Test(timeout = 15_000)
  @Times(10) // regression
  public void testTopicRequestReplyRequestFirst() throws InterruptedException {
    final IDestination topic = s_mom.newTopic("test/mom");

    // Test setup: simulate to initiate 'request-reply' before subscription
    IExecutionSemaphore mutex = Jobs.newExecutionSemaphore(1);

    // 1. Initiate 'request-reply' communication
    IFuture<String> requestFuture = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        return s_mom.request(topic, "hello world");
      }
    }, Jobs.newInput()
        .withExecutionHint(m_testJobExecutionHint)
        .withExceptionHandling(null, false)
        .withExecutionSemaphore(mutex));

    // 2. Subscribe for reply
    IFuture<Void> replyFuture = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        // Subscribe replier
        m_disposables.add(s_mom.reply(topic, new IRequestListener<String, String>() {

          @Override
          public String onRequest(IMessage<String> request) {
            return request.getTransferObject().toUpperCase();
          }
        }, null));
      }
    }, Jobs.newInput()
        .withExecutionHint(m_testJobExecutionHint)
        .withExecutionSemaphore(mutex));

    replyFuture.awaitDone();
    // Verify
    try {
      requestFuture.awaitDoneAndGet(200, TimeUnit.MILLISECONDS);
      fail();
    }
    catch (TimedOutException e) {
      assertTrue(true);
    }
  }

  @Test(timeout = 15_000)
  public void testQueueRequestReplyCancellation() throws InterruptedException {
    final IDestination queue = s_mom.newQueue("test/mom");
    testRequestReplyCancellationInternal(queue);
  }

  @Test(timeout = 15_000)
  public void testTopicRequestReplyCancellation() throws InterruptedException {
    final IDestination queue = s_mom.newTopic("test/mom");
    testRequestReplyCancellationInternal(queue);
  }

  private void testRequestReplyCancellationInternal(final IDestination destination) throws InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(2);

    final AtomicBoolean requestorInterrupted = new AtomicBoolean();
    final AtomicBoolean replierInterrupted = new AtomicBoolean();

    final IExecutionSemaphore mutex = Jobs.newExecutionSemaphore(1);

    // Subscribe for the destination
    m_disposables.add(s_mom.reply(destination, new IRequestListener<String, String>() {

      @Override
      public String onRequest(IMessage<String> request) {
        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          replierInterrupted.set(true);
        }
        finally {
          verifyLatch.countDown();
        }
        return request.getTransferObject().toUpperCase();
      }
    }, null));

    final String requestReplyJobId = UUID.randomUUID().toString();

    // Prepare cancellation job
    IFuture<Void> cancellationJob = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        // Wait until message processing started
        assertTrue(setupLatch.await());

        // Cancel the publishing thread
        // Do this as mutex owner to guarantee the 'request-reply' initiator returned form publishing
        Jobs.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
                .andMatchExecutionHint(requestReplyJobId)
                .toFilter(), true);
          }
        }, Jobs.newInput()
            .withExecutionSemaphore(mutex));

      }
    }, Jobs.newInput()
        .withName("canceller")
        .withExecutionHint(m_testJobExecutionHint));

    // Initiate 'request-reply' communication
    final FinalValue<String> testee = new FinalValue<>();
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          String result = s_mom.request(destination, "hello world");
          testee.set(result);
        }
        catch (ThreadInterruptedException e) {
          requestorInterrupted.set(true);
        }
        finally {
          verifyLatch.countDown();
        }
      }
    }, Jobs.newInput()
        .withName("initiator")
        .withExecutionHint(requestReplyJobId)
        .withExecutionHint(m_testJobExecutionHint)
        .withExecutionSemaphore(mutex));

    // Wait until cancelled requestor thread
    cancellationJob.awaitDoneAndGet();

    assertTrue(verifyLatch.await());

    // Verify
    assertTrue(requestorInterrupted.get());
    assertTrue(replierInterrupted.get());
    assertFalse(testee.isSet());
  }

  @Test(timeout = 15_000)
  public void testQueueRequestReplyTimeout() throws InterruptedException {
    final IDestination queue = s_mom.newTopic("test/mom");
    testRequestReplyTimeoutInternal(queue);
  }

  @Test(timeout = 15_000)
  public void testTopicRequestReplyTimeout() throws InterruptedException {
    final IDestination topic = s_mom.newTopic("test/mom");
    testRequestReplyTimeoutInternal(topic);
  }

  private void testRequestReplyTimeoutInternal(final IDestination destination) throws InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(2);

    final AtomicBoolean requestorTimedOut = new AtomicBoolean();
    final AtomicBoolean replierInterrupted = new AtomicBoolean();

    // Subscribe for the destination
    m_disposables.add(s_mom.reply(destination, new IRequestListener<String, String>() {

      @Override
      public String onRequest(IMessage<String> request) {
        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          replierInterrupted.set(true);
        }
        finally {
          verifyLatch.countDown();
        }
        return request.getTransferObject().toUpperCase();
      }
    }, null));

    // Initiate 'request-reply' communication
    try {
      s_mom.request(destination, "hello world", s_mom.newPublishInput()
          .withRequestReplyTimeout(1, TimeUnit.MILLISECONDS));
    }
    catch (TimedOutException e) {
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

  private void testPublishSubscribeCorrelationIdInternal(final IDestination destination) throws InterruptedException {
    final Capturer<String> cid = new Capturer<>();

    m_disposables.add(s_mom.subscribe(destination, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        cid.set(CorrelationId.CURRENT.get());
      }
    }, RunContexts.copyCurrent()
        .withCorrelationId("cid:xyz")));

    RunContexts.copyCurrent()
        .withCorrelationId("cid:abc")
        .run(new IRunnable() {

          @Override
          public void run() throws Exception {
            s_mom.publish(destination, "hello world");
          }
        });

    assertEquals("cid:abc", cid.get());
  }

  @Test(timeout = 15_000)
  public void testTopicRequestReplyJsonObjectMarshaller() throws InterruptedException {
    IDestination queue = s_mom.newQueue("test/mom");
    testRequestReplyJsonObjectMarshallerInternal(queue);
  }

  @Test(timeout = 15_000)
  public void testQueueRequestReplyJsonObjectMarshaller() throws InterruptedException {
    IDestination topic = s_mom.newTopic("test/mom");
    testRequestReplyJsonObjectMarshallerInternal(topic);
  }

  private void testRequestReplyJsonObjectMarshallerInternal(IDestination destination) throws InterruptedException {
    m_disposables.add(s_mom.registerMarshaller(destination, BEANS.get(JsonMarshaller.class)));

    Person person = new Person();
    person.setLastname("smith");
    person.setFirstname("anna");

    // Subscribe for the destination
    m_disposables.add(s_mom.reply(destination, new IRequestListener<Person, Person>() {

      @Override
      public Person onRequest(IMessage<Person> request) {
        Person result = new Person();
        result.setLastname(request.getTransferObject().getLastname().toUpperCase());
        result.setFirstname(request.getTransferObject().getFirstname().toUpperCase());
        return result;
      }
    }, null));

    // Initiate 'request-reply' communication
    Person testee = s_mom.request(destination, person);

    // Verify
    assertEquals("ANNA", testee.getFirstname());
    assertEquals("SMITH", testee.getLastname());
  }

  @Test(timeout = 15_000)
  public void testRequestReply_ObjectMarshaller_Exception() {
    // Unregister JUnit exception handler
    BEANS.getBeanManager().unregisterBean(BEANS.getBeanManager().getBean(JUnitExceptionHandler.class));

    IDestination destination = s_mom.newQueue("test/mom");

    final RuntimeException runtimeException = new SomethingWrongException("expected-expected-junit-exception");

    // Subscribe for the destination
    m_disposables.add(s_mom.reply(destination, new IRequestListener<Void, String>() {

      @Override
      public String onRequest(IMessage<Void> request) {
        throw runtimeException;
      }
    }, null));

    // Initiate 'request-reply' communication
    try {
      s_mom.request(destination, null);
      fail("SomethingWrongException expected");
    }
    catch (SomethingWrongException e) {
      assertEquals(0, e.getStackTrace().length); // security
    }
  }

  @Test(timeout = 15_000)
  public void testRequestReply_JsonMarshaller_Exception1() {
    // Unregister JUnit exception handler
    BEANS.getBeanManager().unregisterBean(BEANS.getBeanManager().getBean(JUnitExceptionHandler.class));

    IDestination destination = s_mom.newQueue("test/mom");
    s_mom.registerMarshaller(destination, BEANS.get(JsonMarshaller.class));

    final RuntimeException runtimeException = new SomethingWrongException("expected-expected-junit-exception");

    // Subscribe for the destination
    m_disposables.add(s_mom.reply(destination, new IRequestListener<Void, String>() {

      @Override
      public String onRequest(IMessage<Void> request) {
        throw runtimeException;
      }
    }, null));

    // Initiate 'request-reply' communication
    try {
      s_mom.request(destination, null);
      fail("SomethingWrongException expected");
    }
    catch (SomethingWrongException e) {
      assertEquals(0, e.getStackTrace().length); // security
    }
  }

  @Test(timeout = 15_000)
  public void testRequestReply_JsonMarshaller_Exception2() {
    // Unregister JUnit exception handler
    BEANS.getBeanManager().unregisterBean(BEANS.getBeanManager().getBean(JUnitExceptionHandler.class));

    IDestination destination = s_mom.newQueue("test/mom");
    s_mom.registerMarshaller(destination, BEANS.get(JsonMarshaller.class));

    final RuntimeException runtimeException = new VetoException();

    // Subscribe for the destination
    m_disposables.add(s_mom.reply(destination, new IRequestListener<Void, String>() {

      @Override
      public String onRequest(IMessage<Void> request) {
        throw runtimeException;
      }
    }, null));

    // Initiate 'request-reply' communication
    try {
      s_mom.request(destination, null);
      fail("VetoException expected");
    }
    catch (VetoException e) {
      assertEquals(0, e.getStackTrace().length); // security
    }
  }

  @Test(timeout = 15_000)
  public void testRequestReply_StringMarshaller_Exception() {
    // Unregister JUnit exception handler
    BEANS.getBeanManager().unregisterBean(BEANS.getBeanManager().getBean(JUnitExceptionHandler.class));

    IDestination destination = s_mom.newQueue("test/mom");
    s_mom.registerMarshaller(destination, BEANS.get(TextMarshaller.class));

    final RuntimeException runtimeException = new SomethingWrongException("expected-expected-junit-exception");

    // Subscribe for the destination
    m_disposables.add(s_mom.reply(destination, new IRequestListener<Void, String>() {

      @Override
      public String onRequest(IMessage<Void> request) {
        throw runtimeException;
      }
    }, null));

    // Initiate 'request-reply' communication
    try {
      s_mom.request(destination, null);
      fail("ProcessingException expected");
    }
    catch (ProcessingException e) {
      assertTrue(e.getContextInfos().isEmpty()); // security
      assertFalse(e.getDisplayMessage().contains("expected-junit-exception"));
    }
  }

  private class Capturer<TYPE> {

    private volatile TYPE m_message;
    private final CountDownLatch m_latch = new CountDownLatch(1);

    public void set(TYPE message) {
      m_message = message;
      m_latch.countDown();
    }

    public TYPE get() throws InterruptedException {
      return get(15, TimeUnit.SECONDS);
    }

    public TYPE get(long timeout, TimeUnit unit) throws InterruptedException {
      if (!m_latch.await(timeout, unit)) {
        throw new TimedOutException("timout elapsed while waiting for message");
      }
      return m_message;
    }
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
  }

  @Test
  @Times(1)
  public void testPublishJsonDataSecure() throws InterruptedException {
    final Capturer<Person> capturer = new Capturer<>();

    IDestination queue = s_mom.newQueue("test/mom");
    m_disposables.add(s_mom.registerMarshaller(queue, BEANS.get(JsonMarshaller.class)));

    Person person = new Person();
    person.setFirstname("anna");
    person.setLastname("smith");

    s_mom.publish(queue, person);
    m_disposables.add(s_mom.subscribe(queue, new IMessageListener<Person>() {

      @Override
      public void onMessage(IMessage<Person> message) {
        capturer.set(message.getTransferObject());
      }
    }, null));

    // Verify
    Person testee = capturer.get();
    assertEquals("smith", testee.getLastname());
    assertEquals("anna", testee.getFirstname());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCurrentMessagePubSub() throws InterruptedException {
    CorrelationId.CURRENT.set("cid_test");
    try {
      final Capturer<IMessage<?>> capturer = new Capturer<>();

      Person person = new Person();
      person.setLastname("smith");
      person.setFirstname("anna");

      IDestination queue = s_mom.newQueue("test/mom");
      m_disposables.add(s_mom.registerMarshaller(queue, BEANS.get(ObjectMarshaller.class)));

      s_mom.publish(queue, person);
      m_disposables.add(s_mom.subscribe(queue, new IMessageListener<Person>() {

        @Override
        public void onMessage(IMessage<Person> message) {
          capturer.set(IMessage.CURRENT.get());
        }
      }, null));

      // Verify
      IMessage<Person> testee = (IMessage<Person>) capturer.get();
      assertNotNull(testee);
      assertEquals("smith", testee.getTransferObject().getLastname());
      assertEquals("anna", testee.getTransferObject().getFirstname());
      assertThat(testee.getAdapter(Message.class), instanceOf(Message.class));
    }
    finally {
      CorrelationId.CURRENT.remove();
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCurrentMessageRequestReply() throws InterruptedException, JMSException {
    CorrelationId.CURRENT.set("cid_test");
    try {

      final Capturer<IMessage<?>> capturer = new Capturer<>();

      Person person = new Person();
      person.setLastname("smith");
      person.setFirstname("anna");

      IDestination queue = s_mom.newQueue("test/mom");
      m_disposables.add(s_mom.registerMarshaller(queue, BEANS.get(ObjectMarshaller.class)));

      m_disposables.add(s_mom.reply(queue, new IRequestListener<Person, Void>() {

        @Override
        public Void onRequest(IMessage<Person> request) {
          capturer.set(IMessage.CURRENT.get());
          return null;
        }
      }, null));
      s_mom.request(queue, person);

      // Verify
      IMessage<Person> testee = (IMessage<Person>) capturer.get();
      assertNotNull(testee);
      assertEquals("smith", testee.getTransferObject().getLastname());
      assertEquals("anna", testee.getTransferObject().getFirstname());
      assertThat(testee.getAdapter(Message.class), instanceOf(Message.class));
      assertEquals("cid_test", testee.getAdapter(Message.class).getJMSCorrelationID());
    }
    finally {
      CorrelationId.CURRENT.remove();
    }
  }

  @Test
  public void testPublishTransactional() throws InterruptedException {
    final Capturer<Person> capturer = new Capturer<>();

    Person person = new Person();
    person.setLastname("smith");
    person.setFirstname("anna");

    ITransaction tx = BEANS.get(ITransaction.class);
    ITransaction.CURRENT.set(tx);

    IDestination queue = s_mom.newQueue("test/mom");
    m_disposables.add(s_mom.registerMarshaller(queue, BEANS.get(ObjectMarshaller.class)));

    s_mom.publish(queue, person, s_mom.newPublishInput().withTransactional(true));
    m_disposables.add(s_mom.subscribe(queue, new IMessageListener<Person>() {

      @Override
      public void onMessage(IMessage<Person> message) {
        capturer.set(message.getTransferObject());
      }
    }, null));

    try {
      capturer.get(2, TimeUnit.SECONDS);
      fail();
    }
    catch (TimedOutException e) {
      // expected
    }

    tx.commitPhase1();
    tx.commitPhase2();

    try {
      capturer.get(5, TimeUnit.SECONDS);
    }
    catch (TimedOutException e) {
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

  // TODO dwi
  public void testSubscribeTransactional() throws InterruptedException {
    IDestination queue = s_mom.newQueue("test/mom");
    m_disposables.add(s_mom.registerMarshaller(queue, BEANS.get(ObjectMarshaller.class)));

    s_mom.publish(queue, "message-1", s_mom.newPublishInput());

    final AtomicInteger messageCounter = new AtomicInteger();

    // 1. Receive message, but reject it (rollback)
    final BlockingCountDownLatch message1Latch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch message2Latch = new BlockingCountDownLatch(1);

    IMessageListener<String> listener = new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
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
      }
    };

    // Register transactional subscriber
    m_disposables.add(s_mom.subscribe(queue, listener, null, IMom.ACKNOWLEDGE_TRANSACTED));
    assertTrue("message expected to be rejected 3 times", message1Latch.await());

    // Publish a next message
    s_mom.publish(queue, "message-2", s_mom.newPublishInput());

    // Check that the message was received
    assertTrue("message expected to be received", message2Latch.await());

    Thread.sleep(1000); // Wait some time to verify that 'message-1' is no longer received.
    assertEquals(messageCounter.get(), 4);
  }

  private Object testPublishAndConsumeInternal(Object transferObject, IMarshaller marshaller, IEncrypter encrypter) throws InterruptedException {
    final Capturer<Object> capturer = new Capturer<>();

    IDestination queue = s_mom.newQueue("test/mom");

    List<IDisposable> disposables = new ArrayList<>();
    if (encrypter != null) {
      disposables.add(s_mom.registerEncrypter(queue, encrypter));
    }
    if (marshaller != null) {
      disposables.add(s_mom.registerMarshaller(queue, marshaller));
    }

    s_mom.publish(queue, transferObject);
    disposables.add(s_mom.subscribe(queue, new IMessageListener<Object>() {

      @Override
      public void onMessage(IMessage<Object> msg) {
        capturer.set(msg.getTransferObject());
      }
    }, null));

    // Verify
    try {
      return capturer.get();
    }
    finally {
      dispose(disposables);
    }
  }

  private Object testRequestReplyInternal(Object request, IMarshaller marshaller, IEncrypter encrypter) throws InterruptedException {
    IDestination queue = s_mom.newQueue("test/mom");

    List<IDisposable> disposables = new ArrayList<>();
    if (encrypter != null) {
      disposables.add(s_mom.registerEncrypter(queue, encrypter));
    }
    if (marshaller != null) {
      disposables.add(s_mom.registerMarshaller(queue, marshaller));
    }

    try {
      disposables.add(s_mom.reply(queue, new IRequestListener<Object, Object>() {

        @Override
        public Object onRequest(IMessage<Object> req) {
          return req.getTransferObject();
        }
      }, null));

      return s_mom.request(queue, request);
    }
    finally {
      dispose(disposables);
    }
  }

  private void dispose(Collection<IDisposable> disposables) {
    for (IDisposable disposable : disposables) {
      disposable.dispose();
    }
  }

  @Ignore
  private class NullEncrypterProperty extends EncrypterProperty {

    @Override
    protected Class<? extends IEncrypter> parse(String value) {
      return null;
    }
  }

  @Ignore
  private class JUnitPbePasswordProperty extends PbePasswordProperty {

    @Override
    protected char[] getDefaultValue() {
      return "secret".toCharArray();
    }
  }

  @Ignore
  private class JUnitPbeSaltProperty extends PbeSaltProperty {

    @Override
    protected byte[] getDefaultValue() {
      return "fs2wDfIEviPAfGNj1m061g==".getBytes(StandardCharsets.UTF_8);
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
}
