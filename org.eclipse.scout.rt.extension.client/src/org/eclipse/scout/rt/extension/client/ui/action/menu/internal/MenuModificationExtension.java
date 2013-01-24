/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client.ui.action.menu.internal;

import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.extension.client.ui.action.menu.IMenuExtensionFilter;
import org.eclipse.scout.rt.extension.client.ui.action.menu.IMenuModifier;

/**
 * @since 3.9.0
 */
public class MenuModificationExtension extends AbstractMenuExtension {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MenuModificationExtension.class);

  private final Class<? extends IMenuModifier<? extends IMenu>> m_menuModifier;

  public MenuModificationExtension(Class<? extends IMenu> menuClass, IMenuExtensionFilter filter, Class<? extends IMenuModifier<? extends IMenu>> menuModifier) {
    super(menuClass, filter);
    if (menuModifier == null) {
      throw new IllegalArgumentException("menuModifier must not be null");
    }
    // check assignability of given menu class along with the type parameter defined on the menu modifier
    Class<?> menuModifierMenuType = TypeCastUtility.getGenericsParameterClass(menuModifier, IMenuModifier.class);
    if (menuModifierMenuType == null) {
      LOG.warn("could not determine generic type parameter of menu modifier '" + menuModifier.getName() + ";");
    }
    else if (!menuModifierMenuType.isAssignableFrom(menuClass)) {
      throw new IllegalArgumentException("menuClass must be assignalbe to the generic type of given menuModifier. [menuClass: '"
          + menuClass.getName() + "', generic type on menuModifier: '" + menuModifierMenuType.getName() + "'");
    }
    m_menuModifier = menuModifier;
  }

  public Class<? extends IMenuModifier<? extends IMenu>> getMenuModifier() {
    return m_menuModifier;
  }

  @Override
  public boolean accept(Object anchor, Object container, IMenu menu) {
    if (!getMenuClass().isInstance(menu)) {
      return false;
    }
    return super.accept(anchor, container, menu);
  }

  @SuppressWarnings("unchecked")
  public <T extends IMenu> IMenuModifier<T> createMenuModifier() throws ProcessingException {
    try {
      return (IMenuModifier<T>) m_menuModifier.newInstance();
    }
    catch (Exception e) {
      throw new ProcessingException("Error while instantiating menu modifier", e);
    }
  }
}
