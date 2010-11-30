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

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
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
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.service.SERVICES;

public class SmartTreeForm extends AbstractSmartFieldProposalForm {
  private P_ActiveNodesFilter m_activeNodesFilter;
  private P_MatchingNodesFilter m_matchingNodesFilter;

  public SmartTreeForm(ISmartField<?> smartField) throws ProcessingException {
    super(smartField);
  }

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

  public void update(boolean selectCurrentValue) throws ProcessingException {
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
      getNewButton().setVisible(getSingleMatch() == null);
    }
    if (selectCurrentValue) {
      if (tree.getSelectedNodeCount() == 0) {
        selectCurrentValueInternal();
      }
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

  private LookupRow getSingleMatch() {
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
      return getSingleMatch();
    }
  }

  /*
   * Dialog start
   */
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
        ITreeNode[] subTree;
        ISmartField<Object> sf = (ISmartField<Object>) getSmartField();
        if (sf.isBrowseLoadIncremental()) {
          LookupRow b = (LookupRow) (parentNode != null ? parentNode.getCell().getValue() : null);
          LookupRow[] data = sf.callSubTreeLookup(b != null ? b.getKey() : null, TriState.UNDEFINED);
          subTree = new P_TreeNodeBuilder().createTreeNodes(data, ITreeNode.STATUS_NON_CHANGED, false);
        }
        else if (parentNode == getTree().getRootNode()) {
          // called on root only
          LookupRow[] data = sf.callBrowseLookup(ISmartField.BROWSE_ALL_TEXT, 100000, TriState.UNDEFINED);
          subTree = new P_TreeNodeBuilder().createTreeNodes(data, ITreeNode.STATUS_NON_CHANGED, true);
        }
        else {
          subTree = parentNode.getChildNodes();
        }
        //add
        getTree().removeAllChildNodes(parentNode);
        getTree().addChildNodes(parentNode, subTree);
        // auto-expand all
        if (isAutoExpandAll()) {
          getTree().expandAll(parentNode);
        }
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

      private class P_TreeNodeBuilder extends AbstractTreeNodeBuilder {
        @Override
        protected ITreeNode createEmptyTreeNode() throws ProcessingException {
          ITreeNode node = ResultTreeField.this.createTreeNode();
          if (getTree().getIconId() != null) {
            Cell cell = node.getCellForUpdate();
            cell.setIconId(getTree().getIconId());
          }
          return node;
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
      // check pattern
      String s = text;
      if (s == null) s = "";
      s = s.toLowerCase();
      IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
      if (desktop != null && desktop.isAutoPrefixWildcardForTextSearch()) {
        s = "*" + s;
      }
      if (!s.endsWith("*")) {
        s = s + "*";
      }
      s = StringUtility.toRegExPattern(s);
      m_searchPattern = Pattern.compile(s, Pattern.DOTALL);
    }

    @SuppressWarnings("unchecked")
    public boolean accept(ITreeNode node, int level) {
      ISmartField<Object> sf = (ISmartField<Object>) getSmartField();
      LookupRow row = (LookupRow) node.getCell().getValue();
      if (node.isChildrenLoaded()) {
        if (row != null) {
          String q1 = node.getTree().getPathText(node, "\n");
          String q2 = node.getTree().getPathText(node, " ");
          if (q1 != null && q2 != null) {
            String[] path = (q1 + "\n" + q2).split("\n");
            for (String pathText : path) {
              if (pathText != null && m_searchPattern.matcher(pathText.toLowerCase()).matches()) {
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
      getResultTreeField().loadRootNode();
      updateActiveFilter();
      update(false);
      if (getSmartField().isBrowseAutoExpandAll() && !getSmartField().isBrowseLoadIncremental()) {
        ITree tree = getResultTreeField().getTree();
        tree.expandAll(getResultTreeField().getTree().getRootNode());
      }
    }

    @Override
    protected boolean execValidate() throws ProcessingException {
      return getAcceptedProposal() != null;
    }

  }
}
