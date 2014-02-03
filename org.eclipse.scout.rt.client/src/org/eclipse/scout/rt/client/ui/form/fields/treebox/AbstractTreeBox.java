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
package org.eclipse.scout.rt.client.ui.form.fields.treebox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.services.lookup.FormFieldProvisioningContext;
import org.eclipse.scout.rt.client.services.lookup.ILookupCallProvisioningService;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
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
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.lookup.CodeLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.service.SERVICES;

@ClassId("5648579d-1968-47be-a0c9-a8c846d2caf4")
public abstract class AbstractTreeBox<T> extends AbstractValueField<T[]> implements ITreeBox<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractTreeBox.class);

  private ITree m_tree;
  private LookupCall m_lookupCall;
  private Class<? extends ICodeType> m_codeTypeClass;
  private boolean m_valueTreeSyncActive;
  private boolean m_autoExpandAll;
  private boolean m_autoCheckChildNodes;
  private boolean m_loadIncremental;
  private ITreeBoxUIFacade m_uiFacade;
  private ITreeNodeFilter m_activeNodesFilter;
  private ITreeNodeFilter m_checkedNodesFilter;
  // children
  private IFormField[] m_fields;

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
   * ITreeNode[] children;
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
  protected void execLoadChildNodes(ITreeNode parentNode) throws ProcessingException {
    ITreeNode[] children;
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
  @ValidationRule(ValidationRule.LOOKUP_CALL)
  protected Class<? extends LookupCall> getConfiguredLookupCall() {
    return null;
  }

  @ConfigProperty(ConfigProperty.CODE_TYPE)
  @Order(250)
  @ValidationRule(ValidationRule.CODE_TYPE)
  protected Class<? extends ICodeType> getConfiguredCodeType() {
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

  /**
   * @return true: a filter is added to the treebox tree that only accepts nodes
   *         that are active or checked.<br>
   *         Affects {@link ITreeNode#getFilteredChildNodes()}
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(290)
  protected boolean getConfiguredFilterActiveNodes() {
    return false;
  }

  /**
   * @return true: a filter is added to the treebox tree that only accepts
   *         checked nodes<br>
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

  private Class<? extends IFormField>[] getConfiguredFields() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    Class[] filtered = ConfigurationUtility.filterClasses(dca, IFormField.class);
    return ConfigurationUtility.sortFilteredClassesByOrderAnnotation(filtered, IFormField.class);
  }

  /**
   * called before any lookup is performed
   */
  @ConfigOperation
  @Order(230)
  protected void execPrepareLookup(LookupCall call, ITreeNode parent) throws ProcessingException {
  }

  /**
   * @param call
   *          that produced this result
   * @param result
   *          live list containing the result rows. Add, remove, set, replace
   *          and clear of entries in this list is supported
   */
  @ConfigOperation
  @Order(240)
  protected void execFilterLookupResult(LookupCall call, List<LookupRow> result) throws ProcessingException {
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
  protected void execFilterNewNode(ITreeNode newNode, int treeLevel) throws ProcessingException {
  }

  private Class<? extends ITree> getConfiguredTree() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    Class<? extends ITree>[] f = ConfigurationUtility.filterClasses(dca, ITree.class);
    if (f.length == 1) {
      return f[0];
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
  protected void execChangedMasterValue(Object newMasterValue) throws ProcessingException {
    setValue(null);
    loadRootNode();
  }

  @Override
  protected void initConfig() {
    m_uiFacade = createUIFacade();
    m_fields = new IFormField[0];
    super.initConfig();
    setFilterActiveNodes(getConfiguredFilterActiveNodes());
    setFilterActiveNodesValue(TriState.TRUE);
    setFilterCheckedNodes(getConfiguredFilterCheckedNodes());
    setFilterCheckedNodesValue(getConfiguredFilterCheckedNodes());
    setAutoExpandAll(getConfiguredAutoExpandAll());
    setAutoCheckChildNodes(getConfiguredAutoCheckChildNodes());
    setLoadIncremental(getConfiguredLoadIncremental());
    try {
      m_tree = ConfigurationUtility.newInnerInstance(this, getConfiguredTree());
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
                case TreeEvent.TYPE_NODES_UPDATED: {
                  if (getTree().isCheckable()) {
                    if (isAutoCheckChildNodes() && e.getNodes() != null) {
                      for (ITreeNode node : e.getNodes()) {
                        execAutoCheckChildNodes(node, node.isChecked());
                      }
                    }
                    syncTreeToValue();
                  }
                  break;
                }
              }
            }
          }
          );
      m_tree.setEnabled(isEnabled());
      // default icon
      if (this.getConfiguredIconId() != null) {
        m_tree.setIconId(this.getConfiguredIconId());
      }
    }
    catch (Exception e) {
      LOG.warn(null, e);
    }
    if (getConfiguredLookupCall() != null) {
      try {
        LookupCall call = getConfiguredLookupCall().newInstance();
        setLookupCall(call);
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
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
    ArrayList<IFormField> fieldList = new ArrayList<IFormField>();
    Class<? extends IFormField>[] fieldArray = getConfiguredFields();
    for (int i = 0; i < fieldArray.length; i++) {
      IFormField f;
      try {
        f = ConfigurationUtility.newInnerInstance(this, fieldArray[i]);
        fieldList.add(f);
      }// end try
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("field: " + fieldArray[i].getName(), t));
      }
    }
    for (IFormField f : fieldList) {
      f.setParentFieldInternal(this);
    }
    m_fields = fieldList.toArray(new IFormField[0]);
  }

  @Override
  protected void initFieldInternal() throws ProcessingException {
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

  protected ITreeBoxUIFacade createUIFacade() {
    return new P_TreeBoxUIFacade();
  }

  @Override
  public ITreeBoxUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public void loadRootNode() throws ProcessingException {
    loadChildNodes(m_tree.getRootNode());
  }

  @Override
  public final void loadChildNodes(ITreeNode parentNode) throws ProcessingException {
    if (m_tree != null) {
      try {
        m_valueTreeSyncActive = true;
        m_tree.setTreeChanging(true);
        //
        execLoadChildNodes(parentNode);
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

  public AbstractTreeNodeBuilder getTreeNodeBuilder() {
    return new P_TreeNodeBuilder();
  }

  private void prepareLookupCall(LookupCall call, ITreeNode parent) throws ProcessingException {
    prepareLookupCallInternal(call, parent);
    execPrepareLookup(call, parent);
  }

  /**
   * do not use this internal method directly
   */
  private void prepareLookupCallInternal(LookupCall call, ITreeNode parent) {
    // set parent key
    if (parent != null) {
      call.setRec(parent.getPrimaryKey());
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
  public final LookupCall getLookupCall() {
    return m_lookupCall;
  }

  @Override
  public void setLookupCall(LookupCall call) {
    m_lookupCall = call;
  }

  @Override
  public Class<? extends ICodeType> getCodeTypeClass() {
    return m_codeTypeClass;
  }

  @Override
  public void setCodeTypeClass(Class<? extends ICodeType> codeTypeClass) {
    m_codeTypeClass = codeTypeClass;
    // create lookup service call
    m_lookupCall = null;
    if (m_codeTypeClass != null) {
      m_lookupCall = CodeLookupCall.newInstanceByService(m_codeTypeClass);
    }
  }

  @Override
  public ITreeNode[] callChildLookup(ITreeNode parentNode) throws ProcessingException {
    LookupRow[] data = null;
    LookupCall call = getLookupCall();
    if (call != null) {
      call = SERVICES.getService(ILookupCallProvisioningService.class).newClonedInstance(call, new FormFieldProvisioningContext(AbstractTreeBox.this));
      prepareLookupCall(call, parentNode);
      data = call.getDataByRec();
      data = filterLookupResult(call, data);
      ITreeNode[] subTree = getTreeNodeBuilder().createTreeNodes(data, ITreeNode.STATUS_NON_CHANGED, false);
      filterNewNodesRec(subTree, parentNode != null ? parentNode.getTreeLevel() + 1 : 0);
      return subTree;
    }
    else {
      return new ITreeNode[0];
    }
  }

  @Override
  public ITreeNode[] callCompleteTreeLookup() throws ProcessingException {
    LookupRow[] data = null;
    LookupCall call = getLookupCall();
    if (call != null) {
      call = SERVICES.getService(ILookupCallProvisioningService.class).newClonedInstance(call, new FormFieldProvisioningContext(AbstractTreeBox.this));
      prepareLookupCall(call, null);
      data = call.getDataByAll();
      data = filterLookupResult(call, data);
      if (data != null && data.length > 1000) {
        LOG.warn("TreeBox " + getClass().getSimpleName() + " has loadIncremental=false but produced more than 1000 rows; check if this is intended.");
      }
      ITreeNode[] subTree = getTreeNodeBuilder().createTreeNodes(data, ITreeNode.STATUS_NON_CHANGED, true);
      filterNewNodesRec(subTree, 0);
      return subTree;
    }
    else {
      return new ITreeNode[0];
    }
  }

  private LookupRow[] filterLookupResult(LookupCall call, LookupRow[] data) throws ProcessingException {
    ArrayList<LookupRow> result;
    if (data != null) {
      result = new ArrayList<LookupRow>(Arrays.asList(data));
    }
    else {
      result = new ArrayList<LookupRow>();
    }
    execFilterLookupResult(call, result);
    int len = 0;
    for (LookupRow r : result) {
      if (r != null) {
        len++;
      }
    }
    LookupRow[] a = new LookupRow[len];
    int index = 0;
    for (LookupRow r : result) {
      if (r != null) {
        a[index] = r;
        index++;
      }
    }
    return a;
  }

  private void filterNewNodesRec(ITreeNode[] nodes, int level) throws ProcessingException {
    if (nodes != null) {
      for (ITreeNode node : nodes) {
        if (node != null) {
          execFilterNewNode(node, level);
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
  protected String formatValueInternal(T[] validValue) {
    if (validValue == null || validValue.length == 0) {
      return "";
    }
    StringBuffer b = new StringBuffer();
    ITreeNode[] nodes = getTree().findNodes(validValue);
    for (int i = 0; i < nodes.length; i++) {
      if (i > 0) {
        b.append(", ");
      }
      b.append(nodes[i].getCell().getText());
    }
    return b.toString();
  }

  @Override
  protected T[] validateValueInternal(T[] rawValue) {
    T[] validValue = rawValue;
    //
    if (validValue != null && validValue.length == 0) {
      validValue = null;
    }
    ITree tree = getTree();
    if (tree != null && validValue != null) {
      if ((tree.isCheckable() && !tree.isMultiCheck()) || (!tree.isCheckable() && !tree.isMultiSelect())) {
        //only single value
        if (validValue.length > 1) {
          LOG.warn(getClass().getName() + " only accepts a single value. Got " + Arrays.toString(validValue) + ". Using only first value.");
          @SuppressWarnings("unchecked")
          T[] newArray = (T[]) Array.newInstance(validValue.getClass().getComponentType(), 1);
          newArray[0] = validValue[0];
          validValue = newArray;
        }
      }
    }
    return validValue;
  }

  @Override
  public T getSingleValue() {
    T[] a = getValue();
    if (a != null && a.length > 0) {
      return a[0];
    }
    else {
      return null;
    }
  }

  @Override
  public void setSingleValue(T value) {
    if (value == null) {
      setValue(null);
    }
    else {
      @SuppressWarnings("unchecked")
      T[] array = (T[]) Array.newInstance(getHolderType().getComponentType(), 1);
      array[0] = value;
      setValue(array);
    }
  }

  @Override
  public int getCheckedKeyCount() {
    T[] keys = getValue();
    if (keys != null) {
      return keys.length;
    }
    else {
      return 0;
    }
  }

  @Override
  public T getCheckedKey() {
    T[] a = getCheckedKeys();
    if (a != null && a.length > 0) {
      return a[0];
    }
    else {
      return null;
    }
  }

  @Override
  public T[] getCheckedKeys() {
    return getValue();
  }

  @Override
  public LookupRow getCheckedLookupRow() {
    LookupRow[] a = getCheckedLookupRows();
    if (a != null && a.length > 0) {
      return a[0];
    }
    else {
      return null;
    }
  }

  @Override
  public LookupRow[] getCheckedLookupRows() {
    LookupRow[] lookupRows = null;
    ITreeNode[] treeNodes = getTree().getCheckedNodes();
    if (treeNodes != null) {
      lookupRows = new LookupRow[treeNodes.length];
      for (int i = 0; i < treeNodes.length; i++) {
        ITreeNode node = treeNodes[i];
        ICell cell = node.getCell();
        ITreeNode parentNode = node.getParentNode();
        lookupRows[i] = new LookupRow(node.getPrimaryKey(), cell.getText(), cell.getIconId(), cell.getTooltipText(), cell.getBackgroundColor(), cell.getForegroundColor(), cell.getFont(), cell.isEnabled(), parentNode != null ? parentNode.getPrimaryKey() : null, isNodeActive(node));
      }
    }
    return lookupRows;
  }

  @Override
  public void checkKey(T key) {
    if (key == null) {
      checkKeys(null);
    }
    else {
      @SuppressWarnings("unchecked")
      T[] array = (T[]) Array.newInstance(getHolderType().getComponentType(), 1);
      array[0] = key;
      checkKeys(array);
    }
  }

  @Override
  public void checkKeys(T[] keys) {
    setValue(keys);
  }

  @Override
  public void uncheckAllKeys() {
    checkKeys(null);
  }

  @Override
  public T[] getUncheckedKeys() {
    HashSet<T> set = new HashSet<T>();
    T[] a = getInitValue();
    if (a != null) {
      set.addAll(Arrays.asList(a));
    }
    a = getCheckedKeys();
    if (a != null) {
      set.removeAll(Arrays.asList(a));
    }
    @SuppressWarnings("unchecked")
    T[] array = (T[]) Array.newInstance(getHolderType().getComponentType(), 0);
    a = set.toArray(array);
    if (a.length == 0) {
      a = null;
    }
    return a;
  }

  @Override
  public void checkAllKeys() {
    final HashSet<T> keySet = new HashSet<T>();
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
    @SuppressWarnings("unchecked")
    T[] array = (T[]) Array.newInstance(getHolderType().getComponentType(), 0);
    checkKeys(keySet.toArray(array));
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
    return m_autoCheckChildNodes;
  }

  @Override
  public void setAutoCheckChildNodes(boolean b) {
    m_autoCheckChildNodes = b;
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
      T[] checkedKeys = getCheckedKeys();
      ITreeNode[] checkedNodes = m_tree.findNodes(checkedKeys);
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

  protected void execAutoCheckChildNodes(ITreeNode node, boolean value) {
    for (ITreeNode childNode : node.getFilteredChildNodes()) {
      if (childNode.isEnabled() && childNode.isVisible()) {
        childNode.setChecked(value);
      }
      execAutoCheckChildNodes(childNode, value);
    }
  }

  @SuppressWarnings("unchecked")
  private void syncTreeToValue() {
    if (m_valueTreeSyncActive) {
      return;
    }
    try {
      m_valueTreeSyncActive = true;
      getTree().setTreeChanging(true);
      //
      T[] checkedKeys = null;
      ITreeNode[] checkedNodes;
      if (getTree().isCheckable()) {
        checkedNodes = m_tree.getCheckedNodes();
      }
      else {
        checkedNodes = m_tree.getSelectedNodes();
      }
      checkedKeys = (T[]) Array.newInstance(getHolderType().getComponentType(), checkedNodes.length);
      for (int i = 0; i < checkedNodes.length; i++) {
        checkedKeys[i] = (T) checkedNodes[i].getPrimaryKey();
      }
      checkKeys(checkedKeys);
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
    // check if row filter needs to change
    if (!m_tree.getUIFacade().isUIProcessing()) {
      updateActiveNodesFilter();
    }
    updateCheckedNodesFilter();
  }

  /*
   * Implementation of ICompositeField
   */

  @SuppressWarnings("unchecked")
  @Override
  public <F extends IFormField> F getFieldByClass(final Class<F> c) {
    final Holder<IFormField> found = new Holder<IFormField>(IFormField.class);
    IFormFieldVisitor v = new IFormFieldVisitor() {
      @Override
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (field.getClass() == c) {
          found.setValue(field);
        }
        return found.getValue() == null;
      }
    };
    visitFields(v, 0);
    return (F) found.getValue();
  }

  @Override
  public IFormField getFieldById(final String id) {
    final Holder<IFormField> found = new Holder<IFormField>(IFormField.class);
    IFormFieldVisitor v = new IFormFieldVisitor() {
      @Override
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (field.getFieldId().equals(id)) {
          found.setValue(field);
        }
        return found.getValue() == null;
      }
    };
    visitFields(v, 0);
    return found.getValue();
  }

  @Override
  public <X extends IFormField> X getFieldById(final String id, final Class<X> type) {
    final Holder<X> found = new Holder<X>(type);
    IFormFieldVisitor v = new IFormFieldVisitor() {
      @Override
      @SuppressWarnings("unchecked")
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (type.isAssignableFrom(field.getClass()) && field.getFieldId().equals(id)) {
          found.setValue((X) field);
        }
        return found.getValue() == null;
      }
    };
    visitFields(v, 0);
    return found.getValue();
  }

  @Override
  public int getFieldCount() {
    return m_fields.length;
  }

  @Override
  public int getFieldIndex(IFormField f) {
    for (int i = 0; i < m_fields.length; i++) {
      if (m_fields[i] == f) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public IFormField[] getFields() {
    IFormField[] a = new IFormField[m_fields.length];
    System.arraycopy(m_fields, 0, a, 0, a.length);
    return a;
  }

  @Override
  public boolean visitFields(IFormFieldVisitor visitor, int startLevel) {
    // myself
    if (!visitor.visitField(this, startLevel, 0)) {
      return false;
    }
    // children
    int index = 0;
    IFormField[] f = m_fields;
    for (int i = 0; i < f.length; i++) {
      if (f[i] instanceof ICompositeField) {
        if (!((ICompositeField) f[i]).visitFields(visitor, startLevel + 1)) {
          return false;
        }
      }
      else {
        if (!visitor.visitField(f[i], startLevel, index)) {
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
  public class TreeBoxFilterBox extends AbstractTreeBoxFilterBox {
    @Override
    protected ITreeBox getTreeBox() {
      return AbstractTreeBox.this;
    }
  }

  /**
   * TreeNode implementation with delegation of loadChildren to
   * this.loadChildNodes()
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
    public void loadChildren() throws ProcessingException {
      if (isLoadIncremental()) {
        AbstractTreeBox.this.loadChildNodes(this);
      }
      else {
        setChildrenLoaded(true);
      }
    }
  }

  private class P_TreeNodeBuilder extends AbstractTreeNodeBuilder {

    @Override
    protected ITreeNode createEmptyTreeNode() throws ProcessingException {
      return new P_InternalTreeNode();
    }

    @Override
    public ITreeNode createTreeNode(LookupRow lookupRow, int nodeStatus, boolean markChildrenLoaded) throws ProcessingException {
      @SuppressWarnings("unchecked")
      P_InternalTreeNode treeNode = (P_InternalTreeNode) super.createTreeNode(lookupRow, nodeStatus, markChildrenLoaded);
      treeNode.setActive(lookupRow.isActive());
      return treeNode;
    }

  }

  @Order(10000)
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

  /*
   * UI Notifications
   */
  protected class P_TreeBoxUIFacade implements ITreeBoxUIFacade {
  }
}
