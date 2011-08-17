package org.eclipse.scout.testing.client.runner;

import java.util.List;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.testing.shared.runner.AbstractRunBeforesInSeparateScoutSession;
import org.junit.BeforeClass;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Invokes all methods of a test class annotated with {@link BeforeClass} in a separate Scout client session.
 * </p><b>Note:</b> This allows to execute clean-up operations with different privileges than the actual test cases. It
 * has however no impact on Scout's transaction handling. Every operations invoked on the backend is performed in its
 * own Scout transaction.
 */
public class RunBeforesInSeparateScoutClientSession extends AbstractRunBeforesInSeparateScoutSession {
  private final ScoutClientJobWrapperStatement m_beforesStatement;

  public RunBeforesInSeparateScoutClientSession(IClientSession clientSession, Statement statement, List<FrameworkMethod> befores, Object target) {
    super(statement, befores, target);
    m_beforesStatement = new ScoutClientJobWrapperStatement(clientSession, new Statement() {
      @Override
      public void evaluate() throws Throwable {
        evaluateBefores();
      }
    });
  }

  @Override
  protected void evaluateBeforesInScoutSession() throws Throwable {
    m_beforesStatement.evaluate();
  }
}
