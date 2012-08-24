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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.extension.client.internal.AbstractExtensionManager;
import org.eclipse.scout.rt.extension.client.internal.IExtensionProcessor;
import org.eclipse.scout.rt.extension.client.ui.action.menu.CompositeMenuFilter;
import org.eclipse.scout.rt.extension.client.ui.action.menu.IMenuExtensionFilter;
import org.eclipse.scout.rt.extension.client.ui.action.menu.IMenuModifier;
import org.osgi.framework.Bundle;

/**
 * @since 3.9.0
 */
public class MenuExtensionManager extends AbstractExtensionManager {

  public static final String EXTENSION_POINT_ID = org.eclipse.scout.rt.extension.client.Activator.PLUGIN_ID + ".menus";
  public static final String MENU_CONTRIBUTION_ELEMENT = "menuContribution";
  public static final String MENU_REMOVAL_ELEMENT = "menuRemoval";
  public static final String MENU_MODIFICATION_ELEMENT = "menuModification";

  private static P_AnchorDescription NULL_ANCHOR_DESCRIPTION = new P_AnchorDescription(null, null);

  private final Map<Class<?>, List<MenuContributionExtension>> m_menuContributionExtensions;
  private final Map<Class<?>, List<MenuRemoveExtension>> m_menuRemoveExtensions;
  private final Map<Class<?>, List<MenuModificationExtension>> m_menuModificationExtensions;

  public MenuExtensionManager(IExtensionRegistry extensionRegistry) {
    super(extensionRegistry, EXTENSION_POINT_ID);
    m_menuContributionExtensions = new HashMap<Class<?>, List<MenuContributionExtension>>();
    m_menuRemoveExtensions = new HashMap<Class<?>, List<MenuRemoveExtension>>();
    m_menuModificationExtensions = new HashMap<Class<?>, List<MenuModificationExtension>>();
    initExtensionProcessors();
  }

  public List<MenuContributionExtension> getMenuContributionExtensions(Class<?> anchorType) {
    synchronized (getLock()) {
      return getExtensions(m_menuContributionExtensions, anchorType);
    }
  }

  public List<MenuRemoveExtension> getMenuRemoveExtensions(Class<?> anchorType) {
    synchronized (getLock()) {
      return getExtensions(m_menuRemoveExtensions, anchorType);
    }
  }

  public List<MenuModificationExtension> getMenuModificationExtensions(Class<?> anchorType) {
    synchronized (getLock()) {
      return getExtensions(m_menuModificationExtensions, anchorType);
    }
  }

  private <T> List<T> getExtensions(Map<Class<?>, List<T>> extensions, Class<?> anchorType) {
    List<T> allMatchingExtensions = new LinkedList<T>();
    // add by anchor type
    List<T> extensionsByAnchorType = extensions.get(anchorType);
    if (extensionsByAnchorType != null) {
      allMatchingExtensions.addAll(extensionsByAnchorType);
    }
    // add global
    List<T> globalExtensions = extensions.get(null);
    if (globalExtensions != null) {
      allMatchingExtensions.addAll(globalExtensions);
    }
    return allMatchingExtensions;
  }

  private void initExtensionProcessors() {
    addExtensionProcessor(MENU_CONTRIBUTION_ELEMENT,
        new IExtensionProcessor<MenuContributionExtension>() {
          @Override
          public MenuContributionExtension processConfigurationElement(Bundle contributor, IConfigurationElement element) throws Exception {
            Class<? extends IMenu> menuClass = loadClass(contributor, IMenu.class, element.getAttribute("class"));
            Double order = TypeCastUtility.castValue(element.getAttribute("order"), Double.class);
            // anchor and filters
            P_AnchorDescription anchorDescription = getAnchorDescription(contributor, element);
            IMenuExtensionFilter filter = createFilter(element, anchorDescription);
            // create contribution description
            MenuContributionExtension menuContribution = new MenuContributionExtension(menuClass, filter, order);
            List<MenuContributionExtension> extensions = m_menuContributionExtensions.get(anchorDescription.getAnchorType());
            if (extensions == null) {
              extensions = new LinkedList<MenuContributionExtension>();
              m_menuContributionExtensions.put(anchorDescription.getAnchorType(), extensions);
            }
            extensions.add(menuContribution);
            return menuContribution;
          }
        });
    addExtensionProcessor(MENU_REMOVAL_ELEMENT,
        new IExtensionProcessor<MenuRemoveExtension>() {
          @Override
          public MenuRemoveExtension processConfigurationElement(Bundle contributor, IConfigurationElement element) throws Exception {
            Class<? extends IMenu> menuClass = loadClass(contributor, IMenu.class, element.getAttribute("class"));
            // anchor and filters
            P_AnchorDescription anchorDescription = getAnchorDescription(contributor, element);
            IMenuExtensionFilter filter = createFilter(element, anchorDescription);
            // create removal description
            MenuRemoveExtension menuRemoval = new MenuRemoveExtension(menuClass, filter);
            List<MenuRemoveExtension> extensions = m_menuRemoveExtensions.get(anchorDescription.getAnchorType());
            if (extensions == null) {
              extensions = new LinkedList<MenuRemoveExtension>();
              m_menuRemoveExtensions.put(anchorDescription.getAnchorType(), extensions);
            }
            extensions.add(menuRemoval);
            return menuRemoval;
          }
        });
    addExtensionProcessor(MENU_MODIFICATION_ELEMENT,
        new IExtensionProcessor<MenuModificationExtension>() {
          @Override
          public MenuModificationExtension processConfigurationElement(Bundle contributor, IConfigurationElement element) throws Exception {
            Class<? extends IMenu> menuClass = loadClass(contributor, IMenu.class, element.getAttribute("menu"));
            @SuppressWarnings("unchecked")
            Class<? extends IMenuModifier<IMenu>> modifierClass = (Class<? extends IMenuModifier<IMenu>>) loadClass(contributor, IMenuModifier.class, element.getAttribute("class"));
            // anchor and filters
            P_AnchorDescription anchorDescription = getAnchorDescription(contributor, element);
            IMenuExtensionFilter filter = createFilter(element, anchorDescription);
            // create contribution description
            MenuModificationExtension menuModification = new MenuModificationExtension(menuClass, filter, modifierClass);
            List<MenuModificationExtension> extensions = m_menuModificationExtensions.get(anchorDescription.getAnchorType());
            if (extensions == null) {
              extensions = new LinkedList<MenuModificationExtension>();
              m_menuModificationExtensions.put(anchorDescription.getAnchorType(), extensions);
            }
            extensions.add(menuModification);
            return menuModification;
          }
        });
  }

  /**
   * Searches the first anchor description within the given element's children and returns the parsed value or a
   * {@link #NULL_ANCHOR_DESCRIPTION}.
   * 
   * @param contributor
   * @param element
   * @return
   * @throws Exception
   */
  private P_AnchorDescription getAnchorDescription(Bundle contributor, IConfigurationElement element) throws Exception {
    Map<String, Class<?>> anchorConfigurations = new HashMap<String, Class<?>>();
    anchorConfigurations.put("page", IPage.class);
    anchorConfigurations.put("formField", IFormField.class);
    anchorConfigurations.put("parentMenu", IMenu.class);
    anchorConfigurations.put("treeNode", ITreeNode.class);
    //
    Class<?> anchorType = null;
    Class<?> anchorClass = null;
    for (IConfigurationElement child : element.getChildren()) {
      anchorType = anchorConfigurations.get(child.getName());
      if (anchorType != null) {
        anchorClass = loadClass(contributor, anchorType, child.getAttribute("class"));
        // check whether anchor type is ITreeNode.class, but anchorClass implements IPage
        if (anchorType == ITreeNode.class && IPage.class.isAssignableFrom(anchorClass)) {
          anchorType = IPage.class;
        }
        return new P_AnchorDescription(anchorType, anchorClass);
      }
    }
    return NULL_ANCHOR_DESCRIPTION;
  }

  private CompositeMenuFilter createFilter(IConfigurationElement element, P_AnchorDescription anchorDescription) {
    CompositeMenuFilter compositeFilter = new CompositeMenuFilter();

    if (anchorDescription != NULL_ANCHOR_DESCRIPTION) {
      compositeFilter.addFilter(new MenuAnchorFilter(anchorDescription.getAnchorClass()));
    }

    // parse additional filter elements
    for (IConfigurationElement child : element.getChildren("filter")) {
      try {
        IMenuExtensionFilter filter = (IMenuExtensionFilter) child.createExecutableExtension("class");
        compositeFilter.addFilter(filter);
      }
      catch (Exception e) {
        LOG.error("Exception while creating filter class [" + element.getAttribute("class") + "]", e);
      }
    }

    if (!compositeFilter.isEmpty()) {
      return compositeFilter;
    }
    return null;
  }

  @Override
  protected void removeContributions(Set<Object> contributions) {
    for (List<MenuContributionExtension> extensions : m_menuContributionExtensions.values()) {
      extensions.removeAll(contributions);
    }
    //
    for (List<MenuRemoveExtension> extensions : m_menuRemoveExtensions.values()) {
      extensions.removeAll(contributions);
    }
    //
    for (List<MenuModificationExtension> extensions : m_menuModificationExtensions.values()) {
      extensions.removeAll(contributions);
    }
  }

  private static class P_AnchorDescription {

    private final Class<?> m_anchorType;
    private final Class<?> m_anchorClass;

    public P_AnchorDescription(Class<?> anchorType, Class<?> anchorClass) {
      m_anchorType = anchorType;
      m_anchorClass = anchorClass;
    }

    public Class<?> getAnchorType() {
      return m_anchorType;
    }

    public Class<?> getAnchorClass() {
      return m_anchorClass;
    }
  }
}
