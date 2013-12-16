/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.rap.util;

import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;
import org.eclipse.swt.SWT;
import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for {@link RwtUtility}
 * 
 * @since 3.10.0-M4
 */
public class RwtUtilityTest {

  @Test
  public void testRwtKeyCode() {
    KeyStroke ks = new KeyStroke("control-alternate-f11");
    int keyCode = RwtUtility.getRwtKeyCode(ks);
    Assert.assertEquals("should return F11", SWT.F11, keyCode);

    ks = new KeyStroke("F12");
    keyCode = RwtUtility.getRwtKeyCode(ks);
    Assert.assertEquals("should return F12", SWT.F12, keyCode);

    ks = new KeyStroke("alternate-1");
    keyCode = RwtUtility.getRwtKeyCode(ks);
    Assert.assertEquals("should return 1", '1', keyCode);

    ks = new KeyStroke("alternate-f");
    keyCode = RwtUtility.getRwtKeyCode(ks);
    Assert.assertEquals("should return f", 'f', keyCode);

    ks = new KeyStroke("abc");
    keyCode = RwtUtility.getRwtKeyCode(ks);
    Assert.assertEquals("should return SWT.NONE", SWT.NONE, keyCode);
  }

  @Test
  public void testScoutToRwtKey() {
    int key = RwtUtility.scoutToRwtKey("f11");
    Assert.assertEquals("should be SWT.f11", SWT.F11, key);

    key = RwtUtility.scoutToRwtKey("alt");
    Assert.assertEquals("should be SWT.ALT", SWT.ALT, key);

    key = RwtUtility.scoutToRwtKey("alternate");
    Assert.assertEquals("should be SWT.ALT", SWT.ALT, key);

    key = RwtUtility.scoutToRwtKey("control");
    Assert.assertEquals("should be SWT.CTRL", SWT.CTRL, key);
  }

  @Test
  public void testKeyStrokeRepresentation() {
    KeyStroke ks = new KeyStroke("f11");
    Assert.assertEquals("KeyStroke should be f11", "f11", ks.getKeyStroke());
    Assert.assertEquals("KeyStroke pretty printed should be F11", "F11", RwtUtility.getKeyStrokePrettyPrinted(ks));

    ks = new KeyStroke("a");
    Assert.assertEquals("KeyStroke should be 'a'", "a", ks.getKeyStroke());
    Assert.assertEquals("KeyStroke pretty printed should be a", "a", RwtUtility.getKeyStrokePrettyPrinted(ks));

    ks = new KeyStroke("alternate-f1");
    Assert.assertEquals("KeyStroke should be alternate-f1", "alternate-f1", ks.getKeyStroke());
    Assert.assertEquals("KeyStroke pretty printed should be Alt+F1", "Alt+F1", RwtUtility.getKeyStrokePrettyPrinted(ks));

    ks = new KeyStroke("control-alternate-1");
    Assert.assertEquals("KeyStroke should be control-alternate-1", "control-alternate-1", ks.getKeyStroke());
    Assert.assertEquals("KeyStroke pretty printed should be Ctrl+Alt+1", "Ctrl+Alt+1", RwtUtility.getKeyStrokePrettyPrinted(ks));

    ks = new KeyStroke("");
    Assert.assertEquals("KeyStroke should be empty", "", ks.getKeyStroke());
    Assert.assertEquals("KeyStroke pretty printed should be empty", "", RwtUtility.getKeyStrokePrettyPrinted(ks));
  }
}
