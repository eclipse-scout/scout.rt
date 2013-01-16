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

import java.net.URL;
import java.security.Permission;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.action.ActionFinder;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.VirtualPage;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.ITreeBox;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.ITreeField;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.AbstractTreeFieldData;

public interface ITree extends IPropertyObserver, IDNDSupport {

  String PROP_TITLE = "title";
  String PROP_ENABLED = "enabled";
  String PROP_DRAG_ENABLED = "dragEnabled";
  String PROP_ICON_ID = "iconId";
  String PROP_MULTI_SELECT = "multiSelect";
  String PROP_MULTI_CHECK = "multiCheck";
  String PROP_CHECKABLE = "checkable";
  /**
   * Integer default -1
   */
  String PROP_NODE_HEIGHT_HINT = "propNodeHeightHint";
  String PROP_ROOT_NODE_VISIBLE = "rootNodeVisible";
  String PROP_ROOT_HANDLES_VISIBLE = "rootHandlesVisible";
  String PROP_KEY_STROKES = "keyStroks";
  String PROP_SCROLL_TO_SELECTION = "scrollToSelection";
  /**
   * Object
   * <p>
   * Container of this tree, {@link IPage}, {@link ITreeField}, {@link ITreeBox}
   * <p>
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=388227
   * 
   * @since 3.8.1
   */
  String PROP_CONTAINER = "container";

  void initTree() throws ProcessingException;

  void disposeTree();

  /**
   * sets the node as selected node and processes the url.
   * <p>
   * see {@link #selectNode(ITreeNode)}
   */
  void doHyperlinkAction(ITreeNode node, URL url) throws ProcessingException;

  void requestFocus();

  IMenu[] getMenus();

  void setMenus(IMenu[] a);

  /**
   * Convenience to find a menu, uses {@link ActionFinder}
   */
  <T extends IMenu> T getMenu(Class<T> menuType) throws ProcessingException;

  /**
   * @see #setScrollToSelection()
   */
  boolean isScrollToSelection();

  /**
   * @param b
   *          true: advices the attached ui to make the current selection visible.
   *          The current selection will be scrolled to visible (again, whenever the table size changes).
   */
  void setScrollToSelection(boolean b);

  /**
   * May be used when {@link #isScrollToSelection()} = false on individual occasion where selection shall be scrolled to
   * visible, the property scrollToVisible remains untouched.
   * <p>
   * This is a one-time scroll advise to the ui
   */
  void scrollToSelection();

  ITreeNodeFilter[] getNodeFilters();

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

  void setRootNode(ITreeNode root);

  String getTitle();

  void setTitle(String s);

  String getIconId();

  void setIconId(String iconId);

  /**
   * @return path using delimiter " - "
   */
  String getPathText(ITreeNode node);

  String getPathText(ITreeNode node, String delimiter);

  /**
   * A virtual node is a marker tree node used to optimize performance in large trees.
   * It is used mainly in the {@link IPage}, {@link IOutline} area with {@link VirtualPage}s
   * <p>
   * This method resolves a virtual node by its real node and generates a {@link TreeEvent#TYPE_NODES_UPDATED} event.
   * <p>
   * Basically all ui calls such as drag, drop, select, expand etc. automatically call this method. Further also
   * {@link IPage#getChildPage(int)} and {@link IPage#getChildPages()} automatically calls this method.
   * <p>
   * see {@link IVirtualTreeNode} and {@link VirtualPage}
   */
  ITreeNode resolveVirtualNode(ITreeNode node) throws ProcessingException;

  /**
   * see {@link #resolveVirtualNode(ITreeNode)}
   */
  ITreeNode[] resolveVirtualNodes(ITreeNode[] nodes) throws ProcessingException;

  Object getProperty(String name);

  /**
   * With this method it's possible to set (custom) properties.
   * <p>
   * <b>Important: </b> Although this method is intended to be used for custom properties, it's actually possible to
   * change main properties as well. Keep in mind that directly changing main properties may result in unexpected
   * behavior, so do it only if you really know what you are doing. Rather use the officially provided api instead. <br>
   * Example for an unexpected behavior: setVisible() does not only set the property PROP_VISIBLE but also executes
   * additional code. This code would NOT be executed by directly setting the property PROP_VISIBLE with setProperty().
   */
  void setProperty(String name, Object value);

  boolean hasProperty(String name);

  boolean isAutoTitle();

  void setAutoTitle(boolean b);

  boolean isDragEnabled();

  void setDragEnabled(boolean b);

  ITreeNode findNode(Object primaryKey);

  ITreeNode[] findNodes(Object[] primaryKeys);

  boolean isRootNodeVisible();

  void setRootNodeVisible(boolean b);

  boolean isRootHandlesVisible();

  void setRootHandlesVisible(boolean b);

  void ensureVisible(ITreeNode node);

  void expandAll(ITreeNode parent);

  void collapseAll(ITreeNode parent);

  boolean isEnabled();

  void setEnabled(boolean b);

  boolean isEnabledGranted();

  void setEnabledGranted(boolean b);

  void setEnabledPermission(Permission p);

  int getSelectedNodeCount();

  ITreeNode getSelectedNode();

  ITreeNode[] getSelectedNodes();

  boolean isSelectedNode(ITreeNode node);

  void selectNode(ITreeNode node);

  void selectNode(ITreeNode node, boolean append);

  void selectNodes(ITreeNode[] node, boolean append);

  void deselectNode(ITreeNode node);

  void deselectNodes(ITreeNode[] nodes);

  /**
   * Select the previous selectable node in this tree. Does not expand any
   * nodes.
   */
  void selectPreviousNode();

  /**
   * Select the next visible and selectable node in this tree. Does not expand
   * any nodes.
   */
  void selectNextNode();

  /**
   * Select the first visible and selectable node in this tree. Does not expand
   * any nodes.
   */
  void selectFirstNode();

  /**
   * Select the last visible and selectable node in this tree. Does not expand
   * any nodes.
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
  ITreeNode[] getCheckedNodes();

  /**
   * Container of this tree, {@link IPage}, {@link ITreeField}, {@link ITreeBox}
   * <p>
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=388227
   * 
   * @since 3.8.1
   */
  Object getContainer();

  /**
   * @return true if parent is equal to child or parent is an ancestor of child
   * @since 03.07.2009
   */
  boolean isAncestorNodeOf(ITreeNode parent, ITreeNode child);

  void addTreeListener(TreeListener listener);

  void removeTreeListener(TreeListener listener);

  /**
   * Add the listener at the top (front) of the listener list (so it is called as LAST listener).
   * <p>
   * This method is normally only used by the ui layer to update its state before other listeners handle them
   * <p>
   * Use {@link #addTreeListener(TreeListener)} in all other cases
   */
  void addUITreeListener(TreeListener listener);

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
   * true if multiple nodes can be selected (default false)
   */
  boolean isMultiSelect();

  void setMultiSelect(boolean b);

  /**
   * true if multiple nodes can be checked (default true)
   */
  boolean isMultiCheck();

  void setMultiCheck(boolean b);

  boolean isCheckable();

  void setCheckable(boolean b);

  /**
   * This is a hint for the UI if it is not capable of having variable node height based on node contents
   * (such as rap/rwt).
   * <p>
   * This hint defines the node height in pixels being used as the fixed node height for all nodes of this tree.
   * 
   * @return the hint in pixels, default is -1
   */
  int getNodeHeightHint();

  /**
   * see {@link #getNodeHeightHint()}
   */
  void setNodeHeightHint(int h);

  boolean isAutoDiscardOnDelete();

  void setAutoDiscardOnDelete(boolean on);

  boolean isTreeChanging();

  void setTreeChanging(boolean b);

  boolean isNodeExpanded(ITreeNode node);

  void setNodeExpanded(ITreeNode node, boolean b);

  void setNodeEnabledPermission(ITreeNode node, Permission p);

  boolean isNodeEnabled(ITreeNode node);

  void setNodeEnabled(ITreeNode node, boolean b);

  boolean isNodeEnabledGranted(ITreeNode node);

  void setNodeEnabledGranted(ITreeNode node, boolean b);

  void setNodeVisiblePermission(ITreeNode node, Permission p);

  boolean isNodeVisible(ITreeNode node);

  void setNodeVisible(ITreeNode node, boolean b);

  boolean isNodeVisibleGranted(ITreeNode node);

  void setNodeVisibleGranted(ITreeNode node, boolean b);

  boolean isNodeLeaf(ITreeNode node);

  void setNodeLeaf(ITreeNode node, boolean b);

  void setNodeChecked(ITreeNode node, boolean b);

  boolean isNodeChecked(ITreeNode node);

  int getNodeStatus(ITreeNode node);

  void setNodeStatus(ITreeNode node, int status);

  IKeyStroke[] getKeyStrokes();

  void setKeyStrokes(IKeyStroke[] keyStrokes);

  /*
   * modifications
   */

  /**
   * append a child node to the end of the children of parent
   */
  void addChildNode(ITreeNode parent, ITreeNode child);

  /**
   * append a list of child nodes or a complete subtree to the end of the
   * children of parent
   */
  void addChildNodes(ITreeNode parent, ITreeNode[] children);

  void addChildNode(int startIndex, ITreeNode parent, ITreeNode child);

  void addChildNodes(int startIndex, ITreeNode parent, ITreeNode[] children);

  void updateNode(ITreeNode node);

  void updateChildNodes(ITreeNode parent, ITreeNode[] children);

  void updateChildNodeOrder(ITreeNode parent, ITreeNode[] newChildren);

  void removeChildNode(ITreeNode parent, ITreeNode child);

  void removeChildNodes(ITreeNode parent, ITreeNode[] children);

  void removeAllChildNodes(ITreeNode parent);

  void removeNode(ITreeNode node);

  /**
   * clear removed rows from cache
   */
  void clearDeletedNodes();

  int getDeletedNodeCount();

  ITreeNode[] getDeletedNodes();

  int getInsertedNodeCount();

  ITreeNode[] getInsertedNodes();

  int getUpdatedNodeCount();

  ITreeNode[] getUpdatedNodes();

  boolean visitTree(ITreeVisitor v);

  boolean visitVisibleTree(ITreeVisitor v);

  boolean visitNode(ITreeNode node, ITreeVisitor v);

  /**
   * unload all children and mark node as not loaded
   */
  void unloadNode(ITreeNode node) throws ProcessingException;

  /**
   * extract transfer data to be sent to the backend
   * <p>
   * The single root node is not exported, the export starts with the first level after the root node
   */
  void exportTreeData(AbstractTreeFieldData target) throws ProcessingException;

  /**
   * apply transfer data to this tree
   * <p>
   * All nodes are imported starting under the (existing) root node
   */
  void importTreeData(AbstractTreeFieldData source) throws ProcessingException;

  /**
   * To obtain the menus that passed checks such as visibility, empty space action, ... for the given nodes.
   * Please be cautious as depending on the given nodes, there might be a node mismatch among the selected tree nodes
   * and the menu context node.
   * This method is not part of the public API.
   * 
   * @param nodes
   *          the nodes whose menus should be returned
   * @return
   */
  IMenu[] fetchMenusForNodesInternal(ITreeNode[] nodes);

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
   * @param b
   *          {@code true} advices the attached UI to save or restore its horizontal and vertical coordinates of its
   *          scrollbars.
   */
  void setSaveAndRestoreScrollbars(boolean b);
}
