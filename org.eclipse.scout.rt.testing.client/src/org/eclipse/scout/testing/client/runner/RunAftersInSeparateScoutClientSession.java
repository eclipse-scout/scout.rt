package org.eclipse.scout.testing.client.runner;

import java.util.List;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.testing.shared.runner.AbstractRunAftersInSeparateScoutSession;
import org.junit.AfterClass;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Invokes all methods of a test class annotated with {@link AfterClass} in a separate Scout client session.
 * </p><b>Note:</b> This allows to execute clean-up operations with different privileges than the actual test cases. It
 * has however no impact on Scout's transaction handling. Every operations invoked on the backend is performed in its
 * own Scout transaction.
 */
public class RunAftersInSeparateScoutClientSession extends AbstractRunAftersInSeparateScoutSession {
  private final ScoutClientJobWrapperStatement m_aftersStatement;

  public RunAftersInSeparateScoutClientSession(IClientSession clientSession, Statement statement, List<FrameworkMethod> afters, Object target) {
    super(statement, afters, target);
    m_aftersStatement = new ScoutClientJobWrapperStatement(clientSession, new Statement() {
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
