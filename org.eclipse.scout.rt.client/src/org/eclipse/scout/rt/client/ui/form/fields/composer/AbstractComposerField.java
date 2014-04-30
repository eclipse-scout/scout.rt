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
package org.eclipse.scout.rt.client.ui.form.fields.composer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeVisitor;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.attribute.IComposerAttribute;
import org.eclipse.scout.rt.client.ui.form.fields.composer.entity.IComposerEntity;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.AttributeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.EitherOrNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.EntityNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.RootNode;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.AbstractComposerData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerAttributeNodeData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerEitherOrNodeData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerEntityNodeData;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.AbstractTreeFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.TreeNodeData;
import org.eclipse.scout.rt.shared.data.model.AttributePath;
import org.eclipse.scout.rt.shared.data.model.DataModelAttributeOp;
import org.eclipse.scout.rt.shared.data.model.DataModelConstants;
import org.eclipse.scout.rt.shared.data.model.DataModelUtility;
import org.eclipse.scout.rt.shared.data.model.EntityPath;
import org.eclipse.scout.rt.shared.data.model.IDataModel;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttributeOp;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;

@ClassId("8e7f7eb8-be18-48e5-9efe-8a5b3459e247")
@SuppressWarnings("deprecation")
@FormData(value = AbstractComposerData.class, sdkCommand = SdkCommand.USE, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE)
public abstract class AbstractComposerField extends AbstractFormField implements IComposerField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractComposerField.class);

  private IComposerFieldUIFacade m_uiFacade;
  private ITree m_tree;
  private SimpleXmlElement m_initValue;
  private IDataModel m_dataModel;

  public AbstractComposerField() {
    this(true);
  }

  public AbstractComposerField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  public IDataModel getDataModel() {
    return m_dataModel;
  }

  @Override
  public void setDataModel(IDataModel dm) {
    m_dataModel = dm;
  }

  /**
   * The default creates a data model with the inner entities and attributes
   * <p>
   * Other implementations may use a data model defined in the shared area
   */
  @ConfigOperation
  @Order(97)
  protected IDataModel execCreateDataModel() {
    ComposerFieldDataModel m = new ComposerFieldDataModel(this);
    m.init();
    return m;
  }

  /**
   * see {@link #execResolveEntityPath(EntityNode)}
   */
  @ConfigOperation
  @Order(98)
  protected void execResolveRootPathForTopLevelEntity(IDataModelEntity e, List<IDataModelEntity> lifeList) {
    IDataModelEntity tmp = (e != null ? e.getParentEntity() : null);
    while (tmp != null) {
      lifeList.add(0, tmp);
      tmp = tmp.getParentEntity();
    }
  }

  /**
   * see {@link #execResolveAttributePath(AttributeNode)}
   */
  @ConfigOperation
  @Order(98)
  protected void execResolveRootPathForTopLevelAttribute(IDataModelAttribute a, List<IDataModelEntity> lifeList) {
    IDataModelEntity tmp = (a != null ? a.getParentEntity() : null);
    while (tmp != null) {
      lifeList.add(0, tmp);
      tmp = tmp.getParentEntity();
    }
  }

  /**
   * For {@link #exportFormFieldData(AbstractFormFieldData)}, {@link AbstractTree#exportTreeData(AbstractTreeFieldData)}
   * and {@link #storeXML(SimpleXmlElement)} it is necessary to export {@link IDataModelEntity} and
   * {@link IDataModelAttribute} as external strings. see {@link EntityPath}
   * <p>
   * This callback completes an entity path to its root. The parameter path contains the entity path represented in the
   * composer tree of {@link EntityNode}s, the last element is the deepest tree node.
   * <p>
   * The default traverses the tree up to the root and collects all non-null {@link EntityNode#getEntity()}
   * <p>
   * This is prefixed with {@link #execResolveRootPathForTopLevelEntity(IDataModelEntity, List)}
   */
  @ConfigOperation
  @Order(99)
  protected EntityPath execResolveEntityPath(EntityNode node) {
    LinkedList<IDataModelEntity> list = new LinkedList<IDataModelEntity>();
    EntityNode tmp = node;
    while (tmp != null) {
      if (tmp.getEntity() != null) {
        list.add(0, tmp.getEntity());
      }
      //next
      tmp = tmp.getAncestorNode(EntityNode.class);
    }
    if (list.size() > 0) {
      execResolveRootPathForTopLevelEntity(list.get(0), list);
    }
    return new EntityPath(list);
  }

  /**
   * see {@link #execResolveEntityPathForEntityExport(EntityNode)}, {@link AttributePath} for more details
   * <p>
   * The path in the composer tree is prefixed with
   * {@link #execResolveRootPathForTopLevelAttribute(IDataModelAttribute, List)}
   */
  @ConfigOperation
  @Order(99)
  protected AttributePath execResolveAttributePath(AttributeNode node) {
    LinkedList<IDataModelEntity> list = new LinkedList<IDataModelEntity>();
    if (node == null) {
      return null;
    }
    EntityNode tmp = node.getAncestorNode(EntityNode.class);
    while (tmp != null) {
      if (tmp.getEntity() != null) {
        list.add(0, tmp.getEntity());
      }
      //next
      tmp = tmp.getAncestorNode(EntityNode.class);
    }
    if (list.size() > 0) {
      execResolveRootPathForTopLevelEntity(list.get(0), list);
    }
    else {
      execResolveRootPathForTopLevelAttribute(node.getAttribute(), list);
    }
    return new AttributePath(list, node.getAttribute());
  }

  /*
   * Configuration
   */
  private Class<? extends ITree> getConfiguredTree() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<ITree>> f = ConfigurationUtility.filterClasses(dca, ITree.class);
    if (f.size() == 1) {
      return CollectionUtility.firstElement(f);
    }
    else {
      for (Class<? extends ITree> c : f) {
        if (c.getDeclaringClass() != AbstractComposerField.class) {
          return c;
        }
      }
      return null;
    }
  }

  /**
   * Override this method to decorate or enhance new nodes whenever they are created
   * 
   * @return the new node, must not be null
   *         <p>
   *         Normally overrides call super. {@link #execCreateRootNode()}
   */
  @ConfigOperation
  @Order(100)
  protected RootNode execCreateRootNode() {
    return new RootNode(this);
  }

  /**
   * Override this method to decorate or enhance new nodes whenever they are created
   * 
   * @return the new node or null to ignore the add of a new node of this type
   *         <p>
   *         Normally overrides call super. {@link #execCreateEntityNode(ITreeNode, IDataModelEntity, boolean, Object[],
   *         List<String>)}
   */
  @ConfigOperation
  @Order(110)
  protected EntityNode execCreateEntityNode(ITreeNode parentNode, IDataModelEntity e, boolean negated, List<? extends Object> values, List<String> texts) {
    EntityNode node = new EntityNode(this, e);
    node.setValues(values);
    node.setTexts(texts);
    node.setNegative(negated);
    node.setStatus(ITreeNode.STATUS_INSERTED);
    return node;
  }

  /**
   * Override this method to decorate or enhance new nodes whenever they are created
   * 
   * @return the new node or null to ignore the add of a new node of this type
   *         <p>
   *         Normally overrides call super. {@link #execCreateAttributeNode(ITreeNode, IDataModelAttribute, Integer,
   *         IComposerOp, Object[], List<String>)}
   */
  @ConfigOperation
  @Order(120)
  protected AttributeNode execCreateAttributeNode(ITreeNode parentNode, IDataModelAttribute a, Integer aggregationType, IDataModelAttributeOp op, List<? extends Object> values, List<String> texts) {
    if (aggregationType != null && aggregationType == DataModelConstants.AGGREGATION_NONE) {
      aggregationType = null;
    }
    AttributeNode node = new AttributeNode(this, a);
    node.setAggregationType(aggregationType);
    node.setOp(op);
    node.setValues(values);
    node.setTexts(texts);
    node.setStatus(ITreeNode.STATUS_INSERTED);
    return node;
  }

  /**
   * Override this method to decorate or enhance new nodes whenever they are created
   * 
   * @return the new node or null to ignore the add of a new node of this type
   *         <p>
   *         Normally overrides call super.{@link #execCreateEitherNode(ITreeNode, boolean)}
   */
  @ConfigOperation
  @Order(130)
  protected EitherOrNode execCreateEitherNode(ITreeNode parentNode, boolean negated) {
    EitherOrNode node = new EitherOrNode(this, true);
    node.setNegative(negated);
    node.setStatus(ITreeNode.STATUS_INSERTED);
    return node;
  }

  /**
   * Override this method to decorate or enhance new nodes whenever they are created
   * 
   * @return the new node or null to ignore the add of a new node of this type
   *         <p>
   *         Normally overrides call super.{@link #execCreateAdditionalOrNode(ITreeNode, boolean)}
   */
  @ConfigOperation
  @Order(140)
  protected EitherOrNode execCreateAdditionalOrNode(ITreeNode eitherOrNode, boolean negated) {
    EitherOrNode node = new EitherOrNode(this, false);
    node.setNegative(negated);
    node.setStatus(ITreeNode.STATUS_INSERTED);
    return node;
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
    m_dataModel = execCreateDataModel();
    // tree
    if (getConfiguredTree() != null) {
      try {
        m_tree = ConfigurationUtility.newInnerInstance(this, getConfiguredTree());
        RootNode rootNode = execCreateRootNode();
        rootNode.getCellForUpdate().setText(getLabel());
        m_tree.setRootNode(rootNode);
        m_tree.setNodeExpanded(rootNode, true);
        m_tree.setEnabled(isEnabled());
        m_tree.addTreeListener(
            new TreeAdapter() {
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
            );
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    else {
      LOG.warn("there is no inner class of type ITree in " + getClass());
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

  /*
   * Runtime
   */
  @Override
  protected void initFieldInternal() throws ProcessingException {
    getTree().initTree();
    super.initFieldInternal();
  }

  @Override
  protected void disposeFieldInternal() {
    super.disposeFieldInternal();
    getTree().disposeTree();
  }

  @Override
  public final ITree getTree() {
    return m_tree;
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
  public List<IDataModelAttribute> getAttributes() {
    return m_dataModel.getAttributes();
  }

  @Override
  public List<IDataModelEntity> getEntities() {
    return m_dataModel.getEntities();
  }

  /**
   * @deprecated use {@link #getEntities()} instead. Will be removed in the 5.0 Release.
   */
  @Deprecated
  public List<IComposerEntity> getComposerEntities() {
    List<IDataModelEntity> entities = m_dataModel.getEntities();
    List<IComposerEntity> result = new ArrayList<IComposerEntity>(entities.size());
    for (IDataModelEntity e : entities) {
      if (e instanceof IComposerEntity) {
        result.add((IComposerEntity) e);
      }
    }
    return result;
  }

  /**
   * @deprecated use {@link #getAttributes()} instead. Will be removed in the 5.0 Release.
   */
  @Deprecated
  public List<IComposerAttribute> getComposerAttributes() {
    List<IDataModelAttribute> attributes = m_dataModel.getAttributes();
    List<IComposerAttribute> result = new ArrayList<IComposerAttribute>(attributes.size());
    for (IDataModelAttribute attribute : attributes) {
      if (attribute instanceof IComposerAttribute) {
        result.add((IComposerAttribute) attribute);
      }
    }
    return result;
  }

  @Override
  public void loadXML(SimpleXmlElement x) throws ProcessingException {
    super.loadXML(x);
    ITree tree = getTree();
    try {
      tree.setTreeChanging(true);
      //
      getTree().removeAllChildNodes(getTree().getRootNode());
      loadXMLRec(x, getTree().getRootNode());
    }
    finally {
      tree.setTreeChanging(false);
    }
  }

  private void loadXMLRec(SimpleXmlElement x, ITreeNode parent) {
    // build tree
    for (SimpleXmlElement xmlElem : x.getChildren()) {
      if ("attribute".equals(xmlElem.getName())) {
        String id = xmlElem.getStringAttribute("id");
        IDataModelAttributeOp op;
        Integer aggregationType;
        try {
          op = DataModelAttributeOp.create(xmlElem.getIntAttribute("op", DataModelConstants.OPERATOR_EQ));
          aggregationType = xmlElem.getIntAttribute("aggregationType", 0);
          if (aggregationType == 0) {
            aggregationType = null;
          }
        }
        catch (Exception e) {
          LOG.warn("read op", e);
          continue;
        }
        ArrayList<Object> valueList = new ArrayList<Object>();
        try {
          for (int i = 1; i <= 5; i++) {
            String valueName = (i == 1 ? "value" : "value" + i);
            if (xmlElem.hasAttribute(valueName)) {
              valueList.add(xmlElem.getObjectAttribute(valueName, null));
            }
          }
        }
        catch (Exception e) {
          LOG.warn("read value for attribute " + id, e);
          continue;
        }
        ArrayList<String> displayValueList = new ArrayList<String>();
        for (int i = 1; i <= 5; i++) {
          String displayValueName = (i == 1 ? "displayValue" : "displayValue" + i);
          if (xmlElem.hasAttribute(displayValueName)) {
            displayValueList.add(xmlElem.getStringAttribute(displayValueName, null));
          }
        }
        // find definition
        AttributePath attPath = DataModelUtility.externalIdToAttributePath(getDataModel(), id);
        IDataModelAttribute foundAtt = (attPath != null ? attPath.getAttribute() : null);
        if (foundAtt == null) {
          LOG.warn("cannot find attribute with id=" + id);
          continue;
        }
        ITreeNode node = addAttributeNode(parent, foundAtt, aggregationType, op, valueList, displayValueList);
        if (node != null) {
          // add children recursive
          loadXMLRec(xmlElem, node);
        }
      }
      else if ("entity".equals(xmlElem.getName())) {
        String id = xmlElem.getStringAttribute("id");
        boolean negated = xmlElem.getStringAttribute("negated", "false").equalsIgnoreCase("true");
        String text = xmlElem.getStringAttribute("displayValues", null);
        // find definition
        EntityPath entityPath = DataModelUtility.externalIdToEntityPath(getDataModel(), id);
        IDataModelEntity foundEntity = (entityPath != null ? entityPath.lastElement() : null);
        if (foundEntity == null) {
          LOG.warn("cannot find entity with id=" + id);
          continue;
        }
        ITreeNode node = addEntityNode(parent, foundEntity, negated, null, text != null ? Collections.singletonList(text) : null);
        if (node != null) {
          // add children recursive
          loadXMLRec(xmlElem, node);
        }
      }
      else if ("or".equals(xmlElem.getName())) {
        boolean beginning = xmlElem.getStringAttribute("begin", "false").equalsIgnoreCase("true");
        boolean negated = xmlElem.getStringAttribute("negated", "false").equalsIgnoreCase("true");
        ITreeNode node = null;
        if (beginning) {
          node = addEitherNode(parent, negated);
        }
        else {
          // find last EitherOrNode
          EitherOrNode orNode = null;
          for (ITreeNode n : parent.getChildNodes()) {
            if (n instanceof EitherOrNode && ((EitherOrNode) n).isBeginOfEitherOr()) {
              orNode = (EitherOrNode) n;
            }
          }
          if (orNode != null) {
            node = addAdditionalOrNode(orNode, negated);
          }
        }
        if (node != null) {
          // add children recursive
          loadXMLRec(xmlElem, node);
        }
      }

    }
  }

  @Override
  public void storeXML(SimpleXmlElement x) throws ProcessingException {
    super.storeXML(x);
    storeXMLRec(x, getTree().getRootNode());
  }

  private void createDataModelEntityPathRec(EntityNode node, List<IDataModelEntity> list) {
  }

  private void storeXMLRec(SimpleXmlElement x, ITreeNode parent) {
    for (ITreeNode node : parent.getChildNodes()) {
      if (node instanceof EntityNode) {
        EntityNode entityNode = (EntityNode) node;
        SimpleXmlElement xEntity = new SimpleXmlElement("entity");
        xEntity.setAttribute("id", DataModelUtility.entityPathToExternalId(getDataModel(), execResolveEntityPath(entityNode)));
        xEntity.setAttribute("negated", (entityNode.isNegative() ? "true" : "false"));
        List<String> texts = entityNode.getTexts();
        xEntity.setAttribute("displayValues", CollectionUtility.hasElements(texts) ? StringUtility.emptyIfNull(CollectionUtility.firstElement(texts)) : null);
        x.addChild(xEntity);
        // recursion
        storeXMLRec(xEntity, node);
      }
      else if (node instanceof AttributeNode) {
        AttributeNode attNode = (AttributeNode) node;
        SimpleXmlElement xAtt = new SimpleXmlElement("attribute");
        xAtt.setAttribute("id", DataModelUtility.attributePathToExternalId(getDataModel(), execResolveAttributePath(attNode)));
        IDataModelAttributeOp op = attNode.getOp();
        try {
          xAtt.setAttribute("op", op.getOperator());
          if (attNode.getAggregationType() != null) {
            xAtt.setIntAttribute("aggregationType", attNode.getAggregationType());
          }
        }
        catch (Exception e) {
          LOG.warn("write op " + op, e);
        }
        List<String> texts = attNode.getTexts();
        if (CollectionUtility.hasElements(texts)) {
          Iterator<String> it = texts.iterator();
          xAtt.setAttribute("displayValue", StringUtility.emptyIfNull(it.next()));
          int i = 2;
          while (it.hasNext()) {
            xAtt.setAttribute(("displayValue" + i), StringUtility.emptyIfNull(it.next()));
            i++;
          }
        }
        List<Object> values = attNode.getValues();
        if (values != null) {
          int i = 0;
          for (Object value : values) {
            String valueName = (i == 0 ? "value" : "value" + (i + 1));
            try {
              xAtt.setObjectAttribute(valueName, value);
            }
            catch (Exception e) {
              LOG.warn("write value[" + i + "] for attribute " + attNode.getAttribute() + ": " + value, e);
            }
            i++;
          }
        }
        x.addChild(xAtt);
      }
      else if (node instanceof EitherOrNode) {
        EitherOrNode orNode = (EitherOrNode) node;
        SimpleXmlElement xOr = new SimpleXmlElement("or");
        xOr.setAttribute("begin", "" + orNode.isBeginOfEitherOr());
        xOr.setAttribute("negated", (orNode.isNegative() ? "true" : "false"));
        x.addChild(xOr);
        // recursion
        storeXMLRec(xOr, node);
      }
    }
  }

  @Override
  public void resetValue() {
    if (m_initValue == null) {
      getTree().removeAllChildNodes(getTree().getRootNode());
    }
    else {
      try {
        loadXML(m_initValue);
      }
      catch (ProcessingException e) {
        LOG.error("unexpected error occured while restoring initial value", e);
        getTree().removeAllChildNodes(getTree().getRootNode());
      }
    }

    checkSaveNeeded();
    checkEmpty();
  }

  @Override
  public EntityNode addEntityNode(ITreeNode parentNode, IDataModelEntity e, boolean negated, List<? extends Object> values, List<String> texts) {
    EntityNode node = execCreateEntityNode(parentNode, e, negated, values, texts);
    if (node != null) {
      getTree().addChildNode(parentNode, node);
      getTree().setNodeExpanded(node, true);
    }
    return node;
  }

  @Override
  public EitherOrNode addEitherNode(ITreeNode parentNode, boolean negated) {
    EitherOrNode node = execCreateEitherNode(parentNode, negated);
    if (node != null) {
      getTree().addChildNode(parentNode, node);
      getTree().setNodeExpanded(node, true);
    }
    return node;
  }

  @Override
  public EitherOrNode addAdditionalOrNode(ITreeNode eitherOrNode, boolean negated) {
    EitherOrNode node = execCreateAdditionalOrNode(eitherOrNode, negated);
    if (node != null) {
      getTree().addChildNode(eitherOrNode.getChildNodeIndex() + 1, eitherOrNode.getParentNode(), node);
      getTree().setNodeExpanded(node, true);
    }
    return node;
  }

  @Override
  public AttributeNode addAttributeNode(ITreeNode parentNode, IDataModelAttribute a, Integer aggregationType, IDataModelAttributeOp op, List<? extends Object> values, List<String> texts) {
    AttributeNode node = execCreateAttributeNode(parentNode, a, aggregationType, op, values, texts);
    if (node != null) {
      getTree().addChildNode(parentNode, node);
    }
    return node;
  }

  public void updateInitialValue() {
    try {
      // clone composer field by storing as XML
      SimpleXmlElement element = new SimpleXmlElement();
      storeXML(element);
      m_initValue = element;
    }
    catch (ProcessingException e) {
      LOG.warn("unexpected error occured while storing initial value", e);
    }
  }

  @Override
  protected boolean execIsSaveNeeded() throws ProcessingException {
    boolean b = false;
    if (b == false && m_tree.getDeletedNodeCount() > 0) {
      b = true;
    }
    if (b == false && m_tree.getInsertedNodeCount() > 0) {
      b = true;
    }
    if (b == false && m_tree.getUpdatedNodeCount() > 0) {
      b = true;
    }
    return b;
  }

  @Override
  protected void execMarkSaved() throws ProcessingException {
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

      updateInitialValue();
    }
    finally {
      m_tree.setTreeChanging(false);
    }
  }

  @Override
  protected boolean execIsEmpty() throws ProcessingException {
    if (m_tree.getRootNode() != null && m_tree.getRootNode().getChildNodeCount() > 0) {
      return false;
    }
    return true;
  }

  @Override
  public IComposerFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  /**
   * ui facade
   */
  private class P_UIFacade implements IComposerFieldUIFacade {

  }

  /**
   * inner tree type
   */
  public class Tree extends AbstractTree {

    @Override
    protected boolean getConfiguredRootNodeVisible() {
      return true;
    }

    @Override
    protected TreeNodeData exportTreeNodeData(ITreeNode node, AbstractTreeFieldData treeData) throws ProcessingException {
      if (node instanceof EntityNode) {
        EntityNode enode = (EntityNode) node;
        String externalId = DataModelUtility.entityPathToExternalId(getDataModel(), execResolveEntityPath(enode));
        if (externalId == null) {
          if (LOG.isInfoEnabled()) {
            LOG.info("could not find entity data for: " + enode.getEntity());
          }
          return null;
        }
        ComposerEntityNodeData data = new ComposerEntityNodeData();
        data.setEntityExternalId(externalId);
        data.setNegative(enode.isNegative());
        return data;
      }
      else if (node instanceof AttributeNode) {
        AttributeNode anode = (AttributeNode) node;
        String externalId = DataModelUtility.attributePathToExternalId(getDataModel(), execResolveAttributePath(anode));
        if (externalId == null) {
          if (LOG.isInfoEnabled()) {
            LOG.info("could not find attribute data for: " + anode.getAttribute());
          }
          return null;
        }
        ComposerAttributeNodeData data = new ComposerAttributeNodeData();
        data.setAttributeExternalId(externalId);
        data.setNegative(false);
        data.setAggregationType(anode.getAggregationType());
        data.setOperator(anode.getOp().getOperator());
        data.setValues(anode.getValues());
        data.setTexts(anode.getTexts());
        return data;
      }
      else if (node instanceof EitherOrNode) {
        EitherOrNode eonode = (EitherOrNode) node;
        ComposerEitherOrNodeData data = new ComposerEitherOrNodeData();
        data.setNegative(eonode.isNegative());
        data.setBeginOfEitherOr(eonode.isBeginOfEitherOr());
        return data;
      }
      else {
        return null;
      }
    }

    @Override
    protected ITreeNode importTreeNodeData(ITreeNode parentNode, AbstractTreeFieldData treeData, TreeNodeData nodeData) throws ProcessingException {
      if (nodeData instanceof ComposerEntityNodeData) {
        ComposerEntityNodeData enodeData = (ComposerEntityNodeData) nodeData;
        String externalId = enodeData.getEntityExternalId();
        EntityPath entityPath = DataModelUtility.externalIdToEntityPath(getDataModel(), externalId);
        IDataModelEntity e = (entityPath != null ? entityPath.lastElement() : null);
        if (e == null) {
          LOG.warn("could not find entity for: " + enodeData.getEntityExternalId());
          return null;
        }
        return addEntityNode(parentNode, e, enodeData.isNegative(), null, enodeData.getTexts());
      }
      else if (nodeData instanceof ComposerAttributeNodeData) {
        ComposerAttributeNodeData anodeData = (ComposerAttributeNodeData) nodeData;
        String externalId = anodeData.getAttributeExternalId();
        AttributePath attPath = DataModelUtility.externalIdToAttributePath(getDataModel(), externalId);
        IDataModelAttribute a = (attPath != null ? attPath.getAttribute() : null);
        if (a == null) {
          LOG.warn("could not find attribute for: " + anodeData.getAttributeExternalId());
          return null;
        }
        IDataModelAttributeOp op;
        try {
          op = DataModelAttributeOp.create(anodeData.getOperator());
        }
        catch (Exception e) {
          LOG.warn("read op " + anodeData.getOperator(), e);
          return null;
        }
        return addAttributeNode(parentNode, a, anodeData.getAggregationType(), op, anodeData.getValues(), anodeData.getTexts());
      }
      else if (nodeData instanceof ComposerEitherOrNodeData) {
        ComposerEitherOrNodeData eonodeData = (ComposerEitherOrNodeData) nodeData;
        if (eonodeData.isBeginOfEitherOr()) {
          return addEitherNode(parentNode, eonodeData.isNegative());
        }
        else {
          // Bugzilla 400056: get sibling either/or node, must be latest child
          ITreeNode eitherOrNode = parentNode.getChildNode(parentNode.getChildNodeCount() - 1);
          return addAdditionalOrNode(eitherOrNode, eonodeData.isNegative());
        }
      }
      else {
        return null;
      }
    }

    @Override
    protected void execDecorateCell(ITreeNode node, Cell cell) throws ProcessingException {
      node.decorateCell();
      if (getIconId() != null) {
        cell.setIconId(getIconId());
      }
      else {
        if (node instanceof RootNode) {
          cell.setIconId(AbstractIcons.ComposerFieldRoot);
        }
        else if (node instanceof EntityNode) {
          cell.setIconId(AbstractIcons.ComposerFieldEntity);
        }
        else if (node instanceof AttributeNode) {
          if (((AttributeNode) node).getAggregationType() != null) {
            cell.setIconId(AbstractIcons.ComposerFieldAggregation);
          }
          else {
            cell.setIconId(AbstractIcons.ComposerFieldAttribute);
          }
        }
        else if (node instanceof EitherOrNode) {
          cell.setIconId(AbstractIcons.ComposerFieldEitherOrNode);
        }
      }
    }
  }
}
