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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ClientAsyncJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNodeBuilder;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNodeFilter;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeVisitor;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.SmartTreeForm.MainBox.ActiveStateRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.SmartTreeForm.MainBox.NewButton;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.SmartTreeForm.MainBox.ResultTreeField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.SmartTreeForm.MainBox.StatusField;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.AbstractTreeField;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCallFetcher;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.service.SERVICES;

public class SmartTreeForm extends AbstractSmartFieldProposalForm {
  /**
   * Boolean marker on {@link Job#getProperty(QualifiedName)} that can be used to detect that the tree is loading some
   * nodes.
   * <p>
   * This can be used for example to avoid busy handling when a tree smart popup is loading its incremental tree data
   * (only relevant when {@link ISmartField#isBrowseLoadIncremental()}=true.
   */
  public static final QualifiedName JOB_PROPERTY_LOAD_TREE = new QualifiedName(SmartTreeForm.class.getName(), "loadTree");

  private P_ActiveNodesFilter m_activeNodesFilter;
  private P_MatchingNodesFilter m_matchingNodesFilter;
  private boolean m_selectCurrentValueRequested;
  private boolean m_populateInitialTreeDone;
  private JobEx m_populateInitialTreeJob;

  public SmartTreeForm(ISmartField<?> smartField) throws ProcessingException {
    super(smartField);
  }

  /*
   * Operations
   */

  /**
   * Populate initial tree using a {@link ClientAsyncJob}. Amount of tree loaded is depending on
   * {@link ISmartField#isBrowseLoadIncremental()}.
   * <p>
   * loadIncremnental only loads the roots, whereas !loadIncremental loads the complete tree. Normally the latter is
   * configured together with {@link ISmartField#isBrowseAutoExpandAll()}
   * 
   * @throws ProcessingException
   */
  private void startPopulateInitialTree() throws ProcessingException {
    if (getSmartField().isBrowseLoadIncremental()) {
      //do sync
      getResultTreeField().loadRootNode();
      commitPopulateInitialTree(getResultTreeField().getTree());
      structureChanged(getResultTreeField());
    }
    else {
      //show comment that smartfield is loading
      getStatusField().setValue(ScoutTexts.get("searchingProposals"));
      getStatusField().setVisible(true);
      //go async to fetch data
      m_populateInitialTreeJob = getSmartField().callBrowseLookupInBackground(ISmartField.BROWSE_ALL_TEXT, 100000, TriState.UNDEFINED, new ILookupCallFetcher() {
        @Override
        public void dataFetched(LookupRow[] rows, ProcessingException failed) {
          if (failed == null) {
            try {
              getStatusField().setVisible(false);
              ITreeNode[] subTree = new P_TreeNodeBuilder().createTreeNodes(rows, ITreeNode.STATUS_NON_CHANGED, true);
              ITree tree = getResultTreeField().getTree();
              try {
                tree.setTreeChanging(true);
                //
                updateSubTree(tree, tree.getRootNode(), subTree);
                if (getSmartField().isBrowseAutoExpandAll()) {
                  tree.expandAll(getResultTreeField().getTree().getRootNode());
                }
                commitPopulateInitialTree(tree);
              }
              finally {
                tree.setTreeChanging(false);
              }
              structureChanged(getResultTreeField());
            }
            catch (ProcessingException pe) {
              failed = pe;
            }
          }
          if (failed != null) {
            getStatusField().setValue(TEXTS.get("RequestProblem"));
            getStatusField().setVisible(true);
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
  private void commitPopulateInitialTree(ITree tree) throws ProcessingException {
    updateActiveFilter();
    if (m_selectCurrentValueRequested) {
      if (tree.getSelectedNodeCount() == 0) {
        selectCurrentValueInternal();
      }
    }
    m_populateInitialTreeDone = true;
    update(m_selectCurrentValueRequested, true);
  }

  @Override
  public void forceProposalSelection() throws ProcessingException {
    ITree tree = getResultTreeField().getTree();
    tree.selectNextNode();
  }

  @Override
  protected void execInitForm() throws ProcessingException {
    m_activeNodesFilter = new P_ActiveNodesFilter();
    m_matchingNodesFilter = new P_MatchingNodesFilter();
    getResultTreeField().getTree().setIconId(getSmartField().getBrowseIconId());
    getResultTreeField().getTree().addTreeListener(new TreeAdapter() {
      @Override
      public void treeChanged(TreeEvent e) {
        switch (e.getType()) {
          case TreeEvent.TYPE_NODE_EXPANDED:
                  case TreeEvent.TYPE_NODE_COLLAPSED: {
                  structureChanged(getResultTreeField());
                  break;
                }
              }
            }
    });
  }

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
    IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
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
    @SuppressWarnings("unchecked")
    ISmartField<Object> sf = (ISmartField<Object>) getSmartField();
    LookupRow row = (LookupRow) node.getCell().getValue();
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
  protected LookupRow execGetSingleMatch() {
    // when load incremental is set, dont visit the tree but use text-to-key
    // lookup method on smartfield.
    if (getSmartField().isBrowseLoadIncremental()) {
      try {
        LookupRow[] rows = getSmartField().callTextLookup(getSearchText(), 2);
        if (rows.length == 1) {
          return rows[0];
        }
        else {
          return null;
        }
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
        return null;
      }
    }
    else {
      final ArrayList<LookupRow> foundLeafs = new ArrayList<LookupRow>();
      ITreeVisitor v = new ITreeVisitor() {
        @Override
        public boolean visit(ITreeNode node) {
          if (node.isEnabled() && node.isLeaf()) {
            LookupRow row = (LookupRow) node.getCell().getValue();
            if (row != null && row.isEnabled()) {
              foundLeafs.add(row);
            }
          }
          return foundLeafs.size() <= 2;
        }
      };
      getResultTreeField().getTree().visitVisibleTree(v);
      if (foundLeafs.size() == 1) {
        return foundLeafs.get(0);
      }
      else {
        return null;
      }
    }
  }

  /*
   * Operations
   */

  @Override
  public void update(boolean selectCurrentValue, boolean synchonous) throws ProcessingException {
    if (!m_populateInitialTreeDone) {
      m_selectCurrentValueRequested = selectCurrentValue;
      return;
    }
    ITree tree = getResultTreeField().getTree();
    try {
      tree.setTreeChanging(true);
      //
      m_matchingNodesFilter.update(getSearchText());
      tree.addNodeFilter(m_matchingNodesFilter);
    }
    finally {
      tree.setTreeChanging(false);
    }
    String statusText = null;
    getStatusField().setValue(statusText);
    getStatusField().setVisible(statusText != null);
    if (getNewButton().isEnabled()) {
      getNewButton().setVisible(execGetSingleMatch() == null);
    }
    structureChanged(getResultTreeField());
  }

  private void updateActiveFilter() {
    ITree tree = getResultTreeField().getTree();
    try {
      tree.setTreeChanging(true);
      //
      if (getSmartField().isActiveFilterEnabled()) {
        m_activeNodesFilter.update(getSmartField().getActiveFilter());
      }
      else {
        m_activeNodesFilter.update(TriState.TRUE);
      }
      tree.addNodeFilter(m_activeNodesFilter);
    }
    finally {
      tree.setTreeChanging(false);
    }
    structureChanged(getResultTreeField());
  }

  private void updateSubTree(ITree tree, final ITreeNode parentNode, ITreeNode[] subTree) throws ProcessingException {
    if (tree == null || parentNode == null || subTree == null) {
      return;
    }
    tree.removeAllChildNodes(parentNode);
    tree.addChildNodes(parentNode, subTree);
  }

  @Override
  public LookupRow getAcceptedProposal() throws ProcessingException {
    LookupRow row = null;
    ITreeNode node = getResultTreeField().getTree().getSelectedNode();
    if (node != null && node.isFilterAccepted() && node.isEnabled()) {
      row = (LookupRow) node.getCell().getValue();
    }
    if (row != null && row.isEnabled()) {
      return row;
    }
    else if (getSmartField().isAllowCustomText()) {
      return null;
    }
    else {
      return execGetSingleMatch();
    }
  }

  /*
   * Dialog start
   */
  @Override
  public void startForm() throws ProcessingException {
    startInternal(new FormHandler());
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public ResultTreeField getResultTreeField() {
    return getFieldByClass(ResultTreeField.class);
  }

  public ActiveStateRadioButtonGroup getActiveStateRadioButtonGroup() {
    return getFieldByClass(ActiveStateRadioButtonGroup.class);
  }

  /*
   * Fields
   */
  public StatusField getStatusField() {
    return getFieldByClass(StatusField.class);
  }

  public NewButton getNewButton() {
    return getFieldByClass(NewButton.class);
  }

  private boolean selectCurrentValueInternal() throws ProcessingException {
    final Object selectedKey = getSmartField().getValue();
    if (selectedKey != null) {
      //check existing tree
      ITree tree = getResultTreeField().getTree();
      final ArrayList<ITreeNode> matchingNodes = new ArrayList<ITreeNode>();
      tree.visitTree(new ITreeVisitor() {
        @Override
        public boolean visit(ITreeNode node) {
          Object val = node.getCell().getValue();
          if (val instanceof LookupRow && CompareUtility.equals(selectedKey, ((LookupRow) val).getKey())) {
            matchingNodes.add(node);
          }
          return true;
        }
      });
      if (matchingNodes.size() > 0) {
        tree.selectNode(matchingNodes.get(0));
        //ticket 87030
        for (int i = 1; i < matchingNodes.size(); i++) {
          ITreeNode node = matchingNodes.get(i);
          tree.setNodeExpanded(node, true);
          tree.ensureVisible(matchingNodes.get(i));
        }
        return true;
      }
      else {
        //load tree
        ITreeNode node = loadNodeWithKey(selectedKey);
        if (node != null) {
          tree.selectNode(node);
          return true;
        }
      }
    }
    return false;
  }

  private ITreeNode loadNodeWithKey(Object key) throws ProcessingException {
    ArrayList<LookupRow> path = new ArrayList<LookupRow>();
    Object t = key;
    while (t != null) {
      LookupRow row = getLookupRowFor(t);
      if (row != null) {
        path.add(0, row);
        t = row.getParentKey();
      }
      else {
        t = null;
      }
    }
    ITree tree = getResultTreeField().getTree();
    ITreeNode parentNode = tree.getRootNode();
    for (int i = 0; i < path.size() && parentNode != null; i++) {
      parentNode.ensureChildrenLoaded();
      parentNode.setExpanded(true);
      Object childKey = path.get(i).getKey();
      ITreeNode nextNode = null;
      for (ITreeNode n : parentNode.getChildNodes()) {
        if (n.getCell().getValue() instanceof LookupRow) {
          if (CompareUtility.equals(((LookupRow) n.getCell().getValue()).getKey(), childKey)) {
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

  @SuppressWarnings("unchecked")
  private LookupRow getLookupRowFor(Object key) throws ProcessingException {
    if (key instanceof Number && ((Number) key).longValue() == 0) {
      key = null;
    }
    if (key != null) {
      ISmartField<Object> sf = (ISmartField<Object>) getSmartField();
      for (LookupRow row : sf.callKeyLookup(key)) {
        return row;
      }
    }
    return null;
  }

  public class MainBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredGridColumnCount() {
      return 1;
    }

    @Override
    protected boolean getConfiguredGridUseUiWidth() {
      return true;
    }

    @Override
    protected boolean getConfiguredGridUseUiHeight() {
      return true;
    }

    @Order(10)
    public class ResultTreeField extends AbstractTreeField {

      public ResultTreeField() {
        super();
      }

      @Override
      protected boolean getConfiguredAutoLoad() {
        return false;
      }

      @Override
      protected double getConfiguredGridWeightY() {
        return 1;
      }

      @Override
      protected boolean getConfiguredGridUseUiWidth() {
        return true;
      }

      @Override
      protected boolean getConfiguredGridUseUiHeight() {
        return true;
      }

      @Override
      protected boolean getConfiguredLabelVisible() {
        return false;
      }

      @SuppressWarnings("unchecked")
      @Override
      protected void execLoadChildNodes(ITreeNode parentNode) throws ProcessingException {
        ISmartField<Object> sf = (ISmartField<Object>) getSmartField();
        if (sf.isBrowseLoadIncremental()) {
          Job currentJob = Job.getJobManager().currentJob();
          //show loading status
          boolean statusWasVisible = getStatusField().isVisible();
          getStatusField().setValue(ScoutTexts.get("searchingProposals"));
          getStatusField().setVisible(true);
          try {
            currentJob.setProperty(JOB_PROPERTY_LOAD_TREE, Boolean.TRUE);
            //load node
            LookupRow b = (LookupRow) (parentNode != null ? parentNode.getCell().getValue() : null);
            LookupRow[] data = sf.callSubTreeLookup(b != null ? b.getKey() : null, TriState.UNDEFINED);
            ITreeNode[] subTree = new P_TreeNodeBuilder().createTreeNodes(data, ITreeNode.STATUS_NON_CHANGED, false);
            updateSubTree(getTree(), parentNode, subTree);
          }
          finally {
            currentJob.setProperty(JOB_PROPERTY_LOAD_TREE, null);
          }
          //hide loading status
          getStatusField().setVisible(statusWasVisible);
        }
        /*
        else {
          //nop, since complete tree is already loaded (via async job)
        }
        */
      }

      /*
       * inner table
       */
      @Order(4)
      public class Tree extends AbstractTree {

        @Override
        protected boolean getConfiguredMultiSelect() {
          return false;
        }

        @Override
        protected boolean getConfiguredRootNodeVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredScrollToSelection() {
          return true;
        }

        @Override
        protected void execNodeClick(ITreeNode node) throws ProcessingException {
          doOk();
        }

      }
    }

    @Override
    protected boolean getConfiguredBorderVisible() {
      return false;
    }

    @Order(20)
    public class ActiveStateRadioButtonGroup extends AbstractRadioButtonGroup<TriState> {

      @Override
      protected boolean getConfiguredLabelVisible() {
        return false;
      }

      @Override
      protected void execChangedValue() throws ProcessingException {
        if (isVisible() && !isFormLoading()) {
          getSmartField().setActiveFilter(getValue());
          updateActiveFilter();
        }
      }

      @Order(1)
      public class ActiveButton extends AbstractButton {

        @Override
        protected int getConfiguredDisplayStyle() {
          return DISPLAY_STYLE_RADIO;
        }

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("ActiveStates");
        }

        @Override
        protected Object getConfiguredRadioValue() {
          return TriState.TRUE;
        }
      }

      @Order(2)
      public class InactiveButton extends AbstractButton {

        @Override
        protected int getConfiguredDisplayStyle() {
          return DISPLAY_STYLE_RADIO;
        }

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("InactiveStates");
        }

        @Override
        protected Object getConfiguredRadioValue() {
          return TriState.FALSE;
        }
      }

      @Order(3)
      public class ActiveAndInactiveButton extends AbstractButton {

        @Override
        protected int getConfiguredDisplayStyle() {
          return DISPLAY_STYLE_RADIO;
        }

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("ActiveAndInactiveStates");
        }

        @Override
        protected Object getConfiguredRadioValue() {
          return TriState.UNDEFINED;
        }
      }
    }

    @Order(25)
    public class NewButton extends AbstractButton {

      @Override
      protected boolean getConfiguredVisible() {
        return false;
      }

      @Override
      protected boolean getConfiguredEnabled() {
        return false;
      }

      @Override
      protected boolean getConfiguredLabelVisible() {
        return false;
      }

      @Override
      protected boolean getConfiguredFillHorizontal() {
        return false;
      }

      @Override
      protected int getConfiguredDisplayStyle() {
        return DISPLAY_STYLE_LINK;
      }

      @Override
      protected boolean getConfiguredProcessButton() {
        return false;
      }

      @Override
      protected void execClickAction() throws ProcessingException {
        getSmartField().doBrowseNew(getSearchText());
      }
    }// end field

    @Order(30)
    public class StatusField extends AbstractLabelField {
      @Override
      protected boolean getConfiguredLabelVisible() {
        return false;
      }

      @Override
      protected double getConfiguredGridWeightY() {
        return 1;
      }

    }// end field

  }// end main box

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
        LookupRow row = (LookupRow) node.getCell().getValue();
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

  private class P_TreeNodeBuilder extends AbstractTreeNodeBuilder {
    @Override
    protected ITreeNode createEmptyTreeNode() throws ProcessingException {
      ITree tree = getResultTreeField().getTree();
      ITreeNode node = getResultTreeField().createTreeNode();
      if (tree.getIconId() != null) {
        Cell cell = node.getCellForUpdate();
        cell.setIconId(tree.getIconId());
      }
      return node;
    }
  }

  /*
   * handlers
   */
  private class FormHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() throws ProcessingException {
      getActiveStateRadioButtonGroup().setVisible(getSmartField().isActiveFilterEnabled());
      getActiveStateRadioButtonGroup().setValue(getSmartField().getActiveFilter());
      getNewButton().setEnabled(getSmartField().getBrowseNewText() != null);
      getNewButton().setLabel(getSmartField().getBrowseNewText());
      startPopulateInitialTree();
    }

    @Override
    protected boolean execValidate() throws ProcessingException {
      return getAcceptedProposal() != null;
    }

    @Override
    protected void execFinally() throws ProcessingException {
      if (m_populateInitialTreeJob != null) {
        m_populateInitialTreeJob.cancel();
      }
    }

  }
}
