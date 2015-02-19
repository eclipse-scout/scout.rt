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
package org.eclipse.scout.rt.ui.swing.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;

import org.eclipse.scout.rt.client.ui.action.IActionFilter;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;

public class SwingScoutMenu<T extends IActionNode<?>> extends SwingScoutComposite<T> implements ISwingScoutAction<T> {
  private SwingScoutAction<T> m_actionComposite;
  private IActionFilter m_filter;

  public SwingScoutMenu(IActionFilter filter) {
    super();
    m_filter = filter;

  }

  @Override
  protected void initializeSwing() {
    super.initializeSwing();
    m_actionComposite = new SwingScoutAction<T>();
    m_actionComposite.createField(getScoutObject(), getSwingEnvironment());
    JMenu swingMenu = new JMenu(m_actionComposite.getSwingAction());
    setSwingField(swingMenu);
    /**
     * WORKAROUND swing doesn't know "visible" property on action objects, but it
     * knows "enabled" property and others... therefore add a transfer listener to
     * pass this properties from a swing action to a swing item
     */
    swingMenu.setVisible(getScoutObject().isVisible());
    m_actionComposite.getSwingAction().addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        if ("visible".equals(e.getPropertyName())) {
          JComponent c = getSwingField();
          if (c != null) {
            c.setVisible(((Boolean) e.getNewValue()).booleanValue());
          }
        }
      }
    });
  }

  @Override
  public Action getSwingAction() {
    return m_actionComposite.getSwingAction();
  }

  @Override
  protected void handleSwingRemoveNotify() {
    super.handleSwingRemoveNotify();
    m_actionComposite.disposeAction();
  }

  @Override
  public JMenu getSwingField() {
    return (JMenu) super.getSwingField();
  }

  public SwingScoutAction getActionComposite() {
    return m_actionComposite;
  }

  protected void updateChildActionsFromScout() {
    JComponent c = getSwingField();
    if (c != null) {
      c.removeAll();
      getSwingEnvironment().appendActions(c, getScoutObject().getChildActions(), m_filter);
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (IActionNode.PROP_CHILD_ACTIONS.equals(name)) {
      updateChildActionsFromScout();
    }
  }

}
