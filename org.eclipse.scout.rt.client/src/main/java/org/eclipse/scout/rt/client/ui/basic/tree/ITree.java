/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.tree;

import java.security.Permission;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;
import org.eclipse.scout.rt.client.ui.IAppLinkCapable;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.IStyleable;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenuOwner;
import org.eclipse.scout.rt.client.ui.action.menu.root.ITreeContextMenu;
import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.platform.util.visitor.IDepthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.AbstractTreeFieldData;

public interface ITree extends IWidget, IDNDSupport, IStyleable, IAppLinkCapable, IContextMenuOwner {

  String PROP_TITLE = "title";
  String PROP_DRAG_ENABLED = "dragEnabled";
  String PROP_ICON_ID = "iconId";
  String PROP_DEFAULT_ICON_ID = "defaultIconId";
  String PROP_MULTI_SELECT = "multiSelect";
  String PROP_MULTI_CHECK = "multiCheck";
  String PROP_CHECKABLE = "checkable";
  String PROP_AUTO_CHECK_CHILDREN = "autoCheckChildren";
  String PROP_AUTO_CHECK_STYLE = "autoCheckStyle";
  String PROP_LAZY_EXPANDING_ENABLED = "lazyExpandingEnabled";
  /**
   * Integer default -1
   */
  String PROP_NODE_HEIGHT_HINT = "propNodeHeightHint";
  String PROP_ROOT_NODE_VISIBLE = "rootNodeVisible";
  String PROP_ROOT_HANDLES_VISIBLE = "rootHandlesVisible";
  String PROP_KEY_STROKES = "keyStrokes";
  String PROP_SCROLL_TO_SELECTION = "scrollToSelection";
  String PROP_DISPLAY_STYLE = "displayStyle";
  String PROP_TOGGLE_BREADCRUMB_STYLE_ENABLED = "toggleBreadcrumbStyleEnabled";

  /**
   * The strategy how rows can be checked.
   *
   * @since 9.0
   */
  String PROP_CHECKABLE_STYLE = "checkableStyle";

  String PROP_TEXT_FILTER_ENABLED = "textFilterEnabled";

  String DISPLAY_STYLE_DEFAULT = "default";
  String DISPLAY_STYLE_BREADCRUMB = "breadcrumb";

  /**
   * {@link ITreeContextMenu}
   */
  String PROP_CONTEXT_MENU = "contextMenu";

  /**
   * Add this class to a tree if the parent node should not visualize that a child node is checked.
   */
  String CLS_NO_CHILDREN_CHECKED_STYLE = "no-children-checked-style";

  /**
   * @since 5.1.0
   */
  AbstractEventBuffer<TreeEvent> createEventBuffer();

  void requestFocus();

  @Override
  ITreeContextMenu getContextMenu();

  /**
   * @see #setScrollToSelection(boolean)
   */
  boolean isScrollToSelection();

  /**
   * @param scrollToSelection
   *          true: advices the attached ui to make the current selection visible. The current selection will be
   *          scrolled to visible (again, whenever the table size changes).
   */
  void setScrollToSelection(boolean scrollToSelection);

  /**
   * May be used when {@link #isScrollToSelection()} = false on individual occasion where selection shall be scrolled to
   * visible, the property scrollToVisible remains untouched.
   * <p>
   * This is a one-time scroll advise to the ui
   */
  void scrollToSelection();

  List<ITreeNodeFilter> getNodeFilters();

  boolean hasNodeFilters();

  /**
   * adding a filter multiple times is supported. This only adds it the first time. The other times it just calls
   * {@link #applyNodeFilters()}
   */
  void addNodeFilter(ITreeNodeFilter filter);

  void removeNodeFilter(ITreeNodeFilter filter);

  /**
   * (Re-) applies all node filters to the tree.
   * <p>
   * This can be useful when the state of a node filter was changed without adding or removing it.
   * <p>
   * {@link #addNodeFilter(ITreeNodeFilter)} and {@link #removeNodeFilter(ITreeNodeFilter)} automatically apply the
   * filters to the tree.
   */
  void applyNodeFilters();

  ITreeNode getRootNode();

  void setRootNode(ITreeNode rootNode);

  String getTitle();

  void setTitle(String title);

  String getIconId();

  void setIconId(String iconId);

  String getDefaultIconId();

  void setDefaultIconId(String defaultIconId);

  /**
   * @return path using delimiter " - "
   */
  String getPathText(ITreeNode node);

  String getPathText(ITreeNode node, String delimiter);

  boolean isAutoTitle();

  void setAutoTitle(boolean autoTitle);

  boolean isDragEnabled();

  void setDragEnabled(boolean dragEnabled);

  ITreeNode findNode(Object primaryKey);

  List<ITreeNode> findNodes(Collection<?> primaryKeys);

  boolean isRootNodeVisible();

  void setRootNodeVisible(boolean rootNodeVisible);

  boolean isRootHandlesVisible();

  void setRootHandlesVisible(boolean rootHandlesVisible);

  void ensureVisible(ITreeNode node);

  /**
   * to expand all nodes under the given parent node, parent node inclusive.
   *
   * @param parent
   *          the start node for the expansion.
   */
  void expandAll(ITreeNode parent);

  /**
   * to collapse all nodes under the given parent node, parent node inclusive.
   *
   * @param parent
   *          the start node for collapsing.
   */
  void collapseAll(ITreeNode parent);

  boolean isLazyExpandingEnabled();

  void setLazyExpandingEnabled(boolean lazyExpandingEnabled);

  int getSelectedNodeCount();

  ITreeNode getSelectedNode();

  Set<ITreeNode> getSelectedNodes();

  boolean isAutoCheckChildNodes();

  void setAutoCheckChildNodes(boolean autoCheckChildNodes);

  AutoCheckStyle getAutoCheckStyle();

  void setAutoCheckStyle(AutoCheckStyle autoCheckStyle);

  boolean isSelectedNode(ITreeNode node);

  void selectNode(ITreeNode node);

  void selectNode(ITreeNode node, boolean append);

  void selectNodes(Collection<? extends ITreeNode> nodes, boolean append);

  void deselectNode(ITreeNode node);

  void deselectNodes(Collection<? extends ITreeNode> nodes);

  /**
   * Select the previous selectable node in this tree. Does not expand any nodes.
   */
  void selectPreviousNode();

  /**
   * Select the next visible and selectable node in this tree. Does not expand any nodes.
   */
  void selectNextNode();

  /**
   * Select the first visible and selectable node in this tree. Does not expand any nodes.
   *
   * @return The selected node or {@code null} if no visible and selectable node could be found.
   */
  ITreeNode selectFirstNode();

  /**
   * Select the last visible and selectable node in this tree. Does not expand any nodes.
   */
  void selectLastNode();

  /**
   * Expand current selected node and then select next.
   */
  void selectNextChildNode();

  /**
   * Select parent of current selected node.
   */
  void selectPreviousParentNode();

  /**
   * @return a flat array of all checked nodes
   */
  Set<ITreeNode> getCheckedNodes();

  int getCheckedNodesCount();

  /**
   * @return true if parent is equal to child or parent is an ancestor of child
   * @since 03.07.2009
   */
  boolean isAncestorNodeOf(ITreeNode parent, ITreeNode child);

  /**
   * Accessor to the tree listener registry
   */
  TreeListeners treeListeners();

  /**
   * @param eventTypes
   *          of {@link TreeEvent} TYPE_*
   */
  default void addTreeListener(TreeListener listener, Integer... eventTypes) {
    treeListeners().add(listener, false, eventTypes);
  }

  default void removeTreeListener(TreeListener listener, Integer... eventTypes) {
    treeListeners().remove(listener, eventTypes);
  }

  /**
   * Add the listener so it is called as <em>last</em> listener
   * <p>
   * Use {@link AbstractTree#addTreeListener(TreeListener, Integer...)} in all other cases
   *
   * @param eventTypes
   *          of {@link TreeEvent} TYPE_*
   */
  default void addUITreeListener(TreeListener listener, Integer... eventTypes) {
    treeListeners().addLastCalled(listener, false, eventTypes);
  }

  /**
   * @return the {@link IEventHistory} associated with this tree
   *         <p>
   *         The default implementation is a {@link DefaultTreeEventHistory} and created by
   *         {@link AbstractTree#createEventHistory()}
   *         <p>
   *         This method is thread safe.
   * @since 3.8
   */
  IEventHistory<TreeEvent> getEventHistory();

  /**
   * <b>Please note:</b> Multi-select is not supported by the HTML UI yet. Multiple nodes can be selected
   * programmatically using {@link #selectNodes(Collection, boolean)}.
   *
   * @return {@code true} if multiple nodes can be selected (default {@code false}).
   */
  boolean isMultiSelect();

  /**
   * <b>Please note:</b> Multi-select is not supported by the HTML UI yet. Multiple nodes can be selected
   * programmatically using {@link #selectNodes(Collection, boolean)}.
   *
   * @param multiSelect
   *          {@code true} if it should be possible to select multiple nodes. {@code false} otherwise.
   */
  void setMultiSelect(boolean multiSelect);

  /**
   * true if multiple nodes can be checked (default true)
   */
  boolean isMultiCheck();

  void setMultiCheck(boolean multiCheck);

  boolean isCheckable();

  void setCheckable(boolean checkable);

  CheckableStyle getCheckableStyle();

  void setCheckableStyle(CheckableStyle checkableStyle);

  boolean isTextFilterEnabled();

  void setTextFilterEnabled(boolean textFilterEnabled);

  /**
   * This is a hint for the UI if it is not capable of having variable node height based on node contents (such as
   * rap/rwt).
   * <p>
   * This hint defines the node height in pixels being used as the fixed node height for all nodes of this tree.
   *
   * @return the hint in pixels, default is -1
   */
  int getNodeHeightHint();

  /**
   * see {@link #getNodeHeightHint()}
   */
  void setNodeHeightHint(int heightHint);

  boolean isAutoDiscardOnDelete();

  void setAutoDiscardOnDelete(boolean autoDiscardOnDelete);

  boolean isTreeChanging();

  void setTreeChanging(boolean changing);

  boolean isNodeExpanded(ITreeNode node);

  void setNodeExpanded(ITreeNode node, boolean expanded);

  /**
   * @param lazy
   *          true to expand the node lazily, false if not. Only has an effect if the expanded is set to true, see also
   *          {@link ITreeNode#isExpandedLazy()}
   */
  void setNodeExpanded(ITreeNode node, boolean expanded, boolean lazy);

  /**
   * set expanded without check if node is already expanded
   */
  void setNodeExpandedInternal(ITreeNode node, boolean expanded, boolean lazy);

  void setNodeEnabledPermission(ITreeNode node, Permission permission);

  boolean isNodeEnabled(ITreeNode node);

  void setNodeEnabled(ITreeNode node, boolean enabled);

  boolean isNodeEnabledGranted(ITreeNode node);

  void setNodeEnabledGranted(ITreeNode node, boolean enabledGranted);

  void setNodeVisiblePermission(ITreeNode node, Permission permission);

  boolean isNodeVisible(ITreeNode node);

  void setNodeVisible(ITreeNode node, boolean visible);

  boolean isNodeVisibleGranted(ITreeNode node);

  void setNodeVisibleGranted(ITreeNode node, boolean visibleGranted);

  boolean isNodeLeaf(ITreeNode node);

  void setNodeLeaf(ITreeNode node, boolean leaf);

  void setNodeChecked(ITreeNode node, boolean checked);

  void setNodeChecked(ITreeNode node, boolean checked, boolean enabledNodesOnly);

  void setNodeChecked(ITreeNode node, boolean checked, boolean enabledNodesOnly, boolean forceUpdateNode);

  void setNodesChecked(List<ITreeNode> nodes, boolean checked);

  void setNodesChecked(List<ITreeNode> nodes, boolean checked, boolean enabledNodesOnly);

  void setNodesChecked(List<ITreeNode> nodes, boolean checked, boolean enabledNodesOnly, boolean forceUpdateNode);

  void checkAllNodes();

  void uncheckAllNodes();

  void setAllNodesChecked(boolean checked);

  boolean isNodeChecked(ITreeNode node);

  int getNodeStatus(ITreeNode node);

  void setNodeStatus(ITreeNode node, int status);

  List<IKeyStroke> getKeyStrokes();

  void setKeyStrokes(List<? extends IKeyStroke> keyStrokes);

  /*
   * modifications
   */

  /**
   * append a child node to the end of the children of parent
   */
  void addChildNode(ITreeNode parent, ITreeNode child);

  /**
   * append a list of child nodes or a complete subtree to the end of the children of parent
   */
  void addChildNodes(ITreeNode parent, List<? extends ITreeNode> children);

  void addChildNode(int startIndex, ITreeNode parent, ITreeNode child);

  void addChildNodes(int startIndex, ITreeNode parent, List<? extends ITreeNode> children);

  void updateNode(ITreeNode node);

  void updateChildNodes(ITreeNode parent, Collection<? extends ITreeNode> children);

  void updateChildNodeOrder(ITreeNode parent, List<? extends ITreeNode> newChildren);

  void removeChildNode(ITreeNode parent, ITreeNode child);

  void removeChildNodes(ITreeNode parent, Collection<? extends ITreeNode> children);

  void removeAllChildNodes(ITreeNode parent);

  void removeNode(ITreeNode node);

  /**
   * Clears removed nodes from cache and disposes them.
   */
  void clearDeletedNodes();

  /**
   * Clears a specific (already deleted) node from cache and disposes it.
   */
  void disposeDeletedNode(ITreeNode node);

  /**
   * Clears specific (already deleted) nodes from cache and disposes them.
   */
  void disposeDeletedNodes(Collection<ITreeNode> nodes);

  /**
   * Removes a node from deleted nodes (does not discard it, it might be re-used afterwards).
   */
  void discardDeletedNode(ITreeNode node);

  /**
   * Removes nodes from deleted nodes (does not discard them, they might be re-used afterwards).
   */
  void discardDeletedNodes(Collection<ITreeNode> node);

  int getDeletedNodeCount();

  Set<ITreeNode> getDeletedNodes();

  int getInsertedNodeCount();

  Set<ITreeNode> getInsertedNodes();

  int getUpdatedNodeCount();

  Set<ITreeNode> getUpdatedNodes();

  TreeVisitResult visitTree(IDepthFirstTreeVisitor<ITreeNode> visitor);

  TreeVisitResult visitVisibleTree(IDepthFirstTreeVisitor<ITreeNode> visitor);

  TreeVisitResult visitNode(ITreeNode node, IDepthFirstTreeVisitor<ITreeNode> visitor);

  /**
   * unload all children and mark node as not loaded
   */
  void unloadNode(ITreeNode node);

  /**
   * extract transfer data to be sent to the backend
   * <p>
   * The single root node is not exported, the export starts with the first level after the root node
   */
  void exportTreeData(AbstractTreeFieldData target);

  /**
   * apply transfer data to this tree
   * <p>
   * All nodes are imported starting under the (existing) root node
   */
  void importTreeData(AbstractTreeFieldData source);

  /*
   * UI Processes
   */
  ITreeUIFacade getUIFacade();

  /**
   * @return if {@code true}, the attached UI will save or restore its horizontal and vertical coordinates of its
   *         scrollbars.
   */
  boolean isSaveAndRestoreScrollbars();

  /**
   * @param saveAndRestoreScrollbars
   *          {@code true} advices the attached UI to save or restore its horizontal and vertical coordinates of its
   *          scrollbars.
   */
  void setSaveAndRestoreScrollbars(boolean saveAndRestoreScrollbars);

  String getDisplayStyle();

  /**
   * @see #DISPLAY_STYLE_DEFAULT, #DISPLAY_STYLE_BREADCRUMB
   */
  void setDisplayStyle(String displayStyle);

  boolean isToggleBreadcrumbStyleEnabled();

  void setToggleBreadcrumbStyleEnabled(boolean toggleBreadcrumbStyleEnabled);

  /**
   * informs the attached UI that a node has changed in a way that may affect its presentation (e.g. text, font,
   * color...) but no structural changes occurred
   *
   * @since 3.10.0-M5
   */
  void fireNodeChanged(ITreeNode treeNode);
}
