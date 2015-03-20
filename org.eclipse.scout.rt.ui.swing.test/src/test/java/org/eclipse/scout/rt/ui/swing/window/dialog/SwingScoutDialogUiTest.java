/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.window.dialog;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Dialog;
import java.awt.Rectangle;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.ext.JDialogEx;
import org.eclipse.scout.rt.ui.swing.ext.JRootPaneEx;
import org.eclipse.scout.rt.ui.swing.window.AbstractSwingScoutViewTest;
import org.junit.Test;

/**
 * Test for {@link SwingScoutDialog}
 */
public class SwingScoutDialogUiTest extends AbstractSwingScoutViewTest {

  /**
   * Tests that has correct size after a visibility change
   * 
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   * @throws NoSuchMethodException
   */
  @Test
  public void testBoundsAfterVisibilityChange() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    JDialogExMock d = new JDialogExMock();
    createDialog(d);
    Rectangle origBounds = d.getBounds();
    reflow((JRootPaneEx) d.getRootPane());
    assertEquals(origBounds, d.getBounds());
  }

  private void createDialog(JDialogExMock d) {
    d.setPreferredSize(getLargeTestDimensions());
    d.createRootPane();
    ISwingEnvironment env = mock(ISwingEnvironment.class);
    when(env.createJDialogEx(any(Dialog.class))).thenReturn(d);
    JDialogEx parent = new JDialogEx();
    SwingScoutDialog dialog = new SwingScoutDialog(env, parent);
    dialog.adjustSize();
  }

  private class JDialogExMock extends JDialogEx {
    private static final long serialVersionUID = 1L;

    @Override
    public JRootPaneEx createRootPane() {
      return (JRootPaneEx) super.createRootPane();
    }
  }

}
