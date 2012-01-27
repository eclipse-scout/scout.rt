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
package org.eclipse.scout.rt.ui.swing.window;

import java.awt.Dialog;
import java.awt.Window;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swing.SwingUtility;

public final class SwingWindowManager {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingWindowManager.class);

  public static SwingWindowManager getInstance() {
    return new SwingWindowManager();
  }

  private SwingWindowManager() {
  }

  /**
   * @deprecated use {@link SwingUtility#getOwnerForChildWindow()} instead
   */
  @Deprecated
  public Window getActiveWindow() {
    return SwingUtility.getOwnerForChildWindow();
  }

  /**
   * @deprecated use {@link SwingUtility#getOwnerForChildWindow()} instead
   */
  @Deprecated
  public Dialog getActiveModalDialog() {
    Window w = getActiveWindow();
    return (w instanceof Dialog ? (Dialog) w : null);
  }

  /**
   * @deprecated no operation
   */
  @Deprecated
  public void setActiveWindow(Window w) {
  }

  /**
   * @deprecated no operation
   */
  @Deprecated
  public void pushModalDialog(Dialog modalDialog) {
  }

  /**
   * @deprecated no operation
   */
  @Deprecated
  public void popModalDialog(Dialog verifyModalDialog) {
  }
}
