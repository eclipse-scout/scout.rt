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
package org.eclipse.scout.rt.server.services.common.jdbc.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.ClassIdentifier;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.StringUtility.ITagProcessor;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.NVPair;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.services.common.jdbc.parsers.BindModel;
import org.eclipse.scout.rt.server.services.common.jdbc.parsers.BindParser;
import org.eclipse.scout.rt.server.services.common.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.services.common.jdbc.parsers.token.ValueInputToken;
import org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.AbstractComposerAttributeData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.AbstractComposerEntityData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerAttributeNodeData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerConstants;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerEitherOrNodeData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerEntityNodeData;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.AbstractTreeFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.TreeNodeData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;

/**
 * <pre>
 * Usage:
 * <ul>
 * <li>call {@link #setComposerEntityDefinition(Class, String, boolean)}, {@link #setComposerAttributeDefinition(Class, String, boolean)} and {@link #addStatementMapping(Class, String, int, int, boolean)}
 * for all member classes in the FormData</li>
 * <li>call {@link #build(AbstractFormData)}</li>
 * <li>add {@link #getWhereConstraints()} to the base sql statement (starts with an AND)</li>
 * <li>add {@link #getBindMap()} to the sql bind bases</li>
 * </pre>
 * <p>
 * The method {@link #buildComposerEntityNode(ComposerEntityNodeData)} corrects composer trees for correct handling of
 * zero-traversing aggregation attributes and normal attributes using
 * {@link #isZeroTraversingAttribute(ComposerAttributeNodeData)}.<br>
 * An attribute is zero-traversing when it contains 0 and therefore null/non-existence together with the operator &lt;,
 * &gt;, &lt;=, &gt;=, =, !=, &lt;&gt;, between. Only numeric attributes can be zero-traversing. Dates never are.
 * <p>
 * Examples of zero-traversing:
 * <ul>
 * <li>Count(Person) &lt; 3</li>
 * <li>priority between -10 and 10</li>
 * <li>Sum(payment) &lt;= 1'000'000</li>
 * <li></li>
 * </ul>
 * <p>
 * Examples of <b>not</b> zero-traversing:
 * <ul>
 * <li>Count(Person) between 2 and 4</li>
 * <li>priority between 1 and 5</li>
 * <li>Sum(payment) &gt;= 1'000'000</li>
 * <li></li>
 * </ul>
 * <p>
 * When an entity e contains zero-traversing <b>aggregation</b> attributes (such as Count(.), Sum(.)) z1..zn and
 * non-zero-traversing attributes a1..an it is splittet into 2 entities as follows:<br>
 * <code>
 * <pre>either (
 *   e
 *     a1..an
 *     z1..zn
 * )
 * or NOT (
 *   e
 *     a1..an
 * )
 * </pre>
 * </code>
 * <p>
 * In sql this would be something like<br>
 * <code>
 * <pre>exists (select 1 from Person ... where a1 and z1 groupy by ... having a2 and z2)
 * </pre>
 * </code> will be transformed to <code>
 * <pre>
 * (
 *   exists (select 1 from Person ... where a1 and z1 groupy by ... having a2 and z2)
 *   OR NOT
 *   exists (select 1 from Person ... where a1 groupy by ... having a2)
 * )
 * </pre>
 * </code>
 * <p>
 * Zero-traversing non aggregation attributes are simply wrapped using NLV(attribute).
 * <p>
 * That way non-existent matches are added to the result, which matches the expected behaviour.
 * 
 * @author imo
 */
public class FormDataStatementBuilder implements ComposerConstants {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(FormDataStatementBuilder.class);

  private static final Pattern PLAIN_ATTRIBUTE_PATTERN = Pattern.compile("(<attribute>)([a-zA-Z_][a-zA-Z0-9_]*)(</attribute>)");

  private ISqlStyle m_sqlStyle;
  private AliasMapper m_aliasMapper;
  private Map<Class, ComposerAttributePartDefinition> m_composerAttMap;
  private Map<Class, ComposerEntityPartDefinition> m_composerEntMap;
  private List<ValuePartDefinition> m_valueDefs;
  private Map<String, Object> m_bindMap;
  private AtomicInteger m_sequenceProvider;
  private StringBuffer m_where;

  /**
   * @param sqlStyle
   */
  public FormDataStatementBuilder(ISqlStyle sqlStyle) {
    m_sqlStyle = sqlStyle;
    m_aliasMapper = new AliasMapper();
    m_bindMap = new HashMap<String, Object>();
    m_composerAttMap = new HashMap<Class, ComposerAttributePartDefinition>();
    m_composerEntMap = new HashMap<Class, ComposerEntityPartDefinition>();
    m_valueDefs = new ArrayList<ValuePartDefinition>();
    setSequenceProvider(new AtomicInteger(0));
  }

  /**
   * @returns the reference to the sequence provider to be used outside for additional sequenced items or sub statemet
   *          builders
   */
  public AtomicInteger getSequenceProvider() {
    return m_sequenceProvider;
  }

  /**
   * use another sequence provider (counts 0,1,2... for aliases)
   */
  public void setSequenceProvider(AtomicInteger sequenceProvider) {
    m_sequenceProvider = sequenceProvider;
    m_aliasMapper.setSequenceProvider(m_sequenceProvider);
  }

  /**
   * Define the statement part for a sql part. For composer attributes and entites use
   * {@link #addAttributeMapping(Class, String)} and {@link #addEntityMapping(Class, String)}
   * <p>
   * <b>Number, Date, String, Boolean field</b>:<br>
   * The sqlAttribute is something like <code>@PERSON@.LAST_NAME</code><br>
   * When multiple occurrences are simultaneously used, the sqlAttribute may be written as
   * <code>(&lt;attribute&gt;@PERSON@.ORDER_STATUS&lt;/attribute&gt; OR &lt;attribute&gt;@PERSON@.DELIVERY_STATUS&lt;/attribute&gt;)</code>
   * <p>
   * The operator and aggregationType are required, unless a {@link IStatementPartBuilder} is used.
   */
  public void setValueDefinition(Class fieldType, String sqlAttribute, int operator) {
    setValueDefinition(new ValuePartDefinition(fieldType, sqlAttribute, operator));
  }

  /**
   * see {@link #setFieldDefinition(Class, String, int)}
   */
  public void setValueDefinition(ClassIdentifier fieldTypeIdentifier, String sqlAttribute, int operator) {
    setValueDefinition(new ValuePartDefinition(fieldTypeIdentifier, sqlAttribute, operator));
  }

  /**
   * see {@link #setFieldDefinition(Class, String, int)}
   */
  public void setValueDefinition(Class fieldType, String sqlAttribute, int operator, boolean plainBind) {
    setValueDefinition(new ValuePartDefinition(fieldType, sqlAttribute, operator, plainBind));
  }

  /**
   * see {@link #setFieldDefinition(Class, String, int)}
   */
  public void setValueDefinition(ClassIdentifier fieldTypeIdentifier, String sqlAttribute, int operator, boolean plainBind) {
    setValueDefinition(new ValuePartDefinition(fieldTypeIdentifier, sqlAttribute, operator, plainBind));
  }

  /**
   * see {@link #setFieldDefinition(Class, String, int)}
   */
  public void setValueDefinition(Class[] fieldTypes, String sqlAttribute, int operator) {
    setValueDefinition(new ValuePartDefinition(fieldTypes, sqlAttribute, operator, false));
  }

  /**
   * see {@link #setFieldDefinition(Class, String, int)}
   */
  public void setValueDefinition(ClassIdentifier[] fieldTypeIdentifiers, String sqlAttribute, int operator) {
    setValueDefinition(new ValuePartDefinition(fieldTypeIdentifiers, sqlAttribute, operator, false));
  }

  /**
   * see {@link #setFieldDefinition(Class, String, int)}
   */
  public void setValueDefinition(ValuePartDefinition def) {
    m_valueDefs.add(def);
  }

  /**
   * <b>Composer attribute</b>:<br>
   * The sqlAttribute is something like LAST_NAME, STATUS or @PERSON@.LAST_NAME, @PERSON@.STATUS.
   * 
   * @PERSON@ will be replaced by the parent entitie's generated alias.
   *          <p>
   *          The @PERSON@ prefix is added automatically if missing, but only if the entity where the attribute is
   *          contained has only <b>one</b> alias.<br>
   *          When multiple occurrences are simultaneously used, the sqlAttribute may be written as
   *          <code>(&lt;attribute&gt;ORDER_STATUS&lt;/attribute&gt; OR &lt;attribute&gt;DELIVERY_STATUS&lt;/attribute&gt;)</code>
   */
  public void setComposerAttributeDefinition(Class<? extends AbstractComposerAttributeData> attributeType, String sqlAttribute) {
    setComposerAttributeDefinition(attributeType, sqlAttribute, false);
  }

  /**
   * see {@link #setComposerAttributeDefinition(Class, String)}
   */
  public void setComposerAttributeDefinition(Class<? extends AbstractComposerAttributeData> attributeType, String sqlAttribute, boolean plainBind) {
    setComposerAttributeDefinition(new ComposerAttributePartDefinition(attributeType, sqlAttribute, plainBind));
  }

  /**
   * see {@link #setComposerAttributeDefinition(Class, String)}
   */
  public void setComposerAttributeDefinition(Class<? extends AbstractComposerAttributeData> attributeType, String whereClause, String selectClause, boolean plainBind) {
    setComposerAttributeDefinition(new ComposerAttributePartDefinition(attributeType, whereClause, selectClause, plainBind));
  }

  /**
   * see {@link #setComposerAttributeDefinition(Class, String)}
   */
  public void setComposerAttributeDefinition(ComposerAttributePartDefinition def) {
    m_composerAttMap.put(def.getAttributeType(), def);
  }

  /**
   * <b>Composer entity</b>:<br>
   * The sqlAttribute is something like <code><pre>
   * SELECT 1
   * FROM PERSON @PERSON@
   * WHERE @PERSON@.PERSON_ID=@parent.PERSON@.PERSON_ID
   * &lt;whereParts/&gt;
   * &lt;groupBy&gt;
   *  GROUP BY @PERSON@.PERSON_ID
   *  HAVING 1=1
   *  &lt;havingParts/&gt;
   * &lt;/groupBy&gt;
   * </pre></code> <br>
   * The <i>whereParts</i> tag is replaced with all attributes contained in the entity that have no aggregation type.
   * Every attribute contributes a "AND <i>attribute</i> <i>op</i> <i>value</i>" line.<br>
   * The <i>groupBy</i> tag is only used when there are attributes in the entity that have an aggregation type.<br>
   * The <i>havingParts</i> tag is replaced with all attributes contained in the entity that have an aggregation type.
   * Every aggregation attribute contributes a "AND <i>fun</i>(<i>attribute</i>) <i>op</i> <i>value</i>" line.<br>
   */
  public void setComposerEntityDefinition(Class<? extends AbstractComposerEntityData> entityType, String sqlAttribute) {
    setComposerEntityDefinition(new ComposerEntityPartDefinition(entityType, sqlAttribute));
  }

  /**
   * see {@link #setComposerEntityDefinition(Class, String)}
   */
  public void setComposerEntityDefinition(ComposerEntityPartDefinition def) {
    m_composerEntMap.put(def.getEntityType(), def);
  }

  /**
   * Convenience for {@link #getAliasMapper()} and {@link AliasMapper#setRootAlias(String, String)}
   */
  public void setRootAlias(String entityName, String alias) {
    getAliasMapper().setRootAlias(entityName, alias);
  }

  protected FormDataStatementBuilderCheck createCheckInstance() {
    return new FormDataStatementBuilderCheck(this);
  }

  public void check(Object o) {
    FormDataStatementBuilderCheck c = createCheckInstance();
    c.checkRec(o);
    System.out.println(c.toString());
  }

  public String build(AbstractFormData formData) throws ProcessingException {
    m_where = new StringBuffer();
    // get all formData fields and properties defined directly and indirectly by extending template fields, respectively
    Map<Integer, Map<String, AbstractFormFieldData>> fieldsBreathFirstMap = formData.getAllFieldsRec();
    Map<Integer, Map<String, AbstractPropertyData<?>>> propertiesBreathFirstMap = formData.getAllPropertiesRec();
    //build constraints for fields
    for (ValuePartDefinition def : m_valueDefs) {
      if (def.accept(formData, fieldsBreathFirstMap, propertiesBreathFirstMap)) {
        ClassIdentifier[] valueTypes = def.getValueTypeClassIdentifiers();
        List<Object> valueDatas = new ArrayList<Object>(valueTypes.length);
        List<String> bindNames = new ArrayList<String>(valueTypes.length);
        List<Object> bindValues = new ArrayList<Object>(valueTypes.length);
        for (int i = 0; i < valueTypes.length; i++) {
          if (AbstractFormFieldData.class.isAssignableFrom(valueTypes[i].getLastSegment())) {
            AbstractFormFieldData field = formData.findFieldByClass(fieldsBreathFirstMap, valueTypes[i]);
            valueDatas.add(field);
            bindNames.add("" + (char) (((int) 'a') + i));
            if (field instanceof AbstractValueFieldData) {
              bindValues.add(((AbstractValueFieldData) field).getValue());
            }
            else {
              bindValues.add(null);
            }
          }
          else if (AbstractPropertyData.class.isAssignableFrom(valueTypes[i].getLastSegment())) {
            AbstractPropertyData property = formData.findPropertyByClass(propertiesBreathFirstMap, valueTypes[i]);
            valueDatas.add(property);
            bindNames.add("" + (char) (((int) 'a') + i));
            bindValues.add(property.getValue());
          }
          else {
            valueDatas.add(null);
            bindNames.add("" + (char) (((int) 'a') + i));
            bindValues.add(null);
          }
        }
        Map<String, String> parentAliasMap = getAliasMapper().getRootAliases();
        String s = def.createNewInstance(this, valueDatas, bindNames, bindValues, parentAliasMap);
        if (s != null) {
          addWhere(" AND " + s);
        }
      }
    }
    //build constraints for composer trees
    for (Map<String, AbstractFormFieldData> map : fieldsBreathFirstMap.values()) {
      for (AbstractFormFieldData f : map.values()) {
        if (f.isValueSet()) {
          if (f instanceof AbstractTreeFieldData) {
            // composer tree with entity, attribute
            String s = buildTreeField((AbstractTreeFieldData) f);
            if (s != null) {
              addWhere(" AND " + s);
            }
          }
        }
      }
    }
    return getWhereConstraints();
  }

  protected boolean isZeroTraversingAttribute(int operation, Object[] values) {
    Number value1 = values != null && values.length > 0 && values[0] instanceof Number ? (Number) values[0] : null;
    Number value2 = values != null && values.length > 1 && values[1] instanceof Number ? (Number) values[1] : null;
    switch (operation) {
      case OPERATOR_EQ: {
        if (value1 != null) {
          return value1.longValue() == 0;
        }
        break;
      }
      case OPERATOR_GE: {
        if (value1 != null) {
          return value1.doubleValue() <= 0;
        }
        break;
      }
      case OPERATOR_GT: {
        if (value1 != null) {
          return value1.doubleValue() < 0;
        }
        break;
      }
      case OPERATOR_LE: {
        if (value1 != null) {
          return value1.doubleValue() >= 0;
        }
        break;
      }
      case OPERATOR_LT: {
        if (value1 != null) {
          return value1.doubleValue() > 0;
        }
        break;
      }
      case OPERATOR_NEQ: {
        if (value1 != null) {
          return value1.longValue() != 0;
        }
        break;
      }
      case OPERATOR_BETWEEN: {
        if (value1 != null && value2 != null) {
          return value1.doubleValue() <= 0 && value2.doubleValue() >= 0;
        }
        else if (value1 != null) {
          return value1.doubleValue() <= 0;
        }
        else if (value2 != null) {
          return value2.doubleValue() >= 0;
        }
        break;
      }
    }
    return false;
  }

  public AliasMapper getAliasMapper() {
    return m_aliasMapper;
  }

  /**
   * @return the life bind map
   */
  public Map<String, Object> getBindMap() {
    return m_bindMap;
  }

  public ISqlStyle getSqlStyle() {
    return m_sqlStyle;
  }

  /**
   * Convenience for {@link #getBindMap()}.put(name,value)
   */
  public void addBinds(String[] names, Object[] values) {
    if (names != null) {
      for (int i = 0; i < names.length; i++) {
        addBind(names[i], values[i]);
      }
    }
  }

  /**
   * Convenience for {@link #getBindMap()}.put(name,value)
   */
  public void addBind(String name, Object value) {
    if (name != null && !name.startsWith(ISqlStyle.PLAIN_BIND_MARKER_PREFIX)) {
      getBindMap().put(name, value);
    }
  }

  /**
   * add sql part with custom binds the ADD keyword is NOT added (pre-pended)
   * automatically
   */
  public void addWhere(String sql, NVPair... customBinds) {
    if (sql != null) {
      m_where.append(" ");
      m_where.append(sql);
      for (NVPair p : customBinds) {
        addBind(p.getName(), p.getValue());
      }
    }
  }

  public List<ValuePartDefinition> getValuePartDefinitions() {
    return Collections.unmodifiableList(m_valueDefs);
  }

  public Map<Class, ComposerAttributePartDefinition> getComposerAttributePartDefinitions() {
    return Collections.unmodifiableMap(m_composerAttMap);
  }

  public Map<Class, ComposerEntityPartDefinition> getComposerEntityPartDefinitions() {
    return Collections.unmodifiableMap(m_composerEntMap);
  }

  public String getWhereConstraints() {
    return m_where.toString();
  }

  protected long getNextBindSeqNo() {
    return m_sequenceProvider.incrementAndGet();
  }

  /**
   * Replace bind name by unique bind name so that it is not
   * conflicting with other parts that use the same statement
   * part and bind name. For example S is replaces by __S123.
   */
  public String localizeBindName(String bindName, String prefix) {
    if (bindName != null) {
      String locName = prefix + bindName + getNextBindSeqNo();
      return locName;
    }
    else {
      return null;
    }
  }

  /**
   * Replace bind name in statement
   */
  public String localizeStatement(String stm, String oldBindName, String newBindName) {
    stm = stm.replaceAll("#" + oldBindName + "#", "#" + newBindName + "#");
    stm = stm.replaceAll("\\&" + oldBindName + "\\&", "&" + newBindName + "&");
    stm = stm.replaceAll(":" + oldBindName + "([^A-Za-z0-9_])", ":" + newBindName + "$1");
    stm = stm.replaceAll(":" + oldBindName + "$", ":" + newBindName);
    return stm;
  }

  protected String buildTreeField(AbstractTreeFieldData field) throws ProcessingException {
    return buildTreeNodes(field.getRoots());
  }

  /**
   * @return the complete string of all attribute contributions
   * @throws ProcessingException
   */
  protected String buildTreeNodes(List<TreeNodeData> nodes) throws ProcessingException {
    StringBuilder buf = new StringBuilder();
    int count = 0;
    int i = 0;
    while (i < nodes.size()) {
      String s = null;
      if (nodes.get(i) instanceof ComposerEntityNodeData) {
        s = buildComposerEntityNode((ComposerEntityNodeData) nodes.get(i));
        i++;
      }
      else if (nodes.get(i) instanceof ComposerAttributeNodeData) {
        s = buildComposerAttributeNode((ComposerAttributeNodeData) nodes.get(i));
        i++;
      }
      else if (nodes.get(i) instanceof ComposerEitherOrNodeData) {
        ArrayList<ComposerEitherOrNodeData> orNodes = new ArrayList<ComposerEitherOrNodeData>();
        orNodes.add((ComposerEitherOrNodeData) nodes.get(i));
        int k = i;
        while (k + 1 < nodes.size() && (nodes.get(k + 1) instanceof ComposerEitherOrNodeData) && !((ComposerEitherOrNodeData) nodes.get(k + 1)).isBeginOfEitherOr()) {
          orNodes.add((ComposerEitherOrNodeData) nodes.get(k + 1));
          k++;
        }
        s = buildComposerOrNodes(orNodes);
        i = k + 1;
      }
      else {
        s = buildTreeNodes(nodes.get(i).getChildNodes());
      }
      //
      if (s != null) {
        if (count > 0) {
          buf.append(" AND ");
        }
        buf.append(s);
        count++;
      }
    }
    if (count > 0) {
      return buf.toString();
    }
    else {
      return null;
    }
  }

  protected String buildComposerOrNodes(List<ComposerEitherOrNodeData> nodes) throws ProcessingException {
    // check if only one condition
    StringBuilder buf = new StringBuilder();
    int count = 0;
    for (ComposerEitherOrNodeData node : nodes) {
      String s = buildTreeNodes(node.getChildNodes());
      if (s != null) {
        if (count > 0) {
          buf.append(" OR ");
          if (node.isNegative()) {
            buf.append(" NOT ");
          }
        }
        buf.append("(");
        buf.append(s);
        buf.append(")");
        count++;
      }
    }
    if (count > 0) {
      if (count > 1) {
        buf.insert(0, "(");
        buf.append(")");
        return buf.toString();
      }
      else {
        String s = buf.toString();
        if (s.matches("\\(.*\\)")) {
          return s.substring(1, s.length() - 1).trim();
        }
        else {
          return s;
        }
      }
    }
    else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends TreeNodeData> T getParentNodeOfType(TreeNodeData node, Class<T> type) {
    if (node == null) {
      return null;
    }
    while (node != null) {
      node = node.getParentNode();
      if (node != null && type.isAssignableFrom(node.getClass())) {
        return (T) node;
      }
    }
    return null;
  }

  protected String buildComposerEntityNode(ComposerEntityNodeData node) throws ProcessingException {
    if (node.getEntity() == null) {
      LOG.warn("entity does not reference an entity: " + node.getClass());
    }
    ComposerEntityPartDefinition def = m_composerEntMap.get(node.getEntity().getClass());
    if (def == null) {
      LOG.warn("entity does not map to a PartDefinition: " + node.getEntity());
      return null;
    }
    ComposerEntityNodeData parentEntityNode = getParentNodeOfType(node, ComposerEntityNodeData.class);
    Map<String, String> parentAliasMap = (parentEntityNode != null ? m_aliasMapper.getNodeAliases(parentEntityNode) : m_aliasMapper.getRootAliases());
    String stm = def.createNewInstance(this, node, parentAliasMap);
    if (stm == null) {
      return null;
    }
    m_aliasMapper.addAllNodeEntitiesFrom(node, stm);
    stm = m_aliasMapper.replaceMarkersByAliases(stm, m_aliasMapper.getNodeAliases(node), parentAliasMap);
    return buildEntityEitherOrSplit(stm, node.isNegative(), node.getChildNodes());
  }

  protected String buildEntityEitherOrSplit(String baseStm, boolean negative, List<TreeNodeData> childParts) throws ProcessingException {
    List<List<ComposerEitherOrNodeData>> orBlocks = new ArrayList<List<ComposerEitherOrNodeData>>();
    List<TreeNodeData> otherParts = new ArrayList<TreeNodeData>();
    List<ComposerEitherOrNodeData> currentOrBlock = new ArrayList<ComposerEitherOrNodeData>();
    for (TreeNodeData ch : childParts) {
      if (ch instanceof ComposerEitherOrNodeData) {
        ComposerEitherOrNodeData orData = (ComposerEitherOrNodeData) ch;
        if (orData.isBeginOfEitherOr()) {
          if (currentOrBlock.size() > 0) {
            orBlocks.add(new ArrayList<ComposerEitherOrNodeData>(currentOrBlock));
          }
          currentOrBlock.clear();
        }
        currentOrBlock.add(orData);
      }
      else {
        otherParts.add(ch);
      }
    }
    if (currentOrBlock.size() > 0) {
      orBlocks.add(new ArrayList<ComposerEitherOrNodeData>(currentOrBlock));
      currentOrBlock.clear();
    }
    //
    if (orBlocks.size() > 0) {
      StringBuilder blockBuf = new StringBuilder();
      int blockCount = 0;
      for (List<ComposerEitherOrNodeData> list : orBlocks) {
        int elemCount = 0;
        StringBuilder elemBuf = new StringBuilder();
        for (ComposerEitherOrNodeData orData : list) {
          ArrayList<TreeNodeData> subList = new ArrayList<TreeNodeData>();
          subList.addAll(otherParts);
          subList.addAll(orData.getChildNodes());
          String s = buildEntityEitherOrSplit(baseStm, negative ^ orData.isNegative(), subList);
          if (s != null) {
            if (elemCount > 0) {
              elemBuf.append(" OR ");
            }
            elemBuf.append(" ( ");
            elemBuf.append(s);
            elemBuf.append(" ) ");
            elemCount++;
          }
        }
        if (elemCount > 0) {
          if (blockCount > 0) {
            blockBuf.append(" AND ");
          }
          blockBuf.append(" ( ");
          blockBuf.append(elemBuf.toString());
          blockBuf.append(" ) ");
          blockCount++;
        }
      }
      if (blockCount > 0) {
        return blockBuf.toString();
      }
      else {
        return null;
      }
    }
    else {
      return buildEntityZeroTraversingSplit(baseStm, negative, childParts);
    }
  }

  protected String buildEntityZeroTraversingSplit(String baseStm, boolean negative, List<TreeNodeData> childParts) throws ProcessingException {
    // get children that have no aggregation type
    ArrayList<TreeNodeData> wherePartChildren = new ArrayList<TreeNodeData>();
    ArrayList<TreeNodeData> wherePartChildrenNonZeroTraversing = new ArrayList<TreeNodeData>();
    for (TreeNodeData ch : childParts) {
      if (ch instanceof ComposerAttributeNodeData) {
        Integer agg = ((ComposerAttributeNodeData) ch).getAggregationType();
        if (agg == null || agg == ComposerConstants.AGGREGATION_NONE) {
          ComposerAttributeNodeData attributeData = (ComposerAttributeNodeData) ch;
          wherePartChildren.add(attributeData);
          if (!isZeroTraversingAttribute(attributeData.getOperator(), attributeData.getValues())) {
            wherePartChildrenNonZeroTraversing.add(attributeData);
          }
        }
      }
      else {
        wherePartChildren.add(ch);
        wherePartChildrenNonZeroTraversing.add(ch);
      }
    }
    // get children that have an aggregation type
    ArrayList<TreeNodeData> havingPartChildren = new ArrayList<TreeNodeData>();
    ArrayList<TreeNodeData> havingPartChildrenNonZeroTraversing = new ArrayList<TreeNodeData>();
    for (TreeNodeData ch : childParts) {
      if (ch instanceof ComposerAttributeNodeData) {
        Integer agg = ((ComposerAttributeNodeData) ch).getAggregationType();
        if (agg != null && agg != ComposerConstants.AGGREGATION_NONE) {
          ComposerAttributeNodeData attributeData = (ComposerAttributeNodeData) ch;
          havingPartChildren.add(attributeData);
          if (!isZeroTraversingAttribute(attributeData.getOperator(), attributeData.getValues())) {
            havingPartChildrenNonZeroTraversing.add(attributeData);
          }
        }
      }
    }
    //
    //create entity part 1
    String entityPart1 = buildEntityPart(baseStm, negative, wherePartChildren, havingPartChildren);
    //create negated entity part 2
    String entityPart2 = null;
    if (wherePartChildren.size() + havingPartChildren.size() > wherePartChildrenNonZeroTraversing.size() + havingPartChildrenNonZeroTraversing.size()) {
      // negated negation
      entityPart2 = buildEntityPart(baseStm, !negative, wherePartChildrenNonZeroTraversing, havingPartChildrenNonZeroTraversing);
    }
    //combine parts
    if (entityPart2 != null) {
      return " ( " + entityPart1 + " OR " + entityPart2 + " ) ";
    }
    else {
      return entityPart1;
    }
  }

  protected String buildEntityPart(String baseStm, boolean negative, List<TreeNodeData> whereParts, List<TreeNodeData> havingParts) throws ProcessingException {
    String wherePartsText = buildTreeNodes(whereParts);
    String havingPartsText = buildTreeNodes(havingParts);
    String entityPart = baseStm;
    // negation
    if (negative) {
      entityPart = "NOT " + entityPart;
    }
    // add children that have no aggregation type
    if (wherePartsText != null) {
      String s = " AND " + wherePartsText;
      if (entityPart.indexOf("<whereParts/>") >= 0) {
        entityPart = StringUtility.replace(entityPart, "<whereParts/>", s);
      }
      else {
        entityPart = entityPart + s;
      }
    }
    else {
      entityPart = StringUtility.replace(entityPart, "<whereParts/>", "");
    }
    // add children that have an aggregation type
    if (havingPartsText != null) {
      String s = " AND " + havingPartsText;
      entityPart = StringUtility.removeTagBounds(entityPart, "groupBy");
      entityPart = StringUtility.replace(entityPart, "<havingParts/>", s);
    }
    else {
      entityPart = StringUtility.removeTag(entityPart, "groupBy");
    }
    return entityPart;
  }

  protected String buildComposerAttributeNode(final ComposerAttributeNodeData node) throws ProcessingException {
    if (node.getAttribute() == null) {
      LOG.warn("attribute does not reference an attribute: " + node.getClass());
    }
    ComposerAttributePartDefinition def = m_composerAttMap.get(node.getAttribute().getClass());
    if (def == null) {
      Integer agg = node.getAggregationType();
      if (agg != null && agg == ComposerConstants.AGGREGATION_COUNT) {
        def = new ComposerAttributePartDefinition(null, "1", false);
      }
    }
    if (def == null) {
      LOG.warn("composer attribute does not map to a PartDefinition: " + node.getAttribute());
      return null;
    }
    List<Object> bindValues = new ArrayList<Object>();
    if (node.getValues() != null) {
      bindValues.addAll(Arrays.asList(node.getValues()));
    }
    List<String> bindNames = new ArrayList<String>(bindValues.size());
    for (int i = 0; i < bindValues.size(); i++) {
      bindNames.add("" + (char) (((int) 'a') + i));
    }
    AliasMapper aliasMap = getAliasMapper();
    ComposerEntityNodeData parentEntityNode = FormDataStatementBuilder.getParentNodeOfType(node, ComposerEntityNodeData.class);
    Map<String, String> parentAliasMap = parentEntityNode != null ? aliasMap.getNodeAliases(parentEntityNode) : aliasMap.getRootAliases();
    return def.createNewInstance(this, node, bindNames, bindValues, parentAliasMap);
  }

  public String createComposerAttributeStatementPart(final Integer aggregationType, String stm, final int operation, List<String> bindNames, List<Object> bindValues, final boolean plainBind, Map<String, String> parentAliasMap) throws ProcessingException {
    if (stm.indexOf("<attribute>") < 0) {
      stm = "<attribute>" + stm + "</attribute>";
    }
    //convenience: automatically add missing alias on plain attributes, but only if the parent entity has at most 1 alias mapping
    Matcher m = PLAIN_ATTRIBUTE_PATTERN.matcher(stm);
    if (m.find()) {
      if (parentAliasMap.size() == 0) {
        //nop
      }
      else if (parentAliasMap.size() == 1) {
        stm = m.replaceAll("$1@parent." + parentAliasMap.keySet().iterator().next() + "@.$2$3");
      }
      else {
        throw new ProcessingException("composer attribute " + stm + " uses no @...@ alias prefix, but parent has more than 1 alias: " + parentAliasMap);
      }
    }
    //resolve aliases
    stm = m_aliasMapper.replaceMarkersByAliases(stm, parentAliasMap, parentAliasMap);
    return createStatementPart(aggregationType, stm, operation, bindNames, bindValues, plainBind, parentAliasMap);
  }

  /**
   * Create sql text, makes bind names unique, and adds all binds to the bind map
   * <p>
   * To use no operator use {@link ComposerConstants#OPERATOR_NONE} and null for binds and values, stm will be decorated
   * and is the result itself
   * <p>
   * To use no aggregation use {@link ComposerConstants#AGGREGATION_NONE}
   */
  public String createStatementPart(final Integer aggregationType, String stm, final int operation, List<String> bindNames, List<Object> bindValues, final boolean plainBind, Map<String, String> parentAliasMap) throws ProcessingException {
    if (stm == null) stm = "";
    if (bindNames == null) bindNames = new ArrayList<String>(0);
    if (bindValues == null) bindValues = new ArrayList<Object>(0);
    // the attribute was of the form: NAME or
    // <attribute>NAME</attribute>
    // make sure there is an attribute tag in the string, if none enclose all
    // by default
    if (stm.indexOf("<attribute>") < 0) {
      stm = "<attribute>" + stm + "</attribute>";
    }
    //convenience: automatically add missing alias on plain attributes, but only if the parent entity has at most 1 alias mapping
    Matcher m = PLAIN_ATTRIBUTE_PATTERN.matcher(stm);
    if (m.find()) {
      if (parentAliasMap.size() == 0) {
        //nop
      }
      else if (parentAliasMap.size() == 1) {
        stm = m.replaceAll("$1@parent." + parentAliasMap.keySet().iterator().next() + "@.$2$3");
      }
      else {
        throw new ProcessingException("root attribute with " + stm + " uses no @...@ alias prefix, but parent has more than 1 alias: " + parentAliasMap);
      }
    }
    //resolve aliases
    stm = m_aliasMapper.replaceMarkersByAliases(stm, parentAliasMap, parentAliasMap);
    // generate unique bind names
    final ArrayList<String> newBindNames = new ArrayList<String>(2);
    for (int i = 0; i < bindNames.size(); i++) {
      String o = bindNames.get(i);
      String n = localizeBindName(o, "__");
      newBindNames.add(n);
      stm = localizeStatement(stm, o, n);
    }
    // part decoration
    final List<Object> valuesFinal = bindValues;
    ITagProcessor processor = new ITagProcessor() {
      public String processTag(String tagName, String attribute) {
        return createAttributeOpValuePart(attribute, operation, aggregationType, newBindNames, valuesFinal, plainBind);
      }
    };
    return StringUtility.replaceTags(stm, "attribute", processor);
  }

  protected String createAttributeOpValuePart(String attribute, int operation, Integer aggregationType, List<String> bindNames, List<Object> bindValues, boolean plainBind) {
    String[] names = (bindNames != null ? bindNames.toArray(new String[bindNames.size()]) : new String[0]);
    Object[] values = (bindValues != null ? bindValues.toArray(new Object[bindValues.size()]) : new Object[0]);
    if (plainBind && operation != OPERATOR_NONE) {
      //rewrite bindNames by plain values
      for (int i = 0; i < names.length; i++) {
        names[i] = ISqlStyle.PLAIN_BIND_MARKER_PREFIX + m_sqlStyle.toPlainText(values[i]);
      }
    }
    //
    if (aggregationType != null && aggregationType != AGGREGATION_NONE) {
      switch (aggregationType) {
        case AGGREGATION_COUNT: {
          attribute = m_sqlStyle.toAggregationCount(attribute);
          break;
        }
        case AGGREGATION_MIN: {
          attribute = m_sqlStyle.toAggregationMin(attribute);
          break;
        }
        case AGGREGATION_MAX: {
          attribute = m_sqlStyle.toAggregationMax(attribute);
          break;
        }
        case AGGREGATION_SUM: {
          attribute = m_sqlStyle.toAggregationSum(attribute);
          break;
        }
        case AGGREGATION_AVG: {
          attribute = m_sqlStyle.toAggregationAvg(attribute);
          break;
        }
        case AGGREGATION_MEDIAN: {
          attribute = m_sqlStyle.toAggregationMedian(attribute);
          break;
        }
      }
    }
    else if (isZeroTraversingAttribute(operation, values)) {
      attribute = m_sqlStyle.getNvlToken() + "(" + attribute + ",0)";
    }
    //
    switch (operation) {
      case OPERATOR_NONE: {
        if (plainBind) {
          if (names != null) {
            HashMap<String, String> tokenValue = new HashMap<String, String>();
            for (int i = 0; i < names.length; i++) {
              tokenValue.put(names[i], m_sqlStyle.toPlainText(values[i]));
            }
            BindModel m = new BindParser(attribute).parse();
            IToken[] tokens = m.getIOTokens();
            if (tokens != null) {
              for (IToken iToken : tokens) {
                if (iToken instanceof ValueInputToken) {
                  ValueInputToken t = (ValueInputToken) iToken;
                  t.setPlainValue(true);
                  t.setReplaceToken(tokenValue.get(t.getName()));
                }
              }
            }
            attribute = m.getFilteredStatement();
          }
        }
        else {
          addBinds(names, values);
        }
        return attribute;
      }
      case OPERATOR_BETWEEN: {
        if (!plainBind) addBinds(names, values);
        if (values[0] == null) {
          return m_sqlStyle.createLE(attribute, names[1]);
        }
        else if (values[1] == null) {
          return m_sqlStyle.createGE(attribute, names[0]);
        }
        else {
          return m_sqlStyle.createBetween(attribute, names[0], names[1]);
        }
      }
      case OPERATOR_DATE_BETWEEN: {
        if (!plainBind) addBinds(names, values);
        if (values[0] == null) {
          return m_sqlStyle.createDateLE(attribute, names[1]);
        }
        else if (values[1] == null) {
          return m_sqlStyle.createDateGE(attribute, names[0]);
        }
        else {
          return m_sqlStyle.createDateBetween(attribute, names[0], names[1]);
        }
      }
      case OPERATOR_DATE_TIME_BETWEEN: {
        if (!plainBind) addBinds(names, values);
        if (values[0] == null) {
          return m_sqlStyle.createDateTimeLE(attribute, names[1]);
        }
        else if (values[1] == null) {
          return m_sqlStyle.createDateTimeGE(attribute, names[0]);
        }
        else {
          return m_sqlStyle.createDateTimeBetween(attribute, names[0], names[1]);
        }
      }
      case OPERATOR_EQ: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createEQ(attribute, names[0]);
      }
      case OPERATOR_DATE_EQ: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateEQ(attribute, names[0]);
      }
      case OPERATOR_DATE_TIME_EQ: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateTimeEQ(attribute, names[0]);
      }
      case OPERATOR_GE: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createGE(attribute, names[0]);
      }
      case OPERATOR_DATE_GE: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateGE(attribute, names[0]);
      }
      case OPERATOR_DATE_TIME_GE: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateTimeGE(attribute, names[0]);
      }
      case OPERATOR_GT: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createGT(attribute, names[0]);
      }
      case OPERATOR_DATE_GT: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateGT(attribute, names[0]);
      }
      case OPERATOR_DATE_TIME_GT: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateTimeGT(attribute, names[0]);
      }
      case OPERATOR_LE: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createLE(attribute, names[0]);
      }
      case OPERATOR_DATE_LE: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateLE(attribute, names[0]);
      }
      case OPERATOR_DATE_TIME_LE: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateTimeLE(attribute, names[0]);
      }
      case OPERATOR_LT: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createLT(attribute, names[0]);
      }
      case OPERATOR_DATE_LT: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateLT(attribute, names[0]);
      }
      case OPERATOR_DATE_TIME_LT: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateTimeLT(attribute, names[0]);
      }
      case OPERATOR_NEQ: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createNEQ(attribute, names[0]);
      }
      case OPERATOR_DATE_NEQ: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateNEQ(attribute, names[0]);
      }
      case OPERATOR_DATE_TIME_NEQ: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateTimeNEQ(attribute, names[0]);
      }
      case OPERATOR_DATE_IS_IN_DAYS: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateIsInDays(attribute, names[0]);
      }
      case OPERATOR_DATE_IS_IN_GE_DAYS: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateIsInGEDays(attribute, names[0]);
      }
      case OPERATOR_DATE_IS_IN_GE_MONTHS: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateIsInGEMonths(attribute, names[0]);
      }
      case OPERATOR_DATE_IS_IN_LE_DAYS: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateIsInLEDays(attribute, names[0]);
      }
      case OPERATOR_DATE_IS_IN_LE_MONTHS: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateIsInLEMonths(attribute, names[0]);
      }
      case OPERATOR_DATE_IS_IN_LAST_DAYS: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateIsInLastDays(attribute, names[0]);
      }
      case OPERATOR_DATE_IS_IN_LAST_MONTHS: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateIsInLastMonths(attribute, names[0]);
      }
      case OPERATOR_DATE_IS_IN_MONTHS: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateIsInMonths(attribute, names[0]);
      }
      case OPERATOR_DATE_IS_IN_NEXT_DAYS: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateIsInNextDays(attribute, names[0]);
      }
      case OPERATOR_DATE_IS_IN_NEXT_MONTHS: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateIsInNextMonths(attribute, names[0]);
      }
      case OPERATOR_DATE_IS_NOT_TODAY: {
        return m_sqlStyle.createDateIsNotToday(attribute);
      }
      case OPERATOR_DATE_IS_TODAY: {
        return m_sqlStyle.createDateIsToday(attribute);
      }
      case OPERATOR_DATE_TIME_IS_IN_GE_HOURS: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateTimeIsInGEHours(attribute, names[0]);
      }
      case OPERATOR_DATE_TIME_IS_IN_GE_MINUTES: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateTimeIsInGEMinutes(attribute, names[0]);
      }
      case OPERATOR_DATE_TIME_IS_IN_LE_HOURS: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateTimeIsInLEHours(attribute, names[0]);
      }
      case OPERATOR_DATE_TIME_IS_IN_LE_MINUTES: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createDateTimeIsInLEMinutes(attribute, names[0]);
      }
      case OPERATOR_DATE_TIME_IS_NOT_NOW: {
        return m_sqlStyle.createDateTimeIsNotNow(attribute);
      }
      case OPERATOR_DATE_TIME_IS_NOW: {
        return m_sqlStyle.createDateTimeIsNow(attribute);
      }
      case OPERATOR_ENDS_WITH: {
        if (!plainBind) addBind(names[0], m_sqlStyle.toLikePattern(values[0]));
        return m_sqlStyle.createEndsWith(attribute, names[0]);
      }
      case OPERATOR_NOT_ENDS_WITH: {
        if (!plainBind) addBind(names[0], m_sqlStyle.toLikePattern(values[0]));
        return m_sqlStyle.createNotEndsWith(attribute, names[0]);
      }
      case OPERATOR_IN: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createIn(attribute, names[0]);
      }
      case OPERATOR_CONTAINS: {
        if (!plainBind) addBind(names[0], m_sqlStyle.toLikePattern(values[0]));
        return m_sqlStyle.createContains(attribute, names[0]);
      }
      case OPERATOR_LIKE: {
        if (!plainBind) addBind(names[0], m_sqlStyle.toLikePattern(values[0]));
        return m_sqlStyle.createLike(attribute, names[0]);
      }
      case OPERATOR_NOT_IN: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createNotIn(attribute, names[0]);
      }
      case OPERATOR_NOT_CONTAINS: {
        if (!plainBind) addBind(names[0], m_sqlStyle.toLikePattern(values[0]));
        return m_sqlStyle.createNotContains(attribute, names[0]);
      }
      case OPERATOR_NOT_NULL: {
        return m_sqlStyle.createNotNull(attribute);
      }
      case OPERATOR_NUMBER_NOT_NULL: {
        return m_sqlStyle.createNumberNotNull(attribute);
      }
      case OPERATOR_NULL: {
        return m_sqlStyle.createNull(attribute);
      }
      case OPERATOR_NUMBER_NULL: {
        return m_sqlStyle.createNumberNull(attribute);
      }
      case OPERATOR_STARTS_WITH: {
        if (!plainBind) addBind(names[0], m_sqlStyle.toLikePattern(values[0]));
        return m_sqlStyle.createStartsWith(attribute, names[0]);
      }
      case OPERATOR_NOT_STARTS_WITH: {
        if (!plainBind) addBind(names[0], m_sqlStyle.toLikePattern(values[0]));
        return m_sqlStyle.createNotStartsWith(attribute, names[0]);
      }
      case OPERATOR_TIME_IS_IN_GE_HOURS: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createTimeIsInGEHours(attribute, names[0]);
      }
      case OPERATOR_TIME_IS_IN_GE_MINUTES: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createTimeIsInGEMinutes(attribute, names[0]);
      }
      case OPERATOR_TIME_IS_IN_HOURS: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createTimeIsInHours(attribute, names[0]);
      }
      case OPERATOR_TIME_IS_IN_LE_HOURS: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createTimeIsInLEHours(attribute, names[0]);
      }
      case OPERATOR_TIME_IS_IN_LE_MINUTES: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createTimeIsInLEMinutes(attribute, names[0]);
      }
      case OPERATOR_TIME_IS_IN_MINUTES: {
        if (!plainBind) addBinds(names, values);
        return m_sqlStyle.createTimeIsInMinutes(attribute, names[0]);
      }
      case OPERATOR_TIME_IS_NOW: {
        return m_sqlStyle.createTimeIsNow(attribute);
      }
      case OPERATOR_TIME_IS_NOT_NOW: {
        return m_sqlStyle.createTimeIsNotNow(attribute);
      }
      default: {
        throw new IllegalArgumentException("invalid operator: " + operation);
      }
    }
  }

}
