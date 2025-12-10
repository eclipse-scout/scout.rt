/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.ClientConfigProperties.JobCompletionDelayOnSessionShutdown;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.services.common.exceptionhandler.ClientExceptionHandler;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanInstanceProducer;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.shared.job.filter.future.SessionFutureFilter;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithNewPlatform;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.SessionStoreHousekeepingDelayProperty;
import org.eclipse.scout.rt.ui.html.json.JsonStartupRequest;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.eclipse.scout.rt.ui.html.json.testing.TestEnvironmentUiSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWithNewPlatform()
@RunWith(PlatformTestRunner.class)
public class ClientSessionWithBlockingConditionInterruptionTest {
  private List<IBean<?>> m_beans;

  private SessionBehaviour m_sessionBehaviour;
  private DesktopBehaviour m_desktopBehaviour;
  private List<String> m_protocol = Collections.synchronizedList(new ArrayList<>());

  @Before
  public void before() {
    m_protocol.clear();
    m_beans = BeanTestingHelper.get().registerBeans(
        new BeanMetaData(JobCompletionDelayOnSessionShutdown.class).withInitialInstance(new JobCompletionDelayOnSessionShutdown() {
          @Override
          public Long getDefaultValue() {
            return 1L;
          }
        }),

        new BeanMetaData(SessionStoreHousekeepingDelayProperty.class).withInitialInstance(new SessionStoreHousekeepingDelayProperty() {
          @Override
          public Integer getDefaultValue() {
            return 1;
          }
        }),

        new BeanMetaData(FixtureUiSession.class).withProducer((IBeanInstanceProducer<FixtureUiSession>) bean -> new FixtureUiSession()),

        new BeanMetaData(FixtureClientSession.class).withProducer((IBeanInstanceProducer<FixtureClientSession>) bean -> new FixtureClientSession()));
  }

  @After
  public void after() {
    BeanTestingHelper.get().unregisterBeans(m_beans);
    m_protocol.clear();
  }

  private void writeToProtocol(String line) {
    System.out.println("protocol: " + line);
    m_protocol.add(line);
  }

  @Test
  public void testDesktopWithMessageBoxInGuiAttached() {
    m_sessionBehaviour = SessionBehaviour.DO_NOTHING;
    m_desktopBehaviour = DesktopBehaviour.OPEN_MESSAGEBOX_IN_GUI_ATTACHED;

    IUiSession uiSession = JsonTestUtility.createAndInitializeUiSession();
    final FixtureClientSession session = (FixtureClientSession) uiSession.getClientSession();

    JobTestUtil.waitForCondition(() -> session.getDesktop() != null && session.getDesktop().desktopFuture != null && session.getDesktop().desktopFuture.getState() == JobState.WAITING_FOR_BLOCKING_CONDITION);
    writeToProtocol("MessageBox is open");

    writeToProtocol("Session stopping");
    ModelJobs.schedule(() -> session.getDesktop().getUIFacade().closeFromUI(true), ModelJobs.newInput(ClientRunContexts.empty().withSession(session, true)))
        .awaitDone();

    Jobs.getJobManager().awaitFinished(ModelJobs.newFutureFilterBuilder()
        .andMatch(new SessionFutureFilter(session))
        .toFilter(), 30, TimeUnit.SECONDS);

    writeToProtocol("All session jobs terminated");

    List<String> expectedProtocol = Arrays.asList(
        "Before MessageBox",
        "MessageBox is open",
        "Session stopping",
        "Interrupted MessageBox",
        "All session jobs terminated");
    assertEquals(expectedProtocol, m_protocol);
  }

  @Test
  public void testDesktopWithBlockingForm() {
    m_sessionBehaviour = SessionBehaviour.DO_NOTHING;
    m_desktopBehaviour = DesktopBehaviour.DO_NOTHING;
    IUiSession uiSession = JsonTestUtility.createAndInitializeUiSession();
    final FixtureClientSession session = (FixtureClientSession) uiSession.getClientSession();

    final IFuture<?> callingFuture = ModelJobs.schedule(() -> {
      FixtureForm f = new FixtureForm();
      f.start();
      try {
        writeToProtocol("Before Form.waitFor");
        f.waitFor();
        writeToProtocol("After Form.waitFor, interrupted=" + Thread.currentThread().isInterrupted() + ", futureCancelled=" + IFuture.CURRENT.get().isCancelled());
      }
      catch (ThreadInterruptedError e) {
        writeToProtocol("Interrupted Form");
        throw e;
      }
    }, ModelJobs
        .newInput(ClientRunContexts.empty().withSession(session, true))
        .withExceptionHandling(new ClientExceptionHandler(), false));

    JobTestUtil.waitForCondition(() -> callingFuture.getState() == JobState.WAITING_FOR_BLOCKING_CONDITION);
    writeToProtocol("Form is open");

    writeToProtocol("Session stopping");
    ModelJobs.schedule(() -> session.getDesktop().getUIFacade().closeFromUI(true), ModelJobs.newInput(ClientRunContexts.empty().withSession(session, true)))
        .awaitDone();

    Jobs.getJobManager().awaitFinished(ModelJobs.newFutureFilterBuilder()
        .andMatch(new SessionFutureFilter(session))
        .toFilter(), 30, TimeUnit.SECONDS);

    writeToProtocol("All session jobs terminated");

    List<String> expectedProtocol = Arrays.asList(
        "Form.execLoad",
        "Before Form.waitFor",
        "Form is open",
        "Session stopping",
        "Form.execFinally",
        "Form.dispose",
        "Interrupted Form",
        "All session jobs terminated");
    assertEquals(expectedProtocol, m_protocol);
  }

  @Test
  public void testSessionWithMessageBoxInLoad() {
    m_sessionBehaviour = SessionBehaviour.OPEN_MESSAGEBOX_IN_LOAD;
    m_desktopBehaviour = DesktopBehaviour.DO_NOTHING;
    IUiSession uiSession = JsonTestUtility.createAndInitializeUiSession();
    final FixtureClientSession session = (FixtureClientSession) uiSession.getClientSession();

    writeToProtocol("Session stopping");
    ModelJobs.schedule(() -> session.getDesktop().getUIFacade().closeFromUI(true), ModelJobs.newInput(ClientRunContexts.empty().withSession(session, true)))
        .awaitDone();

    Jobs.getJobManager().awaitFinished(ModelJobs.newFutureFilterBuilder()
        .andMatch(new SessionFutureFilter(session))
        .toFilter(), 30, TimeUnit.SECONDS);

    writeToProtocol("All session jobs terminated");

    List<String> expectedProtocol = Arrays.asList(
        "Before MessageBoxInLoad",
        "After MessageBoxInLoad " + IMessageBox.CANCEL_OPTION,
        "Session stopping",
        "All session jobs terminated");
    assertEquals(expectedProtocol, m_protocol);
  }

  @Test
  public void testSessionWithMessageBoxInStore() {
    m_sessionBehaviour = SessionBehaviour.OPEN_MESSAGEBOX_IN_STORE;
    m_desktopBehaviour = DesktopBehaviour.DO_NOTHING;
    IUiSession uiSession = JsonTestUtility.createAndInitializeUiSession();
    final FixtureClientSession session = (FixtureClientSession) uiSession.getClientSession();

    writeToProtocol("Session stopping");
    ModelJobs.schedule(() -> session.getDesktop().getUIFacade().closeFromUI(true), ModelJobs.newInput(ClientRunContexts.empty().withSession(session, true)))
        .awaitDone();

    Jobs.getJobManager().awaitFinished(ModelJobs.newFutureFilterBuilder()
        .andMatch(new SessionFutureFilter(session))
        .toFilter(), 30, TimeUnit.SECONDS);

    writeToProtocol("All session jobs terminated");

    List<String> expectedProtocol = Arrays.asList(
        "Session stopping",
        "Before MessageBoxInStore",
        "After MessageBoxInStore " + IMessageBox.CANCEL_OPTION,
        "All session jobs terminated");
    assertEquals(expectedProtocol, m_protocol);
  }

  @Test(expected = ProcessingException.class)
  public void testSessionWithBlockingFormInLoad() {
    m_sessionBehaviour = SessionBehaviour.OPEN_FORM_IN_LOAD;
    m_desktopBehaviour = DesktopBehaviour.DO_NOTHING;
    IUiSession uiSession = JsonTestUtility.createAndInitializeUiSession();
    uiSession.getClientSession();
  }

  @Test
  public void testSessionWithBlockingFormInStore() {
    m_sessionBehaviour = SessionBehaviour.OPEN_FORM_IN_STORE;
    m_desktopBehaviour = DesktopBehaviour.DO_NOTHING;
    IUiSession uiSession = JsonTestUtility.createAndInitializeUiSession();
    final FixtureClientSession session = (FixtureClientSession) uiSession.getClientSession();

    writeToProtocol("Session stopping");
    ModelJobs.schedule(() -> session.getDesktop().getUIFacade().closeFromUI(true), ModelJobs.newInput(ClientRunContexts.empty().withSession(session, true)))
        .awaitDone();

    Jobs.getJobManager().awaitFinished(ModelJobs.newFutureFilterBuilder()
        .andMatch(new SessionFutureFilter(session))
        .toFilter(), 30, TimeUnit.SECONDS);

    writeToProtocol("All session jobs terminated");

    List<String> expectedProtocol = Arrays.asList(
        "Session stopping",
        "Before Form.start",
        "Form.execLoad",
        "Form error ProcessingException There is no desktop or it is not open in the UI. [severity=ERROR]",
        "All session jobs terminated");
    assertEquals(expectedProtocol, m_protocol);
  }

  private enum SessionBehaviour {
    DO_NOTHING,
    OPEN_MESSAGEBOX_IN_LOAD,
    OPEN_MESSAGEBOX_IN_STORE,
    OPEN_FORM_IN_LOAD,
    OPEN_FORM_IN_STORE,
  }

  @IgnoreBean
  public class FixtureUiSession extends TestEnvironmentUiSession {
    @Override
    public void init(HttpServletRequest req, HttpServletResponse resp, JsonStartupRequest jsonStartupReq) {
      System.out.println("INIT");
      super.init(req, resp, jsonStartupReq);
    }
  }

  @IgnoreBean
  public class FixtureClientSession extends AbstractClientSession {

    public FixtureClientSession() {
      super(true);
    }

    @Override
    public FixtureDesktop getDesktop() {
      return (FixtureDesktop) super.getDesktop();
    }

    @Override
    protected void execLoadSession() {
      FixtureDesktop desktop = new FixtureDesktop();
      setDesktop(desktop);
      switch (m_sessionBehaviour) {
        case OPEN_MESSAGEBOX_IN_LOAD:
          writeToProtocol("Before MessageBoxInLoad");
          try {
            int result = MessageBoxes.createOk().show();
            writeToProtocol("After MessageBoxInLoad " + result);
          }
          catch (ThreadInterruptedError e) {
            writeToProtocol("Interrupted MessageBoxInLoad");
            throw e;
          }
          break;
        case OPEN_FORM_IN_LOAD:
          FixtureForm f = new FixtureForm();
          writeToProtocol("Before Form.start");
          f.start();
          try {
            writeToProtocol("Before Form.waitFor");
            f.waitFor();
            writeToProtocol("After Form.waitFor, interrupted=" + Thread.currentThread().isInterrupted() + ", futureCancelled=" + IFuture.CURRENT.get().isCancelled());
          }
          catch (ThreadInterruptedError e) {
            writeToProtocol("Interrupted Form");
            throw e;
          }
          break;
      }
    }

    @Override
    protected void execStoreSession() {
      switch (m_sessionBehaviour) {
        case OPEN_MESSAGEBOX_IN_STORE:
          writeToProtocol("Before MessageBoxInStore");
          try {
            int result = MessageBoxes.createOk().show();
            writeToProtocol("After MessageBoxInStore " + result);
          }
          catch (ThreadInterruptedError e) {
            writeToProtocol("Interrupted MessageBoxInStore");
            throw e;
          }
          break;
        case OPEN_FORM_IN_STORE:
          FixtureForm f = new FixtureForm();
          writeToProtocol("Before Form.start");
          try {
            f.start();
            writeToProtocol("Before Form.waitFor");
            f.waitFor();
            writeToProtocol("After Form.waitFor, interrupted=" + Thread.currentThread().isInterrupted() + ", futureCancelled=" + IFuture.CURRENT.get().isCancelled());
          }
          catch (RuntimeException | ThreadInterruptedError e) {
            writeToProtocol("Form error " + e.getClass().getSimpleName() + " " + e.getMessage());
            throw e;
          }
          break;
      }
    }
  }

  private enum DesktopBehaviour {
    DO_NOTHING,
    OPEN_MESSAGEBOX_IN_GUI_ATTACHED,
  }

  @ClassId("3f8a1387-edae-44ab-a7e7-fcddfdbcc397")
  private class FixtureDesktop extends AbstractDesktop {
    protected IFuture<?> desktopFuture;

    public FixtureDesktop() {
      super(true);
    }

    @Override
    protected void execGuiAttached() {
      desktopFuture = IFuture.CURRENT.get();
      switch (m_desktopBehaviour) {
        case OPEN_MESSAGEBOX_IN_GUI_ATTACHED:
          IMessageBox messageBox = MessageBoxes.createOk().withBody("Test MessageBox");
          try {
            writeToProtocol("Before MessageBox");
            int ok = messageBox.show();
            writeToProtocol("After MessageBox, result=" + ok + ", interrupted=" + Thread.currentThread().isInterrupted() + ", futureCancelled=" + desktopFuture.isCancelled());
          }
          catch (ThreadInterruptedError e) {
            writeToProtocol("Interrupted MessageBox");
            throw e;
          }
          break;
      }
    }
  }

  @ClassId("883a6b59-8558-427d-bd96-8a646d6c6319")
  private class FixtureForm extends AbstractForm {

    public FixtureForm() {
      super(true);
    }

    @SuppressWarnings("unused")
    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    @ClassId("8d42c646-6c31-4122-9f6c-51e3be81d7fe")
    public class MainBox extends AbstractGroupBox {
    }

    @Override
    public void start() {
      setHandler(new AbstractFormHandler() {
        @Override
        protected void execLoad() {
          writeToProtocol("Form.execLoad");
        }

        @Override
        protected void execStore() {
          writeToProtocol("Form.execStore");
        }

        @Override
        protected void execFinally() {
          writeToProtocol("Form.execFinally");
        }
      });
      super.start();
    }

    @Override
    protected void disposeFormInternal() {
      writeToProtocol("Form.dispose");
      super.disposeFormInternal();
    }
  }
}
