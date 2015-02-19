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

import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.IActionFilter;
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
  public void inject(ISwingEnvironment env, JComponent parent, List<? extends IAction> actions, IActionFilter filter) {
    List<? extends IAction> normalizedActions = ActionUtility.normalizedActions(actions, filter);
    for (IAction a : normalizedActions) {
      if (a.isSeparator()) {
        if (parent instanceof JPopupMenu) {
          ((JPopupMenu) parent).addSeparator();
        }
        else if (parent instanceof JMenu) {
          ((JMenu) parent).addSeparator();
        }
        else {
          LOG.warn("invalid container for separator: " + parent);
        }
      }
      else {
        ISwingScoutAction<IAction> menuComposite = env.createAction(parent, a, filter);
        JComponent child = menuComposite.getSwingField();
        parent.add(child);
        if (a instanceof IActionNode && ((IActionNode) a).hasChildActions()) {
          inject(env, child, ((IActionNode<? extends IAction>) a).getChildActions(), filter);
        }
      }
    }
  }
}
