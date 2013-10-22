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
package org.eclipse.scout.rt.ui.swt.basic.tree;

import java.net.URL;
import java.util.ArrayList;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
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
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.SwtMenuUtility;
import org.eclipse.scout.rt.ui.swt.basic.SwtScoutComposite;
import org.eclipse.scout.rt.ui.swt.ext.tree.TreeEx;
import org.eclipse.scout.rt.ui.swt.form.fields.AbstractSwtScoutDndSupport;
import org.eclipse.scout.rt.ui.swt.keystroke.ISwtKeyStroke;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * <h3>SwtScoutTree</h3> ...
 * 
 * @since 1.0.0 23.07.2008
 * @author Andreas Hoegger
 */
public class SwtScoutTree extends SwtScoutComposite<ITree> implements ISwtScoutTree {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutTree.class);

  private P_ScoutTreeListener m_scoutTreeListener;

  private Menu m_contextMenu;

  private TreeViewer m_treeViewer;

  private boolean m_enabledFromScout = true;
  private ISwtKeyStroke[] m_keyStrokes;

  public SwtScoutTree() {
  }

  @Override
  protected void initializeSwt(Composite parent) {
    TreeViewer viewer = createSwtTree(parent);
    setSwtTreeViewer(viewer);

    initializeSwtTreeModel();
    setSwtField(viewer.getTree());
    // listeners
    viewer.addSelectionChangedListener(new P_SwtSelectionListener());
    viewer.addTreeListener(new P_SwtExpansionListener());
    viewer.addDoubleClickListener(new P_SwtDoubleClickListener());

    P_SwtTreeListener swtTreeListener = new P_SwtTreeListener();
    viewer.getTree().addListener(SWT.MouseUp, swtTreeListener);
    viewer.getTree().addListener(SWT.KeyUp, swtTreeListener);
    viewer.getTree().addKeyListener(new P_SwtKeyReturnAvoidDoubleClickListener());

    // context menu
    m_contextMenu = new Menu(viewer.getTree().getShell(), SWT.POP_UP);
    m_contextMenu.addMenuListener(new P_ContextMenuListener());

    viewer.getTree().setMenu(m_contextMenu);

  }

  protected TreeViewer createSwtTree(Composite parent) {
    int style = isMultiSelect() ? SWT.MULTI : SWT.SINGLE;
    style |= SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL;
    TreeEx tree = getEnvironment().getFormToolkit().createTree(parent, style);
    tree.setLayoutDeferred(true);
    TreeViewer viewer = new TreeViewer(tree);
    viewer.setUseHashlookup(true);
    return viewer;
  }

  @Override
  public boolean isDisposed() {
    return getSwtField() == null || getSwtField().isDisposed();
  }

  protected void initializeSwtTreeModel() {
    // model
    SwtScoutTreeModel model = createTreeModel();
    getSwtTreeViewer().setContentProvider(model);
    getSwtTreeViewer().setLabelProvider(model);
    getSwtTreeViewer().setInput(model);
  }

  protected SwtScoutTreeModel createTreeModel() {
    return new SwtScoutTreeModel(getScoutObject(), getEnvironment(), getSwtTreeViewer());
  }

  protected boolean isMultiSelect() {
    if (getScoutObject() != null) {
      return getScoutObject().isMultiSelect();
    }
    else {
      return false;
    }
  }

  protected void setSwtTreeViewer(TreeViewer viewer) {
    m_treeViewer = viewer;

  }

  public TreeViewer getSwtTreeViewer() {
    return m_treeViewer;
  }

  protected ITreeContentProvider getContentProvider() {
    return (ITreeContentProvider) getSwtTreeViewer().getContentProvider();
  }

  @Override
  public TreeEx getSwtField() {
    return (TreeEx) super.getSwtField();
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
    // dnd support
    new P_DndSupport(getScoutObject(), getScoutObject(), getSwtField(), getEnvironment());
    //handle events from recent history
    final IEventHistory<TreeEvent> h = getScoutObject().getEventHistory();
    if (h != null) {
      getEnvironment().getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          for (TreeEvent e : h.getRecentEvents()) {
            handleScoutTreeEventInSwt(e);
          }
        }
      });
    }
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

  public void setEnabledFromScout(boolean enabled) {
    m_enabledFromScout = enabled;
    if (getSwtField() instanceof TreeEx) {
      ((TreeEx) getSwtField()).setReadOnly(!enabled);
    }
    else {
      getSwtField().setEnabled(enabled);
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
    if (hasChilds && exp != getSwtTreeViewer().getExpandedState(scoutNode)) {
      getSwtTreeViewer().setExpandedState(scoutNode, exp);
    }
    if (exp) {
      for (ITreeNode childNode : filteredChildNodes) {
        setExpansionFromScoutRec(childNode);
      }
    }
  }

  protected void setSelectionFromScout(ITreeNode[] scoutNodes) {
    if (getSwtField().isDisposed()) {
      return;
    }
    getSwtTreeViewer().setSelection(new StructuredSelection(scoutNodes));
    getSwtField().showSelection();
  }

  protected void setKeyStrokeFormScout() {
    // remove old
    if (m_keyStrokes != null) {
      for (ISwtKeyStroke swtKeyStroke : m_keyStrokes) {
        getEnvironment().removeKeyStroke(getSwtField(), swtKeyStroke);
      }
    }
    // add new
    ArrayList<ISwtKeyStroke> newSwtKeyStrokes = new ArrayList<ISwtKeyStroke>();
    IKeyStroke[] scoutKeyStrokes = getScoutObject().getKeyStrokes();
    for (IKeyStroke scoutKeyStroke : scoutKeyStrokes) {
      ISwtKeyStroke[] swtStrokes = SwtUtility.getKeyStrokes(scoutKeyStroke, getEnvironment());
      for (ISwtKeyStroke swtStroke : swtStrokes) {
        getEnvironment().addKeyStroke(getSwtField(), swtStroke);
        newSwtKeyStrokes.add(swtStroke);
      }
    }
    m_keyStrokes = newSwtKeyStrokes.toArray(new ISwtKeyStroke[newSwtKeyStrokes.size()]);
  }

  private void updateTreeStructureAndKeepSelection(ITreeNode node) {
    if (node == getScoutObject().getRootNode()) {
      getSwtTreeViewer().refresh();
    }
    else {
      getSwtTreeViewer().refresh(node);
    }
  }

  protected void setSelectionFromSwt(final ITreeNode[] nodes) {
    if (m_ignoreSelectionEventsFromSwtToScout) {
      return;
    }
    if (getUpdateSwtFromScoutLock().isAcquired()) {
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
      getEnvironment().invokeScoutLater(t, 0);
      // end notify
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
    getSwtField().showSelection();
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (name.equals(ITree.PROP_KEY_STROKES)) {
      setKeyStrokeFormScout();
    }
    else if (name.equals(ITree.PROP_SCROLL_TO_SELECTION)) {
      setScrollToSelectionFromScout();
    }
    super.handleScoutPropertyChange(name, newValue);
  }

  protected void setExpansionFromSwt(final ITreeNode node, final boolean b) {
    if (getUpdateSwtFromScoutLock().isAcquired()) {
      return;
    }
    if (getScoutObject() != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          if (node.isExpanded() != b) {
            getScoutObject().getUIFacade().setNodeExpandedFromUI(node, b);
          }
        }
      };
      getEnvironment().invokeScoutLater(t, 0);
      // end notify
    }
  }

  /**
   * Workaround for misbehaviour of {@link StructuredViewer#handleSelect} when receiving a selection event due to the
   * WM_DISPOSE of the selected tree node.
   * <p>
   * The WM_DISPOSE of the deleted selected tree node causes windows to send a WM_SELECT of the successor node
   * (intelligence).
   * <p>
   * StructuredViewer ignores this event's item and uses its own getSelection() to create a new SelectionEvent. This
   * results in a useless empty selection event.
   * <p>
   * This flag and the method {@link #ignoreSelectionEventsFromSwtToScoutUntilNextDisplayPost()} works around that
   * issue.
   */
  private boolean m_ignoreSelectionEventsFromSwtToScout;

  private void ignoreSelectionEventsFromSwtToScoutUntilNextDisplayPost() {
    m_ignoreSelectionEventsFromSwtToScout = true;
    getSwtField().getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        m_ignoreSelectionEventsFromSwtToScout = false;
      }
    });
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

  protected void handleScoutTreeEventInSwt(TreeEvent e) {
    if (isDisposed()) {
      return;
    }
    ignoreSelectionEventsFromSwtToScoutUntilNextDisplayPost();
    switch (e.getType()) {
      case TreeEvent.TYPE_REQUEST_FOCUS: {
        getSwtField().setFocus();
        break;
      }
      case TreeEvent.TYPE_NODE_EXPANDED:
      case TreeEvent.TYPE_NODE_COLLAPSED: {
        setExpansionFromScout(e.getNode());
        break;
      }
      case TreeEvent.TYPE_NODES_INSERTED: {
        updateTreeStructureAndKeepSelection(e.getCommonParentNode());
        setExpansionFromScout(e.getCommonParentNode());
        break;
      }
      case TreeEvent.TYPE_NODES_UPDATED: {
        //in case a virtual node was resolved, check if selection still valid
        ISelection oldSelection = getSwtTreeViewer().getSelection();
        ISelection newSelection = new StructuredSelection(getScoutObject().getSelectedNodes());
        updateTreeStructureAndKeepSelection(e.getCommonParentNode());
        if (!newSelection.equals(oldSelection)) {
          getSwtTreeViewer().setSelection(newSelection);
        }
        setExpansionFromScout(e.getCommonParentNode());
        break;
      }
      case TreeEvent.TYPE_NODES_DELETED: {
        updateTreeStructureAndKeepSelection(e.getCommonParentNode());
        setExpansionFromScout(e.getCommonParentNode());
        break;
      }
      case TreeEvent.TYPE_NODE_FILTER_CHANGED: {
        updateTreeStructureAndKeepSelection(getScoutObject().getRootNode());
        setExpansionFromScout(getScoutObject().getRootNode());
        // ensure first filtered item is shown.
        if (getSwtField().getSelectionCount() == 0 && getSwtField().getItemCount() > 0) {
          getSwtField().showItem(getSwtField().getItem(0));
        }
        break;
      }
      case TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED: {
        updateTreeStructureAndKeepSelection(e.getCommonParentNode());
        setExpansionFromScout(e.getCommonParentNode());
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

  protected void handleSwtNodeClick(final ITreeNode node) {
    if (getScoutObject() != null) {
      if (node != null) {
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().fireNodeClickFromUI(node);
          }
        };
        getEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
    }
  }

  protected void handleSwtNodeAction(final ITreeNode node) {
    if (getScoutObject() != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().fireNodeActionFromUI(node);
        }
      };
      getEnvironment().invokeScoutLater(t, 400);
      // end notify
    }
  }

  /**
   * TODO not used yet; attach to swt tree with styled or html cells
   */
  protected void handleSwtHyperlinkAction(final ITreeNode node, final URL url) {
    if (getScoutObject() != null && node != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().fireHyperlinkActionFromUI(node, url);
        }
      };
      getEnvironment().invokeScoutLater(t, 0);
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
              getUpdateSwtFromScoutLock().acquire();
              //
              handleScoutTreeEventInSwt(e);
            }
            finally {
              getUpdateSwtFromScoutLock().release();
            }
          }
        };
        getEnvironment().invokeSwtLater(t);
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
              getUpdateSwtFromScoutLock().acquire();
              if (!getSwtField().isDisposed()) {
                getSwtField().setRedraw(false);
              }
              //
              for (TreeEvent element : filteredList) {
                handleScoutTreeEventInSwt(element);
              }
            }
            finally {
              getUpdateSwtFromScoutLock().release();
              if (!getSwtField().isDisposed()) {
                getSwtField().setRedraw(true);
              }
            }
          }
        };
        getEnvironment().invokeSwtLater(t);
      }
    }
  }// end private class

  private class P_SwtSelectionListener implements ISelectionChangedListener {
    @Override
    @SuppressWarnings("unchecked")
    public void selectionChanged(SelectionChangedEvent event) {
      if (isEnabledFromScout()) {
        StructuredSelection sel = (StructuredSelection) event.getSelection();
        ITreeNode[] nodes = (ITreeNode[]) sel.toList().toArray(new ITreeNode[sel.size()]);
        setSelectionFromSwt(nodes);
      }
    }
  } // end class P_SwtSelectionListener

  private class P_SwtTreeListener implements Listener {
    @Override
    public void handleEvent(Event e) {
      switch (e.type) {
        case SWT.MouseUp: {
          //missing single-click listener on tree viewer, add on tree itself (ticket 87693)
          if (e.count == 1) {
            ViewerCell cell = getSwtTreeViewer().getCell(new Point(e.x, e.y));
            if (cell != null && cell.getElement() instanceof ITreeNode) {
              ITreeNode nodeToClick = (ITreeNode) cell.getElement();
              if (getScoutObject().isCheckable()) {
                // find checkbox area
                Rectangle imgBounds = cell.getImageBounds();
                if (imgBounds != null && e.x >= (imgBounds.x) && e.x <= (imgBounds.x + imgBounds.width)) {
                  handleSwtNodeClick(nodeToClick);
                }
              }
              else {
                handleSwtNodeClick(nodeToClick);
              }
            }
          }
          break;
        }
        case SWT.KeyUp: {
          if (getScoutObject().isCheckable()) {
            if (e.stateMask == 0) {
              switch (e.keyCode) {
                case ' ':
                  StructuredSelection sel = (StructuredSelection) getSwtTreeViewer().getSelection();
                  @SuppressWarnings("unchecked")
                  ITreeNode[] nodes = (ITreeNode[]) sel.toList().toArray(new ITreeNode[sel.size()]);
                  if (nodes != null && nodes.length > 0) {
                    handleSwtNodeClick(nodes[0]);
                  }
                  e.doit = false;
                  break;
              }
            }
          }
          break;
        }
      }
    }
  }

  private class P_SwtExpansionListener implements ITreeViewerListener {
    @Override
    public void treeCollapsed(TreeExpansionEvent event) {
      setExpansionFromSwt((ITreeNode) event.getElement(), false);
    }

    @Override
    public void treeExpanded(TreeExpansionEvent event) {
      setExpansionFromSwt((ITreeNode) event.getElement(), true);
    }
  } // end class P_SwtExpansionListener

  /**
   * @rn sle, 03.12.2010, ticket #97056
   */
  private class P_SwtKeyReturnAvoidDoubleClickListener implements KeyListener {
    @Override
    public void keyPressed(KeyEvent e) {
      if (e != null
          && (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR)) {
        //to avoid the postEvent(DoubleClickEvent) from Tree.WM_CHAR(...) set e.doit to false
        e.doit = false;
      }
    }

    @Override
    public void keyReleased(KeyEvent e) {
      //do nothing
    }
  } // end class P_SwtKeyReturnAvoidDoubleClickListener

  private class P_SwtDoubleClickListener implements IDoubleClickListener {
    @Override
    @SuppressWarnings("unchecked")
    public void doubleClick(DoubleClickEvent event) {
      if (event.getSelection() instanceof StructuredSelection) {
        StructuredSelection sel = (StructuredSelection) event.getSelection();
        ITreeNode[] nodes = (ITreeNode[]) sel.toList().toArray(new ITreeNode[sel.size()]);
        if (nodes != null && nodes.length == 1) {
          // if not leaf expand collapse
          if (!nodes[0].isLeaf()) {
            // invert expansion
            setExpansionFromSwt(nodes[0], !getSwtTreeViewer().getExpandedState(nodes[0]));
          }
          else {
            handleSwtNodeAction(nodes[0]);
            if (getScoutObject().isCheckable()) {
              handleSwtNodeClick(nodes[0]);
            }
          }
        }
      }
    }
  }

  private class P_ContextMenuListener extends MenuAdapter {
    @Override
    public void menuShown(MenuEvent e) {
      // clear all previous
      // Windows BUG: fires menu hide before the selection on the menu item is
      // propagated.
      if (m_contextMenu != null) {
        for (MenuItem item : m_contextMenu.getItems()) {
          disposeMenuItem(item);
        }
      }

      if (getScoutObject() == null || !isEnabledFromScout()) {
        return;
      }

      final boolean emptySpace = (getSwtField().getContextItem() == null);
      IMenu[] menus = SwtMenuUtility.collectMenus(getScoutObject(), emptySpace, !emptySpace, getEnvironment());

      SwtMenuUtility.fillContextMenu(menus, m_contextMenu, getEnvironment());
    }

    private void disposeMenuItem(MenuItem item) {
      Menu menu = item.getMenu();
      if (menu != null) {
        for (MenuItem childItem : menu.getItems()) {
          disposeMenuItem(childItem);
        }
        menu.dispose();
      }
      item.dispose();
    }

  } // end class P_ContextMenuListener

  private class P_DndSupport extends AbstractSwtScoutDndSupport {
    public P_DndSupport(IPropertyObserver scoutObject, IDNDSupport scoutDndSupportable, Control control, ISwtEnvironment environment) {
      super(scoutObject, scoutDndSupportable, control, environment);
    }

    @Override
    protected TransferObject handleSwtDragRequest() {
      final Holder<TransferObject> result = new Holder<TransferObject>(TransferObject.class, null);
      Runnable t = new Runnable() {
        @Override
        public void run() {
          TransferObject scoutTransferable = getScoutObject().getUIFacade().fireNodesDragRequestFromUI();
          result.setValue(scoutTransferable);
        }
      };
      try {
        getEnvironment().invokeScoutLater(t, 20000).join(20000);
      }
      catch (InterruptedException e) {
        //nop
      }
      return result.getValue();
    }

    @Override
    protected void handleSwtDropAction(DropTargetEvent event, final TransferObject scoutTransferObject) {
      Object dropTarget = event.item != null ? event.item.getData() : null;
      final ITreeNode node = dropTarget instanceof ITreeNode ? (ITreeNode) dropTarget : null;
      Runnable job = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().fireNodeDropActionFromUI(node, scoutTransferObject);
        }
      };
      getEnvironment().invokeScoutLater(job, 200);
    }
  }// end class P_DndSupport

}
