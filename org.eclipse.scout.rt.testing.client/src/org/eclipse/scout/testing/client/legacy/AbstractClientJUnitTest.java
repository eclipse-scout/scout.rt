/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.testing.client.legacy;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.test.ClientTestUtility;
import org.eclipse.scout.rt.shared.services.common.test.AbstractTest;
import org.eclipse.scout.rt.shared.services.common.test.ITest;
import org.eclipse.scout.rt.shared.services.common.test.TestStatus;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * Base class for JUnit based client tests. The class can be used with
 * the existing ITest/TestUtility framework of Scout. However, it is possible
 * to use the features of JUnit such as annotations to mark test/set up/tear down
 * methods and assertions.
 */
public abstract class AbstractClientJUnitTest extends AbstractTest implements ITest {

  @Override
  protected String getConfiguredSubTitle() {
    return "JUnitTest";
  }

  @Override
  protected String getConfiguredTitle() {
    return this.getClass().getSimpleName().replaceAll("(JUnit)?Test\\$", "");
  }

  @Override
  public final void setUp() throws Throwable {
  }

  @Override
  public final void tearDown() throws Throwable {
  }

  @Override
  public final void run() throws Throwable {
    JUnitCore core = new JUnitCore();
    core.addListener(new CustomListener(this));
    core.run(this.getClass()); //It is possible to get the Result r
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    if (getProduct() == null) {
      IClientSession session = ClientTestUtility.getClientSession();
      if (session != null) {
        setProduct(session.getBundle().getSymbolicName());
      }
    }
  }

  private class CustomListener extends RunListener {
    protected AbstractClientJUnitTest m_test;
    protected boolean m_hasFailed = false;

    /**
     * Initialize with <code>test</code>
     * 
     * @param test
     *          The test used to handle failed tests.
     */
    public CustomListener(AbstractClientJUnitTest test) {
      this.m_test = test;
    }

    /**
     * Called before any tests have been run.
     * 
     * @param description
     *          describes the tests to be run
     */
    @Override
    public void testRunStarted(Description description) throws Exception {
    }

    /**
     * Called when all tests have finished
     * 
     * @param result
     *          the summary of the test run, including all the tests that failed
     */

    public void testRunFinished(Result result) throws Exception {
    }

    /**
     * Called when an atomic test is about to be started.
     * 
     * @param description
     *          the description of the test that is about to be run
     *          (generally a class and method name)
     */
    @Override
    public void testStarted(Description description) throws Exception {
      m_test.startTiming();
      m_hasFailed = false;
    }

    /**
     * Called when an atomic test has finished, whether the test succeeds or fails.
     * 
     * @param description
     *          the description of the test that just ran
     */
    @Override
    public void testFinished(Description description) throws Exception {
      if (!m_hasFailed) {
        long duration = m_test.stopTiming();

        TestStatus s = new TestStatus(description.getDisplayName().replaceAll("^.+\\(", "").replaceAll("\\).*$", ""), description.getDisplayName().replaceAll("\\(.+\\)$", ""), "");
        s.setSeverity(TestStatus.INFO);
        s.setDuration(duration);
        m_test.getTestContext().addStatus(s);
      }
    }

    /**
     * Called when an atomic test fails.
     * 
     * @param failure
     *          describes the test that failed and the exception that was thrown
     */
    @Override
    public void testFailure(Failure failure) throws Exception {
      long duration = m_test.stopTiming();

      m_hasFailed = true;
      TestStatus s = new TestStatus(failure.getTestHeader().replaceAll("^.+\\(", "").replaceAll("\\).*$", ""), failure.getTestHeader().replaceAll("\\(.+\\)$", ""), failure.getMessage());
      s.setSeverity(TestStatus.FATAL);
      s.setDuration(duration);
      s.setException(failure.getException());
      m_test.getTestContext().addStatus(s);
    }

    /**
     * Called when an atomic test flags that it assumes a condition that is
     * false
     * 
     * @param failure
     *          describes the test that failed and the AssumptionViolatedException that was thrown
     */
    @Override
    public void testAssumptionFailure(Failure failure) {
      long duration = m_test.stopTiming();

      m_hasFailed = true;
      TestStatus s = new TestStatus(failure.getTestHeader().replaceAll("^.+\\(", "").replaceAll("\\).*$", ""), failure.getTestHeader().replaceAll("\\(.+\\)$", ""), failure.getMessage());
      s.setSeverity(TestStatus.ERROR);
      s.setDuration(duration);
      s.setException(failure.getException());

      m_test.getTestContext().addStatus(s);
    }

    /**
     * Called when a test will not be run, generally because a test method is annotated
     * with {@link org.junit.Ignore}.
     * 
     * @param description
     *          describes the test that will not be run
     */
    @Override
    public void testIgnored(Description description) throws Exception {
      TestStatus s = new TestStatus(description.getDisplayName().replaceAll("^.+\\(", "").replaceAll("\\).*$", ""), description.getDisplayName().replaceAll("\\(.+\\)$", ""), "The test is ignored.");
      s.setSeverity(TestStatus.WARNING);
      s.setDuration(0);
      m_test.getTestContext().addStatus(s);
    }
  }
}
