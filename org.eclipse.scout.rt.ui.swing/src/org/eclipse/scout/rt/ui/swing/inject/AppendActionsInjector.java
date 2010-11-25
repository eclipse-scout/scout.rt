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

import java.awt.Component;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.action.ISwingScoutAction;

/**
 *
 */
public class AppendActionsInjector {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AppendActionsInjector.class);

  public AppendActionsInjector() {
  }

  @SuppressWarnings("unchecked")
  public void inject(ISwingEnvironment env, JComponent parent, List<? extends IAction> actions) {
    boolean lastActionWasSeparator = true;// don't start with a separator
    for (IAction a : actions) {
      if (a.isSeparator()) {
        if (!lastActionWasSeparator) {
          if (a.isVisible()) {
            if (parent instanceof JPopupMenu) ((JPopupMenu) parent).addSeparator();
            else if (parent instanceof JMenu) ((JMenu) parent).addSeparator();
            else {
              LOG.warn("invalid container for separator: " + parent);
            }
            lastActionWasSeparator = true;
          }
        }
      }
      else {
        if (a.isVisible()) {
          lastActionWasSeparator = false;
        }
        ISwingScoutAction<IAction> menuComposite = env.createAction(parent, a);
        JComponent child = menuComposite.getSwingField();
        parent.add(child);
        if (a instanceof IActionNode && ((IActionNode) a).hasChildActions()) {
          inject(env, child, ((IActionNode<? extends IAction>) a).getChildActions());
        }
      }
    }
    // remove trailing separators
    if (parent instanceof JPopupMenu || parent instanceof JMenu) {
      Component[] menus = null;
      if (parent instanceof JPopupMenu) {
        menus = ((JPopupMenu) parent).getComponents();
      }
      else if (parent instanceof JMenu) {
        menus = ((JMenu) parent).getMenuComponents();
      }
      if (menus != null) {
        for (int i = menus.length - 1; i >= 0; i--) {
          if (menus[i] instanceof JPopupMenu.Separator || menus[i] instanceof JToolBar.Separator) {
            parent.remove(menus[i]);
          }
          else if (menus[i] instanceof JMenuItem && !((JMenuItem) menus[i]).isVisible()) {
            // nop
          }
          else {
            break;
          }
        }
      }
    }
  }

}
