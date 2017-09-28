/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.composer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerFieldChains.ComposerFieldCreateAdditionalOrNodeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerFieldChains.ComposerFieldCreateAttributeNodeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerFieldChains.ComposerFieldCreateDataModelChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerFieldChains.ComposerFieldCreateEitherNodeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerFieldChains.ComposerFieldCreateEntityNodeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerFieldChains.ComposerFieldCreateRootNodeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerFieldChains.ComposerFieldResolveAttributePathChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerFieldChains.ComposerFieldResolveEntityPathChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerFieldChains.ComposerFieldResolveRootPathForTopLevelAttributeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerFieldChains.ComposerFieldResolveRootPathForTopLevelEntityChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.IComposerFieldExtension;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeVisitor;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.AbstractComposerNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.AttributeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.EitherOrNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.EntityNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.RootNode;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.XmlUtility;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@ClassId("8e7f7eb8-be18-48e5-9efe-8a5b3459e247")
@FormData(value = AbstractComposerData.class, sdkCommand = SdkCommand.USE, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE)
public abstract class AbstractComposerField extends AbstractFormField implements IComposerField {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractComposerField.class);

  private IComposerFieldUIFacade m_uiFacade;
  private ITree m_tree;
  private Element m_initValue;
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
   * see {@link #interceptResolveEntityPath(EntityNode)}
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
   * see {@link #interceptResolveAttributePath(AttributeNode)}
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
   * and {@link #storeToXml(Element)} it is necessary to export {@link IDataModelEntity} and {@link IDataModelAttribute}
   * as external strings. see {@link EntityPath}
   * <p>
   * This callback completes an entity path to its root. The parameter path contains the entity path represented in the
   * composer tree of {@link EntityNode}s, the last element is the deepest tree node.
   * <p>
   * The default traverses the tree up to the root and collects all non-null {@link EntityNode#getEntity()}
   * <p>
   * This is prefixed with {@link #interceptResolveRootPathForTopLevelEntity(IDataModelEntity, List)}
   */
  @ConfigOperation
  @Order(99)
  protected EntityPath execResolveEntityPath(EntityNode node) {
    LinkedList<IDataModelEntity> list = new LinkedList<>();
    EntityNode tmp = node;
    while (tmp != null) {
      if (tmp.getEntity() != null) {
        list.add(0, tmp.getEntity());
      }
      //next
      tmp = tmp.getAncestorNode(EntityNode.class);
    }
    if (!list.isEmpty()) {
      interceptResolveRootPathForTopLevelEntity(list.get(0), list);
    }
    return new EntityPath(list);
  }

  /**
   * see {@link #execResolveEntityPathForEntityExport(EntityNode)}, {@link AttributePath} for more details
   * <p>
   * The path in the composer tree is prefixed with
   * {@link #interceptResolveRootPathForTopLevelAttribute(IDataModelAttribute, List)}
   */
  @ConfigOperation
  @Order(99)
  protected AttributePath execResolveAttributePath(AttributeNode node) {
    LinkedList<IDataModelEntity> list = new LinkedList<>();
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
    if (!list.isEmpty()) {
      interceptResolveRootPathForTopLevelEntity(list.get(0), list);
    }
    else {
      interceptResolveRootPathForTopLevelAttribute(node.getAttribute(), list);
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
   *         Normally overrides call super. {@link #interceptCreateRootNode()}
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
  protected EntityNode execCreateEntityNode(ITreeNode parentNode, IDataModelEntity e, boolean negated, List<?> values, List<String> texts) {
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
  protected AttributeNode execCreateAttributeNode(ITreeNode parentNode, IDataModelAttribute a, Integer aggregationType, IDataModelAttributeOp op, List<?> values, List<String> texts) {
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
   *         Normally overrides call super.{@link #interceptCreateEitherNode(ITreeNode, boolean)}
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
   *         Normally overrides call super.{@link #interceptCreateAdditionalOrNode(ITreeNode, boolean)}
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
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    super.initConfig();
    m_dataModel = interceptCreateDataModel();
    // tree
    List<ITree> contributedTrees = m_contributionHolder.getContributionsByClass(ITree.class);
    m_tree = CollectionUtility.firstElement(contributedTrees);

    if (m_tree == null) {
      Class<? extends ITree> configuredTree = getConfiguredTree();
      if (configuredTree != null) {
        m_tree = ConfigurationUtility.newInnerInstance(this, configuredTree);
      }
    }

    if (m_tree != null) {
      RootNode rootNode = interceptCreateRootNode();
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
                case TreeEvent.TYPE_NODES_UPDATED:
                case TreeEvent.TYPE_NODES_CHECKED: {
                  checkSaveNeeded();
                  checkEmpty();
                  break;
                }
              }
            }
          });

      // local enabled listener
      addPropertyChangeListener(PROP_ENABLED_COMPUTED, e -> {
        if (m_tree == null) {
          return;
        }
        boolean newEnabled = ((Boolean) e.getNewValue()).booleanValue();
        m_tree.setEnabled(newEnabled);
      });
    }
    else {
      LOG.warn("there is no inner class of type ITree in {}", getClass().getName());
    }
  }

  /*
   * Runtime
   */
  @Override
  protected void initFieldInternal() {
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
  public void exportFormFieldData(AbstractFormFieldData target) {
    if (m_tree != null) {
      AbstractTreeFieldData treeFieldData = (AbstractTreeFieldData) target;
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
  public List<IDataModelAttribute> getAttributes() {
    return m_dataModel.getAttributes();
  }

  @Override
  public List<IDataModelEntity> getEntities() {
    return m_dataModel.getEntities();
  }

  @Override
  public void loadFromXml(Element x) {
    super.loadFromXml(x);
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

  private void loadXMLRec(Element x, ITreeNode parent) {
    // build tree
    for (Element xmlElem : XmlUtility.getChildElements(x)) {
      if ("attribute".equals(xmlElem.getTagName())) {
        String id = xmlElem.getAttribute("id");
        IDataModelAttributeOp op;
        Integer aggregationType = 0;
        try {
          int operator = DataModelConstants.OPERATOR_EQ;
          String opAttribName = "op";
          if (xmlElem.hasAttribute(opAttribName)) {
            operator = Integer.parseInt(xmlElem.getAttribute(opAttribName));
          }
          op = DataModelAttributeOp.create(operator);

          String aggregTypeName = "aggregationType";
          if (xmlElem.hasAttribute(aggregTypeName)) {
            aggregationType = Integer.parseInt(xmlElem.getAttribute(aggregTypeName));
          }
          if (aggregationType == 0) {
            aggregationType = null;
          }
        }
        catch (Exception e) {
          LOG.warn("read op", e);
          continue;
        }
        List<Object> valueList = new ArrayList<>();
        try {
          for (int i = 1; i <= 5; i++) {
            String valueName = (i == 1 ? "value" : "value" + i);
            if (xmlElem.hasAttribute(valueName)) {
              valueList.add(XmlUtility.getObjectAttribute(xmlElem, valueName));
            }
          }
        }
        catch (Exception e) {
          LOG.warn("read value for attribute {}", id, e);
          continue;
        }
        List<String> displayValueList = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
          String displayValueName = (i == 1 ? "displayValue" : "displayValue" + i);
          if (xmlElem.hasAttribute(displayValueName)) {
            String val = null;
            if (xmlElem.hasAttribute(displayValueName)) {
              val = xmlElem.getAttribute(displayValueName);
            }
            displayValueList.add(val);
          }
        }
        // find definition
        AttributePath attPath = DataModelUtility.externalIdToAttributePath(getDataModel(), id);
        IDataModelAttribute foundAtt = (attPath != null ? attPath.getAttribute() : null);
        if (foundAtt == null) {
          LOG.warn("cannot find attribute with id={}", id);
          continue;
        }
        ITreeNode node = addAttributeNode(parent, foundAtt, aggregationType, op, valueList, displayValueList);
        if (node != null) {
          // add children recursive
          loadXMLRec(xmlElem, node);
        }
      }
      else if ("entity".equals(xmlElem.getTagName())) {
        String id = xmlElem.getAttribute("id");
        boolean negated = Boolean.parseBoolean(xmlElem.getAttribute("negated"));
        String text = null;
        if (xmlElem.hasAttribute("displayValues")) {
          text = xmlElem.getAttribute("displayValues");
        }
        // find definition
        EntityPath entityPath = DataModelUtility.externalIdToEntityPath(getDataModel(), id);
        IDataModelEntity foundEntity = (entityPath != null ? entityPath.lastElement() : null);
        if (foundEntity == null) {
          LOG.warn("cannot find entity with id={}", id);
          continue;
        }
        ITreeNode node = addEntityNode(parent, foundEntity, negated, null, text != null ? Collections.singletonList(text) : null);
        if (node != null) {
          // add children recursive
          loadXMLRec(xmlElem, node);
        }
      }
      else if ("or".equals(xmlElem.getTagName())) {
        boolean beginning = Boolean.parseBoolean(xmlElem.getAttribute("begin"));
        boolean negated = Boolean.parseBoolean(xmlElem.getAttribute("negated"));
        ITreeNode node = null;
        if (beginning) {
          node = addEitherNode(parent, negated);
        }
        else {
          // find last EitherOrNode
          EitherOrNode orNode = null;
          for (ITreeNode n : parent.getChildNodes()) {
            if (n instanceof EitherOrNode && ((EitherOrNode) n).isEndOfEitherOr()) {
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
  public void storeToXml(Element x) {
    super.storeToXml(x);
    storeXMLRec(x, getTree().getRootNode());
  }

  private void storeXMLRec(Element x, ITreeNode parent) {
    for (ITreeNode node : parent.getChildNodes()) {
      if (node instanceof EntityNode) {
        EntityNode entityNode = (EntityNode) node;
        Element xEntity = x.getOwnerDocument().createElement("entity");
        xEntity.setAttribute("id", DataModelUtility.entityPathToExternalId(getDataModel(), interceptResolveEntityPath(entityNode)));
        xEntity.setAttribute("negated", (entityNode.isNegative() ? "true" : "false"));
        List<String> texts = entityNode.getTexts();
        xEntity.setAttribute("displayValues", CollectionUtility.hasElements(texts) ? StringUtility.emptyIfNull(CollectionUtility.firstElement(texts)) : null);
        x.appendChild(xEntity);
        // recursion
        storeXMLRec(xEntity, node);
      }
      else if (node instanceof AttributeNode) {
        AttributeNode attNode = (AttributeNode) node;
        Element xAtt = x.getOwnerDocument().createElement("attribute");
        xAtt.setAttribute("id", DataModelUtility.attributePathToExternalId(getDataModel(), interceptResolveAttributePath(attNode)));
        IDataModelAttributeOp op = attNode.getOp();
        try {
          xAtt.setAttribute("op", op.getOperator() + "");
          if (attNode.getAggregationType() != null) {
            xAtt.setAttribute("aggregationType", attNode.getAggregationType() + "");
          }
        }
        catch (Exception e) {
          LOG.warn("write op {}", op, e);
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
              XmlUtility.setObjectAttribute(xAtt, valueName, value);
            }
            catch (Exception e) {
              LOG.warn("write value[{}] for attribute {}: {}", i, attNode.getAttribute(), value, e);
            }
            i++;
          }
        }
        x.appendChild(xAtt);
      }
      else if (node instanceof EitherOrNode) {
        EitherOrNode orNode = (EitherOrNode) node;
        Element xOr = x.getOwnerDocument().createElement("or");
        xOr.setAttribute("begin", "" + orNode.isBeginOfEitherOr());
        xOr.setAttribute("negated", (orNode.isNegative() ? "true" : "false"));
        x.appendChild(xOr);
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
        loadFromXml(m_initValue);
      }
      catch (RuntimeException e) {
        LOG.error("unexpected error occured while restoring initial value", e);
        getTree().removeAllChildNodes(getTree().getRootNode());
      }
    }

    checkSaveNeeded();
    checkEmpty();
  }

  @Override
  public EntityNode addEntityNode(ITreeNode parentNode, IDataModelEntity e, boolean negated, List<?> values, List<String> texts) {
    EntityNode node = interceptCreateEntityNode(parentNode, e, negated, values, texts);
    if (node != null) {
      getTree().addChildNode(parentNode, node);
      getTree().setNodeExpanded(node, true);
    }
    return node;
  }

  @Override
  public EitherOrNode addEitherNode(ITreeNode parentNode, boolean negated) {
    EitherOrNode node = interceptCreateEitherNode(parentNode, negated);
    if (node != null) {
      getTree().addChildNode(parentNode, node);
      getTree().setNodeExpanded(node, true);
    }
    return node;
  }

  @Override
  public EitherOrNode addAdditionalOrNode(ITreeNode eitherOrNode, boolean negated) {
    EitherOrNode node = interceptCreateAdditionalOrNode(eitherOrNode, negated);
    if (node != null) {
      getTree().addChildNode(eitherOrNode.getChildNodeIndex() + 1, eitherOrNode.getParentNode(), node);
      getTree().setNodeExpanded(node, true);
    }
    return node;
  }

  @Override
  public AttributeNode addAttributeNode(ITreeNode parentNode, IDataModelAttribute a, Integer aggregationType, IDataModelAttributeOp op, List<?> values, List<String> texts) {
    AttributeNode node = interceptCreateAttributeNode(parentNode, a, aggregationType, op, values, texts);
    if (node != null) {
      getTree().addChildNode(parentNode, node);
    }
    return node;
  }

  public void updateInitialValue() {
    try {
      // clone composer field by storing as XML
      Document x = XmlUtility.createNewXmlDocument("field");
      Element element = x.getDocumentElement();
      storeToXml(element);
      m_initValue = element;
    }
    catch (RuntimeException e) {
      LOG.warn("unexpected error occured while storing initial value", e);
    }
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
    try {
      m_tree.setTreeChanging(true);
      //
      ITreeVisitor v = node -> {
        if (!node.isStatusNonchanged()) {
          node.setStatusInternal(ITreeNode.STATUS_NON_CHANGED);
          m_tree.updateNode(node);
        }
        return true;
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
  protected boolean execIsEmpty() {
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
  protected class P_UIFacade implements IComposerFieldUIFacade {

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
    protected void execDisposeTree() {
      super.execDisposeTree();

      // dispose nodes (not necessary to remove them, dispose is sufficient)
      visitTree(node -> {
        if (node instanceof AbstractComposerNode) {
          ((AbstractComposerNode) node).dispose();
        }
        return true;
      });
    }

    @Override
    protected TreeNodeData exportTreeNodeData(ITreeNode node, AbstractTreeFieldData treeData) {
      if (node instanceof EntityNode) {
        EntityNode enode = (EntityNode) node;
        String externalId = DataModelUtility.entityPathToExternalId(getDataModel(), interceptResolveEntityPath(enode));
        if (externalId == null) {
          if (LOG.isInfoEnabled()) {
            LOG.info("could not find entity data for: {}", enode.getEntity());
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
        String externalId = DataModelUtility.attributePathToExternalId(getDataModel(), interceptResolveAttributePath(anode));
        if (externalId == null) {
          if (LOG.isInfoEnabled()) {
            LOG.info("could not find attribute data for: {}", anode.getAttribute());
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
    protected ITreeNode importTreeNodeData(ITreeNode parentNode, AbstractTreeFieldData treeData, TreeNodeData nodeData) {
      if (nodeData instanceof ComposerEntityNodeData) {
        ComposerEntityNodeData enodeData = (ComposerEntityNodeData) nodeData;
        String externalId = enodeData.getEntityExternalId();
        EntityPath entityPath = DataModelUtility.externalIdToEntityPath(getDataModel(), externalId);
        IDataModelEntity e = (entityPath != null ? entityPath.lastElement() : null);
        if (e == null) {
          LOG.warn("could not find entity for: {}", enodeData.getEntityExternalId());
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
          LOG.warn("could not find attribute for: {}", anodeData.getAttributeExternalId());
          return null;
        }
        IDataModelAttributeOp op;
        try {
          op = DataModelAttributeOp.create(anodeData.getOperator());
        }
        catch (Exception e) {
          LOG.warn("read op {}", anodeData.getOperator(), e);
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
    protected void execDecorateCell(ITreeNode node, Cell cell) {
      node.decorateCell();
    }
  }

  protected final EntityPath interceptResolveEntityPath(EntityNode node) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    ComposerFieldResolveEntityPathChain chain = new ComposerFieldResolveEntityPathChain(extensions);
    return chain.execResolveEntityPath(node);
  }

  protected final void interceptResolveRootPathForTopLevelEntity(IDataModelEntity e, List<IDataModelEntity> lifeList) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    ComposerFieldResolveRootPathForTopLevelEntityChain chain = new ComposerFieldResolveRootPathForTopLevelEntityChain(extensions);
    chain.execResolveRootPathForTopLevelEntity(e, lifeList);
  }

  protected final RootNode interceptCreateRootNode() {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    ComposerFieldCreateRootNodeChain chain = new ComposerFieldCreateRootNodeChain(extensions);
    return chain.execCreateRootNode();
  }

  protected final AttributePath interceptResolveAttributePath(AttributeNode node) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    ComposerFieldResolveAttributePathChain chain = new ComposerFieldResolveAttributePathChain(extensions);
    return chain.execResolveAttributePath(node);
  }

  protected final AttributeNode interceptCreateAttributeNode(ITreeNode parentNode, IDataModelAttribute a, Integer aggregationType, IDataModelAttributeOp op, List<?> values, List<String> texts) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    ComposerFieldCreateAttributeNodeChain chain = new ComposerFieldCreateAttributeNodeChain(extensions);
    return chain.execCreateAttributeNode(parentNode, a, aggregationType, op, values, texts);
  }

  protected final IDataModel interceptCreateDataModel() {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    ComposerFieldCreateDataModelChain chain = new ComposerFieldCreateDataModelChain(extensions);
    return chain.execCreateDataModel();
  }

  protected final EitherOrNode interceptCreateEitherNode(ITreeNode parentNode, boolean negated) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    ComposerFieldCreateEitherNodeChain chain = new ComposerFieldCreateEitherNodeChain(extensions);
    return chain.execCreateEitherNode(parentNode, negated);
  }

  protected final void interceptResolveRootPathForTopLevelAttribute(IDataModelAttribute a, List<IDataModelEntity> lifeList) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    ComposerFieldResolveRootPathForTopLevelAttributeChain chain = new ComposerFieldResolveRootPathForTopLevelAttributeChain(extensions);
    chain.execResolveRootPathForTopLevelAttribute(a, lifeList);
  }

  protected final EitherOrNode interceptCreateAdditionalOrNode(ITreeNode eitherOrNode, boolean negated) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    ComposerFieldCreateAdditionalOrNodeChain chain = new ComposerFieldCreateAdditionalOrNodeChain(extensions);
    return chain.execCreateAdditionalOrNode(eitherOrNode, negated);
  }

  protected final EntityNode interceptCreateEntityNode(ITreeNode parentNode, IDataModelEntity e, boolean negated, List<?> values, List<String> texts) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    ComposerFieldCreateEntityNodeChain chain = new ComposerFieldCreateEntityNodeChain(extensions);
    return chain.execCreateEntityNode(parentNode, e, negated, values, texts);
  }

  protected static class LocalComposerFieldExtension<OWNER extends AbstractComposerField> extends LocalFormFieldExtension<OWNER> implements IComposerFieldExtension<OWNER> {

    public LocalComposerFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public EntityPath execResolveEntityPath(ComposerFieldResolveEntityPathChain chain, EntityNode node) {
      return getOwner().execResolveEntityPath(node);
    }

    @Override
    public void execResolveRootPathForTopLevelEntity(ComposerFieldResolveRootPathForTopLevelEntityChain chain, IDataModelEntity e, List<IDataModelEntity> lifeList) {
      getOwner().execResolveRootPathForTopLevelEntity(e, lifeList);
    }

    @Override
    public RootNode execCreateRootNode(ComposerFieldCreateRootNodeChain chain) {
      return getOwner().execCreateRootNode();
    }

    @Override
    public AttributePath execResolveAttributePath(ComposerFieldResolveAttributePathChain chain, AttributeNode node) {
      return getOwner().execResolveAttributePath(node);
    }

    @Override
    public AttributeNode execCreateAttributeNode(ComposerFieldCreateAttributeNodeChain chain, ITreeNode parentNode, IDataModelAttribute a, Integer aggregationType, IDataModelAttributeOp op, List<?> values,
        List<String> texts) {
      return getOwner().execCreateAttributeNode(parentNode, a, aggregationType, op, values, texts);
    }

    @Override
    public IDataModel execCreateDataModel(ComposerFieldCreateDataModelChain chain) {
      return getOwner().execCreateDataModel();
    }

    @Override
    public EitherOrNode execCreateEitherNode(ComposerFieldCreateEitherNodeChain chain, ITreeNode parentNode, boolean negated) {
      return getOwner().execCreateEitherNode(parentNode, negated);
    }

    @Override
    public void execResolveRootPathForTopLevelAttribute(ComposerFieldResolveRootPathForTopLevelAttributeChain chain, IDataModelAttribute a, List<IDataModelEntity> lifeList) {
      getOwner().execResolveRootPathForTopLevelAttribute(a, lifeList);
    }

    @Override
    public EitherOrNode execCreateAdditionalOrNode(ComposerFieldCreateAdditionalOrNodeChain chain, ITreeNode eitherOrNode, boolean negated) {
      return getOwner().execCreateAdditionalOrNode(eitherOrNode, negated);
    }

    @Override
    public EntityNode execCreateEntityNode(ComposerFieldCreateEntityNodeChain chain, ITreeNode parentNode, IDataModelEntity e, boolean negated, List<?> values, List<String> texts) {
      return getOwner().execCreateEntityNode(parentNode, e, negated, values, texts);
    }
  }

  @Override
  protected IComposerFieldExtension<? extends AbstractComposerField> createLocalExtension() {
    return new LocalComposerFieldExtension<>(this);
  }
}
