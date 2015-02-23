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
package org.eclipse.scout.rt.ui.swt.window.desktop.tray;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.swt.basic.ISwtScoutComposite;
import org.eclipse.swt.widgets.TrayItem;

public interface ISwtScoutTray extends ISwtScoutComposite<IDesktop> {

  TrayItem getSwtTrayItem();

  void disposeTray();

}
