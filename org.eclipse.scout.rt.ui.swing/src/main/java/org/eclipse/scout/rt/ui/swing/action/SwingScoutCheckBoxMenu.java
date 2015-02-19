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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;

public class SwingScoutCheckBoxMenu<T extends IMenu> extends SwingScoutComposite<T> implements ISwingScoutAction<T> {
  private SwingScoutAction<T> m_actionComposite;

  @Override
  protected void initializeSwing() {
    super.initializeSwing();
    m_actionComposite = new SwingScoutAction<T>();
    m_actionComposite.createField(getScoutObject(), getSwingEnvironment());
    JCheckBoxMenuItem swingItem = new JCheckBoxMenuItem(m_actionComposite.getSwingAction());
    swingItem.addItemListener(new P_SwingSelectionListener());
    swingItem.setOpaque(false);
    setSwingField(swingItem);
    /**
     * WORKAROUND swing doesn't know "visible" property on action objects, but it
     * knows "enabled" property and others... therefore add a transfer listener to
     * pass this properties from a swing action to a swing item
     */
    swingItem.setVisible(getScoutObject().isVisible());
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
  protected void handleSwingRemoveNotify() {
    super.handleSwingRemoveNotify();
    m_actionComposite.disposeAction();
  }

  @Override
  public JCheckBoxMenuItem getSwingField() {
    return (JCheckBoxMenuItem) super.getSwingField();
  }

  @Override
  public Action getSwingAction() {
    return m_actionComposite.getSwingAction();
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    IMenu scoutAction = getScoutObject();
    setSelectedFromScout(scoutAction.isSelected());
  }

  private void setSelectedFromScout(boolean b) {
    getSwingField().setSelected(b);
  }

  protected void setSelectionFromSwing(final boolean selected) {
    if (getUpdateSwingFromScoutLock().isAcquired()) {
      return;
    }
    if (getScoutObject().isSelected() != selected) {
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().setSelectedFromUI(selected);
        }
      };
      getSwingEnvironment().invokeScoutLater(t, 0);
    }
  }

  /**
   * in swing thread
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IMenu.PROP_SELECTED)) {
      setSelectedFromScout(((Boolean) newValue).booleanValue());
    }
  }

  private class P_SwingSelectionListener implements ItemListener {

    @Override
    public void itemStateChanged(ItemEvent e) {
      setSelectionFromSwing(getSwingField().isSelected());
    }
  }
}
