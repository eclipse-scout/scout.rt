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
package org.eclipse.scout.rt.extension.client.ui.action.menu;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.extension.client.Activator;
import org.eclipse.scout.rt.extension.client.ui.action.menu.internal.MenuContributionExtension;
import org.eclipse.scout.rt.extension.client.ui.action.menu.internal.MenuExtensionManager;
import org.eclipse.scout.rt.extension.client.ui.action.menu.internal.MenuModificationExtension;
import org.eclipse.scout.rt.extension.client.ui.action.menu.internal.MenuRemoveExtension;
import org.eclipse.scout.rt.extension.client.ui.basic.table.AbstractExtensibleTable;
import org.eclipse.scout.rt.extension.client.ui.desktop.internal.OrderedMenuWrapper;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.AbstractExtensiblePageWithNodes;
import org.eclipse.scout.rt.extension.client.ui.form.fields.button.AbstractExtensibleButton;
import org.eclipse.scout.rt.extension.client.ui.form.fields.filechooserfield.AbstractExtensibleFileChooserField;
import org.eclipse.scout.rt.extension.client.ui.form.fields.imagebox.AbstractExtensibleImageField;
import org.eclipse.scout.rt.extension.client.ui.form.fields.smartfield.AbstractExtensibleSmartField;

/**
 * Utility for applying menu extensions. The following abstract classes should be used in general for providing
 * extensible menus.
 * <ul>
 * <li>{@link AbstractExtensibleButton}</li>
 * <li>{@link AbstractExtensibleFileChooserField}</li>
 * <li>{@link AbstractExtensibleImageField}</li>
 * <li>{@link AbstractExtensibleMenu}</li>
 * <li>{@link AbstractExtensiblePageWithNodes}</li>
 * <li>{@link AbstractExtensibleSmartField}</li>
 * <li>{@link AbstractExtensibleTable}</li>
 * </ul>
 * 
 * @since 3.9.0
 */
public final class MenuExtensionUtility {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MenuExtensionUtility.class);

  private MenuExtensionUtility() {
  }

  @SuppressWarnings("unchecked")
  public static <T> Class<T> getAnchorType(T anchor) {
    if (anchor == null) {
      return null;
    }
    if (anchor instanceof IPage) {
      return (Class<T>) IPage.class;
    }
    if (anchor instanceof IFormField) {
      return (Class<T>) IFormField.class;
    }
    if (anchor instanceof IMenu) {
      return (Class<T>) IMenu.class;
    }
    if (anchor instanceof ITreeNode) {
      return (Class<T>) ITreeNode.class;
    }
    if (anchor instanceof IDesktop) {
      return (Class<T>) IDesktop.class;
    }
    return null;
  }

  public static <T> void adaptMenus(T anchor, Object container, List<IMenu> menuList) {
    adaptMenus(anchor, container, menuList, false);
  }

  public static <T> void adaptMenus(T anchor, Object container, List<IMenu> menuList, boolean createOrderedMenuWrapper) {
    Class<T> anchorType = getAnchorType(anchor);
    if (anchorType == null || anchor == null || container == null) {
      return;
    }

    MenuExtensionManager extensionManager = Activator.getDefault().getMenuExtensionManager();
    contributeMenus(anchor, container, extensionManager.getMenuContributionExtensions(anchorType), menuList, createOrderedMenuWrapper);
    removeMenus(anchor, container, extensionManager.getMenuRemoveExtensions(anchorType), menuList);
    modifyMenus(anchor, container, extensionManager.getMenuModificationExtensions(anchorType), menuList);
  }

  static <T> void contributeMenus(T anchor, Object container, List<MenuContributionExtension> extensions, List<IMenu> menuList, boolean createOrderedMenuWrapper) {
    if (extensions == null || extensions.isEmpty()) {
      return;
    }

    // filter matching extensions
    List<MenuContributionExtension> matchingExtensions = new LinkedList<MenuContributionExtension>();
    for (MenuContributionExtension e : extensions) {
      if (e.accept(anchor, container, null)) {
        matchingExtensions.add(e);
      }
    }

    if (matchingExtensions.isEmpty()) {
      return;
    }

    // add existing menus to ordered list
    TreeMap<CompositeObject, IMenu> orderedMenus = new TreeMap<CompositeObject, IMenu>();
    int counter = 0;
    for (IMenu m : menuList) {
      double order;
      Class<?> tmpClass = m.getClass();
      Order orderAnnotation;
      while ((orderAnnotation = tmpClass.getAnnotation(Order.class)) == null && m.getClass().isAnnotationPresent(Replace.class)) {
        tmpClass = tmpClass.getSuperclass();
      }
      if (orderAnnotation != null) {
        order = orderAnnotation.value();
      }
      else {
        order = Double.MAX_VALUE;
      }
      orderedMenus.put(new CompositeObject(order, counter), m);
      counter++;
    }

    // create new menus
    for (MenuContributionExtension e : matchingExtensions) {
      try {
        IMenu m = e.createContribution(anchor, container);
        if (createOrderedMenuWrapper) {
          m = new OrderedMenuWrapper(m, e.getOrder());
        }
        orderedMenus.put(new CompositeObject(e.getOrder(), counter), m);
        counter++;
      }
      catch (Throwable t) {
        LOG.error("Exception while creating an instance of contributed menu " + e, t);
      }
    }

    // reorder existing and add new pages
    menuList.clear();
    menuList.addAll(orderedMenus.values());
  }

  static <T> void removeMenus(T anchor, Object container, List<MenuRemoveExtension> extensions, List<IMenu> menuList) {
    if (extensions == null || extensions.isEmpty()) {
      return;
    }

    for (Iterator<IMenu> it = menuList.iterator(); it.hasNext();) {
      IMenu next = it.next();
      for (MenuRemoveExtension removeExtension : extensions) {
        if (removeExtension.accept(anchor, container, next)) {
          it.remove();
          break;
        }
      }
    }
  }

  static <T> void modifyMenus(T anchor, Object container, List<MenuModificationExtension> extensions, List<IMenu> menuList) {
    if (extensions == null || extensions.isEmpty()) {
      return;
    }

    for (MenuModificationExtension ext : extensions) {
      for (IMenu menu : menuList) {
        try {
          if (ext.accept(anchor, container, menu)) {
            IMenuModifier<IMenu> menuModifier = ext.createMenuModifier();
            menuModifier.modify(anchor, container, menu);
          }
        }
        catch (ProcessingException e) {
          LOG.error("Exception while modifying menu", e);
        }
      }
    }
  }
}
