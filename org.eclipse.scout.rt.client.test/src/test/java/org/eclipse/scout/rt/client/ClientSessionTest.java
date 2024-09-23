/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client;

import static org.junit.Assert.*;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.client.ClientConfigProperties.JobCompletionDelayOnSessionShutdown;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.Coordinates;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.DefaultPlatform;
import org.eclipse.scout.rt.platform.IBeanInstanceProducer;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * This test must be executed by a bare JUnit runner.
 * Reason: The PlatformTestRunner and its subclasses keep track of every job scheduled during test execution and verify that they are completed. The list of scheduled jobs
 *         are referencing a JobInput which in turn references a RunContext and a session. The tests in this class will fail because they assert that the sessions are
 *         not referenced by any other object and therefore garbage collected.
 */
public class ClientSessionTest {
  private static IPlatform oldPlatform;

  @BeforeClass
  public static void beforeClass() {
    oldPlatform = Platform.peek();
  }

  @AfterClass
  public static void afterClass() {
    Platform.set(oldPlatform);
  }

  private IClientSession session;
  private IMessageBox messageBox;

  @Before
  public void before() {
    Platform.set(new DefaultPlatform());
    Platform.get().start();
    Platform.get().awaitPlatformStarted();
  }

  @After
  public void after() {
    Platform.get().stop();
    messageBox = null;
    session = null;
  }

  @Test
  public void testStop() {
    BeanTestingHelper.get().registerBean(new BeanMetaData(TestEnvironmentClientSession.class));
    session = BEANS.get(ClientSessionProvider.class).provide(ClientRunContexts.empty().withUserAgent(UserAgents.createDefault()));

    //run a job
    String jobResult = ModelJobs.schedule(() -> "OK", ModelJobs
        .newInput(ClientRunContexts
            .empty()
            .withSession(session, true)))
        .awaitDoneAndGet();

    assertEquals("OK", jobResult);

    session.stop();
  }

  @Test
  public void testStopWithClosedMessageBox() {
    BeanTestingHelper.get().registerBean(new BeanMetaData(TestEnvironmentClientSession.class));
    session = BEANS.get(ClientSessionProvider.class).provide(ClientRunContexts.empty().withUserAgent(UserAgents.createDefault()));

    //show a messagebox
    IFuture<Integer> f = ModelJobs.schedule(() -> {
      messageBox = MessageBoxes.createYesNo();
      return messageBox.show();
    }, ModelJobs
        .newInput(ClientRunContexts
            .empty()
            .withSession(session, true)));

    //confirm the messagebox
    ModelJobs.schedule(() -> messageBox.getUIFacade().setResultFromUI(IMessageBox.YES_OPTION), ModelJobs
        .newInput(ClientRunContexts
            .empty()
            .withSession(session, true)));

    int messageBoxResult = f.awaitDoneAndGet().intValue();

    assertEquals(IMessageBox.YES_OPTION, messageBoxResult);
    assertTrue(f.isDone());

    //close from ui
    ModelJobs.schedule(() -> session.getDesktop().getUIFacade().closeFromUI(true), ModelJobs
        .newInput(ClientRunContexts
            .empty()
            .withSession(session, true)))
        .awaitDone();
  }

  @Test
  public void testStopWithBlockingMessageBox() {
    BeanTestingHelper.get().registerBean(new BeanMetaData(TestEnvironmentClientSession.class));
    BeanTestingHelper.get().registerBean(
        new BeanMetaData(JobCompletionDelayOnSessionShutdown.class).withProducer((IBeanInstanceProducer<JobCompletionDelayOnSessionShutdown>) bean -> new JobCompletionDelayOnSessionShutdown() {
          @Override
          public Long getDefaultValue() {
            return 1000L;
          }
        }));

    session = BEANS.get(ClientSessionProvider.class).provide(ClientRunContexts.empty().withUserAgent(UserAgents.createDefault()));

    //show a messagebox
    IFuture<Integer> f = ModelJobs.schedule(() -> {
      messageBox = MessageBoxes.createYesNo();
      return messageBox.show();
    }, ModelJobs
        .newInput(ClientRunContexts
            .empty()
            .withSession(session, true)));

    try {
      f.awaitDoneAndGet(1, TimeUnit.SECONDS);
      fail("must throw a " + TimedOutError.class.getName());
    }
    catch (TimedOutError e) {
      //nop
    }
    assertFalse(f.isDone());

    //close from ui
    ModelJobs.schedule(() -> session.getDesktop().getUIFacade().closeFromUI(true), ModelJobs
        .newInput(ClientRunContexts
            .empty()
            .withSession(session, true)))
        .awaitDone();

    assertEquals(JobState.DONE, f.getState());
  }

  @Test
  public void testStopWithBlockingClientCallback() {
    BeanTestingHelper.get().registerBean(new BeanMetaData(TestEnvironmentClientSession.class));
    BeanTestingHelper.get().registerBean(
        new BeanMetaData(JobCompletionDelayOnSessionShutdown.class).withProducer((IBeanInstanceProducer<JobCompletionDelayOnSessionShutdown>) bean -> new JobCompletionDelayOnSessionShutdown() {
          @Override
          public Long getDefaultValue() {
            return 1000L;
          }
        }));

    session = BEANS.get(ClientSessionProvider.class).provide(ClientRunContexts.empty().withUserAgent(UserAgents.createDefault()));

    //request a geo location
    Future<Coordinates> geo = ModelJobs.schedule(() -> {
          IDesktop desktop = IDesktop.CURRENT.get();
          desktop.getUIFacade().readyFromUI();
          return desktop.requestGeolocation();
        }, ModelJobs.newInput(ClientRunContexts.empty().withSession(session, true)))
        .awaitDoneAndGet();

    assertFalse(geo.isDone());

    //close from ui
    ModelJobs.schedule(() -> session.getDesktop().getUIFacade().closeFromUI(true), ModelJobs
        .newInput(ClientRunContexts
            .empty()
            .withSession(session, true)))
        .awaitDone();

    assertTrue(geo.isCancelled());
  }
}
