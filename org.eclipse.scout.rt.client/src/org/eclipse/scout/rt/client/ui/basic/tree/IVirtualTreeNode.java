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
package org.eclipse.scout.rt.client.ui.basic.tree;

import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.VirtualPage;

/**
 * A virtual node is a marker tree node used to optimize performance in large trees.
 * <p>
 * It is used mainly in the {@link IPage}, {@link IOutline} area with {@link VirtualPage}s
 */
public interface IVirtualTreeNode extends ITreeNode {
  /**
   * @return the real node if the virtual node has been resolved, null otherwise
   *         This value is used when a (old) reference to a {@link IVirtualTreeNode} is interested in the real node to
   *         update its
   *         reference
   */
  ITreeNode getResolvedNode();

  /**
   * Set the real node of this virtual node.
   */
  void setResolvedNode(ITreeNode resolvedNode);
}
