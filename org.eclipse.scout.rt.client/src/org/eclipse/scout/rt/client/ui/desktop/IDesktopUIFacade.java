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
package org.eclipse.scout.rt.client.ui.desktop;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;

/**
 * The desktop model (may) consist of
 * <ul>
 * <li>set of available outline
 * <li>active outline
 * <li>active tableview
 * <li>active detail form
 * <li>active search form
 * <li>form stack (swing: dialogs on desktop as JInternalFrames; eclipse: editors or views)
 * <li>dialog stack of model and non-modal dialogs (swing: dialogs as JDialog, JFrame; eclipse: dialogs in a new Shell)
 * <li>active message box stack
 * </ul>
 */
public interface IDesktopUIFacade {

  /**
   * GUI fires this event as soon as the desktop was completely setup and
   * displayed
   */
  void fireDesktopOpenedFromUI();

  /**
   * GUI fires this event when it is in the process of reseting the workbench /
   * application
   */
  void fireDesktopResetFromUI();

  /**
   * GUI fires this event when it is in the process of closing the workbench /
   * application
   */
  void fireDesktopClosingFromUI();

  /**
   * GUI fires this event as soon as a gui is available
   */
  void fireGuiAttached();

  /**
   * GUI fires this event when the application/workbench is closing
   */
  void fireGuiDetached();

  /**
   * GUI fires this event to collect the tray popup menus
   */
  IMenu[] fireTrayPopupFromUI();
}
