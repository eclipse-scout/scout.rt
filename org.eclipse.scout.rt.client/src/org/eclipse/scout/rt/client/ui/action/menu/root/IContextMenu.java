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
package org.eclipse.scout.rt.client.ui.action.menu.root;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.rt.client.ui.action.IActionFilter;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;

/**
 * This is an invisible root menu container. Subclasses of this interface are used of form fields as an invisible root
 * menu.
 */
public interface IContextMenu extends IMenu {

  static String PROP_ACTIVE_FILTER = "activeFilter";

  /**
   * @return the owner field, table, tree of the context menu
   */
  IPropertyObserver getOwner();

  /**
   * the active menu filter is used to filter menus for displaying. E.g. a {@link TableMenuType#SingleSelection} filter
   * for table context menus when exactly one row is selected.
   * 
   * @return
   */
  IActionFilter getActiveFilter();

  void addContextMenuListener(ContextMenuListener listener);

  void removeContextMenuListener(ContextMenuListener listener);

  void callAboutToShow(IActionFilter filter);
}
