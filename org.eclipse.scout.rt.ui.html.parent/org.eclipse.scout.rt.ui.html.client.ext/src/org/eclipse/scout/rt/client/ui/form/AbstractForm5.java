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
package org.eclipse.scout.rt.client.ui.form;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.IActionVisitor;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.ButtonEvent;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.ui.menu.ActionEvent;
import org.eclipse.scout.rt.shared.ui.menu.ActionListener;
import org.eclipse.scout.rt.shared.ui.menu.IMenu5;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractForm5 extends AbstractForm implements IForm5 {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractForm5.class);
  private P_SystemMenuListener m_systemMenuListener;
  private HashSet<Integer> m_enabledSystemTypes;

  public AbstractForm5() throws ProcessingException {
    super(true);
  }

  public AbstractForm5(boolean callInitializer) throws ProcessingException {
    super(callInitializer);
  }

  @Override
  protected void initConfig() throws ProcessingException {
    super.initConfig();
    m_enabledSystemTypes = new HashSet<Integer>();

    // menus
    List<Class<? extends IMenu>> declaredMenus = getDeclaredMenus();
    List<IMenu> menuList = new ArrayList<IMenu>(declaredMenus.size() + 4);
    for (Class<? extends IMenu> menuClazz : declaredMenus) {
      try {
        menuList.add(ConfigurationUtility.newInnerInstance(this, menuClazz));
      }
      catch (Exception e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("error creating instance of class '" + menuClazz.getName() + "'.", e));
      }
    }
    try {
      injectMenusInternal(menuList);
    }
    catch (Exception e) {
      LOG.error("error occured while dynamically contributing menus.", e);
    }

    m_systemMenuListener = new P_SystemMenuListener();
    for (IMenu menu : menuList) {
      if (menu instanceof IMenu5) {
        IMenu5 menu5 = (IMenu5) menu;
        if (menu5.getSystemType() != IMenu5.SYSTEM_TYPE_NONE) {
          if (menu5.isEnabled() && menu5.isVisible() && menu5.isEnabledProcessingAction()) {
            m_enabledSystemTypes.add(menu5.getSystemType());
          }
          menu5.addActionListener(m_systemMenuListener);
        }
      }
    }

    //set container on menus
    IFormContextMenu contextMenu = new FormContextMenu(this, menuList);
    contextMenu.setContainerInternal(this);
    setContextMenu(contextMenu);
  }

  protected List<Class<? extends IMenu>> getDeclaredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IMenu>> filtered = ConfigurationUtility.filterClasses(dca, IMenu.class);
    List<Class<? extends IMenu>> foca = ConfigurationUtility.sortFilteredClassesByOrderAnnotation(filtered, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(foca);
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

  @Override
  public List<IMenu> getMenus() {
    return getContextMenu().getChildActions();
  }

  public <T extends IMenu> T getMenu(final Class<T> menuType) throws ProcessingException {
    IContextMenu contextMenu = getContextMenu();
    if (contextMenu != null) {
      final Holder<T> resultHolder = new Holder<T>();
      contextMenu.acceptVisitor(new IActionVisitor() {

        @SuppressWarnings("unchecked")
        @Override
        public int visit(IAction action) {
          if (menuType.isAssignableFrom(action.getClass())) {
            resultHolder.setValue((T) action);
            return CANCEL;
          }
          return CONTINUE;
        }
      });
      return resultHolder.getValue();
    }
    return null;
  }

  protected void setContextMenu(IFormContextMenu contextMenu) {
    propertySupport.setProperty(PROP_CONTEXT_MENU, contextMenu);
  }

  @Override
  public IFormContextMenu getContextMenu() {
    return (IFormContextMenu) propertySupport.getProperty(PROP_CONTEXT_MENU);
  }

  @Override
  protected void execOnCloseRequest(boolean kill, HashSet<Integer> enabledButtonSystemTypes) throws ProcessingException {
    super.execOnCloseRequest(kill, m_enabledSystemTypes);
  }

  @Override
  protected void handleSystemButtonEventInternal(ButtonEvent e) {
    super.handleSystemButtonEventInternal(e);
  }

  private class P_SystemMenuListener implements ActionListener {
    @Override
    public void actionChanged(ActionEvent e) {
      final IMenu5 action = (IMenu5) e.getAction();

      // auto-detaching
      if (m_systemMenuListener != this) {
        ((IMenu5) e.getSource()).removeActionListener(this);
        return;
      }

      IButton dummyButton = new AbstractButton() {
        @Override
        protected int getConfiguredSystemType() {
          return action.getSystemType();
        }
      };
      handleSystemButtonEventInternal(new ButtonEvent(dummyButton, ButtonEvent.TYPE_CLICKED));

    }
  }// end private class

}
