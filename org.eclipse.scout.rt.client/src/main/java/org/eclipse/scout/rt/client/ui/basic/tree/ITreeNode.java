/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.tree;

import java.security.Permission;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenuOwner;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.shared.dimension.IEnabledDimension;
import org.eclipse.scout.rt.shared.dimension.IVisibleDimension;

/**
 * Tree node used in {@link ITree}.
 */
public interface ITreeNode extends IVisibleDimension, IEnabledDimension, IContextMenuOwner {
  int STATUS_NON_CHANGED = 0;
  int STATUS_INSERTED = 1;
  int STATUS_UPDATED = 2;
  int STATUS_DELETED = 3;

  void initTreeNode();

  boolean isInitializing();

  void setInitializing(boolean b);

  String getNodeId();

  /**
   * called after the node has been added to a tree
   */
  void nodeAddedNotify();

  /**
   * called after the node has been removed from a tree
   */
  void nodeRemovedNotify();

  int getStatus();

  boolean isStatusInserted();

  boolean isStatusUpdated();

  boolean isStatusDeleted();

  boolean isStatusNonchanged();

  /**
   * do not use this method directly use {@link ITree#setNodeExpanded(ITreeNode, boolean)}
   */
  void setExpandedInternal(boolean b);

  /**
   * Note: this method is a Convenience for {@link ITree#setNodeExpanded(ITreeNode, boolean)}
   */
  void setExpanded(boolean b);

  /**
   * Note: this method is a Convenience for {@link ITree#setNodeExpanded(ITreeNode, boolean, boolean)}
   */
  void setExpanded(boolean b, boolean lazy);

  /**
   * Note: this method is a Convenience for {@link ITree#setNodeEnabledPermission(ITreeNode, Permission)}
   */
  void setEnabledPermission(Permission p);

  /**
   * Note: this method is a Convenience for {@link ITree#setNodeEnabledGranted(ITreeNode, boolean)}
   */
  void setEnabledGranted(boolean b);

  /**
   * Note: this method is a Convenience for {@link ITree#setNodeEnabled(ITreeNode, boolean)}
   */
  void setEnabled(boolean b);

  /**
   * Note: this method is a Convenience for {@link ITree#setNodeVisiblePermission(ITreeNode, Permission)}
   */
  void setVisiblePermission(Permission p);

  /**
   * Note: this method is a Convenience for {@link ITree#setNodeVisibleGranted(ITreeNode, boolean)}
   */
  void setVisibleGranted(boolean b);

  /**
   * Note: this method is a Convenience for {@link ITree#setNodeVisible(ITreeNode, boolean)}
   */
  void setVisible(boolean b);

  /**
   * do not use this method directly use {@link ITree#setNodeLeaf(ITreeNode, boolean)}
   */
  void setLeafInternal(boolean b);

  /**
   * Note: this method is a Convenience for {@link ITree#setNodeLeaf(ITreeNode, boolean)}
   */
  void setLeaf(boolean b);

  /**
   * valid only when {@link ITree#isCheckable()}==true
   */
  boolean isChecked();

  void setChecked(boolean checked);

  void setChecked(boolean checked, boolean enabledNodesOnly);

  /**
   * do not use this method directly use {@link ITree#setNodeStatus(ITreeNode, int)}
   */
  void setStatusInternal(int status);

  /**
   * Note: this method is a Convenience for {@link ITree#setNodeStatus(ITreeNode, int)}
   */
  void setStatus(int status);

  ICell getCell();

  Cell getCellForUpdate();

  Object getPrimaryKey();

  void setPrimaryKey(Object key);

  void decorateCell();

  boolean isLeaf();

  /**
   * alias for {@link ITree#isSelectedNode(ITreeNode)}
   */
  boolean isSelectedNode();

  /**
   * see {@link ITree#addNodeFilter(ITreeNodeFilter)}
   */
  boolean isFilterAccepted();

  /**
   * do not use this method directly, use {@link ITree#addNodeFilter(ITreeNodeFilter)},
   * {@link ITree#removeNodeFilter(ITreeNodeFilter)}
   */
  void setFilterAccepted(boolean b);

  void resetFilterCache();

  /**
   * Indicates whether {@link #isFilterAccepted()} returns false because the node has been filtered by the user.
   *
   * @return true if @link IUserFilter is the only filter not accepting the node.
   */
  boolean isRejectedByUser();

  void setRejectedByUser(boolean rejectedByUser);

  /**
   * a dirty marked node is marked for child reload its children are reloaded on the next ui call to
   * {@link ITreeUIFacade#setNodeExpandedFromUI(ITreeNode, boolean, boolean)}
   * {@link ITreeUIFacade#setNodeSelectedAndExpandedFromUI(ITreeNode)}
   * {@link ITreeUIFacade#setNodesSelectedFromUI(List)} and the dirty flag is reset to false default is false
   */
  boolean isChildrenDirty();

  /**
   * mark a node as dirty
   */
  void setChildrenDirty(boolean b);

  /**
   * a node with volatile (rapidly and constantly changing) children is reloaded on ANY ui call to
   * {@link ITreeUIFacade#setNodeExpandedFromUI(ITreeNode, boolean, boolean)}
   * {@link ITreeUIFacade#setNodeSelectedAndExpandedFromUI(ITreeNode)}
   * {@link ITreeUIFacade#setNodesSelectedFromUI(List)} default is false
   */
  boolean isChildrenVolatile();

  /**
   * mark node as containing volatile children
   */
  void setChildrenVolatile(boolean b);

  /**
   * @return true if node is enabled and enabled is granted
   */
  boolean isEnabled();

  boolean isEnabledGranted();

  /**
   * @return true if node is visible and visible is granted
   */
  boolean isVisible();

  boolean isVisibleGranted();

  boolean isInitialExpanded();

  void setInitialExpanded(boolean b);

  boolean isExpanded();

  /**
   * Returns the current expanding state. This flag depends on {@link #isExpanded()} and is only considered if the node
   * is expanded.
   *
   * @return true if the node is in lazy expanding state.
   */
  boolean isExpandedLazy();

  void setExpandedLazyInternal(boolean expandedLazy);

  /**
   * @return <code>true</code> if nodes should be shown lazily when a parent node gets expanded, i.e. only after the
   *         user explicitly requests them with the "show all" function. <code>false</code> otherwise.
   */
  boolean isLazyExpandingEnabled();

  void setLazyExpandingEnabled(boolean lazyExpandingEnabled);

  void setMenus(List<? extends IMenu> a);

  /**
   * get tree containing this node
   */
  ITree getTree();

  /**
   * do not use this internal method
   */
  void setTreeInternal(ITree tree, boolean includeSubtree);

  /**
   * parent
   */
  ITreeNode getParentNode();

  /**
   * @return the parent node before the node was deleted.
   */
  ITreeNode getOldParentNode();

  /**
   * @return the immediate parent node if it is of type T, null otherwise
   */
  <T extends ITreeNode> T getParentNode(Class<T> type);

  /**
   * @return the parent node if it is of type T, null otherwise
   */
  <T extends ITreeNode> T getParentNode(Class<T> type, int backCount);

  /**
   * @return first node in parent path that is of type T, null otherwise
   */
  <T extends ITreeNode> T getAncestorNode(Class<T> type);

  /**
   * do not use this internal method
   */
  void setParentNodeInternal(ITreeNode parent);

  ITreeNode findParentNode(Class<?> interfaceType);

  /**
   * children
   */
  int getChildNodeCount();

  /**
   * @return index of this node in its parent child array
   */
  int getChildNodeIndex();

  /**
   * do not use this internal method
   */
  void setChildNodeIndexInternal(int childNodeIndex);

  ITreeNode getChildNode(int childIndex);

  List<ITreeNode> getChildNodes();

  /**
   * Collects child nodes of this node and adds them the given collector. If recursive is <code>true</code>, grand
   * children are visited as well.<br/>
   */
  void collectChildNodes(Set<ITreeNode> collector, boolean recursive);

  /**
   * @see ITree#getNodeFilters() This method is Thread-Safe
   */
  List<ITreeNode> getFilteredChildNodes();

  /**
   * @return the node's nesting level in the tree (counting upwards from the root node). The root node has level 0, no
   *         matter whether it is visible or invisible.
   */
  int getTreeLevel();

  /**
   * (re)load all children
   */
  void loadChildren();

  boolean isChildrenLoaded();

  void setChildrenLoaded(boolean b);

  void ensureChildrenLoaded();

  /**
   * Convenience for getTree().updateNode(this);
   */
  void update();

  /**
   * Called by the scout framework when the node is disposed in order to release any bound resources. There is usually
   * no need to call this method by the application's code.
   */
  void dispose();

  /**
   * @return {@code true} if this {@link ITreeNode} is disposing.
   */
  boolean isDisposing();
}
