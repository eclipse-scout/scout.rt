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
import java.util.List;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.services.common.icon.IIconProviderService;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuUtility;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.internal.FormFieldContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

@ClassId("998788cf-df0f-480b-bd5a-5037805610c9")
public abstract class AbstractButton extends AbstractFormField implements IButton {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractButton.class);

  private final EventListenerList m_listenerList = new EventListenerList();
  private int m_systemType;
  private int m_displayStyle;
  private boolean m_processButton;
  private Object m_radioValue;
  private List<IMenu> m_menus;
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
  /**
   * Configures the system type of this button. See {@code IButton.SYSTEM_TYPE_* } constants for valid values.
   * System buttons are buttons with pre-defined behavior (such as an 'Ok' button or a 'Cancel' button).
   * <p>
   * Subclasses can override this method. Default is {@code IButton.SYSTEM_TYPE_NONE}.
   * 
   * @return the system type for a system button, or {@code IButton.SYSTEM_TYPE_NONE} for a non-system button
   * @see IButton
   */
  @ConfigProperty(ConfigProperty.BUTTON_SYSTEM_TYPE)
  @Order(200)
  protected int getConfiguredSystemType() {
    return SYSTEM_TYPE_NONE;
  }

  /**
   * Configures whether this button is a process button. Process buttons are typically displayed on a
   * dedicated button bar at the bottom of a form. Non-process buttons can be placed anywhere on a form.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   * 
   * @return {@code true} if this button is a process button, {@code false} otherwise
   * @see IButton
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(220)
  protected boolean getConfiguredProcessButton() {
    return true;
  }

  /**
   * Configures the display style of this button. See {@code IButton.DISPLAY_STYLE_* } constants for valid values.
   * <p>
   * Subclasses can override this method. Default is {@code IButton.DISPLAY_STYLE_DEFAULT}.
   * 
   * @return the display style of this button
   * @see IButton
   */
  @ConfigProperty(ConfigProperty.BUTTON_DISPLAY_STYLE)
  @Order(210)
  protected int getConfiguredDisplayStyle() {
    return DISPLAY_STYLE_DEFAULT;
  }

  /**
   * {@inheritDoc} Default for buttons is false because they usually should only take as much place as is needed to
   * display the button label, but not necessarily more. See also {@link #getConfiguredGridUseUiWidth()}.
   */
  @Override
  protected boolean getConfiguredFillHorizontal() {
    return false;
  }

  /**
   * {@inheritDoc} Default for buttons is false because they usually should only take as much place as is needed to
   * display the button label, but not necessarily more.
   */
  @Override
  protected boolean getConfiguredFillVertical() {
    return false;
  }

  /**
   * {@inheritDoc} Default for buttons is true because they usually should only take as much place as is needed to
   * display the button label, but not necessarily more.
   */
  @Override
  protected boolean getConfiguredGridUseUiWidth() {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean getConfiguredGridUseUiHeight() {
    return false;
  }

  /**
   * Configures the icon for this button. The icon is displayed on the button itself. Depending on UI and look and feel,
   * this button might support icon groups to represent different states of this button, such as enabled, disabled,
   * mouse-over etc.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   * 
   * @return the ID (name) of the icon
   * @see IIconGroup
   * @see IIconProviderService
   */
  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(190)
  protected String getConfiguredIconId() {
    return null;
  }

  /**
   * Called whenever this button is clicked. This button is disabled and cannot be clicked again
   * until this method returns.
   * <p>
   * Subclasses can override this method. The default does nothing.
   * 
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(190)
  protected void execClickAction() throws ProcessingException {
  }

  /**
   * Called whenever the state of a toggle button or radio button changes.</br>
   * Subclasses can override this method. The default does nothing.
   * 
   * @param selected
   *          new state of the button
   * @throws ProcessingException
   * @deprecated will be removed in release 5.0; use {@link AbstractButton#execSelectionChanged(boolean)} instead.
   */
  @Deprecated
  protected void execToggleAction(boolean selected) throws ProcessingException {
  }

  /**
   * Called whenever the selection (of toggle-button) is changed.
   * 
   * @param selection
   *          the new selection state
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(210)
  protected void execSelectionChanged(boolean selection) throws ProcessingException {
  }

  protected List<Class<? extends IMenu>> getDeclaredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IMenu>> menuClasses = ConfigurationUtility.filterClasses(dca, IMenu.class);
    List<Class<? extends IMenu>> filteredMenuClasses = ConfigurationUtility.sortFilteredClassesByOrderAnnotation(menuClasses, IMenu.class);
    List<Class<? extends IMenu>> a = ConfigurationUtility.removeReplacedClasses(filteredMenuClasses);
    return a;
  }

  @Override
  public List<IKeyStroke> getContributedKeyStrokes() {
    return MenuUtility.getKeyStrokesFromMenus(getMenus());
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setSystemType(getConfiguredSystemType());
    setDisplayStyleInternal(getConfiguredDisplayStyle());
    setProcessButton(getConfiguredProcessButton());
    setIconId(getConfiguredIconId());
    // menus
    List<IMenu> menuList = new ArrayList<IMenu>();
    for (Class<? extends IMenu> menuClazz : getDeclaredMenus()) {
      IMenu menu;
      try {
        menu = ConfigurationUtility.newInnerInstance(this, menuClazz);
        menuList.add(menu);
      }
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("error creating instance of class '" + menuClazz.getName() + "'.", t));
      }
    }
    try {
      injectMenusInternal(menuList);
    }
    catch (Exception e) {
      LOG.error("error occured while dynamically contributing menus.", e);
    }
    IContextMenu contextMenu = new FormFieldContextMenu<IButton>(this, menuList);
    contextMenu.setContainerInternal(this);
    setContextMenu(contextMenu);
  }

  @Override
  protected void initFieldInternal() throws ProcessingException {
    super.initFieldInternal();
    // init actions
    ActionUtility.initActions(getMenus());
  }

  /**
   * Override this internal method only in order to make use of dynamic menus<br>
   * Used to manage menu list and add/remove menus
   * 
   * @param menuList
   *          live and mutable list of configured menus
   */
  protected void injectMenusInternal(List<IMenu> menuList) {
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
  public List<IMenu> getMenus() {
    return getContextMenu().getChildActions();
  }

  protected void setContextMenu(IContextMenu contextMenu) {
    propertySupport.setProperty(PROP_CONTEXT_MENU, contextMenu);
  }

  @Override
  public IContextMenu getContextMenu() {
    return (IContextMenu) propertySupport.getProperty(PROP_CONTEXT_MENU);
  }

  @Override
  public void doClick() throws ProcessingException {
    if (isEnabled() && isVisible() && isEnabledProcessingButton()) {
      try {
        setEnabledProcessingButton(false);

        fireButtonClicked();
        execClickAction();
      }
      finally {
        setEnabledProcessingButton(true);
      }
    }
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
        execSelectionChanged(b);
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
