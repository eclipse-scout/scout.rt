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
package org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.extension.client.IExtensibleScoutObject;
import org.eclipse.scout.rt.extension.client.ui.action.menu.MenuExtensionUtility;

/**
 * Page with nodes supporting the following Scout extension features:
 * <ul>
 * <li>adding, removing and modifying statically configured pages</li>
 * <li>adding, removing and modifying statically configured menus</li>
 * </ul>
 * 
 * @since 3.9.0
 */
public abstract class AbstractExtensiblePageWithNodes extends AbstractPageWithNodes implements IExtensibleScoutObject {

  public AbstractExtensiblePageWithNodes() {
    super();
  }

  public AbstractExtensiblePageWithNodes(boolean callInitializer) {
    super(callInitializer);
  }

  /**
   * @deprecated Will be removed with Bug 426088.
   *             Use {@link #AbstractExtensiblePageWithNodes()} in combination with getter and setter (page variable)
   *             instead.
   */
  @Deprecated
  @SuppressWarnings("deprecation")
  public AbstractExtensiblePageWithNodes(org.eclipse.scout.rt.shared.ContextMap contextMap) {
    super(contextMap);
  }

  public AbstractExtensiblePageWithNodes(String userPreferenceContext) {
    super(userPreferenceContext);
  }

  public AbstractExtensiblePageWithNodes(boolean callInitializer, String userPreferenceContext) {
    super(callInitializer, userPreferenceContext);
  }

  /**
   * @deprecated Will be removed with Bug 426088.
   *             Use {@link #AbstractExtensiblePageWithNodes(boolean, String)} in combination with getter and setter
   *             (page
   *             variable) instead.
   */
  @Deprecated
  @SuppressWarnings("deprecation")
  public AbstractExtensiblePageWithNodes(boolean callInitializer, org.eclipse.scout.rt.shared.ContextMap contextMap, String userPreferenceContext) {
    super(callInitializer, contextMap, userPreferenceContext);
  }

  @Override
  protected void createChildPagesInternal(Collection<IPage> pageList) throws ProcessingException {
    super.createChildPagesInternal(pageList);
    PageExtensionUtility.adaptPageWithNodes(this, pageList);
  }

  @Override
  protected void injectMenusInternal(List<IMenu> menuList) {
    super.injectMenusInternal(menuList);
    MenuExtensionUtility.adaptMenus(this, this, menuList);
  }
}
