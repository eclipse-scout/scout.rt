/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests for {@link SwtMenuUtility}
 *
 * @since 5.0-M2
 */
public class SwtMenuUtilityTest {

  @Test
  public void testFormatKeystroke() {
    assertEquals("", SwtMenuUtility.formatKeystroke(""));
    assertEquals("", SwtMenuUtility.formatKeystroke(null));
    assertEquals("a", SwtMenuUtility.formatKeystroke("a"));
    assertEquals("F11", SwtMenuUtility.formatKeystroke("F11"));
    assertEquals("F11", SwtMenuUtility.formatKeystroke("f11"));
    assertEquals("", SwtMenuUtility.formatKeystroke("undefinedkeystroke"));
  }
}
