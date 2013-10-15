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
package org.eclipse.scout.rt.ui.swing;

import static org.junit.Assert.assertTrue;

import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.SwingUtilities;

import org.eclipse.scout.rt.ui.swing.icons.CheckboxWithMarginIcon;
import org.junit.Test;

/**
 * Tests for class {@link AbstractSwingEnvironment}.
 * 
 * @author awe
 * @since 3.10.0-M3
 */
public class AbstractSwingEnvironmentTest {

  static class P_Env extends AbstractSwingEnvironment {

    @Override
    public void init() {
      // NOP - avoid access to Activator.getDefault() in unit-test.
    }
  }

  /**
   * This test must be executed in Swing thread, because of checkThread() in the constructor of the
   * AbstractSwingEnvironment.
   */
  @Test
  public void testCreateCheckboxWithMarginIcon() throws Exception {
    SwingUtilities.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        Icon icon = new P_Env().createCheckboxWithMarginIcon(new Insets(0, 0, 0, 0));
        assertTrue("Default impl. should return CheckboxWithMarginIcon", icon instanceof CheckboxWithMarginIcon);
      }
    });
  }

}
