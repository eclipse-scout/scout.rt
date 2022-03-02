/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.util;

import java.io.IOException;

import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class LambdaUtilityTest {

  @Test(expected = RuntimeException.class)
  public void testIOException() {
    LambdaUtility.invokeSafely(() -> {
      throw new IOException("test");
    });
  }

  @Test(expected = VetoException.class)
  public void testVetoException() {
    LambdaUtility.invokeSafely(() -> {
      throw new VetoException("test");
    });
  }

  @Test(expected = RuntimeException.class)
  public void testRuntimeException() {
    LambdaUtility.invokeSafely(() -> {
      throw new RuntimeException("test");
    });
  }
}
