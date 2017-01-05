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
package org.eclipse.scout.rt.client.ui.form.fields.treefield;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treefield.ITreeFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treefield.TreeFieldChains.TreeFieldLoadChildNodesChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treefield.TreeFieldChains.TreeFieldSaveChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treefield.TreeFieldChains.TreeFieldSaveDeletedNodeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treefield.TreeFieldChains.TreeFieldSaveInsertedNodeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treefield.TreeFieldChains.TreeFieldSaveUpdatedNodeChain;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeVisitor;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformError;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.AbstractTreeFieldData;

@ClassId("bfbf00d0-b70a-48d4-8481-4faff294f37f")
@FormData(value = AbstractTreeFieldData.class, sdkCommand = SdkCommand.USE, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE)
public abstract class AbstractTreeField extends AbstractFormField implements ITreeField {
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
  protected boolean getConfiguredAutoLoad() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(200)
  protected boolean getConfiguredAutoExpandAll() {
    return false;
  }

  @Override
  protected double getConfiguredGridWeightY() {
    return 1;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Default for a tree field is 3.
   */
  @Override
  protected int getConfiguredGridH() {
    return 3;
  }

  /**
   * called before any lookup is performed
   */
  @ConfigOperation
  @Order(190)
  protected void execSave(Collection<? extends ITreeNode> insertedNodes, Collection<? extends ITreeNode> updatedNodes, Collection<? extends ITreeNode> deletedNodes) {
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
  protected void execLoadChildNodes(ITreeNode parentNode) {
  }

  @ConfigOperation
  @Order(210)
  protected void execSaveDeletedNode(ITreeNode row) {
  }

  @ConfigOperation
  @Order(220)
  protected void execSaveInsertedNode(ITreeNode row) {
  }

  @ConfigOperation
  @Order(230)
  protected void execSaveUpdatedNode(ITreeNode row) {
  }

  private Class<? extends ITree> getConfiguredTree() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<ITree>> f = ConfigurationUtility.filterClasses(dca, ITree.class);
    if (f.size() == 1) {
      return CollectionUtility.firstElement(f);
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
    catch (RuntimeException | PlatformError e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setAutoExpandAll(getConfiguredAutoExpandAll());
    List<ITree> contributedTrees = m_contributionHolder.getContributionsByClass(ITree.class);
    ITree tree = CollectionUtility.firstElement(contributedTrees);
    if (tree == null) {
      Class<? extends ITree> configuredTree = getConfiguredTree();
      if (configuredTree != null) {
        tree = ConfigurationUtility.newInnerInstance(this, configuredTree);
      }
    }
    setTreeInternal(tree);
    // local enabled listener
    addPropertyChangeListener(PROP_ENABLED_COMPUTED, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        if (m_tree == null) {
          return;
        }
        boolean newEnabled = ((Boolean) e.getNewValue()).booleanValue();
        m_tree.setEnabled(newEnabled);
      }
    });
  }

  @Override
  protected void initFieldInternal() {
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
  public void exportFormFieldData(AbstractFormFieldData target) {
    AbstractTreeFieldData treeFieldData = (AbstractTreeFieldData) target;
    if (m_tree != null) {
      m_tree.exportTreeData(treeFieldData);
    }
  }

  @Override
  public void importFormFieldData(AbstractFormFieldData source, boolean valueChangeTriggersEnabled) {
    Assertions.assertNotNull(source);
    AbstractTreeFieldData treeFieldData = (AbstractTreeFieldData) source;
    if (treeFieldData.isValueSet() && m_tree != null) {
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
    if (m_tree != null && !m_treeExternallyManaged && m_treeListener != null) {
      m_tree.removeTreeListener(m_treeListener);
      m_treeListener = null;
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
    if (changed && getForm() != null) {
      getForm().structureChanged(this);
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
    if (b && isMandatory()) {
      final ITree tree = getTree();
      if (tree == null || tree.getRootNode() == null) {
        return false;
      }
    }
    return b;
  }

  @Override
  public void loadRootNode() {
    if (m_tree != null && !m_treeExternallyManaged) {
      loadChildNodes(m_tree.getRootNode());
    }
  }

  @Override
  public void loadChildNodes(ITreeNode parentNode) {
    if (m_tree != null && !m_treeExternallyManaged) {
      try {
        m_tree.setTreeChanging(true);
        //
        interceptLoadChildNodes(parentNode);
      }
      finally {
        m_tree.setTreeChanging(false);
      }
    }
  }

  public ITreeNode createTreeNode() {
    ITreeNode node = new P_InternalTreeNode();
    return node;
  }

  @Override
  protected boolean execIsSaveNeeded() {
    if (m_tree == null) {
      return false;
    }
    return m_tree.getDeletedNodeCount() > 0
        || m_tree.getInsertedNodeCount() > 0
        || m_tree.getUpdatedNodeCount() > 0;
  }

  @Override
  protected void execMarkSaved() {
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
  protected boolean execIsEmpty() {
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
  public void doSave() {
    if (m_tree != null && !m_treeExternallyManaged) {
      try {
        m_tree.setTreeChanging(true);
        //
        // 1. batch
        interceptSave(m_tree.getInsertedNodes(), m_tree.getUpdatedNodes(), m_tree.getDeletedNodes());
        // 2. per node
        // deleted nodes
        for (ITreeNode iTreeNode : m_tree.getDeletedNodes()) {
          interceptSaveDeletedNode(iTreeNode);
        }
        m_tree.clearDeletedNodes();
        // inserted nodes
        for (ITreeNode insertedNode : m_tree.getInsertedNodes()) {
          interceptSaveInsertedNode(insertedNode);
          insertedNode.setStatusInternal(ITreeNode.STATUS_NON_CHANGED);
          m_tree.updateNode(insertedNode);
        }
        // updated rows
        for (ITreeNode updatedNode : m_tree.getUpdatedNodes()) {
          interceptSaveUpdatedNode(updatedNode);
          updatedNode.setStatusInternal(ITreeNode.STATUS_NON_CHANGED);
          m_tree.updateNode(updatedNode);
        }
      }
      finally {
        m_tree.setTreeChanging(false);
      }
    }
    markSaved();
  }

  /**
   * TreeNode implementation with delegation of loadChildren to this.loadChildNodes()
   */
  private class P_InternalTreeNode extends AbstractTreeNode {

    @Override
    public void loadChildren() {
      AbstractTreeField.this.loadChildNodes(this);
    }
  }

  private class P_TreeListener extends TreeAdapter {
    @Override
    public void treeChanged(TreeEvent e) {
      switch (e.getType()) {
        case TreeEvent.TYPE_NODES_DELETED:
        case TreeEvent.TYPE_NODES_INSERTED:
        case TreeEvent.TYPE_NODES_UPDATED:
        case TreeEvent.TYPE_NODES_CHECKED: {
          checkSaveNeeded();
          checkEmpty();
          break;
        }
      }
    }
  }

  protected static class LocalTreeFieldExtension<OWNER extends AbstractTreeField> extends LocalFormFieldExtension<OWNER> implements ITreeFieldExtension<OWNER> {

    public LocalTreeFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execSave(TreeFieldSaveChain chain, Collection<? extends ITreeNode> insertedNodes, Collection<? extends ITreeNode> updatedNodes, Collection<? extends ITreeNode> deletedNodes) {
      getOwner().execSave(insertedNodes, updatedNodes, deletedNodes);
    }

    @Override
    public void execSaveDeletedNode(TreeFieldSaveDeletedNodeChain chain, ITreeNode row) {
      getOwner().execSaveDeletedNode(row);
    }

    @Override
    public void execSaveUpdatedNode(TreeFieldSaveUpdatedNodeChain chain, ITreeNode row) {
      getOwner().execSaveUpdatedNode(row);
    }

    @Override
    public void execLoadChildNodes(TreeFieldLoadChildNodesChain chain, ITreeNode parentNode) {
      getOwner().execLoadChildNodes(parentNode);
    }

    @Override
    public void execSaveInsertedNode(TreeFieldSaveInsertedNodeChain chain, ITreeNode row) {
      getOwner().execSaveInsertedNode(row);
    }
  }

  @Override
  protected ITreeFieldExtension<? extends AbstractTreeField> createLocalExtension() {
    return new LocalTreeFieldExtension<AbstractTreeField>(this);
  }

  protected final void interceptSave(Collection<? extends ITreeNode> insertedNodes, Collection<? extends ITreeNode> updatedNodes, Collection<? extends ITreeNode> deletedNodes) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    TreeFieldSaveChain chain = new TreeFieldSaveChain(extensions);
    chain.execSave(insertedNodes, updatedNodes, deletedNodes);
  }

  protected final void interceptSaveDeletedNode(ITreeNode row) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    TreeFieldSaveDeletedNodeChain chain = new TreeFieldSaveDeletedNodeChain(extensions);
    chain.execSaveDeletedNode(row);
  }

  protected final void interceptSaveUpdatedNode(ITreeNode row) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    TreeFieldSaveUpdatedNodeChain chain = new TreeFieldSaveUpdatedNodeChain(extensions);
    chain.execSaveUpdatedNode(row);
  }

  protected final void interceptLoadChildNodes(ITreeNode parentNode) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    TreeFieldLoadChildNodesChain chain = new TreeFieldLoadChildNodesChain(extensions);
    chain.execLoadChildNodes(parentNode);
  }

  protected final void interceptSaveInsertedNode(ITreeNode row) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    TreeFieldSaveInsertedNodeChain chain = new TreeFieldSaveInsertedNodeChain(extensions);
    chain.execSaveInsertedNode(row);
  }

}
