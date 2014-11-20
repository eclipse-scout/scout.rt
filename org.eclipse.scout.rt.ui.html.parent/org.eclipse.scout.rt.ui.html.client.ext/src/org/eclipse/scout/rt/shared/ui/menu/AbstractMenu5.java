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
package org.eclipse.scout.rt.shared.ui.menu;

import java.util.EventListener;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;

public abstract class AbstractMenu5 extends AbstractMenu implements IMenu5 {
  private final EventListenerList m_listenerList = new EventListenerList();
  private int m_systemType;
  private boolean m_default;

  public AbstractMenu5() {
    super(true);
  }

  public AbstractMenu5(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setSystemType(getConfiguredSystemType());
    setDefault(getConfiguredIsDefault());
  }

  @Override
  public int getSystemType() {
    return m_systemType;
  }

  @Override
  public void setSystemType(int systemType) {
    m_systemType = systemType;
  }

  /**
   * Configures the system type of this menu. See {@code IMenu.SYSTEM_TYPE_* } constants for valid values.
   * System menus are menus with pre-defined behavior (such as an 'Ok' menu or a 'Cancel' menu).
   * <p>
   * Subclasses can override this method. Default is {@code IMenu.SYSTEM_TYPE_NONE}.
   *
   * @return the system type for a system menu, or {@code IMenu.SYSTEM_TYPE_NONE} for a non-system menus
   * @see IMenu
   */
  @ConfigProperty(ConfigProperty.BUTTON_SYSTEM_TYPE)
  // TODO change to menu system type
  @Order(200)
  protected int getConfiguredSystemType() {
    return SYSTEM_TYPE_NONE;
  }

  @Override
  public void doAction() throws ProcessingException {
    if (isEnabled() && isVisible()) {
      try {
        setEnabledProcessingAction(false);
        doActionInternal();
        fireActionPerformed();
      }
      finally {
        setEnabledProcessingAction(true);
      }
    }
  }

  @Override
  public void addActionListener(ActionListener listener) {
    m_listenerList.add(ActionListener.class, listener);
  }

  @Override
  public void removeActionListener(ActionListener listener) {
    m_listenerList.remove(ActionListener.class, listener);
  }

  private void fireActionPerformed() {
    fireActionEvent(new ActionEvent(this, ActionEvent.TYPE_PERFORMED));
  }

  private void fireActionEvent(ActionEvent e) {
    EventListener[] listeners = m_listenerList.getListeners(ActionListener.class);
    if (listeners != null && listeners.length > 0) {
      for (int i = 0; i < listeners.length; i++) {
        ((ActionListener) listeners[i]).actionChanged(e);
      }
    }
  }

  //FIXME CGU maybe it would be better to move the property to the groupbox because only one default button is possible (as it is done for the default menu in abstractTable)
  @Override
  public boolean isDefault() {
    return m_default;
  }

  @Override
  public void setDefault(boolean isDefault) {
    m_default = isDefault;
  }

  protected boolean getConfiguredIsDefault() {
    return false;
  }

}
