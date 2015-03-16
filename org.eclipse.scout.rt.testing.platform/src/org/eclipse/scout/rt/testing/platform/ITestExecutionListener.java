/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.platform;

import org.eclipse.scout.rt.platform.cdi.Bean;
import org.eclipse.scout.rt.platform.cdi.IBeanContext;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.runner.Description;

/**
 * Listener to be notified about JUnit test execution events for tests using {@link PlatformTestRunner}. By
 * default, there is no listener installed. A listener is installed by registering it on the global {@link IBeanContext}
 *
 * @see PlatformTestRunner
 */
@Bean
public interface ITestExecutionListener {

  /**
   * Method invoked after the platform was started.
   */
  void platformStarted();

  /**
   * Method invoked before a JUnit test class commence execution.
   *
   * @param description
   *          describes the current test-context.
   */
  void beforeTestClass(Description description);

  /**
   * Method invoked after a JUnit test class finished execution.
   *
   * @param description
   *          describes the current test-context.
   */
  void afterTestClass(Description description);
}
