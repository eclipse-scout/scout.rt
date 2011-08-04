package org.eclipse.scout.rt.testing.server.runner;

import java.util.List;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.testing.shared.runner.AbstractRunAftersInSeparateScoutSession;
import org.junit.AfterClass;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Invokes all methods of a test class annotated with {@link AfterClass} in a separate Scout server session and
 * therefore in a separate Scout transaction.
 */
public class RunAftersInSeparateScoutServerSession extends AbstractRunAftersInSeparateScoutSession {
  private final ScoutServerJobWrapperStatement m_aftersStatement;

  public RunAftersInSeparateScoutServerSession(IServerSession serverSession, Subject subject, Statement statement, List<FrameworkMethod> afters, Object target) {
    super(statement, afters, target);
    m_aftersStatement = new ScoutServerJobWrapperStatement(serverSession, subject, new Statement() {
      @Override
      public void evaluate() throws Throwable {
        evaluateAfters();
      }
    });
  }

  @Override
  protected void evaluateAftersInScoutSession() throws Throwable {
    m_aftersStatement.evaluate();
  }
}
