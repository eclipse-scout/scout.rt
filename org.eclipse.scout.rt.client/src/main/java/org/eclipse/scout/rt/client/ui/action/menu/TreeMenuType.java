/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action.menu;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;

/**
 * All possible menu types of a tree menu. These menu types are used by {@link AbstractMenu#getConfiguredMenuTypes()} on
 * any {@link ITree}.
 * <p>
 * Specificity: {@link #Header}, {@link #EmptySpace}, {@link #SingleSelection}, {@link #MultiSelection}
 */
public enum TreeMenuType implements IMenuType {
  EmptySpace,
  SingleSelection,
  /**
   * <b>Note:</b> The HTML UI does not support multi selection for trees yet. A menu with type {@link #MultiSelection}
   * may therefore only be used if the selection is set programmatically using
   * {@link ITree#selectNodes(java.util.Collection, boolean)}!
   */
  MultiSelection,
  Header
}
