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
package org.eclipse.scout.rt.ui.swing.basic.tree;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeListener;
import org.eclipse.scout.rt.ui.swing.SwingPopupWorker;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.action.SwingScoutAction;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.dnd.TransferHandlerEx;
import org.eclipse.scout.rt.ui.swing.ext.JScrollPaneEx;
import org.eclipse.scout.rt.ui.swing.ext.JTreeEx;
import org.eclipse.scout.rt.ui.swing.ext.MouseClickedBugFix;

/**
 * The prefix SwingScout... denotes a model COMPOSITION between a swing and a
 * scout component
 */
public class SwingScoutTree extends SwingScoutComposite<ITree> implements ISwingScoutTree {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutTree.class);

  private P_ScoutTreeListener m_scoutTreeListener;
  private JScrollPane m_swingScrollPane;
  // cache
  private IKeyStroke[] m_installedScoutKs;

  public SwingScoutTree() {
  }

  @Override
  protected void initializeSwing() {
    JTreeEx tree = new JTreeEx();
    m_swingScrollPane = new JScrollPaneEx(tree);
    m_swingScrollPane.setBackground(tree.getBackground());
    setSwingField(tree);
    // swing properties
    tree.setDragEnabled(true);
    // models
    tree.setModel(new SwingTreeModel(this));
    tree.setSelectionModel(new DefaultTreeSelectionModel());
    // renderers
    tree.setCellRenderer(new SwingTreeCellRenderer(getSwingEnvironment(), tree.getCellRenderer(), this));
    // listeners
    tree.addMouseListener(new P_SwingMouseListener());
    tree.addTreeSelectionListener(new P_SwingSelectionListener());
    tree.addTreeExpansionListener(new P_SwingExpansionListener());
    // attach drag and remove default transfer handler
    P_SwingDragAndDropTransferHandler th = new P_SwingDragAndDropTransferHandler();
    tree.setTransferHandler(th);
    //ticket 87030
    //attach delayed resize: make selection visible
    m_swingScrollPane.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        ITree t = getScoutObject();
        if (t != null && t.isScrollToSelection()) {
          if (e.getComponent().isShowing()) {
            SwingUtilities.invokeLater(new Runnable() {
              @Override
              public void run() {
                scrollToSelection();
              }
            });
          }
        }
      }
    });
    //add context menu key stroke
    tree.getInputMap(JComponent.WHEN_FOCUSED).put(SwingUtility.createKeystroke("CONTEXT_MENU"), "contextMenu");
    tree.getActionMap().put("contextMenu", new AbstractAction() {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        if (getUpdateSwingFromScoutLock().isAcquired()) return;
        //
        if (getScoutObject() != null) {
          TreePath selectedPath = getSwingTree().getSelectionPath();
          if (selectedPath != null) {
            final Point p = getSwingTree().getPathBounds(selectedPath).getLocation();
            final Component source = getSwingTree();
            p.translate(2, 2);
            // notify Scout
            Runnable t = new Runnable() {
              @Override
              public void run() {
                IMenu[] scoutMenus = getScoutObject().getUIFacade().fireNodePopupFromUI();
                // call swing menu
                new SwingPopupWorker(getSwingEnvironment(), source, p, scoutMenus).enqueue();
              }
            };
            getSwingEnvironment().invokeScoutLater(t, 5678);
            // end notify
          }
        }
      }
    });
  }

  @Override
  public JTreeEx getSwingTree() {
    return (JTreeEx) getSwingField();
  }

  protected TreeSelectionModel getSwingTreeSelectionModel() {
    return (TreeSelectionModel) getSwingTree().getSelectionModel();
  }

  @Override
  public JScrollPane getSwingScrollPane() {
    return m_swingScrollPane;
  }

  @Override
  protected void detachScout() {
    super.detachScout();
    if (getScoutObject() != null && m_scoutTreeListener != null) {
      getScoutObject().removeTreeListener(m_scoutTreeListener);
      m_scoutTreeListener = null;
    }
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    if (getScoutObject() != null) {
      if (m_scoutTreeListener == null) {
        m_scoutTreeListener = new P_ScoutTreeListener();
        getScoutObject().addUITreeListener(m_scoutTreeListener);
      }
      setMultiSelectFromScout(getScoutObject().isMultiSelect());
      setRootNodeVisibleFromScout();
      setRootHandlesVisibleFromScout();
      setExpansionFromScout(getScoutObject().getRootNode());
      setSelectionFromScout(getScoutObject().getSelectedNodes());
      setKeyStrokesFromScout();
      // add checkable key mappings
      if (getScoutObject().isCheckable()) {
        getSwingTree().getInputMap(JComponent.WHEN_FOCUSED).put(SwingUtility.createKeystroke("SPACE"), "toggleRow");
        getSwingTree().getActionMap().put("toggleRow", new AbstractAction() {
          private static final long serialVersionUID = 1L;

          @Override
          public void actionPerformed(ActionEvent e) {
            handleSwingNodeClick(getSwingTree().getSelectionPath());
          }
        });
      }
    }
  }

  /*
   * scout settings
   */

  protected void setMultiSelectFromScout(boolean on) {
    if (on) {
      getSwingTree().getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    }
    else {
      getSwingTree().getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }
  }

  protected void setExpansionFromScout(ITreeNode scoutNode) {
    setExpansionFromScoutRec(scoutNode);
    revertSelectionFromScout();
    // ensure that the lead path is set
    if (getSwingTree().getLeadSelectionPath() == null) {
      TreeModel swingModel = getSwingTree().getModel();
      Object leadNode = null;
      if (getSwingTree().isRootVisible()) {
        leadNode = swingModel.getRoot();
      }
      else {
        Object root = swingModel.getRoot();
        if (root != null) {
          leadNode = swingModel.getChild(root, 0);
        }
      }
      if (leadNode != null) {
        getSwingTree().setLeadSelectionPath(scoutNodeToTreePath((ITreeNode) leadNode));
      }
    }
  }

  private void setExpansionFromScoutRec(ITreeNode scoutNode) {
    boolean exp;
    if (scoutNode.getParentNode() == null) exp = true;
    else exp = scoutNode.isExpanded();
    //
    TreePath path = scoutNodeToTreePath(scoutNode);
    if (exp) {
      getSwingTree().expandPath(path);
    }
    else {
      if (getSwingTree().isExpanded(path)) {
        getSwingTree().collapsePath(path);
      }
    }
    // children only if node was expanded
    if (exp) {
      ITreeNode[] childs = scoutNode.getFilteredChildNodes();
      for (int i = 0; i < childs.length; i++) {
        setExpansionFromScoutRec(childs[i]);
      }
    }
  }

  protected void setExpansionFromSwing(TreePath path, final boolean b) {
    if (getUpdateSwingFromScoutLock().isAcquired()) return;
    //
    if (getScoutObject() != null) {
      final ITreeNode scoutNode = treePathToScoutNode(path);
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().setNodeExpandedFromUI(scoutNode, b);
        }
      };

      getSwingEnvironment().invokeScoutLater(t, 0);
      // end notify
    }
  }

  protected void setRootNodeVisibleFromScout() {
    getSwingTree().setRootVisible(getScoutObject().isRootNodeVisible());
    getSwingTree().repaint();
  }

  protected void setRootHandlesVisibleFromScout() {
    getSwingTree().setShowsRootHandles(getScoutObject().isRootHandlesVisible());
    getSwingTree().repaint();
  }

  protected void setSelectionFromScout(ITreeNode[] newScoutNodes) {
    ITreeNode[] oldScoutNodes = treePathsToScoutNodes(getSwingTree().getSelectionPaths());
    if (!CompareUtility.equals(oldScoutNodes, newScoutNodes)) {
      TreePath[] paths = scoutNodesToTreePaths(newScoutNodes);
      TreePath anchorPath = getSwingTree().getAnchorSelectionPath();
      TreePath leadPath = getSwingTree().getLeadSelectionPath();
      getSwingTree().setSelectionPaths(paths);
      getSwingTree().setAnchorSelectionPath(anchorPath);
      getSwingTree().setLeadSelectionPath(leadPath);
    }
  }

  protected void revertSelectionFromScout() {
    ITreeNode[] newScoutNodes = getScoutObject().getSelectedNodes();
    ITreeNode[] oldScoutNodes = treePathsToScoutNodes(getSwingTree().getSelectionPaths());
    if (!CompareUtility.equals(oldScoutNodes, newScoutNodes)) {
      TreePath[] paths = scoutNodesToTreePaths(newScoutNodes);
      getSwingTree().setSelectionPaths(paths);
    }
  }

  protected void setKeyStrokesFromScout() {
    JComponent component = getSwingContainer();
    if (component == null) {
      component = getSwingField();
    }
    if (component != null) {
      // remove old key strokes
      if (m_installedScoutKs != null) {
        for (int i = 0; i < m_installedScoutKs.length; i++) {
          IKeyStroke scoutKs = m_installedScoutKs[i];
          KeyStroke swingKs = SwingUtility.createKeystroke(scoutKs);
          //
          InputMap imap = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
          imap.remove(swingKs);
          ActionMap amap = component.getActionMap();
          amap.remove(scoutKs.getActionId());
        }
      }
      m_installedScoutKs = null;
      // add new key strokes
      IKeyStroke[] scoutKeyStrokes = getScoutObject().getKeyStrokes();
      for (IKeyStroke scoutKs : scoutKeyStrokes) {
        int swingWhen = JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
        KeyStroke swingKs = SwingUtility.createKeystroke(scoutKs);
        SwingScoutAction<IAction> action = new SwingScoutAction<IAction>();
        action.createField(scoutKs, getSwingEnvironment());
        //
        InputMap imap = component.getInputMap(swingWhen);
        imap.put(swingKs, scoutKs.getActionId());
        ActionMap amap = component.getActionMap();
        amap.put(scoutKs.getActionId(), action.getSwingAction());
      }
      m_installedScoutKs = scoutKeyStrokes;
    }
  }

  /**
   * Fires changes of selection as well as changes on lead/anchor indices
   */
  protected void setSelectionFromSwing(TreePath[] paths) {
    if (getUpdateSwingFromScoutLock().isAcquired()) return;
    //
    if (getScoutObject() != null) {
      if (paths != null && paths.length > 0) {
        final ITreeNode[] scoutNodes = treePathsToScoutNodes(paths);
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            try {
              addIgnoredScoutEvent(TreeEvent.class, "" + TreeEvent.TYPE_NODES_SELECTED);
              //
              getScoutObject().getUIFacade().setNodesSelectedFromUI(scoutNodes);
            }
            finally {
              removeIgnoredScoutEvent(TreeEvent.class, "" + TreeEvent.TYPE_NODES_SELECTED);
            }
          }
        };
        getSwingEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
      else {
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            try {
              addIgnoredScoutEvent(TreeEvent.class, "" + TreeEvent.TYPE_NODES_SELECTED);
              //
              getScoutObject().getUIFacade().setNodesSelectedFromUI(null);
            }
            finally {
              removeIgnoredScoutEvent(TreeEvent.class, "" + TreeEvent.TYPE_NODES_SELECTED);
            }
          }
        };
        getSwingEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
    }
  }

  protected void setScrollToSelectionFromScout() {
    if (getScoutObject().isScrollToSelection()) {
      scrollToSelection();
    }
  }

  /**
   * @rn imo, 05.03.2009, tickets #73324, #73707, #74018
   * @rn imo, 18.11.2009, ticket #83255
   */
  protected void scrollToSelection() {
    int[] selectedRows = getSwingTree().getSelectionRows();
    if (selectedRows != null && selectedRows.length > 0) {
      int index = selectedRows[0];
      int rowCount = getSwingTree().getRowCount();
      if (index >= 0 && index < rowCount) {
        TreePath selectedPath = getSwingTree().getPathForRow(index);
        int nextIndex = index;
        while (nextIndex + 1 < rowCount) {
          TreePath path = getSwingTree().getPathForRow(nextIndex + 1);
          if (path.getPathCount() > selectedPath.getPathCount()) {
            nextIndex = nextIndex + 1;
          }
          else {
            break;
          }
        }
        if (nextIndex != index) {
          getSwingTree().scrollRowToVisible(nextIndex);
        }
        getSwingTree().scrollRowToVisible(index);
      }
    }
  }

  /**
   * scout property observer
   */
  @Override
  protected void handleScoutPropertyChange(String propName, Object newValue) {
    if (propName.equals(ITree.PROP_MULTI_SELECT)) {
      setMultiSelectFromScout(((Boolean) newValue).booleanValue());
    }
    else if (propName.equals(ITree.PROP_ROOT_NODE_VISIBLE)) {
      setRootNodeVisibleFromScout();
    }
    else if (propName.equals(ITree.PROP_ROOT_HANDLES_VISIBLE)) {
      setRootHandlesVisibleFromScout();
    }
    else if (propName.equals(ITree.PROP_KEY_STROKES)) {
      setKeyStrokesFromScout();
    }
    else if (propName.equals(ITree.PROP_SCROLL_TO_SELECTION)) {
      setScrollToSelectionFromScout();
    }
  }

  /**
   * scout table observer
   */
  protected boolean isHandleScoutTreeEvent(TreeEvent[] a) {
    for (int i = 0; i < a.length; i++) {
      switch (a[i].getType()) {
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

  protected void handleScoutTreeEventInSwing(TreeEvent e) {
    switch (e.getType()) {
      case TreeEvent.TYPE_NODES_INSERTED: {
        updateTreeStructureAndKeepSelectionFromScout(e.getCommonParentNode());
        setExpansionFromScout(e.getCommonParentNode());
        break;
      }
      case TreeEvent.TYPE_NODES_UPDATED: {
        updateTreeStructureAndKeepSelectionFromScout(e.getCommonParentNode());
        setExpansionFromScout(e.getCommonParentNode());
        break;
      }
      case TreeEvent.TYPE_NODES_DELETED: {
        updateTreeStructureAndKeepSelectionFromScout(e.getCommonParentNode());
        setExpansionFromScout(e.getCommonParentNode());
        break;
      }
      case TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED: {
        updateTreeStructureAndKeepSelectionFromScout(e.getCommonParentNode());
        setExpansionFromScout(e.getCommonParentNode());
        break;
      }
      case TreeEvent.TYPE_NODE_FILTER_CHANGED: {
        updateTreeStructureAndKeepSelectionFromScout(getScoutObject().getRootNode());
        setExpansionFromScout(getScoutObject().getRootNode());
        break;
      }
      case TreeEvent.TYPE_REQUEST_FOCUS: {
        getSwingTree().requestFocus();
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

  /**
   * bsi ticket 95090: avoid multiple and excessive tree structure updates
   */
  protected void handleScoutTreeEventBatchInSwing(List<TreeEvent> eventList) {
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
          updateTreeStructureAndKeepSelectionFromScout(parentNode);
          setExpansionFromScout(parentNode);
        }
      }
    }
    //phase 2: apply remaining events
    for (TreeEvent e : eventList) {
      switch (e.getType()) {
        case TreeEvent.TYPE_REQUEST_FOCUS: {
          getSwingTree().requestFocus();
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

  private void updateTreeStructureAndKeepSelectionFromScout(ITreeNode node) {
    if (getScoutObject() != null) {
      SwingTreeModel swingTreeModel = (SwingTreeModel) getSwingTree().getModel();
      swingTreeModel.fireStructureChanged(node);
      TreePath[] paths = scoutNodesToTreePaths(getScoutObject().getSelectedNodes());
      getSwingTree().setSelectionPaths(paths);
    }
  }

  protected boolean handleSwingDragEnabled() {
    if (getUpdateSwingFromScoutLock().isAcquired()) return false;
    //
    final Holder<Boolean> result = new Holder<Boolean>(Boolean.class, false);
    if (getScoutObject() != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          boolean enabled = getScoutObject().getUIFacade().getNodesDragEnabledFromUI();
          result.setValue(enabled);
        }
      };
      try {
        getSwingEnvironment().invokeScoutLater(t, 2345).join(2345);
      }
      catch (InterruptedException e) {
        //nop
      }
      // end notify
    }
    return result.getValue();
  }

  protected Transferable handleSwingDragRequest() {
    if (getUpdateSwingFromScoutLock().isAcquired()) return null;
    //
    final Holder<TransferObject> result = new Holder<TransferObject>(TransferObject.class, null);
    if (getScoutObject() != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          TransferObject scoutTransferable = getScoutObject().getUIFacade().fireNodesDragRequestFromUI();
          result.setValue(scoutTransferable);
        }
      };
      try {
        getSwingEnvironment().invokeScoutLater(t, 20000).join(20000);
      }
      catch (InterruptedException e) {
        //nop
      }
      // end notify
    }
    TransferObject scoutTransferable = result.getValue();
    Transferable swingTransferable = SwingUtility.createSwingTransferable(scoutTransferable);
    return swingTransferable;
  }

  protected void handleSwingDropAction(TreePath path, Transferable swingTransferable) {
    if (getUpdateSwingFromScoutLock().isAcquired()) return;
    //
    if (swingTransferable != null) {
      if (getScoutObject() != null) {
        final ITreeNode scoutNode = treePathToScoutNode(path);
        final TransferObject scoutTransferable = SwingUtility.createScoutTransferable(swingTransferable);
        if (scoutTransferable != null) {
          // notify Scout (asynchronous !)
          Runnable t = new Runnable() {
            @Override
            public void run() {
              getScoutObject().getUIFacade().fireNodeDropActionFromUI(scoutNode, scoutTransferable);
            }
          };
          getSwingEnvironment().invokeScoutLater(t, 0);
          // end notify
        }
      }
    }
  }

  protected void handleSwingNodePopup(final MouseEvent e) {
    if (getUpdateSwingFromScoutLock().isAcquired()) return;
    //
    if (getScoutObject() != null) {
      TreePath path = getSwingTree().getPathForLocation(e.getX(), e.getY());
      // XXX imo
      final ITreeNode node = path != null ? (ITreeNode) path.getLastPathComponent() : null;
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          IMenu[] scoutMenus;
          if (node != null) {
            scoutMenus = getScoutObject().getUIFacade().fireNodePopupFromUI();
          }
          else {
            scoutMenus = getScoutObject().getUIFacade().fireEmptySpacePopupFromUI();
          }
          // call swing menu
          new SwingPopupWorker(getSwingEnvironment(), e.getComponent(), e.getPoint(), scoutMenus).enqueue();
        }
      };
      getSwingEnvironment().invokeScoutLater(t, 5678);
      // end notify
    }
  }

  protected void handleSwingNodeClick(TreePath path) {
    if (getUpdateSwingFromScoutLock().isAcquired()) return;
    //
    if (getScoutObject() != null) {
      final ITreeNode scoutNode = treePathToScoutNode(path);
      if (scoutNode != null) {
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().fireNodeClickFromUI(scoutNode);
          }
        };

        getSwingEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
    }
  }

  protected void handleSwingNodeAction(TreePath path) {
    if (getUpdateSwingFromScoutLock().isAcquired()) return;
    //
    if (getScoutObject() != null) {
      final ITreeNode scoutNode = treePathToScoutNode(path);
      if (scoutNode != null) {
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().fireNodeActionFromUI(scoutNode);
          }
        };
        getSwingEnvironment().invokeScoutLater(t, 400);
        // end notify
      }
    }
  }

  protected void handleSwingHyperlinkAction(TreePath path, final URL url) {
    if (getUpdateSwingFromScoutLock().isAcquired()) return;
    //
    if (getScoutObject() != null && path != null) {
      final ITreeNode scoutNode = treePathToScoutNode(path);
      if (scoutNode != null) {
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().fireHyperlinkActionFromUI(scoutNode, url);
          }
        };
        getSwingEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
    }
  }

  /*
   * static Convenience helpers
   */
  public static ITreeNode treePathToScoutNode(TreePath path) {
    if (path == null) return null;
    return (ITreeNode) path.getLastPathComponent();
  }

  public static ITreeNode[] treePathsToScoutNodes(TreePath[] paths) {
    if (paths == null) return new ITreeNode[0];
    ITreeNode[] scoutNodes = new ITreeNode[paths.length];
    for (int i = 0; i < paths.length; i++) {
      scoutNodes[i] = treePathToScoutNode(paths[i]);
    }
    return scoutNodes;
  }

  public static TreePath scoutNodeToTreePath(ITreeNode scoutNode) {
    if (scoutNode == null) return null;
    Object[] path = getPathToRoot(scoutNode, 0);
    return new TreePath(path);
  }

  public static TreePath[] scoutNodesToTreePaths(ITreeNode[] scoutNodes) {
    if (scoutNodes == null) return new TreePath[0];
    TreePath[] paths = new TreePath[scoutNodes.length];
    for (int i = 0; i < scoutNodes.length; i++) {
      paths[i] = scoutNodeToTreePath(scoutNodes[i]);
    }
    return paths;
  }

  public static ITreeNode[] getPathToRoot(ITreeNode scoutNode, int depth) {
    ITreeNode[] retNodes;
    if (scoutNode == null) {
      if (depth == 0) return null;
      else retNodes = new ITreeNode[depth];
    }
    else {
      depth++;
      if (scoutNode.getParentNode() == null) retNodes = new ITreeNode[depth];
      else retNodes = getPathToRoot(scoutNode.getParentNode(), depth);
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
        if (isIgnoredScoutEvent(TreeEvent.class, "" + e.getType())) return;
        //
        Runnable t = new Runnable() {
          @Override
          public void run() {
            try {
              getUpdateSwingFromScoutLock().acquire();
              //
              handleScoutTreeEventInSwing(e);
            }
            finally {
              getUpdateSwingFromScoutLock().release();
            }
          }
        };
        getSwingEnvironment().invokeSwingLater(t);
      }
    }

    @Override
    public void treeChangedBatch(final TreeEvent[] a) {
      //
      if (isHandleScoutTreeEvent(a)) {
        final ArrayList<TreeEvent> filteredList = new ArrayList<TreeEvent>();
        for (int i = 0; i < a.length; i++) {
          if (!isIgnoredScoutEvent(TreeEvent.class, "" + a[i].getType())) {
            filteredList.add(a[i]);
          }
        }
        if (filteredList.size() == 0) return;
        Runnable t = new Runnable() {
          @Override
          public void run() {
            try {
              getUpdateSwingFromScoutLock().acquire();
              //
              handleScoutTreeEventBatchInSwing(filteredList);
            }
            finally {
              getUpdateSwingFromScoutLock().release();
            }
          }
        };
        getSwingEnvironment().invokeSwingLater(t);
      }
    }
  }// end private class

  /**
   * Implementation of DropSource's DragGestureListener support for drag/drop
   * 
   * @since Build 202
   */
  private class P_SwingDragAndDropTransferHandler extends TransferHandlerEx {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean canDrag() {
      TreePath[] paths = getSwingTree().getSelectionPaths();
      if (paths != null && paths.length > 0) {
        return handleSwingDragEnabled();
      }
      else {
        return false;
      }
    }

    @Override
    public Transferable createTransferable(JComponent c) {
      TreePath[] paths = getSwingTree().getSelectionPaths();
      if (paths != null && paths.length > 0) {
        return handleSwingDragRequest();
      }
      else {
        return null;
      }
    }

    @Override
    public boolean importDataEx(JComponent comp, Transferable t, Point location) {
      if (location != null) {
        TreePath dropPath = getSwingTree().getPathForLocation(location.x, location.y);
        handleSwingDropAction(dropPath, t);
        return true;
      }
      return false;
    }
  }// end private class

  private class P_SwingSelectionListener implements TreeSelectionListener {
    @Override
    public void valueChanged(TreeSelectionEvent e) {
      setSelectionFromSwing(getSwingTree().getSelectionPaths());
    }
  }// end private class

  private class P_SwingExpansionListener implements TreeExpansionListener {
    @Override
    public void treeCollapsed(TreeExpansionEvent e) {
      TreePath path = e.getPath();
      if (path != null) {
        setExpansionFromSwing(path, false);
      }
    }

    @Override
    public void treeExpanded(TreeExpansionEvent e) {
      TreePath path = e.getPath();
      if (path != null) {
        setExpansionFromSwing(path, true);
      }
    }
  }// end private class

  private class P_SwingMouseListener extends MouseAdapter {
    private Point m_pressedLocation;
    MouseClickedBugFix fix;

    @Override
    public void mousePressed(MouseEvent e) {
      fix = new MouseClickedBugFix(e);
      m_pressedLocation = e.getPoint();
      e.getComponent().requestFocus();
      if (e.isMetaDown()) {
        TreePath path = getSwingTree().getPathForLocation(e.getPoint().x, e.getPoint().y);
        if (path != null && !getSwingTree().isPathSelected(path)) {
          getSwingTree().setSelectionPath(path);
        }
      }
      // Mac popup
      if (e.isPopupTrigger()) {
        handleSwingNodePopup(e);
      }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger()) {
        handleSwingNodePopup(e);
      }
      else {
        //hyperlink
        TreeHtmlLinkDetector detector = new TreeHtmlLinkDetector();
        if (detector.detect((JTree) e.getComponent(), e.getPoint())) {
          handleSwingHyperlinkAction(detector.getTreePath(), detector.getHyperlink());
        }
      }
      if (fix != null) fix.mouseReleased(this, e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
      if (fix.mouseClicked()) return;
      if (e.getButton() == MouseEvent.BUTTON1) {
        if (e.getClickCount() == 1) {
          // ticket 86377
          TreePath path = getSwingTree().getPathForLocation(m_pressedLocation.x, m_pressedLocation.y);
          if (path != null) {
            // no click on +/- icon
            if (e.getPoint().x >= getSwingTree().getPathBounds(path).x) {
              handleSwingNodeClick(path);
            }
          }
        }
        else if (e.getClickCount() == 2) {
          // ticket 86377
          TreePath path = getSwingTree().getPathForLocation(m_pressedLocation.x, m_pressedLocation.y);
          if (path != null) {
            handleSwingNodeAction(path);
          }
        }
      }
    }
  }// end private class

}
