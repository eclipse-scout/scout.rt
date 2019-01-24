package org.eclipse.scout.rt.ui.html;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.shared.job.filter.future.SessionFutureFilter;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil.ICondition;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithNewPlatform;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.SessionStoreHousekeepingDelayProperty;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.SessionStoreHousekeepingMaxWaitShutdownProperty;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.SessionStoreMaxWaitAllShutdownProperty;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.SessionStoreMaxWaitWriteLockProperty;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWithNewPlatform()
@RunWith(PlatformTestRunner.class)
public class DesktopWithBlockingConditionInterruptionTest {
  private List<IBean<?>> m_beans;

  private DesktopBehaviour m_desktopBehaviour;
  private List<String> m_protocol = Collections.synchronizedList(new ArrayList<String>());

  @Before
  public void before() {
    m_protocol.clear();
    m_beans = TestingUtility.registerBeans(
        new BeanMetaData(JobCompletionDelayOnSessionShutdown.class).withProducer(new IBeanInstanceProducer<JobCompletionDelayOnSessionShutdown>() {
          @Override
          public JobCompletionDelayOnSessionShutdown produce(IBean<JobCompletionDelayOnSessionShutdown> bean) {
            return new JobCompletionDelayOnSessionShutdown() {
              @Override
              public Long getDefaultValue() {
                return 0L;
              }
            };
          }
        }),

        new BeanMetaData(SessionStoreHousekeepingDelayProperty.class).withInitialInstance(new SessionStoreHousekeepingDelayProperty() {
          @Override
          public Integer getDefaultValue() {
            return 1;
          }
        }),

        new BeanMetaData(SessionStoreHousekeepingMaxWaitShutdownProperty.class).withInitialInstance(new SessionStoreHousekeepingMaxWaitShutdownProperty() {
          @Override
          public Integer getDefaultValue() {
            return 1;
          }
        }),

        new BeanMetaData(SessionStoreMaxWaitWriteLockProperty.class).withInitialInstance(new SessionStoreMaxWaitWriteLockProperty() {
          @Override
          public Integer getDefaultValue() {
            return 1;
          }
        }),

        new BeanMetaData(SessionStoreMaxWaitAllShutdownProperty.class).withInitialInstance(new SessionStoreMaxWaitAllShutdownProperty() {
          @Override
          public Integer getDefaultValue() {
            return 1;
          }
        }),

        new BeanMetaData(FixtureClientSession.class).withProducer(bean -> new FixtureClientSession()));
  }

  @After
  public void after() {
    TestingUtility.unregisterBeans(m_beans);
    m_protocol.clear();
  }

  private void writeToProtocol(String line) {
    System.out.println("protocol: " + line);
    m_protocol.add(line);
  }

  @Test
  public void testDesktopWithBLockingMessageBoxUponStartup() {
    m_desktopBehaviour = DesktopBehaviour.OPEN_MESSAGEBOX;
    IUiSession uiSession = JsonTestUtility.createAndInitializeUiSession();
    final FixtureClientSession session = (FixtureClientSession) uiSession.getClientSession();

    JobTestUtil.waitForCondition(new ICondition() {
      @Override
      public boolean isFulfilled() {
        return session.getDesktop() != null && session.getDesktop().desktopFuture != null && session.getDesktop().desktopFuture.getState() == JobState.WAITING_FOR_BLOCKING_CONDITION;
      }
    });
    writeToProtocol("MessageBox is open");

    writeToProtocol("Session stopping");
    ModelJobs.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        session.getDesktop().getUIFacade().closeFromUI(true);
      }
    }, ModelJobs.newInput(ClientRunContexts.empty().withSession(session, true)))
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
    m_desktopBehaviour = DesktopBehaviour.DO_NOTHING;
    IUiSession uiSession = JsonTestUtility.createAndInitializeUiSession();
    final FixtureClientSession session = (FixtureClientSession) uiSession.getClientSession();

    IFuture<?> callingFuture = ModelJobs.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
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
      }
    }, ModelJobs
        .newInput(ClientRunContexts.empty().withSession(session, true))
        .withExceptionHandling(new ClientExceptionHandler(), false));

    JobTestUtil.waitForCondition(new ICondition() {
      @Override
      public boolean isFulfilled() {
        return callingFuture.getState() == JobState.WAITING_FOR_BLOCKING_CONDITION;
      }
    });
    writeToProtocol("Form is open");

    writeToProtocol("Session stopping");
    ModelJobs.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        session.getDesktop().getUIFacade().closeFromUI(true);
      }
    }, ModelJobs.newInput(ClientRunContexts.empty().withSession(session, true)))
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
    }
  }

  private enum DesktopBehaviour {
    DO_NOTHING,
    OPEN_MESSAGEBOX,
  }

  private class FixtureDesktop extends AbstractDesktop {
    protected IFuture<?> desktopFuture;

    public FixtureDesktop() {
      super(true);
    }

    @Override
    protected void execGuiAttached() {
      desktopFuture = IFuture.CURRENT.get();
      switch (m_desktopBehaviour) {
        case DO_NOTHING:
          break;
        case OPEN_MESSAGEBOX:
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

  private class FixtureForm extends AbstractForm {

    public FixtureForm() {
      super(true);
    }

    @SuppressWarnings("unused")
    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

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
