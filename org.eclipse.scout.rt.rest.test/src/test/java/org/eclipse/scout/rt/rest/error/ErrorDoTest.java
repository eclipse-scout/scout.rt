/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.error;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ErrorDoTest {

  @Test
  public void testSeverityAsInt() {
    ErrorDo errorDo = BEANS.get(ErrorDo.class);
    assertNull(errorDo.getSeverity());
    assertEquals(IStatus.ERROR, errorDo.getSeverityAsInt());

    errorDo.withSeverity("error");
    assertEquals(IStatus.ERROR, errorDo.getSeverityAsInt());

    errorDo.withSeverity("warning");
    assertEquals(IStatus.WARNING, errorDo.getSeverityAsInt());

    errorDo.withSeverity("info");
    assertEquals(IStatus.INFO, errorDo.getSeverityAsInt());

    errorDo.withSeverity("unknown severity");
    assertEquals(IStatus.ERROR, errorDo.getSeverityAsInt());
  }
}
