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
package org.eclipse.scout.rt.testing.platform;

import org.eclipse.scout.rt.platform.Platform;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 *
 */
public class ScoutPlatformTestRunner extends BlockJUnit4ClassRunner {

  /**
   * @param klass
   * @throws InitializationError
   */
  public ScoutPlatformTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  @Override
  protected Statement classBlock(RunNotifier notifier) {
    final Statement superStatement = super.classBlock(notifier);
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        ((Platform) Platform.get()).ensureStarted();
        superStatement.evaluate();
      }
    };
  }

}
