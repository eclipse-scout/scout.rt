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

import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.jdbc.builder.FormDataStatementBuilder.AttributeStrategy;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerAttributeNodeData;
import org.eclipse.scout.rt.shared.data.model.DataModelConstants;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Definition of a attribute-to-sql mapping for {@link IDataModelAttribute}
 */
public class DataModelAttributePartDefinition implements DataModelConstants {
  private static final Logger LOG = LoggerFactory.getLogger(DataModelAttributePartDefinition.class);

  private final String m_whereClause;
  private final String m_selectClause;
  private final boolean m_plainBind;
  private final Class<? extends IDataModelAttribute> m_attributeType;

  /**
   * see {@link #DataModelAttributePartDefinition(Class, String, String, boolean)}
   */
  public DataModelAttributePartDefinition(Class<? extends IDataModelAttribute> attributeType, String whereClause, boolean plainBind) {
    this(attributeType, whereClause, autoCreateSelectClause(whereClause), plainBind);
  }

  /**
   * @param whereClause
   *          is normally something like @Person@.LAST_NAME
   *          <p>
   *          Maximum extent is using tags like
   *
   *          <pre>
   *          &lt;attribute&gt;@Person@.LAST_NAME&lt;/attribute&gt;
   *          &lt;fromPart&gt;MY_PERSON @Person@&lt;/fromPart&gt;
   *          &lt;wherePart&gt;@Person@.PERSON_ID=...&lt;/wherePart&gt;
   *          &lt;havingPart&gt;...&lt;/havingPart&gt;
   *          </pre>
   *
   *          That way the wherePart is added to the entities whereParts section and never to the havingParts section,
   *          which would be wrong.
   * @param selectClause
   *          is by default the same as the where clause, but sometimes it is necessary to have a different select
   *          clause than the where clause.
   */
  public DataModelAttributePartDefinition(Class<? extends IDataModelAttribute> attributeType, String whereClause, String selectClause, boolean plainBind) {
    m_attributeType = attributeType;
    m_whereClause = whereClause;
    m_selectClause = selectClause;
    m_plainBind = plainBind;
    check();
  }

  private static String autoCreateSelectClause(String whereClause) {
    if (whereClause == null) {
      return null;
    }
    String tmp = whereClause;
    tmp = StringUtility.removeTag(tmp, "fromPart");
    tmp = StringUtility.removeTag(tmp, "wherePart");
    tmp = StringUtility.removeTag(tmp, "havingPart");
    if (!StringUtility.hasText(tmp)) {
      return null;
    }
    if ((" " + tmp).matches(".*[^a-zA-Z_$]SELECT[^a-zA-Z_$].*")) {
      return null;
    }
    return whereClause;
  }

  /**
   * Override to do customized checks on select and where clause or to deactivate default checks.
   * <p>
   * Called by the constructor after calling {@link #autoCreateSelectClause(String)} and setting its member fields
   * <p>
   * Default does 99%-safe check of correct usage of wherePart on non-trivial attributes.
   */
  protected void check() {
    if (m_whereClause != null) {
      String low = m_whereClause.toLowerCase().replaceAll("\\s", " ");
      if (low.indexOf("<attribute") >= 0 && low.indexOf("<wherepart") < 0 && low.indexOf(" and ") >= 0) {
        LOG.info("{} is a non-trivial attribute and should have the form <wherePart>... AND ...</wherePart> <attribute>...</attribute>: {}", m_attributeType.getName(), m_whereClause);
      }
    }
  }

  public String getWhereClause() {
    return m_whereClause;
  }

  public String getSelectClause() {
    return m_selectClause;
  }

  public Class<? extends IDataModelAttribute> getAttributeType() {
    return m_attributeType;
  }

  /**
   * @return true for a plain bind (without jdbc ?) and false for jdbc ? binds.
   */
  public boolean isPlainBind() {
    return m_plainBind;
  }

  /**
   * Override this method to intercept and change part instance properties such as values, operation type, etc.<br>
   * Sometimes it is convenient to set the operation to {@link DataModelConstants#OPERATOR_NONE} which uses the
   * attribute itself as the complete statement part.
   *
   * @param builder
   *          containging all binds and sql parts
   * @param attributeNodeData
   *          the form data object containing the runtime value {@link ComposerAttributeNodeData}
   * @param stm
   *          is either {@link #getSelectClause()} or {@link #getWhereClause()} depending on the strategy
   * @param bindNames
   *          by default the names "a", "b", "c", ... representing then field bindValues in the same order as the fields
   * @param bindValues
   *          the values of the {@link AbstractValueFieldData}s
   * @param parentAliasMap
   *          the map of meta-alias to alias for this entity, for example @Person@ -> p1
   * @param strategy
   *          is one of the {@link AttributeStrategy} enums and decides whether {@link #getSelectClause()} or
   *          {@link #getWhereClause()} is used
   * @return the result; empty {@link EntityContribution} if that part is to be ignored
   *         <p>
   *         default calls
   *         {@link FormDataStatementBuilder#createAttributePart(AttributeStrategy, Integer, String, int, List, List, boolean, Map)}
   *         <p>
   *         Can make use of alias markers such as @Person@.LAST_NAME, these are resolved in the
   *         {@link FormDataStatementBuilder}
   *         <p>
   *         Additional bind values - other than the parameter bindNames/bindValues - must be added using
   *         {@link FormDataStatementBuilder#addBind(String, Object)}
   */
  public EntityContribution createInstance(FormDataStatementBuilder builder, ComposerAttributeNodeData attributeNodeData, AttributeStrategy strategy, String stm, List<String> bindNames, List<Object> bindValues,
      Map<String, String> parentAliasMap) {
    return builder.createAttributePart(strategy, attributeNodeData.getAggregationType(), stm, attributeNodeData.getOperator(), bindNames, bindValues, this.isPlainBind(), parentAliasMap);
  }

}
