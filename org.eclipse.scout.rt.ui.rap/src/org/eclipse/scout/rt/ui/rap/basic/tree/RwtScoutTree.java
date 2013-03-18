/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.basic.tree;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeListener;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.ext.MenuAdapterEx;
import org.eclipse.scout.rt.ui.rap.ext.tree.TreeEx;
import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.rap.form.fields.AbstractRwtScoutDndSupport;
import org.eclipse.scout.rt.ui.rap.keystroke.IRwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.keystroke.RwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.widgets.MarkupValidator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;

/**
 * @since 3.8.0
 */
@SuppressWarnings("restriction")
public class RwtScoutTree extends RwtScoutComposite<ITree> implements IRwtScoutTree {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutTree.class);

  private P_ScoutTreeListener m_scoutTreeListener;

  private TreeViewer m_treeViewer;

  private boolean m_enabledFromScout = true;
  private IRwtKeyStroke[] m_keyStrokes;

  private String m_variant = "";

  public RwtScoutTree() {
  }

  public RwtScoutTree(String variant) {
    m_variant = variant;
  }

  @Override
  protected void initializeUi(Composite parent) {
    TreeViewer viewer = createTreeModel(parent);
    setUiTreeViewer(viewer);
    setUiField(viewer.getTree());

    initNodeHeight();
    initializeTreeModel();

    viewer.getTree().setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
    viewer.getTree().setData(MarkupValidator.MARKUP_VALIDATION_DISABLED, Boolean.TRUE);
    // listeners
    viewer.addSelectionChangedListener(new P_RwtSelectionListener());
    viewer.addTreeListener(new P_RwtExpansionListener());
    viewer.addDoubleClickListener(new P_RwtDoubleClickListener());

    P_RwtTreeListener treeListener = new P_RwtTreeListener();
    viewer.getTree().addListener(SWT.MouseDown, treeListener);
    viewer.getTree().addListener(SWT.MouseUp, treeListener);
    viewer.getTree().addListener(SWT.MenuDetect, treeListener);

    getUiEnvironment().addKeyStroke(viewer.getTree(), new P_RwtToggleKeyStroke(), false);
  }

  protected TreeViewer createTreeModel(Composite parent) {
    int style = isMultiSelect() ? SWT.MULTI : SWT.SINGLE;
    style |= SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL;
    TreeEx tree = getUiEnvironment().getFormToolkit().createTree(parent, style);
    if (StringUtility.hasText(m_variant)) {
      tree.setData(RWT.CUSTOM_VARIANT, m_variant);
    }
    tree.setLayoutDeferred(true);
    // Measure item call ends up in a layout error on windows 2000
    // tree.addListener(SWT.MeasureItem, new Listener(){
    // public void handleEvent(Event event) {
    // // event.height = (int)(event.gc.getFontMetrics().getHeight() *1.5);
    // }
    // });
    TreeViewer viewer = new TreeViewer(tree);
    viewer.setUseHashlookup(true);
    return viewer;
  }

  @Override
  public boolean isUiDisposed() {
    return getUiField() == null || getUiField().isDisposed();
  }

  protected void initializeTreeModel() {
    // model
    RwtScoutTreeModel model = createTreeModel();
    getUiTreeViewer().setContentProvider(model);
    getUiTreeViewer().setLabelProvider(model);
    getUiTreeViewer().setInput(model);
  }

  protected RwtScoutTreeModel createTreeModel() {
    return new RwtScoutTreeModel(getScoutObject(), this, getUiTreeViewer());
  }

  protected boolean isMultiSelect() {
    if (getScoutObject() != null) {
      return getScoutObject().isMultiSelect();
    }
    else {
      return false;
    }
  }

  protected void setUiTreeViewer(TreeViewer viewer) {
    m_treeViewer = viewer;
  }

  @Override
  public TreeViewer getUiTreeViewer() {
    return m_treeViewer;
  }

  protected ITreeContentProvider getContentProvider() {
    return (ITreeContentProvider) getUiTreeViewer().getContentProvider();
  }

  @Override
  public TreeEx getUiField() {
    return (TreeEx) super.getUiField();
  }

  protected void initNodeHeight() {
    int height = UiDecorationExtensionPoint.getLookAndFeel().getTreeNodeHeight();
    if (height >= 0) {
      getUiField().setData(RWT.CUSTOM_ITEM_HEIGHT, height);
    }
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    if (getScoutObject() == null) {
      return;
    }
    if (m_scoutTreeListener == null) {
      m_scoutTreeListener = new P_ScoutTreeListener();
      getScoutObject().addUITreeListener(m_scoutTreeListener);
    }
    if (getScoutObject().isRootNodeVisible()) {
      setExpansionFromScout(getScoutObject().getRootNode());
    }
    else {
      for (ITreeNode node : getScoutObject().getRootNode().getFilteredChildNodes()) {
        setExpansionFromScout(node);
      }
    }
    setSelectionFromScout(getScoutObject().getSelectedNodes());
    setKeyStrokeFormScout();
    setNodeHeightFromScout();
    attachDndSupport();
    handleEventsFromRecentHistory();
  }

  protected void attachDndSupport() {
    if (UiDecorationExtensionPoint.getLookAndFeel().isDndSupportEnabled()) {
      new P_DndSupport(getScoutObject(), getScoutObject(), getUiField());
    }
  }

  private void handleEventsFromRecentHistory() {
    //handle events from recent history
    final IEventHistory<TreeEvent> h = getScoutObject().getEventHistory();
    if (h == null) {
      return;
    }

    getUiEnvironment().getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        for (TreeEvent e : h.getRecentEvents()) {
          handleScoutTreeEventInUi(e);
        }
      }
    });
  }

  @Override
  protected void detachScout() {
    super.detachScout();
    if (getScoutObject() == null) {
      return;
    }
    if (m_scoutTreeListener != null) {
      getScoutObject().removeTreeListener(m_scoutTreeListener);
      m_scoutTreeListener = null;
    }
  }

  @Override
  public void setEnabledFromScout(boolean enabled) {
    m_enabledFromScout = enabled;
    if (getUiField() instanceof TreeEx) {
      (getUiField()).setReadOnly(!enabled);
    }
    else {
      getUiField().setEnabled(enabled);
    }
  }

  public boolean isEnabledFromScout() {
    return m_enabledFromScout;
  }

  protected void setExpansionFromScout(ITreeNode scoutNode) {
    if (scoutNode != null) {
      setExpansionFromScoutRec(scoutNode);
    }
  }

  private void setExpansionFromScoutRec(ITreeNode scoutNode) {
    boolean exp;
    if (scoutNode.getParentNode() == null) {
      exp = true;
    }
    else {
      exp = scoutNode.isExpanded();
    }
    ITreeNode[] filteredChildNodes = scoutNode.getFilteredChildNodes();
    boolean hasChilds = filteredChildNodes.length > 0;
    if (hasChilds && exp != getUiTreeViewer().getExpandedState(scoutNode)) {
      getUiTreeViewer().setExpandedState(scoutNode, exp);
    }
    if (exp) {
      for (ITreeNode childNode : filteredChildNodes) {
        setExpansionFromScoutRec(childNode);
      }
    }
  }

  protected void setSelectionFromScout(ITreeNode[] scoutNodes) {
    if (getUiField().isDisposed()) {
      return;
    }
    getUiTreeViewer().setSelection(new StructuredSelection(scoutNodes));
    updateScrollToSelectionFromScout();
  }

  protected void setKeyStrokeFormScout() {
    // remove old
    if (m_keyStrokes != null) {
      for (IRwtKeyStroke keyStroke : m_keyStrokes) {
        getUiEnvironment().removeKeyStroke(getUiField(), keyStroke);
      }
    }
    // add new
    ArrayList<IRwtKeyStroke> newKeyStrokes = new ArrayList<IRwtKeyStroke>();
    IKeyStroke[] scoutKeyStrokes = getScoutObject().getKeyStrokes();
    for (IKeyStroke scoutKeyStroke : scoutKeyStrokes) {
      IRwtKeyStroke[] strokes = RwtUtility.getKeyStrokes(scoutKeyStroke, getUiEnvironment());
      for (IRwtKeyStroke stroke : strokes) {
        getUiEnvironment().addKeyStroke(getUiField(), stroke, false);
        newKeyStrokes.add(stroke);
      }
    }
    m_keyStrokes = newKeyStrokes.toArray(new IRwtKeyStroke[newKeyStrokes.size()]);
  }

  protected void setNodeHeightFromScout() {
    int h = getScoutObject().getNodeHeightHint();
    if (h >= 0) {
      getUiField().setData(RWT.CUSTOM_ITEM_HEIGHT, h);
    }
    if (isCreated()) {
      getUiTreeViewer().refresh();
    }
  }

  /**
   * bsi ticket 95090: avoid multiple and excessive tree structure updates
   */
  protected void handleScoutTreeEventBatchInUi(List<TreeEvent> eventList) {
    //phase 1: collect all parent nodes that need to be refreshed and refresh once per node
    HashSet<ITreeNode> processedParentNodes = new HashSet<ITreeNode>();
    for (TreeEvent e : eventList) {
      ITreeNode parentNode = null;
      switch (e.getType()) {
        case TreeEvent.TYPE_NODES_INSERTED:
        case TreeEvent.TYPE_NODES_UPDATED:
        case TreeEvent.TYPE_NODES_DELETED:
        case TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED: {
          parentNode = e.getCommonParentNode();
          break;
        }
        case TreeEvent.TYPE_NODE_FILTER_CHANGED: {
          parentNode = getScoutObject().getRootNode();
          break;
        }
      }
      if (parentNode != null) {
        if (!processedParentNodes.contains(parentNode)) {
          processedParentNodes.add(parentNode);
          updateTreeStructureAndKeepSelection(parentNode);
          setExpansionFromScout(parentNode);
        }
      }
    }
    //phase 2: apply remaining events
    for (TreeEvent e : eventList) {
      switch (e.getType()) {
        case TreeEvent.TYPE_REQUEST_FOCUS: {
          getUiField().setFocus();
          break;
        }
        case TreeEvent.TYPE_NODE_EXPANDED:
        case TreeEvent.TYPE_NODE_COLLAPSED: {
          setExpansionFromScout(e.getNode());
          break;
        }
        case TreeEvent.TYPE_NODES_SELECTED: {
          setSelectionFromScout(e.getNodes());
          break;
        }
        case TreeEvent.TYPE_SCROLL_TO_SELECTION: {
          scrollToSelection();
          break;
        }
      }
    }
  }

  private void updateTreeStructureAndKeepSelection(ITreeNode node) {
    if (getUiTreeViewer() != null && getUiTreeViewer().getTree() != null && !getUiTreeViewer().getTree().isDisposed()) {
      if (node == getScoutObject().getRootNode()) {
        getUiTreeViewer().refresh();
      }
      else {
        getUiTreeViewer().refresh(node);
      }
    }
  }

  protected void setSelectionFromRwt(final ITreeNode[] nodes) {
    if (getUpdateUiFromScoutLock().isAcquired()) {
      return;
    }
    if (getScoutObject() != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          try {
            addIgnoredScoutEvent(TreeEvent.class, "" + TreeEvent.TYPE_NODES_SELECTED);
            getScoutObject().getUIFacade().setNodesSelectedFromUI(nodes);
          }
          finally {
            removeIgnoredScoutEvent(TreeEvent.class, "" + TreeEvent.TYPE_NODES_SELECTED);
          }
        }
      };
      getUiEnvironment().invokeScoutLater(t, 0);
      // end notify
    }
  }

  protected void updateScrollToSelectionFromScout() {
    if (getScoutObject().isScrollToSelection()) {
      scrollToSelection();
    }
  }

  /**
   * @rn imo, 05.03.2009, tickets #73324, #73707, #74018
   * @rn imo, 18.11.2009, ticket #83255
   */
  protected void scrollToSelection() {
    if (getUiField() != null && !getUiField().isDisposed()) {
      getUiField().showSelection();
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (name.equals(ITree.PROP_KEY_STROKES)) {
      setKeyStrokeFormScout();
    }
    else if (name.equals(ITree.PROP_SCROLL_TO_SELECTION)) {
      updateScrollToSelectionFromScout();
    }
    else if (name.equals(ITree.PROP_NODE_HEIGHT_HINT)) {
      setNodeHeightFromScout();
    }
    super.handleScoutPropertyChange(name, newValue);
  }

  protected void setExpansionFromUi(final ITreeNode node, final boolean expanded) {
    if (getUpdateUiFromScoutLock().isAcquired()) {
      return;
    }
    if (getScoutObject() != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          if (node.isExpanded() != expanded) {
            getScoutObject().getUIFacade().setNodeExpandedFromUI(node, expanded);
          }
        }
      };
      getUiEnvironment().invokeScoutLater(t, 0);
      // end notify
    }
  }

  /**
   * model thread: scout table observer
   */
  protected boolean isHandleScoutTreeEvent(TreeEvent[] a) {
    for (TreeEvent element : a) {
      switch (element.getType()) {
        case TreeEvent.TYPE_REQUEST_FOCUS:
        case TreeEvent.TYPE_NODE_EXPANDED:
        case TreeEvent.TYPE_NODE_COLLAPSED:
        case TreeEvent.TYPE_NODES_INSERTED:
        case TreeEvent.TYPE_NODES_UPDATED:
        case TreeEvent.TYPE_NODES_DELETED:
        case TreeEvent.TYPE_NODE_FILTER_CHANGED:
        case TreeEvent.TYPE_NODES_SELECTED:
        case TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED:
        case TreeEvent.TYPE_SCROLL_TO_SELECTION: {
          return true;
        }
      }
    }
    return false;
  }

  protected void handleScoutTreeEventInUi(TreeEvent e) {
    if (isUiDisposed()) {
      return;
    }
    switch (e.getType()) {
      case TreeEvent.TYPE_NODES_INSERTED:
      case TreeEvent.TYPE_NODES_DELETED:
      case TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED: {
        updateTreeStructureAndKeepSelection(e.getCommonParentNode());
        setExpansionFromScout(e.getCommonParentNode());
        break;
      }
      case TreeEvent.TYPE_NODES_UPDATED: {
        //in case a virtual node was resolved, check if selection still valid
        ISelection oldSelection = getUiTreeViewer().getSelection();
        ISelection newSelection = new StructuredSelection(getScoutObject().getSelectedNodes());
        updateTreeStructureAndKeepSelection(e.getCommonParentNode());
        if (!newSelection.equals(oldSelection)) {
          getUiTreeViewer().setSelection(newSelection);
        }
        setExpansionFromScout(e.getCommonParentNode());
        break;
      }
      case TreeEvent.TYPE_NODE_FILTER_CHANGED: {
        updateTreeStructureAndKeepSelection(getScoutObject().getRootNode());
        setExpansionFromScout(getScoutObject().getRootNode());
        break;
      }
      case TreeEvent.TYPE_REQUEST_FOCUS: {
        getUiField().setFocus();
        break;
      }
      case TreeEvent.TYPE_NODE_EXPANDED:
      case TreeEvent.TYPE_NODE_COLLAPSED: {
        setExpansionFromScout(e.getNode());
        break;
      }
      case TreeEvent.TYPE_NODES_SELECTED: {
        setSelectionFromScout(e.getNodes());
        break;
      }
      case TreeEvent.TYPE_SCROLL_TO_SELECTION: {
        scrollToSelection();
        break;
      }
    }
  }

  protected void handleUiNodeClick(final ITreeNode node) {
    if (getScoutObject() != null) {
      if (node != null) {
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().fireNodeClickFromUI(node);
          }
        };
        getUiEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
    }
  }

  protected void handleUiNodeAction(final ITreeNode node) {
    if (getScoutObject() != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().fireNodeActionFromUI(node);
        }
      };
      getUiEnvironment().invokeScoutLater(t, 400);
      // end notify
    }
  }

  /**
   * TODO not used yet; attach to rwt tree with styled or html cells
   */
  protected void handleUiHyperlinkAction(final ITreeNode node, final URL url) {
    if (getScoutObject() != null && node != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().fireHyperlinkActionFromUI(node, url);
        }
      };
      getUiEnvironment().invokeScoutLater(t, 0);
      // end notify
    }
  }

  // static convenience helpers
  public static TreePath scoutNodeToTreePath(ITreeNode scoutNode) {
    if (scoutNode == null) {
      return null;
    }
    Object[] path = getPathToRoot(scoutNode, 0);
    return new TreePath(path);
  }

  public static TreePath[] scoutNodesToTreePaths(ITreeNode[] scoutNodes) {
    if (scoutNodes == null) {
      return new TreePath[0];
    }
    TreePath[] paths = new TreePath[scoutNodes.length];
    for (int i = 0; i < scoutNodes.length; i++) {
      paths[i] = scoutNodeToTreePath(scoutNodes[i]);
    }
    return paths;
  }

  public static ITreeNode[] getPathToRoot(ITreeNode scoutNode, int depth) {
    ITreeNode[] retNodes;
    if (scoutNode == null) {
      if (depth == 0) {
        return null;
      }
      else {
        retNodes = new ITreeNode[depth];
      }
    }
    else {
      depth++;
      if (scoutNode.getParentNode() == null) {
        retNodes = new ITreeNode[depth];
      }
      else {
        retNodes = getPathToRoot(scoutNode.getParentNode(), depth);
      }
      retNodes[retNodes.length - depth] = scoutNode;
    }
    return retNodes;
  }

  /*
   * private inner classes
   */
  private class P_ScoutTreeListener implements TreeListener {
    @Override
    public void treeChanged(final TreeEvent e) {
      if (isHandleScoutTreeEvent(new TreeEvent[]{e})) {
        if (isIgnoredScoutEvent(TreeEvent.class, "" + e.getType())) {
          return;
        }
        Runnable t = new Runnable() {
          @Override
          public void run() {
            try {
              getUpdateUiFromScoutLock().acquire();
              //
              handleScoutTreeEventInUi(e);
            }
            finally {
              getUpdateUiFromScoutLock().release();
            }
          }
        };
        getUiEnvironment().invokeUiLater(t);
      }
    }

    @Override
    public void treeChangedBatch(final TreeEvent[] a) {
      if (isHandleScoutTreeEvent(a)) {
        final ArrayList<TreeEvent> filteredList = new ArrayList<TreeEvent>();
        for (TreeEvent element : a) {
          if (!isIgnoredScoutEvent(TreeEvent.class, "" + element.getType())) {
            filteredList.add(element);
          }
        }
        if (filteredList.size() == 0) {
          return;
        }
        Runnable t = new Runnable() {
          @Override
          public void run() {
            try {
              getUpdateUiFromScoutLock().acquire();
              if (!getUiField().isDisposed()) {
                getUiField().setRedraw(false);
              }
              //
              handleScoutTreeEventBatchInUi(filteredList);
            }
            finally {
              getUpdateUiFromScoutLock().release();
              if (!getUiField().isDisposed()) {
                getUiField().setRedraw(true);
              }
            }
          }
        };
        getUiEnvironment().invokeUiLater(t);
      }
    }
  }// end private class

  private class P_RwtSelectionListener implements ISelectionChangedListener {
    @Override
    @SuppressWarnings("unchecked")
    public void selectionChanged(SelectionChangedEvent event) {
      if (isEnabledFromScout()) {
        StructuredSelection sel = (StructuredSelection) event.getSelection();
        ITreeNode[] nodes = (ITreeNode[]) sel.toList().toArray(new ITreeNode[sel.size()]);
        setSelectionFromRwt(nodes);
      }
    }
  } // end class P_RwtSelectionListener

  private Menu createMenu() {
    if (getUiField().getMenu() != null) {
      getUiField().getMenu().dispose();
      getUiField().setMenu(null);
    }

    Menu contextMenu = new Menu(getUiField().getShell(), SWT.POP_UP);
    contextMenu.addMenuListener(new P_ContextMenuListener());
    getUiField().setMenu(contextMenu);

    return contextMenu;
  }

  private void createAndShowMenu(Point location) {
    Menu menu = createMenu();
    showMenu(menu, location);
  }

  private void showMenu(Menu menu, Point location) {
    menu.setLocation(location);
    menu.setVisible(true);
  }

  private void handleUiDoubleClick(StructuredSelection sel) {
    @SuppressWarnings("unchecked")
    ITreeNode[] nodes = (ITreeNode[]) sel.toList().toArray(new ITreeNode[sel.size()]);
    if (nodes != null && nodes.length == 1) {
      // if not leaf expand collapse
      if (!nodes[0].isLeaf()) {
        // invert expansion
        setExpansionFromUi(nodes[0], !getUiTreeViewer().getExpandedState(nodes[0]));
      }
      else {
        handleUiNodeAction(nodes[0]);
        if (getScoutObject().isCheckable()) {
          handleUiNodeClick(nodes[0]);
        }
      }
    }
  }

  protected void handleUiToggleAction(Event event) {
    if (!event.doit || !getScoutObject().isCheckable()) {
      return;
    }
    if (event.stateMask == 0 && event.keyCode == ' ') {
      StructuredSelection sel = (StructuredSelection) getUiTreeViewer().getSelection();
      @SuppressWarnings("unchecked")
      ITreeNode[] nodes = (ITreeNode[]) sel.toList().toArray(new ITreeNode[sel.size()]);
      if (nodes != null && nodes.length > 0) {
        handleUiNodeClick(nodes[0]);
      }
      event.doit = false;
    }
  }

  private class P_RwtTreeListener implements Listener {
    private static final long serialVersionUID = 1L;

    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.MouseUp: {
          ViewerCell cell = getUiTreeViewer().getCell(new Point(event.x, event.y));
          if (cell != null && cell.getElement() instanceof ITreeNode) {
            ITreeNode nodeToClick = (ITreeNode) cell.getElement();
            handleUiNodeClick(nodeToClick);
          }
          break;
        }
        case SWT.Resize: {
          updateScrollToSelectionFromScout();
          break;
        }
        case SWT.MenuDetect: {
          Point eventPosition = new Point(event.x, event.y);
          createAndShowMenu(eventPosition);
          break;
        }
      }
    }
  }

  private class P_RwtExpansionListener implements ITreeViewerListener {
    @Override
    public void treeCollapsed(TreeExpansionEvent event) {
      setExpansionFromUi((ITreeNode) event.getElement(), false);
    }

    @Override
    public void treeExpanded(TreeExpansionEvent event) {
      setExpansionFromUi((ITreeNode) event.getElement(), true);
    }
  } // end class P_RwtExpansionListener

  private class P_RwtToggleKeyStroke extends RwtKeyStroke {
    private static final long serialVersionUID = 1L;

    public P_RwtToggleKeyStroke() {
      super(' ');
    }

    @Override
    public void handleUiAction(Event e) {
      handleUiToggleAction(e);
    }
  } // end class P_RwtKeyReturnAvoidDoubleClickListener

  private class P_RwtDoubleClickListener implements IDoubleClickListener {
    @Override
    public void doubleClick(DoubleClickEvent event) {
      if (event.getSelection() instanceof StructuredSelection) {
        StructuredSelection sel = (StructuredSelection) event.getSelection();
        handleUiDoubleClick(sel);
      }
    }
  }

  private class P_ContextMenuListener extends MenuAdapterEx {
    private static final long serialVersionUID = 1L;

    public P_ContextMenuListener() {
      super(RwtScoutTree.this.getUiTreeViewer().getTree(), RwtScoutTree.this.getUiTreeViewer().getTree().getParent());
    }

    @Override
    public void menuShown(MenuEvent e) {
      super.menuShown(e);

      if (getScoutObject() == null || !isEnabledFromScout()) {
        return;
      }

      final boolean emptySpace = (getUiField().getContextItem() == null);
      IMenu[] menus = RwtMenuUtility.collectMenus(getScoutObject(), emptySpace, !emptySpace, getUiEnvironment());

      Menu menu = ((Menu) e.getSource());
      RwtMenuUtility.fillContextMenu(menus, getUiEnvironment(), menu);
    }

  } // end class P_ContextMenuListener

  private class P_DndSupport extends AbstractRwtScoutDndSupport {
    public P_DndSupport(IPropertyObserver scoutObject, IDNDSupport scoutDndSupportable, Control control) {
      super(scoutObject, scoutDndSupportable, control, RwtScoutTree.this.getUiEnvironment());
    }

    @Override
    protected TransferObject handleUiDragRequest() {
      final Holder<TransferObject> result = new Holder<TransferObject>(TransferObject.class, null);
      Runnable t = new Runnable() {
        @Override
        public void run() {
          TransferObject scoutTransferable = getScoutObject().getUIFacade().fireNodesDragRequestFromUI();
          result.setValue(scoutTransferable);
        }
      };
      try {
        getUiEnvironment().invokeScoutLater(t, 20000).join(20000);
      }
      catch (InterruptedException e) {
        //nop
      }
      return result.getValue();
    }

    @Override
    protected void handleUiDropAction(DropTargetEvent event, final TransferObject scoutTransferObject) {
      Object dropTarget = event.item != null ? event.item.getData() : null;
      final ITreeNode node = dropTarget instanceof ITreeNode ? (ITreeNode) dropTarget : null;
      Runnable job = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().fireNodeDropActionFromUI(node, scoutTransferObject);
        }
      };
      getUiEnvironment().invokeScoutLater(job, 200);
    }
  }// end class P_DndSupport

}
