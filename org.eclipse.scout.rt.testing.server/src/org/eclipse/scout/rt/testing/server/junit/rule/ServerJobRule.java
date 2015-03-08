/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.server.junit.rule;

import java.util.concurrent.atomic.AtomicReference;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.rt.platform.cdi.IBean;
import org.eclipse.scout.rt.platform.cdi.IBeanContext;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.job.IServerJobManager;
import org.eclipse.scout.rt.server.job.ServerJobInput;
import org.eclipse.scout.rt.server.session.ServerSessionProviderWithCache;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * JUnit {@link Rule} to run test-methods in a separate server job. By default, the user 'anonymous' is used to create
 * the associated server session. If a test-method should be run on behalf of another user, annotate that test-method
 * with the {@link RunAs} annotation to provide a specific user.
 * <p/>
 * This rule is installed a following:<br/>
 *
 * <pre>
 * &#064;Rule
 * public TestRule serverJobRule = new ServerJobRule();
 * </pre>
 *
 * A static rule can be installed as following. This is useful if the static {@link BeforeClass} or {@link AfterClass}
 * methods should be run in a server job.
 *
 * <pre>
 * &#064;ClassRule
 * public static TestRule serverJobClassRule = new ServerJobRule();
 * </pre>
 *
 * The {@link ServerJobRule} has two constructors. Instantiate the rule with the default constructor to use the session
 * class registered on the global {@link IBeanContext}. To provide a specific test-server-session class, use the
 * constructor which accepts a server session class.
 * <p/>
 * Even though each test-method runs within a separate server job, the server session is shared among methods with the
 * same user.
 *
 * @see RunAs
 * @since 5.1
 */
public class ServerJobRule implements TestRule {

  public static final String RUN_AS_ANONYMOUS = "anonymous";

  private Class<? extends IServerSession> m_serverSessionClass;

  /**
   * Use this constructor to run your tests on behalf of the server-session class registered on the global
   * {@link IBeanContext}.
   */
  public ServerJobRule() {
  }

  /**
   * Use this constructor to run your tests on behalf of a specific server-session class.
   */
  public ServerJobRule(final Class<? extends IServerSession> serverSessionClass) {
    Assertions.assertTrue(!serverSessionClass.isInterface());
    m_serverSessionClass = serverSessionClass;
  }

  @Override
  public Statement apply(final Statement base, final Description description) {
    final Subject subject = newTestSubject(description);

    return new Statement() {

      @Override
      public void evaluate() throws Throwable {
        if (m_serverSessionClass == null) {
          runInServerJob(base, subject);
        }
        else {
          // Temporary register custom session class in global bean context.
          final IBean<?> oldServerSessionBean = OBJ.registerClass(OBJ.one(IServerSession.class).getClass());
          OBJ.unregisterBean(oldServerSessionBean);
          try {
            final IBean<?> newServerSessionBean = OBJ.registerClass(m_serverSessionClass);
            try {
              runInServerJob(base, subject);
            }
            finally {
              OBJ.unregisterBean(newServerSessionBean);
            }
          }
          finally {
            OBJ.registerBean(oldServerSessionBean, null);
          }
        }
      }
    };
  }

  @Internal
  protected void runInServerJob(final Statement base, final Subject subject) throws Throwable {
    final ServerJobInput input = ServerJobInput.empty();
    input.name("JUnit-test");
    input.subject(subject);
    input.session(lookupServerSession(input.copy()));

    final AtomicReference<Throwable> throwable = new AtomicReference<>();

    OBJ.one(IServerJobManager.class).runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          base.evaluate();
        }
        catch (final Throwable t) {
          throwable.set(t);
        }
      }
    }, input);

    if (throwable.get() != null) {
      throw throwable.get();
    }
  }

  @Internal
  protected Subject newTestSubject(final Description description) {
    // 1. Get 'runAs' from current invocation context, e.g. before, test-method, after, ...
    RunAs runAsAnnotation = description.getAnnotation(RunAs.class);
    if (runAsAnnotation == null) {
      // 2. Get 'runAs' as defined on test-class.
      runAsAnnotation = description.getTestClass().getAnnotation(RunAs.class);
    }

    final String runAs = runAsAnnotation != null ? runAsAnnotation.value() : null;

    final Subject subject = new Subject();
    subject.getPrincipals().add(new SimplePrincipal(StringUtility.nvl(runAs, RUN_AS_ANONYMOUS)));
    subject.setReadOnly();
    return subject;
  }

  @Internal
  protected IServerSession lookupServerSession(final ServerJobInput input) throws ProcessingException {
    return OBJ.one(ServerSessionProviderWithCache.class).provide(input);
  }
}
