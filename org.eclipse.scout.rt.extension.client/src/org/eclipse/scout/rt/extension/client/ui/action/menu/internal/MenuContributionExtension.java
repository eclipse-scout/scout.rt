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

import org.eclipse.scout.commons.BeanUtility;
import org.eclipse.scout.commons.NumberUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.extension.client.ui.action.menu.IMenuExtensionFilter;

/**
 * @since 3.9.0
 */
public class MenuContributionExtension extends AbstractMenuExtension {

  private final double m_order;

  public MenuContributionExtension(Class<? extends IMenu> menuClass, IMenuExtensionFilter filter, Double order) {
    super(menuClass, filter);
    m_order = NumberUtility.nvl(order, Double.MAX_VALUE);
  }

  public double getOrder() {
    return m_order;
  }

  public IMenu createContribution(Object anchor, Object container) throws ProcessingException {
    if (anchor == null || container == null) {
      throw new IllegalArgumentException("anchor or container must not be null");
    }
    if (anchor != container) {
      // 1. anchor and container are not the same
      // 1.a try with anchor and container parameter
      IMenu menu = BeanUtility.createInstance(getMenuClass(), anchor, container);
      if (menu != null) {
        return menu;
      }
      // 1.b try with container and anchor parameter
      menu = BeanUtility.createInstance(getMenuClass(), container, anchor);
      if (menu != null) {
        return menu;
      }
      // 1.c try with container parameter
      menu = BeanUtility.createInstance(getMenuClass(), container);
      if (menu != null) {
        return menu;
      }
    }
    // 2. try with anchor parameter
    IMenu menu = BeanUtility.createInstance(getMenuClass(), anchor);
    if (menu != null) {
      return menu;
    }
    if (anchor == container) {
      // 3. anchor and container are the same, also try <anchor, anchor> constructor
      menu = BeanUtility.createInstance(getMenuClass(), anchor, anchor);
      if (menu != null) {
        return menu;
      }
    }
    // 4. try default constructor
    menu = BeanUtility.createInstance(getMenuClass());
    if (menu != null) {
      return menu;
    }
    throw new ProcessingException("Cannot create new instance of class [" + getMenuClass() + "] with argument anchor=[" + anchor + "], container=[" + container + "]");
  }
}
