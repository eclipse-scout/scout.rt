/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
