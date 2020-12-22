/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.action.menu;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;

/**
 * All possible menu types of a tree menu. These menu types are used by {@link AbstractMenu#getConfiguredMenuTypes()} on
 * any {@link ITree}.
 * <p>
 * Specificity: {@link #Header}, {@link #EmptySpace}, {@link #SingleSelection}, {@link #MultiSelection}
 */
public enum TreeMenuType implements IMenuType {
  /**
   * Specifies menus which are visible independent of the selection of the tree.<br>
   * The menu will be disabled if the tree itself is disabled. If the menu has multiple types, the most restrictive type
   * wins (e.g. a menu with type EmptySpace and SingleSelection will be disabled if a disabled tree node is selected).
   */
  EmptySpace,
  /**
   * Specifies menus which are visible if a single tree node is selected.<br>
   * If the tree node is disabled or the tree itself is disabled, the menu will be disabled as well. If the menu has
   * multiple types, the most restrictive type wins (e.g. a menu with type EmptySpace and SingleSelection will be
   * disabled if a disabled node is selected).
   */
  SingleSelection,
  /**
   * Specifies menus which are visible if multiple tree nodes are selected.<br>
   * If the selection contains disabled nodes or the tree itself is disabled, the menu will be disabled as well. If the
   * menu has multiple types, the most restrictive type wins (e.g. a menu with type EmptySpace and SingleSelection will
   * be disabled if a disabled node is selected).<br>
   * <b>Note:</b> The HTML UI does not support multi selection for trees yet. Such a menu type may therefore only be
   * used if the selection is set programmatically using {@link ITree#selectNodes(java.util.Collection, boolean)}!
   */
  MultiSelection,
  Header
}
