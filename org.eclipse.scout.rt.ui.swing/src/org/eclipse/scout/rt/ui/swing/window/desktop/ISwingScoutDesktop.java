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

import javax.swing.JDesktopPane;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.swing.basic.ISwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.ext.JInternalFrameEx;

public interface ISwingScoutDesktop extends ISwingScoutComposite<IDesktop> {

  JDesktopPane getSwingDesktopPane();

  void addView(JInternalFrameEx f, Object constraints);

  void removeView(JInternalFrameEx f);
}
