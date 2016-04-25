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
package org.eclipse.scout.rt.server.jdbc.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.classid.ClassIdentifier;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.StringUtility.ITagProcessor;
import org.eclipse.scout.rt.server.jdbc.parsers.BindModel;
import org.eclipse.scout.rt.server.jdbc.parsers.BindParser;
import org.eclipse.scout.rt.server.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueInputToken;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerAttributeNodeData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerEitherOrNodeData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerEntityNodeData;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.AbstractTreeFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.TreeNodeData;
import org.eclipse.scout.rt.shared.data.model.AttributePath;
import org.eclipse.scout.rt.shared.data.model.DataModelConstants;
import org.eclipse.scout.rt.shared.data.model.DataModelUtility;
import org.eclipse.scout.rt.shared.data.model.EntityPath;
import org.eclipse.scout.rt.shared.data.model.IDataModel;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 * Usage:
 * <ul>
 * <li>call {@link #setDataModelEntityDefinition(Class, String)}, {@link #setDataModelAttributeDefinition(Class, String, boolean)} for all member classes in the FormData</li>
 * <li>call {@link #build(AbstractFormData)}</li>
 * <li>add {@link #getWhereConstraints()} to the base sql statement (starts with an AND)</li>
 * <li>add {@link #getBindMap()} to the sql bind bases</li>
 * </pre>
 * <p>
 * The method {@link #buildComposerEntityNodeContribution(ComposerEntityNodeData, EntityStrategy)} corrects composer
 * trees for correct handling of zero-traversing aggregation attributes and normal attributes using
 * {@link #isZeroTraversingAttribute(int, Object[])}.<br>
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
 */
public class FormDataStatementBuilder implements DataModelConstants {
  private static final Logger LOG = LoggerFactory.getLogger(FormDataStatementBuilder.class);
  private static final Pattern PLAIN_ATTRIBUTE_PATTERN = Pattern.compile("(<attribute>)([a-zA-Z_][a-zA-Z0-9_]*)(</attribute>)");

  /**
   * Strategy used in
   * {@link DataModelAttributePartDefinition#createInstance(FormDataStatementBuilder, ComposerAttributeNodeData, AttributeStrategy, String, List, List, Map)}
   */
  public static enum AttributeStrategy {
    /**
     * Assuming the constraint "SALARY &gt;= 1000" and the attribute statement
     *
     * <pre>
     * &lt;attribute&gt;@Person@.SALARY&lt;/attribute&gt;
     * AND ACTIVE=1
     * </pre>
     *
     * this strategy only creates the contraint of the attribute part
     *
     * <pre>
     * {@link EntityContribution#getWhereParts()} = SALARY&gt;=1000
     * </pre>
     */
    BuildConstraintOfAttribute,
    /**
     * Assuming the constraint "SALARY &gt;= 1000" and the attribute statement
     *
     * <pre>
     * &lt;attribute&gt;@Person@.SALARY&lt;/attribute&gt;
     * AND ACTIVE=1
     * </pre>
     *
     * this strategy only creates the contraint of the context (excluding the attribute)
     *
     * <pre>
     * {@link EntityContribution#getWhereParts()} = ACTIVE=1
     * </pre>
     */
    BuildConstraintOfContext,
    /**
     * Assuming the constraint "SALARY &gt;= 1000" and the attribute statement
     *
     * <pre>
     * &lt;attribute&gt;@Person@.SALARY&lt;/attribute&gt;
     * AND ACTIVE=1
     * </pre>
     *
     * this strategy creates the contraint of the context and the attribute
     *
     * <pre>
     * {@link EntityContribution#getWhereParts()} = SALARY&gt;=1000 AND ACTIVE=1
     * </pre>
     */
    BuildConstraintOfAttributeWithContext,
    /**
     * Assuming the query "SALARY" and the attribute statement
     *
     * <pre>
     * &lt;attribute&gt;@Person@.SALARY&lt;/attribute&gt;
     * AND ACTIVE=1
     * </pre>
     *
     * this strategy creates the select query part of the attribute and adds constraints for the context
     *
     * <pre>
     * {@link EntityContribution#getSelectParts()} = SALARY
     * {@link EntityContribution#getWhereParts()} = ACTIVE=1
     * </pre>
     */
    BuildQueryOfAttributeAndConstraintOfContext,
  }

  /**
   * Strategy used in
   * {@link DataModelEntityPartDefinition#createInstance(FormDataStatementBuilder, ComposerEntityNodeData, EntityStrategy, String, Map)}
   */
  public static enum EntityStrategy {
    BuildConstraints,
    BuildQuery,
  }

  public static enum AttributeKind {
    /**
     * no attribute node
     */
    Undefined,
    NonAggregation,
    Aggregation,
    NonAggregationNonZeroTraversing,
    AggregationNonZeroTraversing,
  }

  private ISqlStyle m_sqlStyle;
  private IDataModel m_dataModel;
  private AliasMapper m_aliasMapper;
  private Map<Class<?>, DataModelAttributePartDefinition> m_dataModelAttMap;
  private Map<Class<?>, DataModelEntityPartDefinition> m_dataModelEntMap;
  private List<BasicPartDefinition> m_basicDefs;
  private Map<String, Object> m_bindMap;
  private AtomicInteger m_sequenceProvider;
  private StringBuffer m_where;
  private List<IFormDataStatementBuilderInjection> m_formDataStatementBuilderInjections;

  /**
   * @param sqlStyle
   */
  public FormDataStatementBuilder(ISqlStyle sqlStyle) {
    m_sqlStyle = sqlStyle;
    m_aliasMapper = new AliasMapper();
    m_bindMap = new HashMap<String, Object>();
    m_dataModelAttMap = new HashMap<Class<?>, DataModelAttributePartDefinition>();
    m_dataModelEntMap = new HashMap<Class<?>, DataModelEntityPartDefinition>();
    m_basicDefs = new ArrayList<BasicPartDefinition>();
    setSequenceProvider(new AtomicInteger(0));
  }

  public IDataModel getDataModel() {
    return m_dataModel;
  }

  /**
   * @return true to consume child contributions by this entity. Default returns true. If the entity is a 1:1 or 1:0
   *         relation to its base and its sql contribution is just a join clause or similar, this method must return
   *         false to let the parent entity colelct all parts. Use <code>return
   *         {@link IDataModelEntity#isOneToMany()}</code> when such behaviour is implemented.
   */
  protected boolean isConsumeChildContributions(EntityPath ePath) {
    return true;
    //return ePath.lastElement().isOneToMany();
  }

  /**
   * add an injection that allows to manipulate every call to
   * {@link #buildComposerAttributeNode(ComposerAttributeNodeData, AttributeStrategy)} and
   * {@link #buildComposerEntityNodeContribution(ComposerEntityNodeData, EntityStrategy)}
   */
  public void addFormDataStatementBuilderInjection(IFormDataStatementBuilderInjection j) {
    if (j == null) {
      return;
    }
    if (m_formDataStatementBuilderInjections == null) {
      m_formDataStatementBuilderInjections = new ArrayList<IFormDataStatementBuilderInjection>(1);
    }
    m_formDataStatementBuilderInjections.add(j);
  }

  public void removeFormDataStatementBuilderInjection(IFormDataStatementBuilderInjection j) {
    if (j == null) {
      return;
    }
    if (m_formDataStatementBuilderInjections != null) {
      m_formDataStatementBuilderInjections.remove(j);
      if (m_formDataStatementBuilderInjections.isEmpty()) {
        m_formDataStatementBuilderInjections = null;
      }
    }
  }

  private boolean hasInjections() {
    return (m_formDataStatementBuilderInjections != null && !m_formDataStatementBuilderInjections.isEmpty());
  }

  private void injectPreBuildEntity(ComposerEntityNodeData node, EntityStrategy entityStrategy, EntityContribution childContrib) {
    if (m_formDataStatementBuilderInjections != null) {
      for (IFormDataStatementBuilderInjection j : m_formDataStatementBuilderInjections) {
        j.preBuildEntity(node, entityStrategy, childContrib);
      }
    }
  }

  private void injectPostBuildEntity(ComposerEntityNodeData node, EntityStrategy entityStrategy, EntityContribution parentContrib) {
    if (m_formDataStatementBuilderInjections != null) {
      for (IFormDataStatementBuilderInjection j : m_formDataStatementBuilderInjections) {
        j.postBuildEntity(node, entityStrategy, parentContrib);
      }
    }
  }

  private void injectPostBuildAttribute(ComposerAttributeNodeData node, AttributeStrategy attributeStrategy, EntityContribution contrib) {
    if (m_formDataStatementBuilderInjections != null) {
      for (IFormDataStatementBuilderInjection j : m_formDataStatementBuilderInjections) {
        j.postBuildAttribute(node, attributeStrategy, contrib);
      }
    }
  }

  public void setDataModel(IDataModel dataModel) {
    m_dataModel = dataModel;
  }

  /**
   * @return the reference to the sequence provider to be used outside for additional sequenced items or sub statemet
   *         builders
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
   * {@link #setDataModelAttributeDefinition(DataModelAttributePartDefinition)} and
   * {@link #setDataModelEntityDefinition(DataModelEntityPartDefinition)}
   * <p>
   * <b>Number, Date, String, Boolean field</b>:<br>
   * The sqlAttribute is something like <code>@PERSON@.LAST_NAME</code><br>
   * When multiple occurrences are simultaneously used, the sqlAttribute may be written as
   * <code>(&lt;attribute&gt;@PERSON@.ORDER_STATUS&lt;/attribute&gt; OR &lt;attribute&gt;@PERSON@.DELIVERY_STATUS&lt;/attribute&gt;)</code>
   * <p>
   * The operator and aggregationType are required, unless a {@link BasicPartDefinition} is used.
   */
  public void setBasicDefinition(Class<?> fieldType, String sqlAttribute, int operator) {
    setBasicDefinition(new BasicPartDefinition(fieldType, sqlAttribute, operator));
  }

  /**
   * see {@link #setBasicDefinition(Class, String, int)}
   */
  public void setBasicDefinition(ClassIdentifier fieldTypeIdentifier, String sqlAttribute, int operator) {
    setBasicDefinition(new BasicPartDefinition(fieldTypeIdentifier, sqlAttribute, operator));
  }

  /**
   * see {@link #setBasicDefinition(Class, String, int)}
   */
  public void setBasicDefinition(Class<?> fieldType, String sqlAttribute, int operator, boolean plainBind) {
    setBasicDefinition(new BasicPartDefinition(fieldType, sqlAttribute, operator, plainBind));
  }

  /**
   * see {@link #setBasicDefinition(Class, String, int)}
   */
  public void setBasicDefinition(ClassIdentifier fieldTypeIdentifier, String sqlAttribute, int operator, boolean plainBind) {
    setBasicDefinition(new BasicPartDefinition(fieldTypeIdentifier, sqlAttribute, operator, plainBind));
  }

  /**
   * see {@link #setBasicDefinition(Class, String, int)}
   */
  public void setBasicDefinition(Class<?>[] fieldTypes, String sqlAttribute, int operator) {
    setBasicDefinition(new BasicPartDefinition(fieldTypes, sqlAttribute, operator, false));
  }

  /**
   * see {@link #setBasicDefinition(Class, String, int)}
   */
  public void setBasicDefinition(ClassIdentifier[] fieldTypeIdentifiers, String sqlAttribute, int operator) {
    setBasicDefinition(new BasicPartDefinition(fieldTypeIdentifiers, sqlAttribute, operator, false));
  }

  /**
   * see {@link #setBasicDefinition(Class, String, int)}
   */
  public void setBasicDefinition(BasicPartDefinition def) {
    m_basicDefs.add(def);
  }

  /**
   * <b>Data model attribute</b>:<br>
   * The sqlAttribute is something like LAST_NAME, STATUS or @PERSON@.LAST_NAME, @PERSON@.STATUS. @PERSON@ will be
   * replaced by the parent entitie's generated alias.
   * <p>
   * The @PERSON@ prefix is added automatically if missing, but only if the entity where the attribute is contained has
   * only <b>one</b> alias.<br>
   * When multiple occurrences are simultaneously used, the sqlAttribute may be written as
   * <code>(&lt;attribute&gt;ORDER_STATUS&lt;/attribute&gt; OR &lt;attribute&gt;DELIVERY_STATUS&lt;/attribute&gt;)</code>
   */
  public void setDataModelAttributeDefinition(Class<? extends IDataModelAttribute> attributeType, String sqlAttribute) {
    setDataModelAttributeDefinition(attributeType, sqlAttribute, false);
  }

  /**
   * see {@link #setDataModelAttributeDefinition(Class, String)}
   */
  public void setDataModelAttributeDefinition(Class<? extends IDataModelAttribute> attributeType, String sqlAttribute, boolean plainBind) {
    setDataModelAttributeDefinition(new DataModelAttributePartDefinition(attributeType, sqlAttribute, plainBind));
  }

  /**
   * see {@link #setDataModelAttributeDefinition(Class, String)}
   */
  public void setDataModelAttributeDefinition(Class<? extends IDataModelAttribute> attributeType, String whereClause, String selectClause, boolean plainBind) {
    setDataModelAttributeDefinition(new DataModelAttributePartDefinition(attributeType, whereClause, selectClause, plainBind));
  }

  /**
   * see {@link #setDataModelAttributeDefinition(Class, String)}
   */
  public void setDataModelAttributeDefinition(DataModelAttributePartDefinition def) {
    m_dataModelAttMap.put(def.getAttributeType(), def);
  }

  /**
   * see {@link #setDataModelEntityDefinition(Class, String, String)}
   */
  public void setDataModelEntityDefinition(Class<? extends IDataModelEntity> entityType, String whereClause) {
    setDataModelEntityDefinition(new DataModelEntityPartDefinition(entityType, whereClause));
  }

  /**
   * <b>Data model entity</b>:<br>
   * The whereClause is something like <code><pre>
   * EXISTS (
   * SELECT 1
   * FROM PERSON @PERSON@
   * WHERE @PERSON@.PERSON_ID=@parent.PERSON@.PERSON_ID
   * &lt;whereParts/&gt;
   * &lt;groupBy&gt;
   *  GROUP BY @PERSON@.PERSON_ID
   *  HAVING 1=1
   *  &lt;havingParts/&gt;
   * &lt;/groupBy&gt;
   * )
   * </pre></code> <br>
   * The selectClause is something like <code><pre>
   * ( SELECT &lt; selectParts/&gt;
   * FROM PERSON @PERSON@
   * WHERE @PERSON@.PERSON_ID=@parent.PERSON@.PERSON_ID
   * &lt;whereParts/&gt;
   * )
   * </pre></code> It is not allowed, that the selectClause contains a <i>UNION</i> because this part is needed for
   * aggregation too.<br>
   * The <i>selectParts</i> tag is replaced with all attributes which are selected. If there are more than one
   * attributes, they are separated by a comma "<i>attribute1</i> <i>,</i> <i>attribute2</i>".<br>
   * The <i>whereParts</i> tag is replaced with all attributes contained in the entity that have no aggregation type.
   * Every attribute contributes a "AND <i>attribute</i> <i>op</i> <i>value</i>" line.<br>
   * The <i>groupBy</i> tag is only used when there are attributes in the entity that have an aggregation type.<br>
   * The <i>havingParts</i> tag is replaced with all attributes contained in the entity that have an aggregation type.
   * Every aggregation attribute contributes a "AND <i>fun</i>(<i>attribute</i>) <i>op</i> <i>value</i>" line.<br>
   */

  public void setDataModelEntityDefinition(Class<? extends IDataModelEntity> entityType, String whereClause, String selectClause) {
    setDataModelEntityDefinition(new DataModelEntityPartDefinition(entityType, whereClause, selectClause));
  }

  /**
   * see {@link #setDataModelEntityDefinition(Class, String)}
   */
  public void setDataModelEntityDefinition(DataModelEntityPartDefinition def) {
    m_dataModelEntMap.put(def.getEntityType(), def);
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
    c.check(o);
    System.out.println(c.toString());
  }

  @SuppressWarnings("cast")
  public String build(AbstractFormData formData) {
    m_where = new StringBuffer();
    // get all formData fields and properties defined directly and indirectly by extending template fields, respectively
    //build constraints for fields
    for (BasicPartDefinition def : m_basicDefs) {
      if (def.accept(formData)) {
        Map<String, String> parentAliasMap = getAliasMapper().getRootAliases();
        EntityContribution contrib = def.createInstance(this, formData, parentAliasMap);
        String cons = createWhereConstraint(contrib);
        if (cons != null) {
          addWhere(" AND " + cons);
        }
      }
    }
    //build constraints for composer trees
    Map<Integer, Map<String, AbstractFormFieldData>> fieldsBreathFirstMap = formData.getAllFieldsRec();
    for (Map<String, AbstractFormFieldData> map : fieldsBreathFirstMap.values()) {
      for (AbstractFormFieldData f : map.values()) {
        if (f.isValueSet()) {
          if (f instanceof AbstractTreeFieldData) {
            // composer tree with entity, attribute
            EntityContribution contrib = buildTreeNodes(((AbstractTreeFieldData) f).getRoots(), EntityStrategy.BuildConstraints, AttributeStrategy.BuildConstraintOfAttributeWithContext);
            String cons = createWhereConstraint(contrib);
            if (cons != null) {
              addWhere(" AND " + cons);
            }
          }
        }
      }
    }
    return getWhereConstraints();
  }

  /**
   * Creates a select statement by merging the given entity contributions with the given base statement. This builder's
   * {@link #getWhereConstraints()} are added as well.
   *
   * @param stm
   *          base statement with &lt;selectParts/&gt;, &lt;fromParts/&gt;, &lt;whereParts/&gt;, &lt;groupByParts/&gt;
   *          or &lt;havingParts/&gt; place holders.
   * @param contributions
   *          entity contributions that are used to replace markers in the given base statement.
   * @return Returns given base statement having all place holders replaced by the given entity contributions.
   * @since 3.8.1
   */
  public String createSelectStatement(String stm, EntityContribution... contributions) {
    EntityContribution mergedContribution = new EntityContribution();
    if (contributions != null) {
      for (EntityContribution c : contributions) {
        mergedContribution.add(c);
      }
    }
    String where = StringUtility.trim(getWhereConstraints());
    if (StringUtility.hasText(where)) {
      if (where.toUpperCase().startsWith("AND")) {
        where = where.substring(3);
      }
      mergedContribution.getWhereParts().add(where);
    }
    return createEntityPart(stm, false, mergedContribution);
  }

  /**
   * do not use or override this method, it is protected for unit test purposes
   */
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
   * add sql part with custom binds the ADD keyword is NOT added (pre-pended) automatically
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

  public List<BasicPartDefinition> getBasicPartDefinitions() {
    return CollectionUtility.arrayList(m_basicDefs);
  }

  public Map<Class<?>, DataModelAttributePartDefinition> getDataModelAttributePartDefinitions() {
    return CollectionUtility.copyMap(m_dataModelAttMap);
  }

  public Map<Class<?>, DataModelEntityPartDefinition> getDataModelEntityPartDefinitions() {
    return CollectionUtility.copyMap(m_dataModelEntMap);
  }

  public String getWhereConstraints() {
    return (m_where != null ? m_where.toString() : null);
  }

  /**
   * Replace bind name by unique bind name so that it is not conflicting with other parts that use the same statement
   * part and bind name. For example S is replaces by __S123.
   */
  public String localizeBindName(String bindName, String prefix) {
    if (bindName != null) {
      String locName = prefix + bindName + getNextBindSeqNo();
      return locName;
    }
    return null;
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

  protected long getNextBindSeqNo() {
    return m_sequenceProvider.incrementAndGet();
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

  /**
   * moved to {@link EntityContributionUtility#contributionToConstraintText(EntityContribution)}
   */
  public static String createWhereConstraint(EntityContribution contrib) {
    return EntityContributionUtility.contributionToConstraintText(contrib);
  }

  public AttributeKind getAttributeKind(TreeNodeData node) {
    if (!(node instanceof ComposerAttributeNodeData)) {
      return AttributeKind.Undefined;
    }
    //
    ComposerAttributeNodeData attributeNode = (ComposerAttributeNodeData) node;
    Integer agg = attributeNode.getAggregationType();
    if (agg == null || agg == AGGREGATION_NONE) {
      if (!isZeroTraversingAttribute(attributeNode.getOperator(), attributeNode.getValues().toArray())) {
        return AttributeKind.NonAggregationNonZeroTraversing;
      }
      return AttributeKind.NonAggregation;
    }
    //
    if (!isZeroTraversingAttribute(attributeNode.getOperator(), attributeNode.getValues().toArray())) {
      return AttributeKind.AggregationNonZeroTraversing;
    }
    return AttributeKind.Aggregation;
  }

  /**
   * @param nodes
   * @return the complete string of all attribute contributions
   */
  public EntityContribution buildTreeNodes(List<TreeNodeData> nodes, EntityStrategy entityStrategy, AttributeStrategy attributeStrategy) {
    EntityContribution contrib = new EntityContribution();
    int i = 0;
    while (i < nodes.size()) {
      if (nodes.get(i) instanceof ComposerEntityNodeData) {
        EntityContribution subContrib = buildComposerEntityNodeContribution((ComposerEntityNodeData) nodes.get(i), entityStrategy);
        appendTreeSubContribution(contrib, subContrib, entityStrategy);
        i++;
      }
      else if (nodes.get(i) instanceof ComposerAttributeNodeData) {
        EntityContribution subContrib = buildComposerAttributeNode((ComposerAttributeNodeData) nodes.get(i), attributeStrategy);
        appendTreeSubContribution(contrib, subContrib, entityStrategy);
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
        EntityContribution subContrib = buildComposerOrNodes(orNodes, entityStrategy, attributeStrategy);
        appendTreeSubContribution(contrib, subContrib, entityStrategy);
        i = k + 1;
      }
      else {
        EntityContribution subContrib = buildTreeNodes(nodes.get(i).getChildNodes(), entityStrategy, attributeStrategy);
        appendTreeSubContribution(contrib, subContrib, entityStrategy);
      }
    }
    return contrib;
  }

  private void appendTreeSubContribution(EntityContribution parent, EntityContribution child, EntityStrategy entityStrategy) {
    switch (entityStrategy) {
      case BuildConstraints: {
        EntityContribution whereConstraints = EntityContributionUtility.createConstraintsContribution(child);
        if (whereConstraints != null) {
          parent.add(whereConstraints);
        }
        break;
      }
      default: {
        if (child != null && !child.isEmpty()) {
          parent.add(child);
        }
      }
    }
  }

  /**
   * do not use or override this method, it is protected for unit test purposes
   */
  protected EntityContribution buildComposerOrNodes(List<ComposerEitherOrNodeData> nodes, EntityStrategy entityStrategy, AttributeStrategy attributeStrategy) {
    EntityContribution contrib = new EntityContribution();
    // check if only one condition
    StringBuilder buf = new StringBuilder();
    int count = 0;
    for (ComposerEitherOrNodeData node : nodes) {
      EntityContribution subContrib = buildTreeNodes(node.getChildNodes(), entityStrategy, attributeStrategy);
      contrib.getFromParts().addAll(subContrib.getFromParts());
      if (subContrib.getWhereParts().size() + subContrib.getHavingParts().size() > 0) {
        if (count > 0) {
          buf.append(" OR ");
          if (node.isNegative()) {
            buf.append(" NOT ");
          }
        }
        buf.append("(");
        // remove possible outer join signs (+) in where / having constraint
        // this is necessary because outer joins are not allowed in OR clause
        // the removal of outer joins does not influence the result set
        buf.append(CollectionUtility.format(CollectionUtility.combine(subContrib.getWhereParts(), subContrib.getHavingParts()), " AND ").replaceAll("\\(\\+\\)", ""));
        buf.append(")");
        count++;
      }
    }
    if (count > 0) {
      if (count > 1) {
        buf.insert(0, "(");
        buf.append(")");
        contrib.getWhereParts().add(buf.toString());
      }
      else {
        String s = buf.toString();
        if (s.matches("\\(.*\\)")) {
          s = s.substring(1, s.length() - 1).trim();
        }
        contrib.getWhereParts().add(s);
      }
    }
    return contrib;
  }

  public EntityContribution buildComposerEntityNodeContribution(ComposerEntityNodeData node, EntityStrategy entityStrategy) {
    if (getDataModel() == null) {
      throw new ProcessingException("there is no data model set, call FormDataStatementBuilder.setDataModel to set one");
    }
    EntityPath entityPath = DataModelUtility.externalIdToEntityPath(getDataModel(), node.getEntityExternalId());
    IDataModelEntity entity = (entityPath != null ? entityPath.lastElement() : null);
    if (entity == null) {
      LOG.warn("no entity for external id: {}", node.getEntityExternalId());
      return null;
    }
    DataModelEntityPartDefinition def = m_dataModelEntMap.get(entity.getClass());
    if (def == null) {
      LOG.warn("no PartDefinition for entity: {}", entity);
      return null;
    }
    ComposerEntityNodeData parentEntityNode = getParentNodeOfType(node, ComposerEntityNodeData.class);
    Map<String, String> parentAliasMap = (parentEntityNode != null ? m_aliasMapper.getNodeAliases(parentEntityNode) : m_aliasMapper.getRootAliases());
    String baseStm;
    switch (entityStrategy) {
      case BuildQuery: {
        baseStm = def.getSelectClause();
        break;
      }
      case BuildConstraints: {
        baseStm = def.getWhereClause();
        break;
      }
      default: {
        baseStm = null;
      }
    }
    String stm = null;
    if (baseStm != null) {
      stm = def.createInstance(this, node, entityStrategy, baseStm, parentAliasMap);
    }
    if (stm == null) {
      return null;
    }
    m_aliasMapper.addAllNodeEntitiesFrom(node, stm);
    stm = m_aliasMapper.replaceMarkersByAliases(stm, m_aliasMapper.getNodeAliases(node), parentAliasMap);
    switch (entityStrategy) {
      case BuildQuery: {
        EntityContribution resultContrib = buildComposerEntityUnitContribution(node, entityStrategy, stm, node.getChildNodes(), isConsumeChildContributions(entityPath));
        return resultContrib;
      }
      case BuildConstraints: {
        String s = buildComposerEntityEitherOrSplit(entityStrategy, stm, node.isNegative(), node.getChildNodes());
        EntityContribution resultContrib = (s != null ? EntityContribution.create(s) : new EntityContribution());
        return resultContrib;
      }
      default: {
        return null;
      }
    }
  }

  /**
   * only used with strategy {@link EntityStrategy#BuildConstraints}
   * <p>
   * do not use or override this method, it is protected for unit test purposes
   */
  protected String buildComposerEntityEitherOrSplit(EntityStrategy entityStrategy, String baseStm, boolean negative, List<TreeNodeData> childParts) {
    if (entityStrategy != EntityStrategy.BuildConstraints) {
      return null;
    }
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
          String s = buildComposerEntityEitherOrSplit(entityStrategy, baseStm, negative ^ orData.isNegative(), subList);
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
      return null;
    }
    return buildComposerEntityZeroTraversingSplit(entityStrategy, baseStm, negative, childParts);
  }

  /**
   * only used with strategy {@link EntityStrategy#BuildConstraints} and one-to-many entity relation
   * <p>
   * do not use or override this method, it is protected for unit test purposes
   */
  protected String buildComposerEntityZeroTraversingSplit(EntityStrategy entityStrategy, String baseStm, boolean negative, List<TreeNodeData> childParts) {
    if (entityStrategy != EntityStrategy.BuildConstraints) {
      return null;
    }
    ArrayList<TreeNodeData> nonZeroChildren = new ArrayList<TreeNodeData>(2);
    for (TreeNodeData ch : childParts) {
      switch (getAttributeKind(ch)) {
        case Undefined:
        case NonAggregation/*non-aggregations must not be handled as zero-traversal*/:
        case NonAggregationNonZeroTraversing:
        case AggregationNonZeroTraversing: {
          nonZeroChildren.add(ch);
          break;
        }
      }
    }
    //
    //create entity part 1
    String entityPart1 = buildComposerEntityUnit(entityStrategy, baseStm, negative, childParts);
    //create negated entity part 2
    String entityPart2 = null;
    if (nonZeroChildren.size() < childParts.size()) {
      // negated negation
      entityPart2 = buildComposerEntityUnit(entityStrategy, baseStm, !negative, nonZeroChildren);
    }
    //combine parts
    if (entityPart2 != null) {
      return " ( " + entityPart1 + " OR " + entityPart2 + " ) ";
    }
    return entityPart1;
  }

  /**
   * do not use or override this method, it is protected for unit test purposes
   */
  protected EntityContribution buildComposerEntityUnitContribution(ComposerEntityNodeData node, EntityStrategy entityStrategy, String baseStm, List<TreeNodeData> childParts, boolean consumeChildContributions) {
    EntityContribution childContributions = new EntityContribution();
    switch (entityStrategy) {
      case BuildConstraints: {
        ArrayList<TreeNodeData> nonAggregationParts = new ArrayList<TreeNodeData>(childParts.size());
        ArrayList<TreeNodeData> aggregationParts = new ArrayList<TreeNodeData>(2);
        for (TreeNodeData ch : childParts) {
          switch (getAttributeKind(ch)) {
            case Undefined:
            case NonAggregation:
            case NonAggregationNonZeroTraversing: {
              nonAggregationParts.add(ch);
              break;
            }
            case Aggregation:
            case AggregationNonZeroTraversing: {
              aggregationParts.add(ch);
              break;
            }
          }
        }
        //
        EntityContribution subContrib = buildTreeNodes(nonAggregationParts, entityStrategy, AttributeStrategy.BuildConstraintOfAttributeWithContext);
        childContributions.add(subContrib);
        //
        subContrib = buildTreeNodes(aggregationParts, entityStrategy, AttributeStrategy.BuildConstraintOfContext);
        childContributions.add(subContrib);
        //
        subContrib = buildTreeNodes(aggregationParts, entityStrategy, AttributeStrategy.BuildConstraintOfAttribute);
        childContributions.add(subContrib);
        break;
      }
      case BuildQuery: {
        EntityContribution subContrib = buildTreeNodes(childParts, entityStrategy, AttributeStrategy.BuildQueryOfAttributeAndConstraintOfContext);
        childContributions.add(subContrib);
        break;
      }
    }
    //legacy: node may be null from legacy calls
    if (node != null && hasInjections()) {
      injectPreBuildEntity(node, entityStrategy, childContributions);
    }
    EntityContribution parentContributions = createEntityPart(entityStrategy, baseStm, childContributions, consumeChildContributions);
    if (node != null && hasInjections()) {
      injectPostBuildEntity(node, entityStrategy, parentContributions);
    }
    return parentContributions;
  }

  /**
   * only used with strategy {@link EntityStrategy#BuildConstraints} and one-to-many entity relation
   * <p>
   * do not use or override this method, it is protected for unit test purposes
   */
  protected String buildComposerEntityUnit(EntityStrategy entityStrategy, String baseStm, boolean negative, List<TreeNodeData> childParts) {
    EntityContribution contrib = buildComposerEntityUnitContribution(null, entityStrategy, baseStm, childParts, true);
    List<String> list = contrib.getWhereParts();
    if (list.isEmpty()) {
      list = contrib.getFromParts();
    }
    if (list.isEmpty()) {
      list = contrib.getSelectParts();
    }
    if (list.isEmpty()) {
      return "1=1";
    }
    String s = list.get(0);
    // negation
    if (negative) {
      s = " NOT (" + s + ") ";
    }
    return s;
  }

  public EntityContribution buildComposerAttributeNode(final ComposerAttributeNodeData node, AttributeStrategy attributeStrategy) {
    if (getDataModel() == null) {
      throw new ProcessingException("there is no data model set, call FormDataStatementBuilder.setDataModel to set one");
    }
    AttributePath attPath = DataModelUtility.externalIdToAttributePath(getDataModel(), node.getAttributeExternalId());
    IDataModelAttribute attribute = (attPath != null ? attPath.getAttribute() : null);
    if (attribute == null) {
      LOG.warn("no attribute for external id: {}", node.getAttributeExternalId());
      return new EntityContribution();
    }
    DataModelAttributePartDefinition def = m_dataModelAttMap.get(attribute.getClass());
    if (def == null) {
      Integer agg = node.getAggregationType();
      if (agg != null && agg == AGGREGATION_COUNT) {
        def = new DataModelAttributePartDefinition(null, "1", false);
      }
    }
    if (def == null) {
      LOG.warn("no PartDefinition for attribute: {}", attribute);
      return new EntityContribution();
    }
    List<Object> bindValues = new ArrayList<Object>();
    if (node.getValues() != null) {
      bindValues.addAll(node.getValues());
    }
    List<String> bindNames = new ArrayList<String>(bindValues.size());
    for (int i = 0; i < bindValues.size(); i++) {
      bindNames.add("" + (char) (((int) 'a') + i));
    }
    AliasMapper aliasMap = getAliasMapper();
    ComposerEntityNodeData parentEntityNode = FormDataStatementBuilder.getParentNodeOfType(node, ComposerEntityNodeData.class);
    Map<String, String> parentAliasMap = parentEntityNode != null ? aliasMap.getNodeAliases(parentEntityNode) : aliasMap.getRootAliases();
    String stm = null;
    switch (attributeStrategy) {
      case BuildConstraintOfAttribute:
      case BuildConstraintOfContext:
      case BuildConstraintOfAttributeWithContext: {
        stm = def.getWhereClause();
        break;
      }
      case BuildQueryOfAttributeAndConstraintOfContext: {
        stm = def.getSelectClause();
        break;
      }
    }
    EntityContribution contrib = null;
    if (stm != null) {
      contrib = def.createInstance(this, node, attributeStrategy, stm, bindNames, bindValues, parentAliasMap);
    }
    if (contrib == null) {
      contrib = new EntityContribution();
    }
    switch (attributeStrategy) {
      case BuildQueryOfAttributeAndConstraintOfContext: {
        if (contrib.getSelectParts().isEmpty()) {
          contrib.getSelectParts().add("NULL");
          contrib.getGroupByParts().add("NULL");
        }
        break;
      }
    }
    if (hasInjections()) {
      injectPostBuildAttribute(node, attributeStrategy, contrib);
    }
    return contrib;
  }

  /**
   * Evaluates the collecting tags in the entity statement and fills in the values of the {@link EntityContribution}. If
   * the contributing tags are missing, the complete part is treated as 'select' on {@link EntityStrategy#BuildQuery}
   * and as 'where' on {@link EntityStrategy#BuildConstraints}
   * <p>
   * Default calls
   * {@link EntityContributionUtility#mergeContributions(EntityStrategy, String, EntityContribution, boolean)}
   *
   * @param entityStrategy
   * @param entityPartWithTags
   *          may contain the collecting tags selectParts, fromParts, whereParts, groupBy, groupByParts, havingParts
   *          <br/>
   *          as well as the contributing selectPart, fromPart, wherePart, groupByPart, havingPart for the outer calling
   *          part.
   * @param childContributions
   *          is the set of tags collected by all children
   * @param consumeChildContributions
   *          true: consume the child tags inside the entity statement. The returned entity contributions will not
   *          contain any of these tags
   *          <p>
   *          false: don't consume the child tags inside the entity statement. The returned entity contribution contains
   *          its onw plus all of these child tags (proxy)
   */
  public EntityContribution createEntityPart(EntityStrategy entityStrategy, String entityPartWithTags, EntityContribution childContributions, boolean consumeChildContributions) {
    if (consumeChildContributions) {
      entityPartWithTags = autoCompleteEntityPartTags(entityPartWithTags);
    }
    EntityContribution parentContrib = EntityContributionUtility.mergeContributions(entityStrategy, entityPartWithTags, childContributions, consumeChildContributions);
    return parentContrib;
  }

  /**
   * only used with strategy {@link EntityStrategy#BuildConstraints}
   * <p>
   *
   * @return the statement combined with the contributions
   */
  public String createEntityPart(String stm, boolean negative, EntityContribution childContributions) {
    EntityContribution contrib = createEntityPart(EntityStrategy.BuildConstraints, stm, childContributions, true);
    List<String> list = contrib.getWhereParts();
    if (list.isEmpty()) {
      list = contrib.getFromParts();
    }
    if (list.isEmpty()) {
      list = contrib.getSelectParts();
    }
    if (list.isEmpty()) {
      return "1=1";
    }
    String s = list.get(0);
    // negation
    if (negative) {
      s = " NOT (" + s + ") ";
    }
    return s;
  }

  protected String autoCompleteEntityPartTags(String s) {
    if (s == null) {
      return null;
    }
    if (StringUtility.getTag(s, "whereParts") == null) {
      s = s + " <whereParts/>";
    }
    if (StringUtility.getTag(s, "groupBy") == null) {
      s = s + " <groupBy/>";
    }
    s = s.replace("<groupBy/>", "<groupBy>GROUP BY <groupByParts/> HAVING 1=1 <havingParts/></groupBy>");
    return s;
  }

  public static final int STATUS_CODE_INVALID_GROUP_BY_PART = EntityContributionUtility.STATUS_CODE_INVALID_GROUP_BY_PART;

  /**
   * Check if a group by part is valid, i.e. ist not a SELECT clause. default uses
   * {@link EntityContributionUtility#checkGroupByPart(String)}
   *
   * @throws ProcessingException
   *           with {@link IStatus#getCode()} = X
   * @since 3.8
   */
  protected void checkGroupByPart(String groupByPart) {
    EntityContributionUtility.checkGroupByPart(groupByPart);
  }

  /**
   * adding an attribute as an entity contribution
   * <p>
   * Evaluates the tags in the attribute statement and creates an {@link EntityContribution} based on it.
   *
   * @param stm
   *          may contain attribute, fromPart and wherePart tags
   */
  public EntityContribution createAttributePart(AttributeStrategy attributeStrategy, Integer aggregationType, String stm, int operation, List<String> bindNames, List<Object> bindValues, final boolean plainBind,
      Map<String, String> parentAliasMap) {
    if (stm == null) {
      return new EntityContribution();
    }
    //convenience: automatically wrap attribute in attribute tags
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
    boolean isAg = (aggregationType != null && aggregationType != AGGREGATION_NONE);
    EntityContribution contrib = new EntityContribution();
    //special handling of NOT: wrap NOT around complete constraint text and not only in attribute operator
    int positiveOperation;
    boolean negation;
    switch (operation) {
      case OPERATOR_DATE_IS_NOT_TODAY: {
        positiveOperation = OPERATOR_DATE_IS_TODAY;
        negation = true;
        break;
      }
      case OPERATOR_DATE_NEQ: {
        positiveOperation = OPERATOR_DATE_EQ;
        negation = true;
        break;
      }
      case OPERATOR_DATE_TIME_IS_NOT_NOW: {
        positiveOperation = OPERATOR_DATE_TIME_IS_NOW;
        negation = true;
        break;
      }
      case OPERATOR_DATE_TIME_NEQ: {
        positiveOperation = OPERATOR_DATE_TIME_EQ;
        negation = true;
        break;
      }
      case OPERATOR_NEQ: {
        positiveOperation = OPERATOR_EQ;
        negation = true;
        break;
      }
      case OPERATOR_NOT_CONTAINS: {
        positiveOperation = OPERATOR_CONTAINS;
        negation = true;
        break;
      }
      case OPERATOR_NOT_ENDS_WITH: {
        positiveOperation = OPERATOR_ENDS_WITH;
        negation = true;
        break;
      }
      case OPERATOR_NOT_IN: {
        positiveOperation = OPERATOR_IN;
        negation = true;
        break;
      }
      case OPERATOR_NOT_NULL: {
        positiveOperation = OPERATOR_NULL;
        negation = true;
        break;
      }
      case OPERATOR_NOT_STARTS_WITH: {
        positiveOperation = OPERATOR_STARTS_WITH;
        negation = true;
        break;
      }
      case OPERATOR_NUMBER_NOT_NULL: {
        positiveOperation = OPERATOR_NUMBER_NULL;
        negation = true;
        break;
      }
      case OPERATOR_TIME_IS_NOT_NOW: {
        positiveOperation = OPERATOR_TIME_IS_NOW;
        negation = true;
        break;
      }
      default: {
        positiveOperation = operation;
        negation = false;
      }
    }
    //
    String fromPart = StringUtility.getTag(stm, "fromPart");
    stm = StringUtility.removeTag(stm, "fromPart").trim();
    String wherePart = StringUtility.getTag(stm, "wherePart");
    if (wherePart == null) {
      String tmp = StringUtility.removeTag(stm, "attribute").trim();
      if (tmp.length() > 0) {
        wherePart = stm;
        stm = "";
      }
    }
    stm = StringUtility.removeTag(stm, "wherePart").trim();
    String attPart = StringUtility.getTag(stm, "attribute");
    stm = StringUtility.removeTag(stm, "attribute").trim();
    if (stm.length() > 0) {
      LOG.warn("attribute part is not well-formed; contains wherePart tag and also other sql text: {}", stm);
    }
    //
    //from
    if (fromPart != null) {
      //resolve aliases in from
      // mis-using 'contrib' as a "node" because real node is not accessible
      m_aliasMapper.addMissingNodeEntitiesFrom(contrib, fromPart);
      Map<String, String> aliasMap = m_aliasMapper.getNodeAliases(contrib);
      parentAliasMap.putAll(aliasMap);
      fromPart = m_aliasMapper.replaceMarkersByAliases(fromPart, parentAliasMap, parentAliasMap);
      contrib.getFromParts().add(fromPart);
    }
    switch (attributeStrategy) {
      //select ... where
      case BuildQueryOfAttributeAndConstraintOfContext: {
        //select
        if (attPart != null) {
          String sql = createSqlPart(aggregationType, attPart, OPERATOR_NONE, bindNames, bindValues, plainBind, parentAliasMap);
          if (sql != null) {
            contrib.getSelectParts().add(sql);
            if (!isAg) {
              contrib.getGroupByParts().add(sql);
            }
          }
        }
        //where
        if (wherePart != null) {
          wherePart = StringUtility.replaceTags(wherePart, "attribute", "1=1").trim();
          String sql = createSqlPart(wherePart, bindNames, bindValues, plainBind, parentAliasMap);
          if (sql != null) {
            contrib.getWhereParts().add(sql);
          }
        }
        break;
      }
        //where / having
      case BuildConstraintOfAttribute: {
        if (attPart != null) {
          String sql = createSqlPart(aggregationType, attPart, positiveOperation, bindNames, bindValues, plainBind, parentAliasMap);
          if (sql != null) {
            if (negation) {
              sql = "NOT(" + sql + ")";
            }
            if (isAg) {
              contrib.getHavingParts().add(sql);
            }
            else {
              contrib.getWhereParts().add(sql);
            }
          }
        }
        break;
      }
      case BuildConstraintOfContext: {
        if (wherePart != null) {
          wherePart = StringUtility.replaceTags(wherePart, "attribute", "1=1").trim();
          String sql = createSqlPart(wherePart, bindNames, bindValues, plainBind, parentAliasMap);
          if (sql != null) {
            contrib.getWhereParts().add(sql);
          }
        }
        break;
      }
      case BuildConstraintOfAttributeWithContext: {
        String whereAndAttPart = (wherePart != null ? wherePart : "") + (wherePart != null && attPart != null ? " AND " : "") + (attPart != null ? "<attribute>" + attPart + "</attribute>" : "");
        if (whereAndAttPart.length() > 0) {
          String sql = createSqlPart(aggregationType, whereAndAttPart, positiveOperation, bindNames, bindValues, plainBind, parentAliasMap);
          if (sql != null) {
            if (negation) {
              sql = "NOT(" + sql + ")";
            }
            contrib.getWhereParts().add(sql);
          }
        }
        break;
      }
    }
    return contrib;
  }

  /**
   * adding an attribute as an entity contribution
   * <p>
   *
   * @param stm
   *          may contain attribute, fromPart and wherePart tags
   */
  public String createAttributePartSimple(AttributeStrategy attributeStrategy, Integer aggregationType, String stm, int operation, List<String> bindNames, List<Object> bindValues, boolean plainBind, Map<String, String> parentAliasMap) {
    EntityContribution contrib = createAttributePart(attributeStrategy, aggregationType, stm, operation, bindNames, bindValues, plainBind, parentAliasMap);
    if (contrib.isEmpty()) {
      return null;
    }
    return CollectionUtility.format(contrib.getWhereParts(), " AND ");
  }

  /**
   * Create sql text, makes bind names unique, and adds all binds to the bind map
   * <p>
   * Convenience for <code>createSqlPart(AGGREGATION_NONE, String, OPERATOR_NONE, List, List, boolean, Map)</code>
   */
  public String createSqlPart(String sql, List<String> bindNames, List<Object> bindValues, final boolean plainBind, Map<String, String> parentAliasMap) {
    return createSqlPart(AGGREGATION_NONE, sql, OPERATOR_NONE, bindNames, bindValues, plainBind, parentAliasMap);
  }

  /**
   * Create sql text, makes bind names unique, and adds all binds to the bind map
   * <p>
   * To use no operator use {@link DataModelConstants#OPERATOR_NONE} and null for binds and values, stm will be
   * decorated and is the result itself
   * <p>
   * To use no aggregation use {@link DataModelConstants#AGGREGATION_NONE}
   */
  public String createSqlPart(final Integer aggregationType, String sql, final int operation, List<String> bindNames, List<Object> bindValues, final boolean plainBind, Map<String, String> parentAliasMap) {
    if (sql == null) {
      sql = "";
    }
    if (bindNames == null) {
      bindNames = new ArrayList<String>(0);
    }
    if (bindValues == null) {
      bindValues = new ArrayList<Object>(0);
    }
    // the attribute was of the form: NAME or
    // <attribute>NAME</attribute>
    // make sure there is an attribute tag in the string, if none enclose all
    // by default
    if (sql.indexOf("<attribute>") < 0) {
      sql = "<attribute>" + sql + "</attribute>";
    }
    //convenience: automatically add missing alias on plain attributes, but only if the parent entity has at most 1 alias mapping
    Matcher m = PLAIN_ATTRIBUTE_PATTERN.matcher(sql);
    if (m.find()) {
      if (parentAliasMap.size() == 0) {
        //nop
      }
      else if (parentAliasMap.size() == 1) {
        sql = m.replaceAll("$1@parent." + parentAliasMap.keySet().iterator().next() + "@.$2$3");
      }
      else {
        throw new ProcessingException("root attribute with " + sql + " uses no @...@ alias prefix, but parent has more than 1 alias: " + parentAliasMap);
      }
    }
    //resolve aliases
    sql = m_aliasMapper.replaceMarkersByAliases(sql, parentAliasMap, parentAliasMap);
    // generate unique bind names
    final ArrayList<String> newBindNames = new ArrayList<String>(2);
    for (int i = 0; i < bindNames.size(); i++) {
      String o = bindNames.get(i);
      String n = localizeBindName(o, "__");
      newBindNames.add(n);
      sql = localizeStatement(sql, o, n);
    }
    // part decoration
    final List<Object> valuesFinal = bindValues;
    ITagProcessor processor = new ITagProcessor() {
      @Override
      public String processTag(String tagName, String a) {
        return createSqlOpValuePart(aggregationType, a, operation, newBindNames, valuesFinal, plainBind);
      }
    };
    return StringUtility.replaceTags(sql, "attribute", processor);
  }

  public String createSqlOpValuePart(Integer aggregationType, String sql, int operation, List<String> bindNames, List<Object> bindValues, boolean plainBind) {
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
          sql = m_sqlStyle.toAggregationCount(sql);
          break;
        }
        case AGGREGATION_MIN: {
          sql = m_sqlStyle.toAggregationMin(sql);
          break;
        }
        case AGGREGATION_MAX: {
          sql = m_sqlStyle.toAggregationMax(sql);
          break;
        }
        case AGGREGATION_SUM: {
          sql = m_sqlStyle.toAggregationSum(sql);
          break;
        }
        case AGGREGATION_AVG: {
          sql = m_sqlStyle.toAggregationAvg(sql);
          break;
        }
        case AGGREGATION_MEDIAN: {
          sql = m_sqlStyle.toAggregationMedian(sql);
          break;
        }
      }
    }
    else if (isZeroTraversingAttribute(operation, values)) {
      sql = m_sqlStyle.getNvlToken() + "(" + sql + ",0)";
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
            BindModel m = new BindParser(sql).parse();
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
            sql = m.getFilteredStatement();
          }
        }
        else {
          addBinds(names, values);
        }
        return sql;
      }
      case OPERATOR_BETWEEN: {
        if (!plainBind) {
          addBinds(names, values);
        }
        if (values[0] == null) {
          return m_sqlStyle.createLE(sql, names[1]);
        }
        else if (values[1] == null) {
          return m_sqlStyle.createGE(sql, names[0]);
        }
        else {
          return m_sqlStyle.createBetween(sql, names[0], names[1]);
        }
      }
      case OPERATOR_DATE_BETWEEN: {
        if (!plainBind) {
          addBinds(names, values);
        }
        if (values[0] == null) {
          return m_sqlStyle.createDateLE(sql, names[1]);
        }
        else if (values[1] == null) {
          return m_sqlStyle.createDateGE(sql, names[0]);
        }
        else {
          return m_sqlStyle.createDateBetween(sql, names[0], names[1]);
        }
      }
      case OPERATOR_DATE_TIME_BETWEEN: {
        if (!plainBind) {
          addBinds(names, values);
        }
        if (values[0] == null) {
          return m_sqlStyle.createDateTimeLE(sql, names[1]);
        }
        else if (values[1] == null) {
          return m_sqlStyle.createDateTimeGE(sql, names[0]);
        }
        else {
          return m_sqlStyle.createDateTimeBetween(sql, names[0], names[1]);
        }
      }
      case OPERATOR_EQ: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createEQ(sql, names[0]);
      }
      case OPERATOR_DATE_EQ: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateEQ(sql, names[0]);
      }
      case OPERATOR_DATE_TIME_EQ: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateTimeEQ(sql, names[0]);
      }
      case OPERATOR_GE: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createGE(sql, names[0]);
      }
      case OPERATOR_DATE_GE: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateGE(sql, names[0]);
      }
      case OPERATOR_DATE_TIME_GE: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateTimeGE(sql, names[0]);
      }
      case OPERATOR_GT: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createGT(sql, names[0]);
      }
      case OPERATOR_DATE_GT: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateGT(sql, names[0]);
      }
      case OPERATOR_DATE_TIME_GT: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateTimeGT(sql, names[0]);
      }
      case OPERATOR_LE: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createLE(sql, names[0]);
      }
      case OPERATOR_DATE_LE: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateLE(sql, names[0]);
      }
      case OPERATOR_DATE_TIME_LE: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateTimeLE(sql, names[0]);
      }
      case OPERATOR_LT: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createLT(sql, names[0]);
      }
      case OPERATOR_DATE_LT: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateLT(sql, names[0]);
      }
      case OPERATOR_DATE_TIME_LT: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateTimeLT(sql, names[0]);
      }
      case OPERATOR_NEQ: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createNEQ(sql, names[0]);
      }
      case OPERATOR_DATE_NEQ: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateNEQ(sql, names[0]);
      }
      case OPERATOR_DATE_TIME_NEQ: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateTimeNEQ(sql, names[0]);
      }
      case OPERATOR_DATE_IS_IN_DAYS: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateIsInDays(sql, names[0]);
      }
      case OPERATOR_DATE_IS_IN_GE_DAYS: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateIsInGEDays(sql, names[0]);
      }
      case OPERATOR_DATE_IS_IN_GE_MONTHS: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateIsInGEMonths(sql, names[0]);
      }
      case OPERATOR_DATE_IS_IN_LE_DAYS: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateIsInLEDays(sql, names[0]);
      }
      case OPERATOR_DATE_IS_IN_LE_MONTHS: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateIsInLEMonths(sql, names[0]);
      }
      case OPERATOR_DATE_IS_IN_LAST_DAYS: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateIsInLastDays(sql, names[0]);
      }
      case OPERATOR_DATE_IS_IN_LAST_MONTHS: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateIsInLastMonths(sql, names[0]);
      }
      case OPERATOR_DATE_IS_IN_MONTHS: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateIsInMonths(sql, names[0]);
      }
      case OPERATOR_DATE_IS_IN_NEXT_DAYS: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateIsInNextDays(sql, names[0]);
      }
      case OPERATOR_DATE_IS_IN_NEXT_MONTHS: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateIsInNextMonths(sql, names[0]);
      }
      case OPERATOR_DATE_IS_NOT_TODAY: {
        return m_sqlStyle.createDateIsNotToday(sql);
      }
      case OPERATOR_DATE_IS_TODAY: {
        return m_sqlStyle.createDateIsToday(sql);
      }
      case OPERATOR_DATE_TIME_IS_IN_GE_HOURS: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateTimeIsInGEHours(sql, names[0]);
      }
      case OPERATOR_DATE_TIME_IS_IN_GE_MINUTES: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateTimeIsInGEMinutes(sql, names[0]);
      }
      case OPERATOR_DATE_TIME_IS_IN_LE_HOURS: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateTimeIsInLEHours(sql, names[0]);
      }
      case OPERATOR_DATE_TIME_IS_IN_LE_MINUTES: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createDateTimeIsInLEMinutes(sql, names[0]);
      }
      case OPERATOR_DATE_TIME_IS_NOT_NOW: {
        return m_sqlStyle.createDateTimeIsNotNow(sql);
      }
      case OPERATOR_DATE_TIME_IS_NOW: {
        return m_sqlStyle.createDateTimeIsNow(sql);
      }
      case OPERATOR_ENDS_WITH: {
        if (!plainBind) {
          addBind(names[0], m_sqlStyle.toLikePattern(values[0]));
        }
        return m_sqlStyle.createEndsWith(sql, names[0]);
      }
      case OPERATOR_NOT_ENDS_WITH: {
        if (!plainBind) {
          addBind(names[0], m_sqlStyle.toLikePattern(values[0]));
        }
        return m_sqlStyle.createNotEndsWith(sql, names[0]);
      }
      case OPERATOR_IN: {
        if (!plainBind) {
          addBinds(names, values);
        }
        //no support for plain bind in here. otherwise, ArrayInput gets confused.
        return m_sqlStyle.createInList(sql, true, values[0]);
      }
      case OPERATOR_CONTAINS: {
        if (!plainBind) {
          addBind(names[0], m_sqlStyle.toLikePattern(values[0]));
        }
        return m_sqlStyle.createContains(sql, names[0]);
      }
      case OPERATOR_LIKE: {
        if (!plainBind) {
          addBind(names[0], m_sqlStyle.toLikePattern(values[0]));
        }
        return m_sqlStyle.createLike(sql, names[0]);
      }
      case OPERATOR_NOT_LIKE: {
        if (!plainBind) {
          addBind(names[0], m_sqlStyle.toLikePattern(values[0]));
        }
        return m_sqlStyle.createNotLike(sql, names[0]);
      }
      case OPERATOR_NOT_IN: {
        if (!plainBind) {
          addBinds(names, values);
        }
        //no support for plain bind in here. otherwise, ArrayInput gets confused.
        return m_sqlStyle.createNotInList(sql, true, values[0]);
      }
      case OPERATOR_NOT_CONTAINS: {
        if (!plainBind) {
          addBind(names[0], m_sqlStyle.toLikePattern(values[0]));
        }
        return m_sqlStyle.createNotContains(sql, names[0]);
      }
      case OPERATOR_NOT_NULL: {
        return m_sqlStyle.createNotNull(sql);
      }
      case OPERATOR_NUMBER_NOT_NULL: {
        return m_sqlStyle.createNumberNotNull(sql);
      }
      case OPERATOR_NULL: {
        return m_sqlStyle.createNull(sql);
      }
      case OPERATOR_NUMBER_NULL: {
        return m_sqlStyle.createNumberNull(sql);
      }
      case OPERATOR_STARTS_WITH: {
        if (!plainBind) {
          addBind(names[0], m_sqlStyle.toLikePattern(values[0]));
        }
        return m_sqlStyle.createStartsWith(sql, names[0]);
      }
      case OPERATOR_NOT_STARTS_WITH: {
        if (!plainBind) {
          addBind(names[0], m_sqlStyle.toLikePattern(values[0]));
        }
        return m_sqlStyle.createNotStartsWith(sql, names[0]);
      }
      case OPERATOR_TIME_IS_IN_GE_HOURS: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createTimeIsInGEHours(sql, names[0]);
      }
      case OPERATOR_TIME_IS_IN_GE_MINUTES: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createTimeIsInGEMinutes(sql, names[0]);
      }
      case OPERATOR_TIME_IS_IN_HOURS: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createTimeIsInHours(sql, names[0]);
      }
      case OPERATOR_TIME_IS_IN_LE_HOURS: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createTimeIsInLEHours(sql, names[0]);
      }
      case OPERATOR_TIME_IS_IN_LE_MINUTES: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createTimeIsInLEMinutes(sql, names[0]);
      }
      case OPERATOR_TIME_IS_IN_MINUTES: {
        if (!plainBind) {
          addBinds(names, values);
        }
        return m_sqlStyle.createTimeIsInMinutes(sql, names[0]);
      }
      case OPERATOR_TIME_IS_NOW: {
        return m_sqlStyle.createTimeIsNow(sql);
      }
      case OPERATOR_TIME_IS_NOT_NOW: {
        return m_sqlStyle.createTimeIsNotNow(sql);
      }
      default: {
        throw new IllegalArgumentException("invalid operator: " + operation);
      }
    }
  }
}
