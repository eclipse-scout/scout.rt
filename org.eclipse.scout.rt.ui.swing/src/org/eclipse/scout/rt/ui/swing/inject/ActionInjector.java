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
package org.eclipse.scout.rt.ui.swing.inject;

import javax.swing.JComponent;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.menu.checkbox.ICheckBoxMenu;
import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.action.ISwingScoutAction;
import org.eclipse.scout.rt.ui.swing.action.SwingScoutAction;
import org.eclipse.scout.rt.ui.swing.action.SwingScoutCheckBoxMenu;
import org.eclipse.scout.rt.ui.swing.action.SwingScoutMenu;
import org.eclipse.scout.rt.ui.swing.action.SwingScoutMenuItem;
import org.eclipse.scout.rt.ui.swing.action.SwingScoutToolTab;
import org.eclipse.scout.rt.ui.swing.action.SwingScoutViewTab;

/**
 * Factory to create a SwingScout representation of a Scout model action.
 */
public class ActionInjector {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ActionInjector.class);

  public ActionInjector() {
  }

  public ISwingScoutAction<?> inject(ISwingEnvironment env, JComponent parent, IAction action) {
    if (action instanceof IToolButton) {
      return createSwingScoutToolButton((IToolButton) action, env);
    }
    else if (action instanceof IViewButton) {
      return createSwingScoutViewButton((IViewButton) action, env);
    }
    else if (action instanceof ICheckBoxMenu) {
      ISwingScoutAction<ICheckBoxMenu> ui = new SwingScoutCheckBoxMenu<ICheckBoxMenu>();
      ui.createField((ICheckBoxMenu) action, env);
      return ui;
    }
    else if (action instanceof IActionNode) {
      IActionNode node = (IActionNode) action;
      if (node.hasChildActions()) {
        ISwingScoutAction<IActionNode> ui = new SwingScoutMenu<IActionNode>();
        ui.createField((IActionNode) action, env);
        //no recursion
        return ui;
      }
      else {
        ISwingScoutAction<IActionNode> ui = new SwingScoutMenuItem<IActionNode>();
        ui.createField((IActionNode) action, env);
        return ui;
      }
    }
    else {
      ISwingScoutAction<IAction> ui = new SwingScoutAction<IAction>();
      ui.createField((IAction) action, env);
      return ui;
    }
  }

  protected ISwingScoutAction<IToolButton> createSwingScoutToolButton(IToolButton toolButton, ISwingEnvironment env) {
    ISwingScoutAction<IToolButton> ui = new SwingScoutToolTab();
    ui.createField(toolButton, env);
    return ui;
  }

  protected ISwingScoutAction<IViewButton> createSwingScoutViewButton(IViewButton viewButton, ISwingEnvironment env) {
    ISwingScoutAction<IViewButton> ui = new SwingScoutViewTab();
    ui.createField((IViewButton) viewButton, env);
    return ui;
  }
}
