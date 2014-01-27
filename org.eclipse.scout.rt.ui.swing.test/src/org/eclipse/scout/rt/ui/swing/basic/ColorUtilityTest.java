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
package org.eclipse.scout.rt.ui.swing.basic;

import java.awt.Color;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link ColorUtility}
 */
public class ColorUtilityTest {
  @Test
  public void testColorToString() {
    String hex = ColorUtility.createStringFromColor(null);
    Assert.assertNull(hex);
    Color red = Color.RED;
    Assert.assertEquals("#ff0000", ColorUtility.createStringFromColor(red));
    Color black = Color.BLACK;
    Assert.assertEquals("#000000", ColorUtility.createStringFromColor(black));
    Color white = Color.white;
    Assert.assertEquals("#ffffff", ColorUtility.createStringFromColor(white));
  }
}
