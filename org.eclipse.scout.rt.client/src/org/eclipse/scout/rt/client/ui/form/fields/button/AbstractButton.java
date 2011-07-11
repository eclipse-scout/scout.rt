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
package org.eclipse.scout.rt.client.ui.form.fields.button;

import java.util.ArrayList;
import java.util.EventListener;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractButton extends AbstractFormField implements IButton {
  private final EventListenerList m_listenerList = new EventListenerList();
  private int m_systemType;
  private int m_displayStyle;
  private boolean m_processButton;
  private Object m_radioValue;
  private IMenu[] m_menus;
  private final IButtonUIFacade m_uiFacade;
  private final OptimisticLock m_uiFacadeSetSelectedLock;

  public AbstractButton() {
    this(true);
  }

  public AbstractButton(boolean callInitializer) {
    super(false);
    m_uiFacade = new P_UIFacade();
    m_uiFacadeSetSelectedLock = new OptimisticLock();
    if (callInitializer) {
      callInitializer();
    }
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.BUTTON_SYSTEM_TYPE)
  @Order(200)
  @ConfigPropertyValue("SYSTEM_TYPE_NONE")
  protected int getConfiguredSystemType() {
    return SYSTEM_TYPE_NONE;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(220)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredProcessButton() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BUTTON_DISPLAY_STYLE)
  @Order(210)
  @ConfigPropertyValue("DISPLAY_STYLE_DEFAULT")
  protected int getConfiguredDisplayStyle() {
    return DISPLAY_STYLE_DEFAULT;
  }

  @ConfigPropertyValue("false")
  @Override
  protected boolean getConfiguredFillHorizontal() {
    return false;
  }

  @ConfigPropertyValue("false")
  @Override
  protected boolean getConfiguredFillVertical() {
    return false;
  }

  @ConfigPropertyValue("true")
  @Override
  protected boolean getConfiguredGridUseUiWidth() {
    return true;
  }

  @ConfigPropertyValue("false")
  @Override
  protected boolean getConfiguredGridUseUiHeight() {
    return false;
  }

  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(230)
  @ConfigPropertyValue("null")
  protected Object getConfiguredRadioValue() {
    return null;
  }

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(190)
  @ConfigPropertyValue("null")
  protected String getConfiguredIconId() {
    return null;
  }

  @ConfigOperation
  @Order(190)
  protected void execClickAction() throws ProcessingException {
  }

  @ConfigOperation
  @Order(200)
  protected void execToggleAction(boolean selected) throws ProcessingException {

  }

  private Class<? extends IMenu>[] getConfiguredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.sortFilteredClassesByOrderAnnotation(dca, IMenu.class);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setSystemType(getConfiguredSystemType());
    setDisplayStyleInternal(getConfiguredDisplayStyle());
    setProcessButton(getConfiguredProcessButton());
    setIconId(getConfiguredIconId());
    setRadioValue(getConfiguredRadioValue());
    // menus
    ArrayList<IMenu> menuList = new ArrayList<IMenu>();
    Class<? extends IMenu>[] menuArray = getConfiguredMenus();
    for (int i = 0; i < menuArray.length; i++) {
      IMenu menu;
      try {
        menu = ConfigurationUtility.newInnerInstance(this, menuArray[i]);
        menuList.add(menu);
      }
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("menu: " + menuArray[i].getName(), t));
      }
    }
    m_menus = menuList.toArray(new IMenu[0]);
  }

  /*
   * Properties
   */
  @Override
  public String getIconId() {
    return propertySupport.getPropertyString(PROP_ICON_ID);
  }

  @Override
  public void setIconId(String iconId) {
    propertySupport.setPropertyString(PROP_ICON_ID, iconId);
  }

  @Override
  public Object getImage() {
    return propertySupport.getProperty(PROP_IMAGE);
  }

  @Override
  public void setImage(Object nativeImg) {
    propertySupport.setProperty(PROP_IMAGE, nativeImg);
  }

  @Override
  public int getSystemType() {
    return m_systemType;
  }

  @Override
  public void setSystemType(int systemType) {
    m_systemType = systemType;
  }

  @Override
  public boolean isProcessButton() {
    return m_processButton;
  }

  @Override
  public void setProcessButton(boolean on) {
    m_processButton = on;
  }

  @Override
  public boolean hasMenus() {
    return m_menus.length > 0;
  }

  @Override
  public IMenu[] getMenus() {
    return m_menus;
  }

  @Override
  public void doClick() throws ProcessingException {
    if (isEnabled() && isVisible()) {
      fireButtonClicked();
      execClickAction();
    }
  }

  /*
   * Radio Buttons
   */
  @Override
  public Object getRadioValue() {
    return m_radioValue;
  }

  @Override
  public void setRadioValue(Object o) {
    m_radioValue = o;
  }

  /**
   * Toggle and Radio Buttons
   */
  @Override
  public boolean isSelected() {
    return propertySupport.getPropertyBool(PROP_SELECTED);
  }

  /**
   * Toggle and Radio Buttons
   */
  @Override
  public void setSelected(boolean b) {
    boolean changed = propertySupport.setPropertyBool(PROP_SELECTED, b);
    // single observer for config
    if (changed) {
      try {
        execToggleAction(b);
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }
  }

  @Override
  public void disarm() {
    fireDisarmButton();
  }

  @Override
  public void requestPopup() {
    fireRequestPopup();
  }

  @Override
  public int getDisplayStyle() {
    return m_displayStyle;
  }

  @Override
  public void setDisplayStyleInternal(int i) {
    m_displayStyle = i;
  }

  @Override
  public IButtonUIFacade getUIFacade() {
    return m_uiFacade;
  }

  /**
   * Model Observer
   */
  @Override
  public void addButtonListener(ButtonListener listener) {
    m_listenerList.add(ButtonListener.class, listener);
  }

  @Override
  public void removeButtonListener(ButtonListener listener) {
    m_listenerList.remove(ButtonListener.class, listener);
  }

  private void fireButtonClicked() {
    fireButtonEvent(new ButtonEvent(this, ButtonEvent.TYPE_CLICKED));
  }

  private void fireDisarmButton() {
    fireButtonEvent(new ButtonEvent(this, ButtonEvent.TYPE_DISARM));
  }

  private void fireRequestPopup() {
    fireButtonEvent(new ButtonEvent(this, ButtonEvent.TYPE_REQUEST_POPUP));
  }

  private IMenu[] fireButtonPopup() {
    ButtonEvent e = new ButtonEvent(this, ButtonEvent.TYPE_POPUP);
    // single observer add our menus
    IMenu[] a = getMenus();
    for (int i = 0; i < a.length; i++) {
      IMenu m = a[i];
      m.prepareAction();
      if (m.isVisible()) {
        e.addPopupMenu(m);
      }
    }
    fireButtonEvent(e);
    return e.getPopupMenus();
  }

  // main handler
  private void fireButtonEvent(ButtonEvent e) {
    EventListener[] listeners = m_listenerList.getListeners(ButtonListener.class);
    if (listeners != null && listeners.length > 0) {
      for (int i = 0; i < listeners.length; i++) {
        ((ButtonListener) listeners[i]).buttonChanged(e);
      }
    }
  }

  /**
   * Default implementation for buttons
   */
  private class P_UIFacade implements IButtonUIFacade {
    @Override
    public void fireButtonClickedFromUI() {
      try {
        if (isEnabled() && isVisible()) {
          doClick();
        }
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }

    @Override
    public IMenu[] fireButtonPopupFromUI() {
      return fireButtonPopup();
    }

    /**
     * Toggle and Radio Buttons
     */
    @Override
    public void setSelectedFromUI(boolean b) {
      try {
        /*
         * Ticket 76711: added optimistic lock
         */
        if (m_uiFacadeSetSelectedLock.acquire()) {
          setSelected(b);
        }
      }
      finally {
        m_uiFacadeSetSelectedLock.release();
      }
    }
  }
}
