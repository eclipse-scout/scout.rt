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
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractForm5 extends AbstractForm implements IForm5 {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractForm5.class);

  public AbstractForm5() throws ProcessingException {
    super(true);
  }

  public AbstractForm5(boolean callInitializer) throws ProcessingException {
    super(callInitializer);
  }

  @Override
  protected void initConfig() throws ProcessingException {
    super.initConfig();

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

}
