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
package org.eclipse.scout.rt.ui.swing.window.desktop;

import java.awt.Frame;

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.swing.basic.ISwingScoutComposite;

public interface ISwingScoutRootFrame extends ISwingScoutComposite<IDesktop> {

  Frame getSwingFrame();

  ISwingScoutDesktop getDesktopComposite();

  /**
   * start GUI process by presenting a desktop frame use this method to show the
   * dialog, not getSwingFrame().setVisible()
   */
  void showSwingFrame();

  /**
   * set a status on the root frame (may be delegated to a tray icon)
   */
  void setSwingStatus(IProcessingStatus newStatus);
}
