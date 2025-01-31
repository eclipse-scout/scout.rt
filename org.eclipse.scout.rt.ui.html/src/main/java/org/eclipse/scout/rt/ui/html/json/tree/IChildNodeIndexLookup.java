/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.tree;

import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;

/**
 * The calculation of the child index of a tree node in its parent can be expensive. This lookup helper prepares and
 * caches reverse child index information during a call to
 * {@link JsonTree#treeNodeToJson(org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode, ITreeNodeChildIndexLookup)}
 *
 * @since 16.1
 */
public interface IChildNodeIndexLookup {
  /**
   * @return the index of this node in its {@link ITreeNode#getParentNode()} child list. If this node has no parent or
   * is filtered then -1 is returned.
   */
  int childNodeIndexOf(ITreeNode node);
}
