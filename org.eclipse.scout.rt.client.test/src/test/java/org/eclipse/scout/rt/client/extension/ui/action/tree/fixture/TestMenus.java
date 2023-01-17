/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.action.tree.fixture;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.action.tree.MoveActionNodesHandler;
import org.eclipse.scout.rt.client.ui.action.ActionFinder;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;

public class TestMenus {

  private List<IMenu> m_menus;

  public TestMenus() {
    OrderedCollection<IMenu> menus = new OrderedCollection<>();
    for (Class<? extends IMenu> menuClazz : getConfiguredMenus()) {
      menus.addOrdered(ConfigurationUtility.newInnerInstance(this, menuClazz));
    }
    new MoveActionNodesHandler<>(menus).moveModelObjects();
    m_menus = menus.getOrderedList();
  }

  private List<Class<? extends IMenu>> getConfiguredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IMenu>> fca = ConfigurationUtility.filterClasses(dca, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(fca);
  }

  public List<IMenu> getMenus() {
    return CollectionUtility.arrayList(m_menus);
  }

  public <T extends IMenu> T getMenu(Class<? extends T> searchType) {
    return new ActionFinder().findAction(getMenus(), searchType);
  }

  @Order(10)
  public class Top1Menu extends AbstractMenu {

    @Order(5)
    public class Sub1Top1Menu extends AbstractMenu {

      @Order(10)
      public class Sub1Sub1Top1Menu extends AbstractMenu {
      }
    }

    @Order(20)
    public class Sub2Top1Menu extends AbstractMenu {
    }
  }

  @Order(20)
  public class Top2Menu extends AbstractMenu {
    @Order(10)
    public class Sub1Top2Menu extends AbstractMenu {
    }

    @Order(20)
    public class Sub2Top2Menu extends AbstractMenu {
    }
  }
}
