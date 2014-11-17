package org.eclipse.scout.rt.testing.server.runner;

import java.util.List;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.server.IServerJobFactory;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerJobFactory;
import org.eclipse.scout.rt.testing.shared.runner.AbstractRunAftersInSeparateScoutSession;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Invokes all methods of a test class annotated with {@link org.junit.AfterClass} in a separate Scout server session
 * and therefore in a separate Scout transaction.
 */
public class RunAftersInSeparateScoutServerSession extends AbstractRunAftersInSeparateScoutSession {
  private final ScoutServerJobWrapperStatement m_aftersStatement;

  public RunAftersInSeparateScoutServerSession(IServerJobFactory factory, Statement statement, List<FrameworkMethod> afters, Object target) {
    super(statement, afters, target);
    m_aftersStatement = new ScoutServerJobWrapperStatement(factory, new Statement() {
      @Override
      public void evaluate() throws Throwable {
        evaluateAfters();
      }
    });
  }

  /**
   * @deprecated use RunAftersInSeparateScoutServerSession(IServerJobFactory, Statement, List<FrameworkMethod>, Object)
   */
  @Deprecated
  public RunAftersInSeparateScoutServerSession(IServerSession serverSession, Subject subject, Statement statement, List<FrameworkMethod> afters, Object target) {
    this(new ServerJobFactory(serverSession, subject), statement, afters, target);
  }

  @Override
  protected void evaluateAftersInScoutSession() throws Throwable {
    m_aftersStatement.evaluate();
  }
}
