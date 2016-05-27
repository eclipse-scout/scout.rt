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
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
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
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.job.DoneEvent;
import org.eclipse.scout.rt.platform.job.IDoneHandler;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRowFetchedCallback;
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

  private P_ActiveNodesFilter m_activeNodesFilter;
  private P_MatchingNodesFilter m_matchingNodesFilter;
  private boolean m_selectCurrentValueRequested;
  private boolean m_populateInitialTreeDone;
  private volatile IFuture<Void> m_initialPolulatorFuture;
  private boolean m_modelExternallyManaged = false;

  public TreeProposalChooser(IContentAssistField<?, LOOKUP_KEY> contentAssistField, boolean allowCustomText) {
    super(contentAssistField, allowCustomText);
  }

  @Override
  protected ITree createModel() {
    m_activeNodesFilter = new P_ActiveNodesFilter();
    m_matchingNodesFilter = new P_MatchingNodesFilter();

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
    if (m_initialPolulatorFuture != null) {
      m_initialPolulatorFuture.cancel(false);
    }

    m_model.disposeTree();
    m_model = null;
  }

  @Override
  public void forceProposalSelection() {
    m_model.selectNextNode();
  }

  @Override
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
  }

  /**
   * Populate initial tree using a {@link ClientAsyncJob}. Amount of tree loaded is depending on
   * {@link IContentAssistField#isBrowseLoadIncremental()}.
   * <p>
   * loadIncremnental only loads the roots, whereas !loadIncremental loads the complete tree. Normally the latter is
   * configured together with {@link IContentAssistField#isBrowseAutoExpandAll()}
   */
  @Override
  protected void init() {
    if (m_contentAssistField.isBrowseLoadIncremental()) {
      // Load lookup rows asynchronously
      loadRootNode();
    }
    else {
      setStatus(new Status(ScoutTexts.get("searchingProposals"), IStatus.OK));
      // Load lookup rows asynchronously
      m_initialPolulatorFuture = m_contentAssistField.callBrowseLookupInBackground(m_contentAssistField.getWildcard(), 100000, TriState.UNDEFINED, new ILookupRowFetchedCallback<LOOKUP_KEY>() {

        @Override
        public void onSuccess(List<? extends ILookupRow<LOOKUP_KEY>> rows) {
          m_initialPolulatorFuture = null;

          List<ITreeNode> subTree = new P_TreeNodeBuilder().createTreeNodes(rows, ITreeNode.STATUS_NON_CHANGED, true);

          m_model.setTreeChanging(true);
          try {
            updateSubTree(m_model, m_model.getRootNode(), subTree);
            if (m_contentAssistField.isBrowseAutoExpandAll()) {
              m_model.expandAll(m_model.getRootNode());
            }
            commitPopulateInitialTree();
            setStatus(null);
          }
          catch (RuntimeException e) {
            setStatus(new Status(TEXTS.get("RequestProblem"), IStatus.ERROR));
          }
          finally {
            m_model.setTreeChanging(false);
          }
        }

        @Override
        public void onFailure(RuntimeException e) {
          m_initialPolulatorFuture = null;

          setStatus(new Status(TEXTS.get("RequestProblem"), IStatus.ERROR));
        }
      });
      m_initialPolulatorFuture.addExecutionHint(IContentAssistField.EXECUTION_HINT_INITIAL_LOOKUP);
    }
  }

  /**
   * Called when the initial tree has been loaded and the form is therefore ready to accept
   * {@link #update(boolean, boolean)} requests.
   */
  private void commitPopulateInitialTree() {
    updateActiveFilter();
    if (m_selectCurrentValueRequested) {
      if (m_model.getSelectedNodeCount() == 0) {
        selectCurrentValueInternal();
      }
    }
    m_populateInitialTreeDone = true;
    m_contentAssistField.doSearch(m_selectCurrentValueRequested, true);
  }

  private boolean selectCurrentValueInternal() {
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
        // ticket 87030
        for (int i = 1; i < matchingNodes.size(); i++) {
          ITreeNode node = matchingNodes.get(i);
          m_model.setNodeExpanded(node, true);
          m_model.ensureVisible(matchingNodes.get(i));
        }
        return true;
      }
      else {
        // load tree
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

  private ITreeNode loadNodeWithKey(LOOKUP_KEY key) {
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
    return parentNode;
  }

  private ILookupRow<LOOKUP_KEY> getLookupRowFor(LOOKUP_KEY key) {
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
  }

  private void updateSubTree(ITree tree, final ITreeNode parentNode, List<ITreeNode> subTree) {
    if (tree == null || parentNode == null || subTree == null) {
      return;
    }
    tree.removeAllChildNodes(parentNode);
    tree.addChildNodes(parentNode, subTree);
  }

  public void loadRootNode() {
    if (m_model != null && !m_modelExternallyManaged) {
      loadChildNodes(m_model.getRootNode());
    }
  }

  public void loadChildNodes(ITreeNode parentNode) {
    if (m_model != null && !m_modelExternallyManaged) {
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
      final IStatus originalStatus = getStatus();
      setStatus(new Status(ScoutTexts.get("searchingProposals"), IStatus.OK));

      //load node
      ILookupRow<LOOKUP_KEY> b = (LookupRow) (parentNode != null ? parentNode.getCell().getValue() : null);
      LOOKUP_KEY parentKey = b != null ? b.getKey() : null;
      m_contentAssistField.callSubTreeLookupInBackground(parentKey, TriState.UNDEFINED, false)
          .whenDone(new IDoneHandler<List<ILookupRow<LOOKUP_KEY>>>() {
            @Override
            public void onDone(DoneEvent<List<ILookupRow<LOOKUP_KEY>>> event) {
              final List<? extends ILookupRow<LOOKUP_KEY>> result = event.getResult();
              final Throwable exception = event.getException();

              ModelJobs.schedule(new IRunnable() {

                @Override
                public void run() {
                  if (result != null) {
                    List<ITreeNode> subTree = new P_TreeNodeBuilder().createTreeNodes(result, ITreeNode.STATUS_NON_CHANGED, false);
                    updateSubTree(m_model, parentNode, subTree);
                    commitPopulateInitialTree();

                    // hide loading status
                    setStatus(originalStatus);
                  }
                  else if (exception != null) {
                    LOG.error("Error in subtree lookup", exception);
                    setStatus(new Status(TEXTS.get("RequestProblem"), IStatus.ERROR));
                  }
                }
              }, ModelJobs.newInput(ClientRunContexts.copyCurrent()));
            }
          }, ClientRunContexts.copyCurrent());
    }

  }

  /**
   * @return the pattern used to filter tree nodes based on the text typed into the smartfield
   */
  @ConfigOperation
  @Order(20)
  protected Pattern execCreatePatternForTreeFilter(String filterText) {
    // check pattern
    String s = filterText;
    if (s == null) {
      s = "";
    }
    s = s.toLowerCase();
    IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
    if (desktop != null && desktop.isAutoPrefixWildcardForTextSearch()) {
      s = getContentAssistField().getWildcard() + s;
    }
    s = s.replace(getContentAssistField().getWildcard(), "@wildcard@");
    s = StringUtility.escapeRegexMetachars(s);
    s = s.replace("@wildcard@", ".*");
    if (!s.endsWith(".*")) {
      s = s + ".*";
    }
    return Pattern.compile(s, Pattern.DOTALL);

  }

  /**
   * @return true if the node is accepted by the tree filter pattern defined in
   *         {@link #execCreatePatternForTreeFilter(String)}
   */
  @ConfigOperation
  @Order(30)
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
   * Override this method to change that behaviour of what is a single match. By default a single match is when there is
   * a single enabled LEAF node in the tree
   */
  @ConfigOperation
  @Order(40)
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
      catch (RuntimeException e) {
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
}
