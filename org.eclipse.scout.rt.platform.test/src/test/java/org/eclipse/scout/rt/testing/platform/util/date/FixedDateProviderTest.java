/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.util.date;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

/**
 * Test for {@link FixedDateProvider}
 */
public class FixedDateProviderTest {

  @Test
  public void testDateProvider() {
    Date now = new Date(0);
    FixedDateProvider dateProvider = new FixedDateProvider(now);
    assertEquals(now, dateProvider.currentMillis());
  }
}
