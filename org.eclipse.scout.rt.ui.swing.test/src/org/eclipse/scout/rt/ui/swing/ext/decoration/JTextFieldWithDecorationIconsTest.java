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
package org.eclipse.scout.rt.ui.swing.ext.decoration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Graphics;
import java.awt.Insets;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link JTextFieldWithDecorationIcons}
 *
 * @since 4.0-RC1
 */
public class JTextFieldWithDecorationIconsTest {

  /**
   * see Bugzilla 434856
   */
  @Test
  public void testSameMargins() {
    P_JTextFieldWithDecorationIcons textField = new P_JTextFieldWithDecorationIcons();
    textField.setIconWidth(10);
    Graphics g = Mockito.mock(Graphics.class);
    textField.paint(g);
    textField.paint(g);
    textField.paint(g);
    assertEquals("setMargin() should only be called if insets change!", 1, textField.getHowOftenWasSetMarginCalled());
  }

  /**
   * see Bugzilla 434856
   */
  @Test
  public void testDifferentMargins() {
    P_JTextFieldWithDecorationIcons textField = new P_JTextFieldWithDecorationIcons();
    textField.setIconWidth(10);
    Graphics g = Mockito.mock(Graphics.class);
    textField.paint(g);
    textField.setIconWidth(5);
    textField.paint(g);
    textField.setIconWidth(10);
    textField.paint(g);
    assertEquals("setMargin() should always called if insets change!", 3, textField.getHowOftenWasSetMarginCalled());
  }

  private class P_JTextFieldWithDecorationIcons extends JTextFieldWithDecorationIcons {
    private static final long serialVersionUID = 1L;
    private int m_setMarginCalled;
    private int m_iconWidth;

    @Override
    public void setMargin(Insets m) {
      m_setMarginCalled++;
    }

    public int getHowOftenWasSetMarginCalled() {
      return m_setMarginCalled;
    }

    @Override
    public Insets getInsets() {
      return new Insets(1, 1, 1, 1);
    }

    @Override
    public Insets getMargin() {
      return new Insets(1, 1, 1, 1);
    }

    @Override
    public IDecoration getDecorationIcon() {
      IDecoration icon = mock(IDecoration.class);
      when(icon.getWidth()).thenReturn(m_iconWidth);
      return icon;
    }

    public void setIconWidth(int val) {
      m_iconWidth = val;
    }
  }
}
