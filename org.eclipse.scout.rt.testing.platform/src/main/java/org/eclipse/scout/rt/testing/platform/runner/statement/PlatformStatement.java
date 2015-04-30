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
package org.eclipse.scout.rt.testing.platform.runner.statement;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSharedPlatform;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.model.Statement;

/**
 * This statement is used by {@link PlatformTestRunner} for executing test classes. It supports two modes, shared
 * platform and private platform:
 * <table border="1">
 * <tr>
 * <th>Annotation on test class</th>
 * <th>Mode</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td><em>none</em></td>
 * <td rowspan="2"><b>Shared Platform</b></td>
 * <td rowspan="2">The first {@link PlatformTestRunner} starts a shared platform that is used by all other shared
 * platform tests.</td>
 * </tr>
 * <tr>
 * <td><code>@</code>{@link RunWithPrivatePlatform}<code>(false)</code></td>
 * </tr>
 * <tr>
 * <td><code>@</code>{@link RunWithPrivatePlatform}</td>
 * <td rowspan="3"><b>Private Platform</b></td>
 * <td rowspan="3">A new private platform is started for the annotated test class. {@link BeforeClass}, {@link Before},
 * {@link Test}, {@link After} and {@link AfterClass} are all using the same private {@link IPlatform}. The platform
 * that was valid before is restored after the test.</td>
 * </tr>
 * <tr>
 * <td><code>@</code>{@link RunWithPrivatePlatform}<code>(true)</code></td>
 * </tr>
 * <tr>
 * <td><code>@</code>{@link RunWithPrivatePlatform}<code>(value = true)</code></td>
 * </tr>
 * </table>
 *
 * @since 5.1
 */
public class PlatformStatement extends Statement {
  private static IPlatform s_sharedPlatform;

  private final Statement m_next;
  private final RunWithSharedPlatform m_runWithSharedPlatform;

  public PlatformStatement(Statement next, RunWithSharedPlatform runWithSharedPlatform) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
    m_runWithSharedPlatform = runWithSharedPlatform;
  }

  @Override
  public void evaluate() throws Throwable {
    if (m_runWithSharedPlatform == null) {
      evaluateWithNewPlatform();
    }
    else {
      evaluateWithSharedPlatform();
    }
  }

  protected void evaluateWithNewPlatform() throws Throwable {
    Platform.setDefault();
    try {
      Platform.get().start();
      m_next.evaluate();
    }
    finally {
      Platform.get().stop();
    }
  }

  protected void evaluateWithSharedPlatform() throws Throwable {
    if (s_sharedPlatform == null) {
      Platform.setDefault();
      Platform.get().start();
      s_sharedPlatform = Platform.get();
    }
    Platform.set(s_sharedPlatform);
    try {
      m_next.evaluate();
    }
    finally {
      Platform.set(null);
    }
  }
}
