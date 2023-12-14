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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

import org.apache.activemq.broker.BrokerRegistry;
import org.apache.activemq.broker.BrokerService;
import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.platform.AnnotationFactory;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.BeanUtility;
import org.eclipse.scout.rt.platform.util.IDisposable;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.parameterized.IScoutTestParameter;
import org.eclipse.scout.rt.testing.platform.runner.parameterized.ParameterizedPlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(ParameterizedPlatformTestRunner.class)
public abstract class AbstractJmsMomTest {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractJmsMomTest.class);

  @ClassRule
  public static final ArtemisJmsBrokerTestRule ARTEMIS_RULE = new ArtemisJmsBrokerTestRule();

  @Parameters
  public static List<IScoutTestParameter> getParameters() {
    return FixtureParameters.createParameters();
  }

  @Rule
  public final TestName m_testName = new TestName();

  protected final AbstractJmsMomTestParameter m_testParameter;
  protected final List<IBean<?>> m_beans = new ArrayList<>();
  protected final List<IDisposable> m_disposables = new ArrayList<>();
  protected FixtureMom m_mom;
  protected long m_testStarted;

  protected AbstractJmsMomTest(AbstractJmsMomTestParameter parameter) {
    m_testParameter = parameter;
  }

  @Before
  public void before() {
    m_beans.add(BEANS.getBeanManager().registerBean(new BeanMetaData(FixtureJobInput.class)));

    LOG.info("---------------------------------------------------");
    LOG.info("<{}>", m_testName.getMethodName());
    m_testStarted = System.nanoTime();
  }

  @After
  public void after() {
    dispose(m_disposables);
    try {
      cancelJobs();
    }
    finally {
      // remember used used brokers here because they might be removed after mom.destroy()
      // but it is necessary to wait for broker stop completion later because stopping is executed asynchronously.
      List<BrokerService> brokers = new ArrayList<>(BrokerRegistry.getInstance().getBrokers().values());
      if (m_mom != null) {
        m_mom.destroy();
        m_mom = null;
      }
      BeanTestingHelper.get().unregisterBeans(m_beans);
      m_beans.clear();

      // ensure activeMQ is stopped and wait for it
      brokers.stream().filter(Objects::nonNull).forEach(brokerService -> {
        try {
          brokerService.stop();
          brokerService.waitUntilStopped();
        }
        catch (Exception e) {
          throw new ProcessingException("Unable to stop broker {}.", brokerService, e);
        }
      });

      LOG.info("Finished test in {} ms", StringUtility.formatNanos(System.nanoTime() - m_testStarted));
      LOG.info("</{}>", m_testName.getMethodName());
    }
  }

  protected void installMom() {
    installMom(FixtureMom.class);
  }

  @SuppressWarnings("unchecked")
  protected <MOM extends FixtureMom> MOM installMom(Class<MOM> transportType) {
    assertNull("installMom was already called in this test", m_mom);
    IJmsMessageHandler messageHandler = mock(IJmsMessageHandler.class);
    LOG.info("Registering IJmsMessageHandler mock '{}'.", messageHandler);
    m_beans.add(BeanTestingHelper.get().registerBean(new BeanMetaData(IJmsMessageHandler.class, messageHandler).withAnnotation(AnnotationFactory.createApplicationScoped())));
    if (messageHandler != BEANS.get(IJmsMessageHandler.class)) {
      StringBuilder registeredBeans = new StringBuilder();
      for (IBean<IJmsMessageHandler> bean : BEANS.getBeanManager().getBeans(IJmsMessageHandler.class)) {
        registeredBeans.append("\n - instance=").append(bean.getInstance()).append(", beanClass=").append(bean.getBeanClazz());
        Order orderAnnotation = bean.getBeanAnnotation(Order.class);
        if (orderAnnotation != null) {
          registeredBeans.append(", order=").append(orderAnnotation.value());
        }
        ApplicationScoped applicationScopedAnnotation = bean.getBeanAnnotation(ApplicationScoped.class);
        if (applicationScopedAnnotation != null) {
          registeredBeans.append(", ApplicationScoped");
        }

      }
      fail("IJmsMessageHandler mock registration unsuccessful. Registered mock is not returned from BEANS.get. Available beans:" + registeredBeans);
    }

    FixtureMom transport = BeanUtility.createInstance(transportType, m_testParameter);
    m_beans.add(BEANS.getBeanManager().registerBean(new BeanMetaData(transportType, transport)));
    m_mom = BEANS.get(transportType);
    return (MOM) m_mom;
  }

  protected void dispose(Collection<IDisposable> disposables) {
    if (disposables.isEmpty()) {
      return;
    }

    LOG.info("Disposing {} objects: {}", disposables.size(), disposables);
    for (IDisposable disposable : disposables) {
      try {
        disposable.dispose();
      }
      catch (Throwable t) {
        LOG.info("Unable to dispose '{}'.", disposable, t);
      }
    }
    disposables.clear();
  }

  protected void cancelJobs() {
    // Cancel regular jobs
    Predicate<IFuture<?>> testJobsFilter = Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(FixtureJobInput.EXPLICIT_HINT)
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
      catch (ThreadInterruptedError e) {
        LOG.warn("Interrupted while waiting for Mom Jobs to complete.", e);
      }
    }
    // Cancel jms subscriber jobs
    Predicate<IFuture<?>> jmsJobsFilter = Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(FixtureJobInput.IMPLICIT_HINT)
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
      catch (ThreadInterruptedError e) {
        LOG.warn("Interrupted while waiting for Mom Jobs to complete.", e);
      }
    }
  }

  public static <DTO> void verifyMessageHandlerHandleOutgoingCalled(IDestination<DTO> expectedDestination, IMarshaller marshaller, DTO expectedContent) {
    ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
    verify(BEANS.get(IJmsMessageHandler.class)).handleOutgoing(eq(expectedDestination), messageCaptor.capture(), any(marshaller.getClass()));
    verifyJmsMessage(messageCaptor.getValue(), marshaller, expectedContent);
  }

  @SafeVarargs
  public static <DTO> void verifyMessageHandlerHandleIncomingCalled(IDestination<DTO> expectedDestination, IMarshaller marshaller, DTO... expectedContents) {
    ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
    verify(BEANS.get(IJmsMessageHandler.class), times(expectedContents.length)).handleIncoming(eq(expectedDestination), messageCaptor.capture(), any(marshaller.getClass()));
    for (int i = 0; i < expectedContents.length; i++) {
      verifyJmsMessage(messageCaptor.getAllValues().get(i), marshaller, expectedContents[i]);
    }
  }

  public static <DTO> void verifyJmsMessage(Message message, IMarshaller marshaller, DTO expectedContent) {
    try {
      if (message instanceof BytesMessage) {
        // ensure that the stream of bytes is repositioned to the beginning before start reading the JMS message
        ((BytesMessage) message).reset();
      }
      JmsMessageReader<DTO> reader = JmsMessageReader.newInstance(message, marshaller);
      DTO readTransferObject = reader.readTransferObject();
      if (expectedContent != null && expectedContent.getClass() == byte[].class) {
        assertArrayEquals((byte[]) expectedContent, (byte[]) readTransferObject);
      }
      else {
        assertEquals(expectedContent, readTransferObject);
      }
    }
    catch (JMSException e) {
      throw new ProcessingException("Exception while reading the message", e);
    }
  }

  public static <DTO> void verifyRequestReplyMessageHandler(IDestination<DTO> expectedDestination, IMarshaller marshaller, DTO expectedRequest, DTO expectedReply) {
    verify(BEANS.get(IJmsMessageHandler.class), times(2)).handleOutgoing(any(), any(), any());
    verifyMessageHandlerHandleOutgoingCalled(expectedDestination, marshaller, expectedRequest);
    verifyMessageHandlerHandleOutgoingCalled(null, marshaller, expectedReply); // "reply" message is sent only with JMS destination (but without a Scout MOM destination)
    verify(BEANS.get(IJmsMessageHandler.class), times(2)).handleIncoming(eq(expectedDestination), any(), any());
    verifyMessageHandlerHandleIncomingCalled(expectedDestination, marshaller, expectedRequest, expectedReply);
  }
}
