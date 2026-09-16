/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.date;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.eclipse.scout.rt.testing.platform.util.date.LogicalTimeDateProvider;
import org.junit.Test;

/**
 * Testcases for {@link LogicalTimeDateProvider}
 */
public class LogicalTimeDateProviderTest {

  private static final Date NOW = new Date();

  @Test
  public void testGetDate() {
    LogicalTimeDateProvider dateProvider = new LogicalTimeDateProvider(NOW);
    assertEquals(NOW, dateProvider.getDate());
    for (int i = 1; i <= 5; i++) {
      assertEquals(DateUtility.addMinutes(NOW, i), dateProvider.getDate());
    }
  }
}
