/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.client.runner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.reflect.ReflectionUtility;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.job.filter.future.SessionFutureFilter;
import org.eclipse.scout.rt.testing.client.runner.statement.ClientRunContextStatement;
import org.eclipse.scout.rt.testing.client.runner.statement.RunInModelJobStatement;
import org.eclipse.scout.rt.testing.client.runner.statement.TimeoutClientRunContextStatement;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.testing.client.BlockingTestUtility;
import org.eclipse.scout.testing.client.BlockingTestUtility.IBlockingConditionTimeoutHandle;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Use this Runner to run tests which require a session context.
 * <p/>
 * Use the following mandatory annotations to configure the Runner:
 * <ul>
 * <li><strong>RunWithClientSession</strong>:<br/>
 * to specify the client-session to be used; can be defined on class or method-level;</li>
 * <li><strong>RunWithSubject</strong>:<br/>
 * to specify the user on behalf of which to run the test; can be defined on class or method-level;</li>
 * </ul>
 * Example:
 *
 * <pre>
 * &#064;RunWith(ClientTestRunner.class)
 * &#064;RunWithClientSession()
 * &#064;RunWithSubject(&quot;anna&quot;)
 * public class YourTest {
 *    ...
 * }
 * </pre>
 *
 * Execution:
 * <ul>
 * <li>By default, client sessions are shared among same users. This can be changed by setting the
 * {@link ClientSessionProvider} or a custom provider to {@link RunWithClientSession#provider()}.</li>
 * </ul>
 * <b>Note</b>: Usually, all {@link Before}, the {@link Test}-annotated method and all {@link After} methods are invoked
 * in a single model job. But if the {@link Test}-annotated method uses the timeout feature (i.e. {@link Test#timeout()}
 * ), the three parts are executed in different model jobs.
 *
 * @see RunWithClientSession
 * @see RunWithSubject
 * @since 5.1
 */
public class ClientTestRunner extends PlatformTestRunner {

  public ClientTestRunner(final Class<?> clazz) throws InitializationError {
    super(clazz);
  }

  @Override
  protected Statement classBlock(RunNotifier notifier) {
    final Statement s2 = super.classBlock(notifier);
    final Statement s1 = new CheckDesktopCleanupStatement(s2); //make sure that the test cleans up the desktop; no leftover forms, messageboxes or filechoosers
    return s1;
  }

  @Override
  protected Statement interceptClassLevelStatement(final Statement next, final Class<?> testClass) {
    final Statement s3 = new RunInModelJobStatement(next);
    final Statement s2 = new ClientRunContextStatement(s3, ReflectionUtility.getAnnotation(RunWithClientSession.class, testClass));
    final Statement s1 = super.interceptClassLevelStatement(s2, testClass);
    return s1;
  }

  @Override
  protected Statement interceptMethodLevelStatement(final Statement next, final Class<?> testClass, final Method testMethod) {
    final Statement s5;
    if (hasNoTimeout(testMethod)) {
      s5 = new RunInModelJobStatement(next);
    }
    else {
      // Three different model jobs are scheduled for all @Before methods, the @Test-annotated method and all @After methods.
      s5 = next;
    }
    final Statement s4 = new WaitUntilScheduledModelJobsHaveCompletedStatement(s5);
    final Statement s3 = new AddBlockingConditionTimeoutStatement(s4);
    final Statement s2 = new ClientRunContextStatement(s3, ReflectionUtility.getAnnotation(RunWithClientSession.class, testMethod, testClass));
    final Statement s1 = super.interceptMethodLevelStatement(s2, testClass, testMethod);
    return s1;
  }

  @Override
  protected Statement withPotentialTimeout(FrameworkMethod method, Object test, Statement next) {
    long timeoutMillis = getTimeoutMillis(method.getMethod());
    if (timeoutMillis <= 0) {
      // no timeout specified
      return next;
    }
    return new TimeoutClientRunContextStatement(next, timeoutMillis);
  }

  @Override
  protected Statement interceptBeforeStatement(Statement next, Class<?> testClass, Method testMethod) {
    Statement interceptedBeforeStatement = super.interceptBeforeStatement(next, testClass, testMethod);
    if (hasNoTimeout(testMethod)) {
      // no timeout specified
      return interceptedBeforeStatement;
    }
    return new TimeoutClientRunContextStatement(interceptedBeforeStatement, 0);
  }

  @Override
  protected Statement interceptAfterStatement(Statement next, Class<?> testClass, Method testMethod) {
    Statement interceptedAfterStatement = super.interceptAfterStatement(next, testClass, testMethod);
    if (hasNoTimeout(testMethod)) {
      // no timeout specified
      return interceptedAfterStatement;
    }
    return new TimeoutClientRunContextStatement(interceptedAfterStatement, 0);
  }

  @Override
  protected RunContext createJUnitRunContext() {
    return ClientRunContexts.empty();
  }

  /**
   * Wait for pending (scheduled) model jobs to complete. Typically these are jobs like desktop updates, locale switch,
   * lazy loads etc. Give these jobs some decent timeout, but not more.
   */
  protected static class WaitUntilScheduledModelJobsHaveCompletedStatement extends Statement {
    private final Statement m_statement;

    public WaitUntilScheduledModelJobsHaveCompletedStatement(Statement statement) {
      m_statement = statement;
    }

    @Override
    public void evaluate() throws Throwable {
      m_statement.evaluate();
      if (ISession.CURRENT.get() != null) {
        IFilter<IFuture<?>> filter =
            ModelJobs
                .newFutureFilterBuilder()
                .andAreSingleExecuting()
                .andMatch(new SessionFutureFilter(ISession.CURRENT.get()))
                .toFilter();
        Jobs.getJobManager().awaitFinished(filter, 1, TimeUnit.MINUTES);
      }
    }
  }

  protected static class AddBlockingConditionTimeoutStatement extends Statement {
    private final Statement m_statement;

    public AddBlockingConditionTimeoutStatement(Statement statement) {
      m_statement = statement;
    }

    @Override
    public void evaluate() throws Throwable {
      IBlockingConditionTimeoutHandle reg = BlockingTestUtility.addBlockingConditionTimeoutListener(2, TimeUnit.MINUTES);
      try {
        m_statement.evaluate();
        if (reg.getFirstException() != null) {
          throw reg.getFirstException();
        }
      }
      finally {
        reg.dispose();
      }
    }
  }

  protected static class CheckDesktopCleanupStatement extends Statement {
    private final Statement m_statement;

    public CheckDesktopCleanupStatement(Statement statement) {
      m_statement = statement;
    }

    @Override
    public void evaluate() throws Throwable {
      int messageBoxesBefore = 0;
      int messageBoxesAfter = 0;
      int formsBefore = 0;
      int formsAfter = 0;
      int fileChoosersBefore = 0;
      int fileChoosersAfter = 0;
      IDesktop desktop = IDesktop.CURRENT.get();
      if (desktop != null) {
        messageBoxesBefore = desktop.getMessageBoxes().size();
        formsBefore = desktop.getDialogs().size() + desktop.getViews().size();
        fileChoosersBefore = desktop.getFileChoosers().size();
      }
      m_statement.evaluate();
      desktop = IDesktop.CURRENT.get();
      if (desktop != null) {
        messageBoxesAfter = desktop.getMessageBoxes().size();
        formsAfter = desktop.getDialogs().size() + desktop.getViews().size();
        fileChoosersAfter = desktop.getFileChoosers().size();
      }
      if (messageBoxesAfter > messageBoxesBefore || formsAfter > formsBefore || fileChoosersAfter > fileChoosersBefore) {
        try {
          Assert.fail(String.format("Desktop has leftovers: %d message boxes, %d forms, %d file choosers. Clean up the desktop in a @After method.", messageBoxesAfter, formsAfter, fileChoosersAfter));
        }
        finally {
          if (desktop != null) {
            for (IMessageBox m : desktop.getMessageBoxes()) {
              dropMessageBox(desktop, m);
            }
            for (IForm f : desktop.getDialogs()) {
              dropForm(desktop, f);
            }
            for (IForm f : desktop.getViews()) {
              dropForm(desktop, f);
            }
            for (IFileChooser f : desktop.getFileChoosers()) {
              dropFileChooser(desktop, f);
            }
          }
        }
      }
    }

    private void dropMessageBox(IDesktop desktop, IMessageBox m) {
      try {
        m.getUIFacade().setResultFromUI(IMessageBox.CANCEL_OPTION);
      }
      catch (Exception ex) { // NOSONAR
        //nop
      }
      finally {
        desktop.hideMessageBox(m);
      }
    }

    private void dropForm(IDesktop desktop, IForm f) {
      try {
        f.setAskIfNeedSave(false);
        f.doClose();
      }
      catch (Exception ex) { // NOSONAR
        //nop
      }
      finally {
        desktop.hideForm(f);
      }
    }

    private void dropFileChooser(IDesktop desktop, IFileChooser f) {
      try {
        f.getUIFacade().setResultFromUI(new ArrayList<BinaryResource>());
      }
      catch (Exception ex) { // NOSONAR
        //nop
      }
      finally {
        desktop.hideFileChooser(f);
      }
    }
  }

}
