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
package org.eclipse.scout.rt.ui.swing.window.desktop.menubar;

import java.util.List;

import javax.swing.JPopupMenu;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;

/**
 * MenuBar as popup
 * 
 * @author awe
 */
public class SwingScoutMainMenuPopup extends SwingScoutComposite<IDesktop> {

  private static final long serialVersionUID = 1L;

  @Override
  protected void initializeSwing() {
    JPopupMenu popupMenu = new JPopupMenu();
    setSwingField(popupMenu);
    rebuildMenu();
  }

  public JPopupMenu getSwingPopupMenu() {
    return (JPopupMenu) getSwingField();
  }

  private void rebuildMenu() {
    List<IMenu> toplevelMenus = getScoutObject().getMenus();
    JPopupMenu popupMenu = getSwingPopupMenu();
    popupMenu.removeAll();
    getSwingEnvironment().appendActions(popupMenu, toplevelMenus);
  }

}
