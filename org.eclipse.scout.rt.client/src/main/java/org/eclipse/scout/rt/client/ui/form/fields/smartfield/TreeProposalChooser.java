/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.status.IStatus;
import org.eclipse.scout.commons.status.Status;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNodeBuilder;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNodeFilter;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeVisitor;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCallFetcher;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

public class TreeProposalChooser<LOOKUP_KEY> extends AbstractProposalChooser<ITree, LOOKUP_KEY> {

  class P_Tree extends AbstractTree {

    @Override
    protected void execNodeClick(ITreeNode node, MouseButton mouseButton) throws ProcessingException {
      execResultTreeNodeClick(node);
    }
  }

  private P_ActiveNodesFilter m_activeNodesFilter;
  private P_MatchingNodesFilter m_matchingNodesFilter;
  private boolean m_selectCurrentValueRequested;
  private boolean m_populateInitialTreeDone;
  private IFuture<?> m_populateInitialTreeJob;

  public TreeProposalChooser(IContentAssistField<?, LOOKUP_KEY> contentAssistField, boolean allowCustomText) throws ProcessingException {
    super(contentAssistField, allowCustomText);
  }

  @Override
  protected ITree createModel() throws ProcessingException {
    m_activeNodesFilter = new P_ActiveNodesFilter();
    m_matchingNodesFilter = new P_MatchingNodesFilter();

    ITree tree = new P_Tree();
    tree.setIconId(m_contentAssistField.getBrowseIconId());
    tree.addTreeListener(new TreeAdapter() {
      @Override
      @SuppressWarnings("deprecation")
      public void treeChanged(TreeEvent e) {
        switch (e.getType()) {
          case TreeEvent.TYPE_NODE_EXPANDED:
          case TreeEvent.TYPE_NODE_COLLAPSED: {
            fireStructureChanged();
            break;
          }
        }
      }
    });
    return tree;
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
    if (m_populateInitialTreeJob != null) {
      m_populateInitialTreeJob.cancel(true);
    }
    m_model.disposeTree();
    m_model = null;
  }

  @Override
  public void forceProposalSelection() throws ProcessingException {
    m_model.selectNextNode();
  }

  @Override
  @SuppressWarnings("deprecation")
  protected void dataFetchedDelegateImpl(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result, int maxCount) {
    String searchText = null;
    boolean selectCurrentValue = false;
    if (result != null) {
      selectCurrentValue = result.isSelectCurrentValue();
      searchText = result.getSearchText();
    }
    if (!m_populateInitialTreeDone) {
      m_selectCurrentValueRequested = selectCurrentValue;
      return;
    }
    try {
      m_model.setTreeChanging(true);
      //
      m_matchingNodesFilter.update(searchText);
      m_model.addNodeFilter(m_matchingNodesFilter);
    }
    finally {
      m_model.setTreeChanging(false);
    }
    setStatus(null);
    setStatusVisible(false);
    fireStructureChanged();
  }

  /**
   * Populate initial tree using a {@link ClientAsyncJob}. Amount of tree loaded is depending on
   * {@link IContentAssistField#isBrowseLoadIncremental()}.
   * <p>
   * loadIncremnental only loads the roots, whereas !loadIncremental loads the complete tree. Normally the latter is
   * configured together with {@link IContentAssistField#isBrowseAutoExpandAll()}
   *
   * @throws ProcessingException
   */
  @Override
  @SuppressWarnings("deprecation")
  protected void init() throws ProcessingException {
    if (m_contentAssistField.isBrowseLoadIncremental()) {
      // do sync
      loadRootNode();
      commitPopulateInitialTree();
      fireStructureChanged();
    }
    else {
      //show comment that smartfield is loading
      setStatus(new Status(ScoutTexts.get("searchingProposals"), IStatus.OK));
      setStatusVisible(true);
      //go async to fetch data
      m_populateInitialTreeJob = m_contentAssistField.callBrowseLookupInBackground(IContentAssistField.BROWSE_ALL_TEXT, 100000, TriState.UNDEFINED, new ILookupCallFetcher<LOOKUP_KEY>() {
        @Override
        public void dataFetched(List<? extends ILookupRow<LOOKUP_KEY>> rows, ProcessingException failed) {
          if (failed == null) {
            try {
              setStatusVisible(false);
              List<ITreeNode> subTree = new P_TreeNodeBuilder().createTreeNodes(rows, ITreeNode.STATUS_NON_CHANGED, true);
              try {
                m_model.setTreeChanging(true);
                //
                updateSubTree(m_model, m_model.getRootNode(), subTree);
                if (m_contentAssistField.isBrowseAutoExpandAll()) {
                  m_model.expandAll(m_model.getRootNode());
                }
                commitPopulateInitialTree();
              }
              finally {
                m_model.setTreeChanging(false);
              }
              fireStructureChanged();
            }
            catch (ProcessingException pe) {
              failed = pe;
            }
          }
          if (failed != null) {
            setStatus(new Status(TEXTS.get("RequestProblem"), IStatus.ERROR));
            setStatusVisible(true);
            return;
          }
        }
      });
    }
  }

  /**
   * Called when the initial tree has been loaded and the form is therefore ready to accept
   * {@link #update(boolean, boolean)} requests.
   *
   * @throws ProcessingException
   */
  private void commitPopulateInitialTree() throws ProcessingException {
    updateActiveFilter();
    if (m_selectCurrentValueRequested) {
      if (m_model.getSelectedNodeCount() == 0) {
        selectCurrentValueInternal();
      }
    }
    m_populateInitialTreeDone = true;
    m_contentAssistField.doSearch(m_selectCurrentValueRequested, true);
  }

  private boolean selectCurrentValueInternal() throws ProcessingException {
    final LOOKUP_KEY selectedKey = m_contentAssistField.getValueAsLookupKey();
    if (selectedKey != null) {
      //check existing tree
      final ArrayList<ITreeNode> matchingNodes = new ArrayList<ITreeNode>();
      m_model.visitTree(new ITreeVisitor() {
        @Override
        public boolean visit(ITreeNode node) {
          Object val = node.getCell().getValue();
          if (val instanceof ILookupRow && CompareUtility.equals(selectedKey, ((ILookupRow) val).getKey())) {
            matchingNodes.add(node);
          }
          return true;
        }
      });
      if (matchingNodes.size() > 0) {
        selectValue(m_model, matchingNodes.get(0));

        //ticket 87030
        for (int i = 1; i < matchingNodes.size(); i++) {
          ITreeNode node = matchingNodes.get(i);
          m_model.setNodeExpanded(node, true);
          m_model.ensureVisible(matchingNodes.get(i));
        }
        return true;
      }
      else {
        //load tree
        ITreeNode node = loadNodeWithKey(selectedKey);
        if (node != null) {
          selectValue(m_model, node);
          return true;
        }
      }
    }
    return false;
  }

  private void selectValue(ITree tree, ITreeNode node) {
    if (tree == null || node == null) {
      return;
    }

    tree.selectNode(node);

    if (tree.isCheckable()) {
      tree.setNodeChecked(node, true);
    }
  }

  private ITreeNode loadNodeWithKey(LOOKUP_KEY key) throws ProcessingException {
    ArrayList<ILookupRow<LOOKUP_KEY>> path = new ArrayList<ILookupRow<LOOKUP_KEY>>();
    LOOKUP_KEY t = key;
    while (t != null) {
      ILookupRow<LOOKUP_KEY> row = getLookupRowFor(t);
      if (row != null) {
        path.add(0, row);
        t = row.getParentKey();
      }
      else {
        t = null;
      }
    }
    ITreeNode parentNode = m_model.getRootNode();
    for (int i = 0; i < path.size() && parentNode != null; i++) {
      parentNode.ensureChildrenLoaded();
      parentNode.setExpanded(true);
      Object childKey = path.get(i).getKey();
      ITreeNode nextNode = null;
      for (ITreeNode n : parentNode.getChildNodes()) {
        if (n.getCell().getValue() instanceof ILookupRow) {
          if (CompareUtility.equals(((ILookupRow) n.getCell().getValue()).getKey(), childKey)) {
            nextNode = n;
            break;
          }
        }
      }
      parentNode = nextNode;
    }
    //
    return parentNode;
  }

  private ILookupRow<LOOKUP_KEY> getLookupRowFor(LOOKUP_KEY key) throws ProcessingException {
    if (key instanceof Number && ((Number) key).longValue() == 0) {
      key = null;
    }
    if (key != null) {
      IContentAssistField<?, LOOKUP_KEY> sf = (IContentAssistField<?, LOOKUP_KEY>) m_contentAssistField;
      for (ILookupRow<LOOKUP_KEY> row : sf.callKeyLookup(key)) {
        return row;
      }
    }
    return null;
  }

  @SuppressWarnings("deprecation")
  private void updateActiveFilter() {
    try {
      m_model.setTreeChanging(true);
      //
      if (m_contentAssistField.isActiveFilterEnabled()) {
        m_activeNodesFilter.update(m_contentAssistField.getActiveFilter());
      }
      else {
        m_activeNodesFilter.update(TriState.TRUE);
      }
      m_model.addNodeFilter(m_activeNodesFilter);
    }
    finally {
      m_model.setTreeChanging(false);
    }
    fireStructureChanged();
  }

  private void updateSubTree(ITree tree, final ITreeNode parentNode, List<ITreeNode> subTree) throws ProcessingException {
    if (tree == null || parentNode == null || subTree == null) {
      return;
    }
    tree.removeAllChildNodes(parentNode);
    tree.addChildNodes(parentNode, subTree);
  }

  private class P_ActiveNodesFilter implements ITreeNodeFilter {
    private TriState m_ts;

    public P_ActiveNodesFilter() {
    }

    public void update(TriState ts) {
      m_ts = ts;
    }

    @Override
    public boolean accept(ITreeNode node, int level) {
      if (m_ts.isUndefined()) {
        return true;
      }
      else {
        ILookupRow row = (LookupRow) node.getCell().getValue();
        if (row != null) {
          return row.isActive() == m_ts.equals(TriState.TRUE);
        }
        else {
          return true;
        }
      }
    }
  }

  private class P_MatchingNodesFilter implements ITreeNodeFilter {
    private Pattern m_searchPattern;

    public P_MatchingNodesFilter() {
    }

    public void update(String text) {
      m_searchPattern = execCreatePatternForTreeFilter(text);
    }

    @Override
    public boolean accept(ITreeNode node, int level) {
      return execAcceptNodeByTreeFilter(m_searchPattern, node, level);
    }
  }

  private class P_TreeNodeBuilder extends AbstractTreeNodeBuilder<LOOKUP_KEY> {
    @Override
    protected ITreeNode createEmptyTreeNode() throws ProcessingException {
      ITreeNode node = TreeProposalChooser.this.createTreeNode();
      if (m_model.getIconId() != null) {
        Cell cell = node.getCellForUpdate();
        cell.setIconId(m_model.getIconId());
      }
      return node;
    }
  }

  // FIXME AWE: (smart-field) Zeugs das auf dem AbstractTreeField implementiert ist
  // ----------------------------------------------------------------------------------------------

  private boolean m_modelExternallyManaged = false;

  public void loadRootNode() throws ProcessingException {
    if (m_model != null && !m_modelExternallyManaged) {
      loadChildNodes(m_model.getRootNode());
    }
  }

  public void loadChildNodes(ITreeNode parentNode) throws ProcessingException {
    if (m_model != null && !m_modelExternallyManaged) {
      try {
        m_model.setTreeChanging(true);
        //
        // FIXME AWE: (smart-field) hier fehlt der ganze intercept-Teil
        execLoadChildNodes(parentNode);
      }
      finally {
        m_model.setTreeChanging(false);
      }
    }
  }

  public ITreeNode createTreeNode() throws ProcessingException {
    ITreeNode node = new P_InternalTreeNode();
    return node;
  }

//  protected final void interceptLoadChildNodes(ITreeNode parentNode) throws ProcessingException {
//    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
//    TreeFieldLoadChildNodesChain chain = new TreeFieldLoadChildNodesChain(extensions);
//    chain.execLoadChildNodes(parentNode);
//  }

  // FIXME AWE: (smart-field) Quelle ContentAssistTreeForm > ResultTreeField
  // ----------------------------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  protected void execLoadChildNodes(ITreeNode parentNode) throws ProcessingException {
    if (m_contentAssistField.isBrowseLoadIncremental()) {
      //show loading status
      boolean statusWasVisible = isStatusVisible();
      setStatus(new Status(ScoutTexts.get("searchingProposals"), IStatus.OK));
      setStatusVisible(true);
      //load node
      ILookupRow<LOOKUP_KEY> b = (LookupRow) (parentNode != null ? parentNode.getCell().getValue() : null);
      List<? extends ILookupRow<LOOKUP_KEY>> data = m_contentAssistField.callSubTreeLookup(b != null ? b.getKey() : null, TriState.UNDEFINED);
      List<ITreeNode> subTree = new P_TreeNodeBuilder().createTreeNodes(data, ITreeNode.STATUS_NON_CHANGED, false);
      updateSubTree(m_model, parentNode, subTree);
      //hide loading status
      setStatusVisible(statusWasVisible);
    }
  }

  /**
   * TreeNode implementation with delegation of loadChildren to
   * this.loadChildNodes()
   */
  private class P_InternalTreeNode extends AbstractTreeNode {

    @Override
    public void loadChildren() throws ProcessingException {
      TreeProposalChooser.this.loadChildNodes(this);
    }
  }

  // FIXME AWE: (smart-field) das hier war überschreibbar/konfigurierbar via ContentAssistTreeForm
  // ----------------------------------------------------------------------------------------------

  /**
   * @return the pattern used to filter tree nodes based on the text typed into the smartfield
   */
  @ConfigOperation
  @Order(100)
  protected Pattern execCreatePatternForTreeFilter(String filterText) {
    // check pattern
    String s = filterText;
    if (s == null) {
      s = "";
    }
    s = s.toLowerCase();
    IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
    if (desktop != null && desktop.isAutoPrefixWildcardForTextSearch()) {
      s = "*" + s;
    }
    if (!s.endsWith("*")) {
      s = s + "*";
    }
    s = StringUtility.toRegExPattern(s);
    return Pattern.compile(s, Pattern.DOTALL);
  }

  /**
   * @return true if the node is accepted by the tree filter pattern defined in
   *         {@link #execCreatePatternForTreeFilter(String)}
   */
  @ConfigOperation
  @Order(110)
  protected boolean execAcceptNodeByTreeFilter(Pattern filterPattern, ITreeNode node, int level) {
    IContentAssistField<?, LOOKUP_KEY> sf = m_contentAssistField;
    @SuppressWarnings("unchecked")
    ILookupRow<LOOKUP_KEY> row = (ILookupRow<LOOKUP_KEY>) node.getCell().getValue();
    if (node.isChildrenLoaded()) {
      if (row != null) {
        String q1 = node.getTree().getPathText(node, "\n");
        String q2 = node.getTree().getPathText(node, " ");
        if (q1 != null && q2 != null) {
          String[] path = (q1 + "\n" + q2).split("\n");
          for (String pathText : path) {
            if (pathText != null && filterPattern.matcher(pathText.toLowerCase()).matches()) {
              // use "level-1" because a tree smart field assumes its tree to
              // have multiple roots, but the ITree model is built as
              // single-root tree with invisible root node
              if (sf.acceptBrowseHierarchySelection(row.getKey(), level - 1, node.isLeaf())) {
                return true;
              }
            }
          }
        }
        return false;
      }
    }
    return true;
  }

  /**
   * Override this method to change that behaviour of what is a single match.
   * <p>
   * By default a single match is when there is a single enabled LEAF node in the tree
   * <p>
   */
  @ConfigOperation
  @Order(120)
  @Override
  protected ILookupRow<LOOKUP_KEY> execGetSingleMatch() {
    // when load incremental is set, don't visit the tree but use text-to-key
    // lookup method on smartfield.
    if (m_contentAssistField.isBrowseLoadIncremental()) {
      try {
        List<? extends ILookupRow<LOOKUP_KEY>> rows = m_contentAssistField.callTextLookup(getSearchText(), 2);
        if (rows != null && rows.size() == 1) {
          return rows.get(0);
        }
        else {
          return null;
        }
      }
      catch (ProcessingException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
        return null;
      }
    }
    else {
      final List<ILookupRow<LOOKUP_KEY>> foundLeafs = new ArrayList<ILookupRow<LOOKUP_KEY>>();
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
  }

  protected void execResultTreeNodeClick(ITreeNode node) throws ProcessingException {
    m_contentAssistField.acceptProposal();
  }

  @Override
  public void deselect() {
    m_model.deselectNode(m_model.getSelectedNode());
  }

}
