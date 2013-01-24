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
package org.eclipse.scout.rt.client.ui.form.fields.treefield;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeVisitor;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.AbstractTreeFieldData;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractTreeField extends AbstractFormField implements ITreeField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractTreeField.class);

  private LookupCall m_lookupCall;
  private ITree m_tree;
  private boolean m_treeExternallyManaged;
  private boolean m_autoExpandAll;
  private P_TreeListener m_treeListener;

  public AbstractTreeField() {
    this(true);
  }

  public AbstractTreeField(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(190)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredAutoLoad() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(200)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredAutoExpandAll() {
    return false;
  }

  /**
   * called before any lookup is performed
   */
  @ConfigOperation
  @Order(190)
  protected void execSave(ITreeNode[] insertedNodes, ITreeNode[] updatedNodes, ITreeNode[] deletedNodes) {
  }

  /**
   * This callback is responsible to add new nodes to the parent.
   * <p>
   * It is recommended to use code similar to the following <code>
   * <pre>
   * getTree().removeAllChildNodes(parentNode);
   * getTree().addChildNode(parentNode, ...);
   * getTree().addChildNodes(parentNode, ...);
   * // auto-expand all
   * if (isAutoExpandAll()) {
   *   getTree().expandAll(parentNode);
   * }
   * </pre>
   * </code>
   * <p>
   * 
   * @see the createTreeNode methods for creating new nodes
   */
  @ConfigOperation
  @Order(200)
  protected void execLoadChildNodes(ITreeNode parentNode) throws ProcessingException {
  }

  @ConfigOperation
  @Order(210)
  protected void execSaveDeletedNode(ITreeNode row) throws ProcessingException {
  }

  @ConfigOperation
  @Order(220)
  protected void execSaveInsertedNode(ITreeNode row) throws ProcessingException {
  }

  @ConfigOperation
  @Order(230)
  protected void execSaveUpdatedNode(ITreeNode row) throws ProcessingException {
  }

  private Class<? extends ITree> getConfiguredTree() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    Class<? extends ITree>[] f = ConfigurationUtility.filterClasses(dca, ITree.class);
    if (f.length == 1) {
      return f[0];
    }
    else {
      for (Class<? extends ITree> c : f) {
        if (c.getDeclaringClass() != AbstractTreeField.class) {
          return c;
        }
      }
      return null;
    }
  }

  @Override
  protected void execChangedMasterValue(Object newMasterValue) {
    try {
      loadRootNode();
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setAutoExpandAll(getConfiguredAutoExpandAll());
    if (getConfiguredTree() != null) {
      try {
        setTreeInternal(ConfigurationUtility.newInnerInstance(this, getConfiguredTree()));
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
    // local enabled listener
    addPropertyChangeListener(PROP_ENABLED, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        if (m_tree != null) {
          m_tree.setEnabled(isEnabled());
        }
      }
    });
  }

  @Override
  protected void initFieldInternal() throws ProcessingException {
    if (m_tree != null && !m_treeExternallyManaged) {
      m_tree.initTree();
    }
    if (getConfiguredAutoLoad()) {
      loadRootNode();
    }
    super.initFieldInternal();
  }

  @Override
  protected void disposeFieldInternal() {
    super.disposeFieldInternal();
    if (m_tree != null && !m_treeExternallyManaged) {
      m_tree.disposeTree();
    }
  }

  @Override
  public void exportFormFieldData(AbstractFormFieldData target) throws ProcessingException {
    AbstractTreeFieldData treeFieldData = (AbstractTreeFieldData) target;
    if (m_tree != null) {
      m_tree.exportTreeData(treeFieldData);
    }
  }

  @Override
  public void importFormFieldData(AbstractFormFieldData source, boolean valueChangeTriggersEnabled) throws ProcessingException {
    AbstractTreeFieldData treeFieldData = (AbstractTreeFieldData) source;
    if (treeFieldData.isValueSet()) {
      if (m_tree != null) {
        try {
          if (!valueChangeTriggersEnabled) {
            setValueChangeTriggerEnabled(false);
          }
          //
          m_tree.importTreeData(treeFieldData);
        }
        finally {
          if (!valueChangeTriggersEnabled) {
            setValueChangeTriggerEnabled(true);
          }
        }
      }
    }
  }

  @Override
  public final ITree getTree() {
    return m_tree;
  }

  @Override
  public final void setTree(ITree tree, boolean externallyManaged) {
    m_treeExternallyManaged = externallyManaged;
    setTreeInternal(tree);
  }

  private void setTreeInternal(ITree tree) {
    if (m_tree == tree) {
      return;
    }
    if (m_tree instanceof AbstractTree) {
      ((AbstractTree) m_tree).setContainerInternal(null);
    }
    if (m_tree != null && !m_treeExternallyManaged) {
      if (m_treeListener != null) {
        m_tree.removeTreeListener(m_treeListener);
        m_treeListener = null;
      }
    }
    m_tree = tree;
    if (m_tree instanceof AbstractTree) {
      ((AbstractTree) m_tree).setContainerInternal(this);
    }
    if (m_tree != null && !m_treeExternallyManaged) {
      m_tree.setAutoDiscardOnDelete(false);
      m_treeListener = new P_TreeListener();
      m_tree.addTreeListener(m_treeListener);
    }
    if (m_tree != null) {
      m_tree.setEnabled(isEnabled());
    }
    boolean changed = propertySupport.setProperty(PROP_TREE, m_tree);
    if (changed) {
      if (getForm() != null) {
        getForm().structureChanged(this);
      }
    }
  }

  public boolean isAutoExpandAll() {
    return m_autoExpandAll;
  }

  public void setAutoExpandAll(boolean b) {
    m_autoExpandAll = b;
  }

  @Override
  public boolean isContentValid() {
    boolean b = super.isContentValid();
    if (b) {
      if (isMandatory()) {
        if (getTree() == null || getTree().getRootNode() == null) {
          return false;
        }
      }
    }
    return b;
  }

  @Override
  public void loadRootNode() throws ProcessingException {
    if (m_tree != null && !m_treeExternallyManaged) {
      loadChildNodes(m_tree.getRootNode());
    }
  }

  @Override
  public void loadChildNodes(ITreeNode parentNode) throws ProcessingException {
    if (m_tree != null && !m_treeExternallyManaged) {
      try {
        m_tree.setTreeChanging(true);
        //
        execLoadChildNodes(parentNode);
      }
      finally {
        m_tree.setTreeChanging(false);
      }
    }
  }

  public ITreeNode createTreeNode() throws ProcessingException {
    ITreeNode node = new P_InternalTreeNode();
    return node;
  }

  @Override
  protected boolean execIsSaveNeeded() throws ProcessingException {
    boolean b = false;
    if (m_tree != null) {
      if (b == false && m_tree.getDeletedNodeCount() > 0) {
        b = true;
      }
      if (b == false && m_tree.getInsertedNodeCount() > 0) {
        b = true;
      }
      if (b == false && m_tree.getUpdatedNodeCount() > 0) {
        b = true;
      }
    }
    return b;
  }

  @Override
  protected void execMarkSaved() throws ProcessingException {
    if (m_tree != null && !m_treeExternallyManaged) {
      try {
        m_tree.setTreeChanging(true);
        //
        ITreeVisitor v = new ITreeVisitor() {
          @Override
          public boolean visit(ITreeNode node) {
            if (!node.isStatusNonchanged()) {
              node.setStatusInternal(ITreeNode.STATUS_NON_CHANGED);
              m_tree.updateNode(node);
            }
            return true;
          }
        };
        m_tree.visitNode(m_tree.getRootNode(), v);
        m_tree.clearDeletedNodes();
      }
      finally {
        m_tree.setTreeChanging(false);
      }
    }
  }

  @Override
  protected boolean execIsEmpty() throws ProcessingException {
    if (m_tree != null) {
      if (m_tree.isRootNodeVisible()) {
        if (m_tree.getRootNode() != null) {
          return false;
        }
      }
      else {
        if (m_tree.getRootNode() != null && m_tree.getRootNode().getChildNodeCount() > 0) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public void doSave() throws ProcessingException {
    if (m_tree != null && !m_treeExternallyManaged) {
      try {
        m_tree.setTreeChanging(true);
        //
        // 1. batch
        execSave(m_tree.getInsertedNodes(), m_tree.getUpdatedNodes(), m_tree.getDeletedNodes());
        // 2. per node
        ITreeNode[] insertedNodes = m_tree.getInsertedNodes();
        ITreeNode[] updatedNodes = m_tree.getUpdatedNodes();
        ITreeNode[] deletedNodes = m_tree.getDeletedNodes();
        // deleted nodes
        for (int i = 0; i < deletedNodes.length; i++) {
          execSaveDeletedNode(deletedNodes[i]);
        }
        m_tree.clearDeletedNodes();
        // inserted nodes
        for (int i = 0; i < insertedNodes.length; i++) {
          ITreeNode node = insertedNodes[i];
          execSaveInsertedNode(node);
          node.setStatusInternal(ITreeNode.STATUS_NON_CHANGED);
          m_tree.updateNode(node);
        }
        // updated rows
        for (int i = 0; i < updatedNodes.length; i++) {
          ITreeNode node = insertedNodes[i];
          execSaveUpdatedNode(node);
          node.setStatusInternal(ITreeNode.STATUS_NON_CHANGED);
          m_tree.updateNode(node);
        }
      }
      finally {
        m_tree.setTreeChanging(false);
      }
    }
    markSaved();
  }

  /**
   * TreeNode implementation with delegation of loadChildren to
   * this.loadChildNodes()
   */
  private class P_InternalTreeNode extends AbstractTreeNode {

    @Override
    public void loadChildren() throws ProcessingException {
      AbstractTreeField.this.loadChildNodes(this);
    }
  }

  private class P_TreeListener extends TreeAdapter {
    @Override
    public void treeChanged(TreeEvent e) {
      switch (e.getType()) {
        case TreeEvent.TYPE_NODES_DELETED:
        case TreeEvent.TYPE_NODES_INSERTED:
        case TreeEvent.TYPE_NODES_UPDATED: {
          checkSaveNeeded();
          checkEmpty();
          break;
        }
      }
    }
  }

}
