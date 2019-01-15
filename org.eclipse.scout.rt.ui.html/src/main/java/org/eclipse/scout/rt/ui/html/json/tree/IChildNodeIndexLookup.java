/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
   *         is filtered then -1 is returned.
   */
  int childNodeIndexOf(ITreeNode node);
}
