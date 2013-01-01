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
package org.eclipse.scout.rt.shared.services.common.test;

import java.util.List;

/**
 * @deprecated Use Scout JUnit Testing Support: {@link org.eclipse.scout.testing.client.runner.ScoutClientTestRunner} or
 *             {@link org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner} to run Unit tests.
 *
 * Context used to run a series of tests using {@link TestUtility#runTests(ITestContext, java.util.Collection)}
 */
@Deprecated
@SuppressWarnings("deprecation")
public interface ITestContext {

  /**
   * Called by the test framework before the first test is running
   */
  void begin();

  /**
   * Called by the test framework after the last test was run
   */
  void end();

  /**
   * each {@link ITest} can add multiple status to the test context
   */
  void addStatus(TestStatus e);

  /**
   * @return the life list with all test status of all {@link ITest}
   */
  List<TestStatus> getStatusList();

}
