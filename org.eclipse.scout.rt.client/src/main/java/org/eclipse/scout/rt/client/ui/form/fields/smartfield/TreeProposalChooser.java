/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNodeBuilder;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeVisitor;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Proposal chooser with a tree to choose hierarchical proposals. You can provide your own tree implementation when your
 * smart-field defines an inner AbstractTree class. You should call <code>smartField.acceptProposal()</code> in your
 * <code>tree.execNodeClick(ITreeNode, MouseButton)</code> method, otherwise the proposal-chooser isn't closed when a
 * proposal is selected in the tree.
 *
 * @since 6.0.0
 * @param <LOOKUP_KEY>
 */
public class TreeProposalChooser<LOOKUP_KEY> extends AbstractProposalChooser<ITree, LOOKUP_KEY> {
  private static final Logger LOG = LoggerFactory.getLogger(TreeProposalChooser.class);
  private final P_KeyLookupProvider m_keyLookupProvider;

  public TreeProposalChooser(IContentAssistField<?, LOOKUP_KEY> contentAssistField, boolean allowCustomText) {
    super(contentAssistField, allowCustomText);
    m_keyLookupProvider = new P_KeyLookupProvider();
  }

  @Override
  protected ITree createModel() {
    ITree tree = createConfiguredOrDefaultModel(ITree.class);
    tree.setDefaultIconId(m_contentAssistField.getBrowseIconId());
    return tree;
  }

  @Override
  protected ITree createDefaultModel() {
    return new P_DefaultProposalTree();
  }

  @Override
  @SuppressWarnings("unchecked")
  public ILookupRow<LOOKUP_KEY> getSelectedLookupRow() {
    ILookupRow<LOOKUP_KEY> row = null;
    ITreeNode node = null;
    if (m_model.isCheckable()) {
      Collection<ITreeNode> checkedNodes = m_model.getCheckedNodes();
      if (CollectionUtility.hasElements(checkedNodes)) {
        node = CollectionUtility.firstElement(checkedNodes);
      }
    }
    else {
      node = m_model.getSelectedNode();
    }
    if (node != null && node.isFilterAccepted() && node.isEnabled()) {
      row = (ILookupRow<LOOKUP_KEY>) node.getCell().getValue();
    }
    return row;
  }

  @Override
  public void dispose() {
    m_model.disposeTree();
    m_model = null;
  }

  @Override
  public void forceProposalSelection() {
    m_model.selectNextNode();
  }

  @Override
  protected void dataFetchedDelegateImpl(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result, int maxCount) {
    if (result.getException() == null) {
      List<ITreeNode> subTree = getSubtree(result);
      ITreeNode parentNode = getParent(result);

      try {
        if (m_model != null) {
          m_model.setTreeChanging(true);
          updateSubTree(m_model, parentNode, subTree);
          expand();
          selectNode(result);
        }

      }
      finally {
        if (m_model != null) {
          m_model.setTreeChanging(false);
        }
      }
    }
    updateStatus(result);
  }

  /**
   * Selects a node depending on the search, if available. Selected node must be loaded.
   */
  private void selectNode(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result) {
    if (result.getSearchParam().isSelectCurrentValue()) {
      ITreeNode currentValueNode = getNode(m_contentAssistField.getValueAsLookupKey());
      if (currentValueNode != null) {
        selectValue(currentValueNode);
      }
    }
    else {
      //select first search text
      selectNodeByText(result.getSearchParam().getSearchText());
    }
  }

  private void expand() {
    if (m_contentAssistField.isBrowseAutoExpandAll() && !m_contentAssistField.isBrowseLoadIncremental()) {
      m_model.expandAll(m_model.getRootNode());
    }
    else {
      expandNodesWithChildren();
    }
  }

  private ITreeNode getParent(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result) {
    if (m_model != null) {
      if (result.getSearchParam().getParentKey() == null) {
        return m_model.getRootNode();
      }
      return getNode(result.getSearchParam().getParentKey());
    }
    return null;
  }

  private List<ITreeNode> getSubtree(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result) {
    List<? extends ILookupRow<LOOKUP_KEY>> rows = getRows(result);
    boolean markChildrenLoaded = !m_contentAssistField.isBrowseLoadIncremental() || !result.getSearchParam().isByParentSearch();
    return new P_TreeNodeBuilder()
        .createTreeNodes(rows, ITreeNode.STATUS_NON_CHANGED, markChildrenLoaded);
  }

  /**
   * @return all new rows to be inserted (including parents of search result)
   */
  private List<? extends ILookupRow<LOOKUP_KEY>> getRows(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result) {
    if (m_contentAssistField.isLoadParentNodes()) {
      IKeyLookupProvider<LOOKUP_KEY> loopProvider;
      if (m_contentAssistField.isBrowseLoadIncremental()) {
        loopProvider = m_keyLookupProvider;
      }
      else {
        loopProvider = new P_AllKeyLookupProvider();
      }

      LOOKUP_KEY parent = result.getSearchParam().getParentKey();
      return new IncrementalTreeBuilder<LOOKUP_KEY>(loopProvider)
          .getRowsWithParents(result.getLookupRows(), parent, m_model);
    }
    else {
      return result.getLookupRows();
    }
  }

  /**
   * find first node with matching text
   */
  private void selectNodeByText(final String searchText) {
    if (searchText != null) {
      m_model.visitTree(new ITreeVisitor() {

        @Override
        public boolean visit(ITreeNode node) {
          if (searchText.equals(node.getCell().getText())) {
            selectValue(node);
            return false;
          }
          return true;
        }
      });
    }
  }

  private void expandNodesWithChildren() {
    m_model.visitTree(new ITreeVisitor() {

      @Override
      public boolean visit(ITreeNode node) {
        if (node.getChildNodeCount() > 0) {
          node.setChildrenLoaded(true);
          node.setExpanded(true);
        }
        return true;
      }
    });
  }

  private void selectValue(ITreeNode node) {
    if (node != null) {
      m_model.selectNode(node);
      if (m_model.isCheckable()) {
        m_model.setNodeChecked(node, true);
      }
    }
  }

  /**
   * Node with a given key. Assumes the node is already loaded at this point
   */
  private ITreeNode getNode(final LOOKUP_KEY key) {
    final Holder<ITreeNode> holder = new Holder<>(ITreeNode.class);
    m_model.visitTree(new ITreeVisitor() {

      @Override
      public boolean visit(ITreeNode node) {
        if (node.getCell().getValue() instanceof ILookupRow && ObjectUtility.equals(((ILookupRow) node.getCell().getValue()).getKey(), key)) {
          holder.setValue(node);
          return false;
        }
        return true;
      }
    });
    return holder.getValue();
  }

  private void updateSubTree(ITree tree, final ITreeNode parentNode, List<ITreeNode> subTree) {
    if (tree == null || parentNode == null || subTree == null) {
      return;
    }
    tree.removeAllChildNodes(parentNode);
    tree.addChildNodes(parentNode, subTree);
    parentNode.setChildrenLoaded(true);
  }

  public void loadChildNodes(ITreeNode parentNode) {
    if (m_model != null) {
      try {
        m_model.setTreeChanging(true);
        //
        execLoadChildNodes(parentNode);
      }
      finally {
        m_model.setTreeChanging(false);
      }
    }
  }

  protected ITreeNode createTreeNode() {
    return new P_InternalTreeNode();
  }

  @ConfigOperation
  @Order(10)
  @SuppressWarnings("unchecked")
  protected void execLoadChildNodes(final ITreeNode parentNode) {
    if (m_contentAssistField.isBrowseLoadIncremental()) {
      ILookupRow<LOOKUP_KEY> b = (LookupRow) (parentNode != null ? parentNode.getCell().getValue() : null);
      LOOKUP_KEY parentKey = b != null ? b.getKey() : null;
      getContentAssistField().doSearch(ContentAssistSearchParam.createParentParam(parentKey, false), false);
      if (parentNode != null) {
        parentNode.setChildrenLoaded(true);
      }
    }
  }

  /**
   * Override this method to change that behaviour of what is a single match. By default a single match is when there is
   * a single enabled LEAF node in the tree
   */
  @ConfigOperation
  @Order(40)
  @Override
  protected ILookupRow<LOOKUP_KEY> execGetSingleMatch() {
    final List<ILookupRow<LOOKUP_KEY>> foundLeafs = new ArrayList<>();
    ITreeVisitor v = new ITreeVisitor() {
      @Override
      public boolean visit(ITreeNode node) {
        if (node.isEnabled() && node.isLeaf()) {
          @SuppressWarnings("unchecked")
          ILookupRow<LOOKUP_KEY> row = (ILookupRow<LOOKUP_KEY>) node.getCell().getValue();
          if (row != null && row.isEnabled()) {
            foundLeafs.add(row);
          }
        }
        return foundLeafs.size() <= 2;
      }
    };
    m_model.visitVisibleTree(v);
    if (foundLeafs.size() == 1) {
      return foundLeafs.get(0);
    }
    else {
      return null;
    }
  }

  @Override
  public void deselect() {
    m_model.deselectNode(m_model.getSelectedNode());
  }

  /**
   * Default tree class used when smart-field doesn't provide a custom tree class.
   */
  private class P_DefaultProposalTree extends AbstractTree {

    @Override
    protected void execNodeClick(ITreeNode node, MouseButton mouseButton) {
      if (node.isEnabled()) {
        m_contentAssistField.acceptProposal();
      }
    }

    @Override
    protected boolean getConfiguredScrollToSelection() {
      return true;
    }
  }

  private class P_TreeNodeBuilder extends AbstractTreeNodeBuilder<LOOKUP_KEY> {
    @Override
    protected ITreeNode createEmptyTreeNode() {
      ITreeNode node = TreeProposalChooser.this.createTreeNode();
      if (m_model != null && m_model.getDefaultIconId() != null) {
        Cell cell = node.getCellForUpdate();
        cell.setIconId(m_model.getDefaultIconId());
      }
      return node;
    }
  }

  /**
   * TreeNode implementation with delegation of loadChildren to this.loadChildNodes()
   */
  private class P_InternalTreeNode extends AbstractTreeNode {

    @Override
    public void loadChildren() {
      TreeProposalChooser.this.loadChildNodes(this);
    }
  }

  private class P_KeyLookupProvider implements IKeyLookupProvider<LOOKUP_KEY> {

    @Override
    public ILookupRow<LOOKUP_KEY> getLookupRow(LOOKUP_KEY key) {
      //do not cancel lookups that are already in progress
      List<ILookupRow<LOOKUP_KEY>> rows = LookupJobHelper.await(m_contentAssistField.callKeyLookupInBackground(key, false));
      if (rows.isEmpty()) {
        return null;
      }
      else if (rows.size() > 1) {
        LOG.error("More than one row found for key {}", key);
        return null;
      }

      return rows.get(0);
    }

  }

  private class P_AllKeyLookupProvider implements IKeyLookupProvider<LOOKUP_KEY> {

    private final FinalValue<Map<LOOKUP_KEY, ILookupRow<LOOKUP_KEY>>> m_rows = new FinalValue<>();

    @Override
    public ILookupRow<LOOKUP_KEY> getLookupRow(LOOKUP_KEY key) {
      m_rows.setIfAbsentAndGet(new Callable<Map<LOOKUP_KEY, ILookupRow<LOOKUP_KEY>>>() {

        @Override
        public Map<LOOKUP_KEY, ILookupRow<LOOKUP_KEY>> call() throws Exception {
          List<ILookupRow<LOOKUP_KEY>> rows = LookupJobHelper.await(m_contentAssistField.callBrowseLookupInBackground(false));
          HashMap<LOOKUP_KEY, ILookupRow<LOOKUP_KEY>> rowMap = new HashMap<>();
          for (ILookupRow<LOOKUP_KEY> r : rows) {
            rowMap.put(r.getKey(), r);
          }
          return Collections.unmodifiableMap(rowMap);
        }

      });

      return m_rows.get().get(key);
    }

  }

}
