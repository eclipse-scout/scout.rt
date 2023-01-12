/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
