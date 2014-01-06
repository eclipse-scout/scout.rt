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
package org.eclipse.scout.rt.ui.rap.action;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.checkbox.ICheckBoxMenu;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * @since 3.10.0-M5
 */
public class MenuFactory {
  private boolean m_addKeyStrokeTextEnabled;

  public MenuFactory() {
    m_addKeyStrokeTextEnabled = true;
  }

  public void fillContextMenu(Menu menu, IMenu[] scoutMenus, IRwtEnvironment uiEnvironment) {
    if (scoutMenus == null || scoutMenus.length == 0) {
      menu.setVisible(false);
      return;
    }

    List<IActionNode> scoutActionNodes = new LinkedList<IActionNode>();
    for (IMenu scoutMenu : scoutMenus) {
      scoutActionNodes.add(scoutMenu);
    }

    fillContextMenu(menu, scoutActionNodes, uiEnvironment);
  }

  public void fillContextMenu(Menu menu, List<? extends IActionNode> scoutActionNodes, IRwtEnvironment uiEnvironment) {
    if (scoutActionNodes == null || scoutActionNodes.size() == 0) {
      menu.setVisible(false);
      return;
    }

    List<IActionNode> cleanedScoutActions = RwtMenuUtility.cleanup(scoutActionNodes);
    for (IActionNode scoutActionNode : cleanedScoutActions) {
      fillContextMenuRec(menu, scoutActionNode, uiEnvironment);
    }

  }

  private void fillContextMenuRec(Menu menu, IActionNode<?> scoutActionNode, IRwtEnvironment uiEnvironment) {
    if (!scoutActionNode.isVisible()) {
      return;
    }
    if (scoutActionNode.isSeparator()) {
      new MenuItem(menu, SWT.SEPARATOR);
    }
    else if (scoutActionNode instanceof ICheckBoxMenu) {
      createCheckBoxMenuAction(menu, scoutActionNode, uiEnvironment);
    }
    else if (scoutActionNode.getChildActionCount() > 0) {
      AbstractRwtMenuAction group = createMenuGroup(menu, scoutActionNode, uiEnvironment);
      Menu subMenu = new Menu(menu);
      group.getUiMenuItem().setMenu(subMenu);
      List<IActionNode> childActions = RwtMenuUtility.cleanup(scoutActionNode.getChildActions());
      for (IActionNode<?> subAction : childActions) {
        fillContextMenuRec(subMenu, subAction, uiEnvironment);
      }
    }
    else {
      createMenuAction(menu, scoutActionNode, uiEnvironment);
    }
  }

  protected AbstractRwtMenuAction createMenuGroup(Menu menu, IActionNode<?> scoutActionNode, IRwtEnvironment uiEnvironment) {
    RwtScoutMenuGroup group = new RwtScoutMenuGroup(menu, scoutActionNode, uiEnvironment, false);
    group.setAddKeyStrokeTextEnabled(isAddKeyStrokeTextEnabled());
    group.init();
    return group;
  }

  protected AbstractRwtMenuAction createCheckBoxMenuAction(Menu menu, IActionNode<?> scoutActionNode, IRwtEnvironment uiEnvironment) {
    RwtScoutCheckboxMenu action = new RwtScoutCheckboxMenu(menu, (ICheckBoxMenu) scoutActionNode, uiEnvironment, false);
    action.setAddKeyStrokeTextEnabled(isAddKeyStrokeTextEnabled());
    action.init();
    return action;
  }

  protected AbstractRwtMenuAction createMenuAction(Menu menu, IActionNode<?> scoutActionNode, IRwtEnvironment uiEnvironment) {
    RwtScoutMenuAction action = new RwtScoutMenuAction(menu, scoutActionNode, uiEnvironment, false);
    action.setAddKeyStrokeTextEnabled(isAddKeyStrokeTextEnabled());
    action.init();
    return action;
  }

  public boolean isAddKeyStrokeTextEnabled() {
    return m_addKeyStrokeTextEnabled;
  }

  public void setAddKeyStrokeTextEnabled(boolean addKeyStrokeTextEnabled) {
    m_addKeyStrokeTextEnabled = addKeyStrokeTextEnabled;
  }

}
