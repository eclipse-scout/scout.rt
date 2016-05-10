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
package org.eclipse.scout.rt.client.ui.form.fields.treebox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treebox.ITreeBoxExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treebox.TreeBoxChains.TreeBoxFilterLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treebox.TreeBoxChains.TreeBoxFilterNewNodeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treebox.TreeBoxChains.TreeBoxLoadChildNodesChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treebox.TreeBoxChains.TreeBoxPrepareLookupChain;
import org.eclipse.scout.rt.client.services.lookup.FormFieldProvisioningContext;
import org.eclipse.scout.rt.client.services.lookup.ILookupCallProvisioningService;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNodeBuilder;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNodeFilter;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeVisitor;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.CompositeFieldUtility;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.OrderedComparator;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.CodeLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("5648579d-1968-47be-a0c9-a8c846d2caf4")
public abstract class AbstractTreeBox<T> extends AbstractValueField<Set<T>> implements ITreeBox<T> {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractTreeBox.class);

  private ITree m_tree;
  private ILookupCall<T> m_lookupCall;
  private Class<? extends ICodeType<?, T>> m_codeTypeClass;
  private boolean m_valueTreeSyncActive;
  private boolean m_autoExpandAll;
  private boolean m_loadIncremental;
  private ITreeNodeFilter m_activeNodesFilter;
  private ITreeNodeFilter m_checkedNodesFilter;
  // children
  private List<IFormField> m_fields;
  private Map<Class<? extends IFormField>, IFormField> m_movedFormFieldsByClass;

  public AbstractTreeBox() {
    this(true);
  }

  public AbstractTreeBox(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  /**
   * Called to (re)load children of a node.
   * <p>
   * When loadIncremental=false then this method is only called once on the invisible root node.<br>
   * When loadIncremental=true then this method is called on every node.
   * <p>
   * The default implementation is:
   *
   * <pre>
   * List&lt;ITreeNode&gt; children;
   * if (isLoadIncremental()) {
   *   children = callChildLookup(parentNode);
   * }
   * else {
   *   children = callCompleteTreeLookup();
   * }
   * getTree().removeAllChildNodes(parentNode);
   * getTree().addChildNodes(parentNode, children);
   * parentNode.setChildrenLoaded(true);
   * </pre>
   */
  @ConfigOperation
  @Order(240)
  protected void execLoadChildNodes(ITreeNode parentNode) {
    List<ITreeNode> children;
    if (isLoadIncremental()) {
      children = callChildLookup(parentNode);
    }
    else {
      children = callCompleteTreeLookup();
    }
    m_tree.removeAllChildNodes(parentNode);
    m_tree.addChildNodes(parentNode, children);
    parentNode.setChildrenLoaded(true);
  }

  @ConfigProperty(ConfigProperty.LOOKUP_CALL)
  @Order(240)
  protected Class<? extends ILookupCall<T>> getConfiguredLookupCall() {
    return null;
  }

  @ConfigProperty(ConfigProperty.CODE_TYPE)
  @Order(250)
  protected Class<? extends ICodeType<?, T>> getConfiguredCodeType() {
    return null;
  }

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(230)
  protected String getConfiguredIconId() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(260)
  protected boolean getConfiguredAutoLoad() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(270)
  protected boolean getConfiguredLoadIncremental() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(280)
  protected boolean getConfiguredAutoExpandAll() {
    return false;
  }

  @Override
  @Order(210)
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredAutoAddDefaultMenus() {
    return false;
  }

  /**
   * @return true: a filter is added to the treebox tree that only accepts nodes that are active or checked.<br>
   *         Affects {@link ITreeNode#getFilteredChildNodes()}
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(290)
  protected boolean getConfiguredFilterActiveNodes() {
    return false;
  }

  /**
   * @return true: a filter is added to the treebox tree that only accepts checked nodes<br>
   *         Affects {@link ITreeNode#getFilteredChildNodes()}
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(300)
  protected boolean getConfiguredFilterCheckedNodes() {
    return false;
  }

  /**
   * Checks / unchecks all visible child nodes if the parent node gets checked / unchecked.
   * <p>
   * Makes only sense if
   *
   * <pre>
   * {@link #getConfiguredCheckable()}
   * </pre>
   *
   * is set to true.
   *
   * @since 3.10-M1 (backported to 3.8 / 3.9)
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(310)
  protected boolean getConfiguredAutoCheckChildNodes() {
    return false;
  }

  @Override
  protected double getConfiguredGridWeightY() {
    return 1.0;
  }

  private List<Class<IFormField>> getConfiguredFields() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClasses(dca, IFormField.class);
  }

  /**
   * On any value change or call to {@link #checkEmpty()} this method is called to calculate if the field represents an
   * empty state (semantics)
   * <p>
   */
  @Override
  protected boolean execIsEmpty() {
    return getValue().isEmpty();
  }

  /**
   * called before any lookup is performed
   */
  @ConfigOperation
  @Order(230)
  protected void execPrepareLookup(ILookupCall<T> call, ITreeNode parent) {
  }

  /**
   * @param call
   *          that produced this result
   * @param result
   *          live list containing the result rows. Add, remove, set, replace and clear of entries in this list is
   *          supported
   */
  @ConfigOperation
  @Order(240)
  protected void execFilterLookupResult(ILookupCall<T> call, List<ILookupRow<T>> result) {
  }

  /**
   * @param newNode
   *          a new node that was created for the tree, but not yet attached to the tree
   * @param treeLevel
   *          Since {@link ITreeNode#getTreeLevel()} is not yet valid, treeLevel is the level where the node will be
   *          attached at
   *          <p>
   *          Can be useful for example to disable nodes on some levels so they cannot be checked.
   */
  @ConfigOperation
  @Order(250)
  protected void execFilterNewNode(ITreeNode newNode, int treeLevel) {
  }

  private Class<? extends ITree> getConfiguredTree() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<ITree>> f = ConfigurationUtility.filterClasses(dca, ITree.class);
    if (f.size() == 1) {
      return CollectionUtility.firstElement(f);
    }
    else {
      for (Class<? extends ITree> c : f) {
        if (c.getDeclaringClass() != AbstractTreeBox.class) {
          return c;
        }
      }
      return null;
    }
  }

  @Override
  protected void execChangedMasterValue(Object newMasterValue) {
    setValue(null);
    loadRootNode();
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void initConfig() {
    m_fields = CollectionUtility.emptyArrayList();
    m_movedFormFieldsByClass = new HashMap<Class<? extends IFormField>, IFormField>();
    super.initConfig();
    setFilterActiveNodes(getConfiguredFilterActiveNodes());
    setFilterActiveNodesValue(TriState.TRUE);
    setFilterCheckedNodes(getConfiguredFilterCheckedNodes());
    setFilterCheckedNodesValue(getConfiguredFilterCheckedNodes());
    setAutoExpandAll(getConfiguredAutoExpandAll());
    setLoadIncremental(getConfiguredLoadIncremental());
    List<ITree> contributedTrees = m_contributionHolder.getContributionsByClass(ITree.class);
    m_tree = CollectionUtility.firstElement(contributedTrees);
    if (m_tree == null) {
      Class<? extends ITree> configuredTree = getConfiguredTree();
      if (configuredTree != null) {
        m_tree = ConfigurationUtility.newInnerInstance(this, configuredTree);
      }
    }
    if (m_tree != null) {
      if (m_tree instanceof AbstractTree) {
        ((AbstractTree) m_tree).setContainerInternal(this);
      }
      m_tree.setRootNode(getTreeNodeBuilder().createTreeNode(new LookupRow(null, "Root"), ITreeNode.STATUS_NON_CHANGED, false));
      m_tree.setAutoDiscardOnDelete(false);
      updateActiveNodesFilter();
      updateCheckedNodesFilter();
      m_tree.addTreeListener(
          new TreeAdapter() {
            @Override
            public void treeChanged(TreeEvent e) {
              switch (e.getType()) {
                case TreeEvent.TYPE_NODES_SELECTED: {
                  if (!getTree().isCheckable()) {
                    syncTreeToValue();
                  }
                  break;
                }
                case TreeEvent.TYPE_NODES_CHECKED: {
                  if (getTree().isCheckable()) {
                    syncTreeToValue();
                  }
                  break;
                }
              }
            }
          });
      m_tree.setEnabled(isEnabled());
      // default icon
      if (this.getConfiguredIconId() != null) {
        m_tree.setDefaultIconId(this.getConfiguredIconId());
      }
    }
    else {
      LOG.warn("there is no inner class of type ITree in {}", getClass().getName());
    }
    getTree().setAutoCheckChildNodes(getConfiguredAutoCheckChildNodes());

    Class<? extends ILookupCall<T>> lookupCallClass = getConfiguredLookupCall();
    if (lookupCallClass != null) {
      ILookupCall<T> call = BEANS.get(lookupCallClass);
      setLookupCall(call);
    }
    // code type
    if (getConfiguredCodeType() != null) {
      setCodeTypeClass(getConfiguredCodeType());
    }
    // local property listener
    addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        if (m_tree != null) {
          String name = e.getPropertyName();
          if (PROP_ENABLED.equals(name)) {
            m_tree.setEnabled(isEnabled());
          }
          else if (PROP_FILTER_CHECKED_NODES_VALUE.equals(name)) {
            updateCheckedNodesFilter();
          }
          else if (PROP_FILTER_ACTIVE_NODES_VALUE.equals(name)) {
            updateActiveNodesFilter();
          }
        }
      }
    });

    // add fields
    List<Class<IFormField>> configuredFields = getConfiguredFields();
    List<IFormField> contributedFields = m_contributionHolder.getContributionsByClass(IFormField.class);

    List<IFormField> fieldList = new ArrayList<IFormField>(configuredFields.size() + contributedFields.size());
    for (Class<? extends IFormField> fieldClazz : configuredFields) {
      fieldList.add(ConfigurationUtility.newInnerInstance(this, fieldClazz));
    }
    fieldList.addAll(contributedFields);
    Collections.sort(fieldList, new OrderedComparator());
    for (IFormField f : fieldList) {
      f.setParentFieldInternal(this);
    }
    m_fields = fieldList;
  }

  @Override
  public void addField(IFormField f) {
    CompositeFieldUtility.addField(f, this, m_fields);
  }

  @Override
  public void removeField(IFormField f) {
    CompositeFieldUtility.removeField(f, this, m_fields);
  }

  @Override
  public void moveFieldTo(IFormField f, ICompositeField newContainer) {
    CompositeFieldUtility.moveFieldTo(f, this, newContainer, m_movedFormFieldsByClass);
  }

  @Override
  public Map<Class<? extends IFormField>, IFormField> getMovedFields() {
    return Collections.unmodifiableMap(m_movedFormFieldsByClass);
  }

  @Override
  protected void initFieldInternal() {
    getTree().initTree();
    if (getConfiguredAutoLoad()) {
      try {
        setValueChangeTriggerEnabled(false);
        //
        loadRootNode();
      }
      finally {
        setValueChangeTriggerEnabled(true);
      }
    }
    super.initFieldInternal();
  }

  @Override
  protected void disposeFieldInternal() {
    super.disposeFieldInternal();
    getTree().disposeTree();
  }

  @SuppressWarnings("unchecked")
  public TreeBoxFilterBox getTreeBoxFilterBox() {
    return getFieldByClass(TreeBoxFilterBox.class);
  }

  /*
   * Runtime
   */
  @Override
  public final ITree getTree() {
    return m_tree;
  }

  @Override
  public void loadRootNode() {
    loadChildNodes(m_tree.getRootNode());
  }

  @Override
  public final void loadChildNodes(ITreeNode parentNode) {
    if (m_tree != null) {
      try {
        m_valueTreeSyncActive = true;
        m_tree.setTreeChanging(true);
        //
        interceptLoadChildNodes(parentNode);
        // when tree is non-incremental, mark all leaf cadidates as leafs
        if (!isLoadIncremental()) {
          ITreeVisitor v = new ITreeVisitor() {
            @Override
            public boolean visit(ITreeNode node) {
              if (node.getChildNodeCount() == 0) {
                node.setLeafInternal(true);
              }
              else {
                node.setLeafInternal(false);
              }
              return true;
            }
          };
          getTree().visitNode(getTree().getRootNode(), v);
        }
        // auto-expand all
        if (isAutoExpandAll()) {
          m_tree.expandAll(parentNode);
        }
      }
      finally {
        m_tree.setTreeChanging(false);
        m_valueTreeSyncActive = false;
      }
      syncValueToTree();
    }
  }

  public AbstractTreeNodeBuilder<T> getTreeNodeBuilder() {
    return new P_TreeNodeBuilder();
  }

  private void prepareLookupCall(ILookupCall<T> call, ITreeNode parent) {
    prepareLookupCallInternal(call, parent);
    interceptPrepareLookup(call, parent);
  }

  /**
   * do not use this internal method directly
   */
  @SuppressWarnings("unchecked")
  private void prepareLookupCallInternal(ILookupCall<T> call, ITreeNode parent) {
    // set parent key
    if (parent != null) {
      call.setRec((T) parent.getPrimaryKey());
    }
    else {
      call.setRec(null);
    }
    //when there is a master value defined in the original call, don't set it to null when no master value is available
    if (getMasterValue() != null || getLookupCall() == null || getLookupCall().getMaster() == null) {
      call.setMaster(getMasterValue());
    }
  }

  @Override
  public final ILookupCall<T> getLookupCall() {
    return m_lookupCall;
  }

  @Override
  public void setLookupCall(ILookupCall<T> call) {
    m_lookupCall = call;
  }

  @Override
  public Class<? extends ICodeType<?, T>> getCodeTypeClass() {
    return m_codeTypeClass;
  }

  @Override
  public void setCodeTypeClass(Class<? extends ICodeType<?, T>> codeTypeClass) {
    m_codeTypeClass = codeTypeClass;
    // create lookup service call
    m_lookupCall = null;
    if (m_codeTypeClass != null) {
      m_lookupCall = CodeLookupCall.newInstanceByService(m_codeTypeClass);
    }
  }

  @Override
  public List<ITreeNode> callChildLookup(ITreeNode parentNode) {
    List<? extends ILookupRow<T>> data = null;
    ILookupCall<T> call = getLookupCall();
    if (call != null) {
      call = BEANS.get(ILookupCallProvisioningService.class).newClonedInstance(call, new FormFieldProvisioningContext(AbstractTreeBox.this));
      prepareLookupCall(call, parentNode);
      data = call.getDataByRec();
      data = filterLookupResult(call, data);
      List<ITreeNode> subTree = new ArrayList<ITreeNode>(getTreeNodeBuilder().createTreeNodes(data, ITreeNode.STATUS_NON_CHANGED, false));
      filterNewNodesRec(subTree, parentNode != null ? parentNode.getTreeLevel() + 1 : 0);
      return subTree;
    }
    else {
      return CollectionUtility.emptyArrayList();
    }
  }

  @Override
  public List<ITreeNode> callCompleteTreeLookup() {
    List<? extends ILookupRow<T>> data = null;
    ILookupCall<T> call = getLookupCall();
    if (call != null) {
      call = BEANS.get(ILookupCallProvisioningService.class).newClonedInstance(call, new FormFieldProvisioningContext(AbstractTreeBox.this));
      prepareLookupCall(call, null);
      data = call.getDataByAll();
      data = filterLookupResult(call, data);
      if (data != null && data.size() > 1000) {
        LOG.warn("TreeBox {} has loadIncremental=false but produced more than 1000 rows; check if this is intended.", getClass().getSimpleName());
      }
      List<ITreeNode> subTree = getTreeNodeBuilder().createTreeNodes(data, ITreeNode.STATUS_NON_CHANGED, true);
      filterNewNodesRec(subTree, 0);
      return subTree;
    }
    else {
      return CollectionUtility.emptyArrayList();
    }
  }

  private List<? extends ILookupRow<T>> filterLookupResult(ILookupCall<T> call, List<? extends ILookupRow<T>> data) {
    List<ILookupRow<T>> result = CollectionUtility.arrayList(data);
    interceptFilterLookupResult(call, result);
    Iterator<ILookupRow<T>> resultIt = result.iterator();
    while (resultIt.hasNext()) {
      ILookupRow<T> row = resultIt.next();
      if (row == null) {
        resultIt.remove();
      }
      else if (row.getKey() == null) {
        LOG.warn("The key of a lookup row may not be null. Row has been removed for tree box '{}'.", getClass().getName());
        resultIt.remove();
      }
    }
    return result;
  }

  private void filterNewNodesRec(List<ITreeNode> nodes, int level) {
    if (nodes != null) {
      for (ITreeNode node : nodes) {
        if (node != null) {
          interceptFilterNewNode(node, level);
          filterNewNodesRec(node.getChildNodes(), level + 1);
        }
      }
    }
  }

  @Override
  protected void valueChangedInternal() {
    super.valueChangedInternal();
    syncValueToTree();
  }

  @Override
  protected String formatValueInternal(Set<T> validValue) {
    if (validValue == null || validValue.size() == 0) {
      return "";
    }
    StringBuilder b = new StringBuilder();
    Collection<ITreeNode> nodes = getTree().findNodes(validValue);
    if (nodes != null && !nodes.isEmpty()) {
      Iterator<ITreeNode> nodeIt = nodes.iterator();
      b.append(nodeIt.next().getCell().getText());
      while (nodeIt.hasNext()) {
        b.append(", ").append(nodeIt.next().getCell().getText());
      }
    }
    return b.toString();
  }

  @Override
  protected final Set<T> validateValueInternal(Set<T> rawValue0) {
    // ensure a copy of the input values
    Set<T> rawValue = CollectionUtility.hashSet(rawValue0);
    return doValidateValueInternal(rawValue);
  }

  /**
   * override this method to perform detailed validation in subclasses
   */
  protected Set<T> doValidateValueInternal(Set<T> rawValue) {
    if (CollectionUtility.isEmpty(rawValue)) {
      // fast return
      return rawValue;
    }
    ITree tree = getTree();
    if (tree != null) {
      if ((tree.isCheckable() && !tree.isMultiCheck()) || (!tree.isCheckable() && !tree.isMultiSelect())) {
        //only single value
        if (rawValue.size() > 1) {
          LOG.warn("{} only accepts a single value. Got {}. Using only first value.", getClass().getName(), rawValue);
          return CollectionUtility.hashSet(CollectionUtility.firstElement(rawValue));
        }
      }
    }
    return rawValue;
  }

  @Override
  public boolean isContentValid() {
    boolean valid = super.isContentValid();
    if (valid && isMandatory() && getValue().isEmpty()) {
      return false;
    }
    return valid;
  }

  /**
   * Value, empty {@link Set} in case of an empty value, never <code>null</code>.
   */
  @Override
  public Set<T> getValue() {
    return CollectionUtility.hashSet(super.getValue());
  }

  /**
   * Initial value, empty {@link Set} in case of an empty value, never <code>null</code>.
   */
  @Override
  public Set<T> getInitValue() {
    return CollectionUtility.hashSet(super.getInitValue());
  }

  @Override
  public T getSingleValue() {
    return CollectionUtility.firstElement(super.getValue());
  }

  @Override
  public void setSingleValue(T value) {
    Set<T> valueSet = new HashSet<T>();
    if (value != null) {
      valueSet.add(value);
    }
    setValue(valueSet);
  }

  @Override
  public int getCheckedKeyCount() {
    return getValue().size();
  }

  @Override
  public T getCheckedKey() {
    return CollectionUtility.firstElement(getCheckedKeys());
  }

  @Override
  public Set<T> getCheckedKeys() {
    return getValue();
  }

  @Override
  public void checkKey(T key) {
    Set<T> keySet = new HashSet<T>();
    if (key != null) {
      keySet.add(key);
    }
    checkKeys(keySet);
  }

  @Override
  public void checkKeys(Set<T> keys) {
    setValue(CollectionUtility.hashSetWithoutNullElements(keys));
  }

  @Override
  public void uncheckAllKeys() {
    checkKeys(null);
  }

  @Override
  public Set<T> getUncheckedKeys() {
    Set<T> set = new HashSet<T>();
    Set<T> a = getInitValue();
    if (a != null) {
      set.addAll(a);
    }
    a = getCheckedKeys();
    if (a != null) {
      set.removeAll(a);
    }
    return set;
  }

  @Override
  public void checkAllKeys() {
    final Set<T> keySet = new HashSet<T>();
    ITreeVisitor v = new ITreeVisitor() {
      @SuppressWarnings("unchecked")
      @Override
      public boolean visit(ITreeNode node) {
        if (node.getPrimaryKey() != null) {
          keySet.add((T) node.getPrimaryKey());
        }
        return true;
      }
    };
    m_tree.visitNode(m_tree.getRootNode(), v);
    checkKeys(keySet);
  }

  @Override
  public void exportFormFieldData(AbstractFormFieldData target) {
    @SuppressWarnings("unchecked")
    AbstractValueFieldData<Set<T>> v = (AbstractValueFieldData<Set<T>>) target;
    Set<T> value = getValue();
    if (CollectionUtility.isEmpty(value)) {
      v.setValue(null);
    }
    else {
      v.setValue(CollectionUtility.hashSet(this.getValue()));
    }
  }

  @Override
  public boolean isLoadIncremental() {
    return m_loadIncremental;
  }

  @Override
  public void setLoadIncremental(boolean b) {
    m_loadIncremental = b;
  }

  @Override
  public boolean isAutoExpandAll() {
    return m_autoExpandAll;
  }

  @Override
  public void setAutoExpandAll(boolean b) {
    m_autoExpandAll = b;
  }

  @Override
  public boolean isAutoCheckChildNodes() {
    return getTree().isAutoCheckChildNodes();
  }

  @Override
  public void setAutoCheckChildNodes(boolean b) {
    getTree().setAutoCheckChildNodes(b);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean isNodeActive(ITreeNode node) {
    if (node instanceof AbstractTreeBox.P_InternalTreeNode) {
      return ((P_InternalTreeNode) node).isActive();
    }
    else {
      return false;
    }
  }

  @Override
  public boolean isFilterActiveNodes() {
    return propertySupport.getPropertyBool(PROP_FILTER_ACTIVE_NODES);
  }

  @Override
  public void setFilterActiveNodes(boolean b) {
    propertySupport.setPropertyBool(PROP_FILTER_ACTIVE_NODES, b);
  }

  @Override
  public boolean getFilterCheckedNodesValue() {
    return propertySupport.getPropertyBool(PROP_FILTER_CHECKED_NODES_VALUE);
  }

  @Override
  public void setFilterCheckedNodesValue(boolean b) {
    propertySupport.setPropertyBool(PROP_FILTER_CHECKED_NODES_VALUE, b);
  }

  @Override
  public boolean isFilterCheckedNodes() {
    return propertySupport.getPropertyBool(PROP_FILTER_CHECKED_NODES);
  }

  @Override
  public void setFilterCheckedNodes(boolean b) {
    propertySupport.setPropertyBool(PROP_FILTER_CHECKED_NODES, b);
  }

  @Override
  public TriState getFilterActiveNodesValue() {
    return (TriState) propertySupport.getProperty(PROP_FILTER_ACTIVE_NODES_VALUE);
  }

  @Override
  public void setFilterActiveNodesValue(TriState t) {
    if (t == null) {
      t = TriState.TRUE;
    }
    propertySupport.setProperty(PROP_FILTER_ACTIVE_NODES_VALUE, t);
  }

  private void updateActiveNodesFilter() {
    try {
      m_tree.setTreeChanging(true);
      //
      if (m_activeNodesFilter != null) {
        m_tree.removeNodeFilter(m_activeNodesFilter);
        m_activeNodesFilter = null;
      }
      m_activeNodesFilter = new ActiveOrCheckedNodesFilter(this, getFilterActiveNodesValue());
      m_tree.addNodeFilter(m_activeNodesFilter);
    }
    finally {
      m_tree.setTreeChanging(false);
    }
  }

  private void updateCheckedNodesFilter() {
    try {
      m_tree.setTreeChanging(true);
      //
      if (m_checkedNodesFilter != null) {
        m_tree.removeNodeFilter(m_checkedNodesFilter);
        m_checkedNodesFilter = null;
      }
      if (getFilterCheckedNodesValue()) {
        m_checkedNodesFilter = new CheckedNodesFilter();
        m_tree.addNodeFilter(m_checkedNodesFilter);
      }
    }
    finally {
      m_tree.setTreeChanging(false);
    }
  }

  private void syncValueToTree() {
    if (m_valueTreeSyncActive) {
      return;
    }
    try {
      m_valueTreeSyncActive = true;
      getTree().setTreeChanging(true);
      //
      Set<T> checkedKeys = getCheckedKeys();
      Collection<ITreeNode> checkedNodes = m_tree.findNodes(checkedKeys);
      getTree().visitTree(new ITreeVisitor() {
        @Override
        public boolean visit(ITreeNode node) {
          node.setChecked(false);
          return true;
        }
      });
      for (ITreeNode node : checkedNodes) {
        node.setChecked(true);
      }
      if (!getTree().isCheckable()) {
        getTree().selectNodes(checkedNodes, false);
      }
    }
    finally {
      getTree().setTreeChanging(false);
      m_valueTreeSyncActive = false;
    }
  }

  @SuppressWarnings("unchecked")
  private void syncTreeToValue() {
    if (m_valueTreeSyncActive) {
      return;
    }
    boolean resync = false;
    try {
      m_valueTreeSyncActive = true;
      getTree().setTreeChanging(true);
      //
      Collection<ITreeNode> checkedNodes;
      if (getTree().isCheckable()) {
        checkedNodes = m_tree.getCheckedNodes();
      }
      else {
        checkedNodes = m_tree.getSelectedNodes();
      }
      Set<T> checkedKeys = new HashSet<T>();
      for (ITreeNode checkedNode : checkedNodes) {
        checkedKeys.add((T) checkedNode.getPrimaryKey());
      }
      checkKeys(checkedKeys);
      // Due to validate logic, the actual value
      // may differ now, making a resync of the value is necessary
      Set<T> validatedCheckedKeys = getCheckedKeys();
      if (!CollectionUtility.equalsCollection(checkedKeys, validatedCheckedKeys)) {
        resync = true;
      }
      if (!getTree().isCheckable()) {
        //checks follow selection
        getTree().visitTree(new ITreeVisitor() {
          @Override
          public boolean visit(ITreeNode node) {
            node.setChecked(node.isSelectedNode());
            return true;
          }
        });
      }
    }
    finally {
      getTree().setTreeChanging(false);
      m_valueTreeSyncActive = false;
    }
    if (resync) {
      // The value of the treeBox is different
      // from the one represented in the tree.
      // Need to sync.
      syncValueToTree();
    }

    // check if row filter needs to change
    if (!m_tree.getUIFacade().isUIProcessing()) {
      updateActiveNodesFilter();
    }
    updateCheckedNodesFilter();
  }

/*
 * Implementation of ICompositeField
 */

  /**
   * Sets the property on the field and on every child. <br>
   * During the initialization phase the children are not informed.
   *
   * @see #getConfiguredStatusVisible()
   */
  @Override
  public void setStatusVisible(boolean statusVisible) {
    setStatusVisible(statusVisible, isInitialized());
  }

  @Override
  public void setStatusVisible(boolean statusVisible, boolean recursive) {
    super.setStatusVisible(statusVisible);

    if (recursive) {
      for (IFormField f : m_fields) {
        f.setStatusVisible(statusVisible);
      }
    }
  }

  @Override
  public <F extends IFormField> F getFieldByClass(Class<F> c) {
    return CompositeFieldUtility.getFieldByClass(this, c);
  }

  @Override
  public IFormField getFieldById(String id) {
    return CompositeFieldUtility.getFieldById(this, id);
  }

  @Override
  public <X extends IFormField> X getFieldById(String id, Class<X> type) {
    return CompositeFieldUtility.getFieldById(this, id, type);
  }

  @Override
  public int getFieldCount() {
    return m_fields.size();
  }

  @Override
  public int getFieldIndex(IFormField f) {
    return m_fields.indexOf(f);
  }

  @Override
  public List<IFormField> getFields() {
    return CollectionUtility.arrayList(m_fields);
  }

  @Override
  public boolean visitFields(IFormFieldVisitor visitor, int startLevel) {
    // myself
    if (!visitor.visitField(this, startLevel, 0)) {
      return false;
    }
    // children
    int index = 0;
    for (IFormField field : m_fields) {
      if (field instanceof ICompositeField) {
        if (!((ICompositeField) field).visitFields(visitor, startLevel + 1)) {
          return false;
        }
      }
      else {
        if (!visitor.visitField(field, startLevel, index)) {
          return false;
        }
      }
      index++;
    }
    return true;
  }

  @Override
  public final int getGridColumnCount() {
    return 1;
  }

  @Override
  public final int getGridRowCount() {
    return 1;
  }

  @Override
  public void rebuildFieldGrid() {
  }

  @Order(1)
  @ClassId("5cfd2944-5dfd-4b66-ae45-419bb1b71378")
  public class TreeBoxFilterBox extends AbstractTreeBoxFilterBox {
    @Override
    protected ITreeBox getTreeBox() {
      return AbstractTreeBox.this;
    }
  }

  /**
   * TreeNode implementation with delegation of loadChildren to this.loadChildNodes()
   */
  private class P_InternalTreeNode extends AbstractTreeNode {
    private boolean m_active;

    public boolean isActive() {
      return m_active;
    }

    public void setActive(boolean b) {
      m_active = b;
    }

    @Override
    public void loadChildren() {
      if (isLoadIncremental()) {
        AbstractTreeBox.this.loadChildNodes(this);
      }
      else {
        setChildrenLoaded(true);
      }
    }
  }

  private class P_TreeNodeBuilder extends AbstractTreeNodeBuilder<T> {

    @Override
    protected ITreeNode createEmptyTreeNode() {
      return new P_InternalTreeNode();
    }

    @Override
    public ITreeNode createTreeNode(ILookupRow<T> lookupRow, int nodeStatus, boolean markChildrenLoaded) {
      @SuppressWarnings("unchecked")
      P_InternalTreeNode treeNode = (P_InternalTreeNode) super.createTreeNode(lookupRow, nodeStatus, markChildrenLoaded);
      treeNode.setActive(lookupRow.isActive());
      return treeNode;
    }

  }

  public class DefaultTreeBoxTree extends AbstractTree {

    @Override
    protected boolean getConfiguredMultiSelect() {
      return false;
    }

    @Override
    protected boolean getConfiguredCheckable() {
      return true;
    }

    @Override
    protected boolean getConfiguredRootNodeVisible() {
      return false;
    }
  }

  protected final void interceptFilterNewNode(ITreeNode newNode, int treeLevel) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    TreeBoxFilterNewNodeChain<T> chain = new TreeBoxFilterNewNodeChain<T>(extensions);
    chain.execFilterNewNode(newNode, treeLevel);
  }

  protected final void interceptLoadChildNodes(ITreeNode parentNode) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    TreeBoxLoadChildNodesChain<T> chain = new TreeBoxLoadChildNodesChain<T>(extensions);
    chain.execLoadChildNodes(parentNode);
  }

  protected final void interceptPrepareLookup(ILookupCall<T> call, ITreeNode parent) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    TreeBoxPrepareLookupChain<T> chain = new TreeBoxPrepareLookupChain<T>(extensions);
    chain.execPrepareLookup(call, parent);
  }

  protected final void interceptFilterLookupResult(ILookupCall<T> call, List<ILookupRow<T>> result) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    TreeBoxFilterLookupResultChain<T> chain = new TreeBoxFilterLookupResultChain<T>(extensions);
    chain.execFilterLookupResult(call, result);
  }

  protected static class LocalTreeBoxExtension<T, OWNER extends AbstractTreeBox<T>> extends LocalValueFieldExtension<Set<T>, OWNER> implements ITreeBoxExtension<T, OWNER> {

    public LocalTreeBoxExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execFilterNewNode(TreeBoxFilterNewNodeChain<T> chain, ITreeNode newNode, int treeLevel) {
      getOwner().execFilterNewNode(newNode, treeLevel);
    }

    @Override
    public void execLoadChildNodes(TreeBoxLoadChildNodesChain<T> chain, ITreeNode parentNode) {
      getOwner().execLoadChildNodes(parentNode);
    }

    @Override
    public void execPrepareLookup(TreeBoxPrepareLookupChain<T> chain, ILookupCall<T> call, ITreeNode parent) {
      getOwner().execPrepareLookup(call, parent);
    }

    @Override
    public void execFilterLookupResult(TreeBoxFilterLookupResultChain<T> chain, ILookupCall<T> call, List<ILookupRow<T>> result) {
      getOwner().execFilterLookupResult(call, result);
    }
  }

  @Override
  protected ITreeBoxExtension<T, ? extends AbstractTreeBox<T>> createLocalExtension() {
    return new LocalTreeBoxExtension<T, AbstractTreeBox<T>>(this);
  }

}
