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

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerAttributeNodeData;
import org.eclipse.scout.rt.shared.data.model.DataModelConstants;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;

/**
 * Definition of a attribute-to-sql mapping for {@link IDataModelAttribute}
 */
public class DataModelAttributePartDefinition {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DataModelAttributePartDefinition.class);

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
   *          is normally something like @Person@.LAST_NAME or in a special case EXISTS(...<attribute>...)
   *          <p>
   *          When using a non-trivial attribute (containing the attribute tag) make sure to separate the non-attribute
   *          part using the wherePart tag. For example
   * 
   *          <pre>
   * &lt;wherePart&gt;@Person@.LAST_NAME IS NOT NULL&lt;/wherePart&gt; &lt;attribute&gt;@Person@.LAST_NAME&lt;/attribute&gt;
   * </pre>
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
    //99%-safe check of correct usage of wherePart on non-trivial attributes
    if (m_whereClause != null) {
      String low = m_whereClause.toLowerCase().replaceAll("\\s", " ");
      if (low.indexOf("<attribute") >= 0 && low.indexOf("<wherepart") < 0 && low.indexOf(" and ") >= 0) {
        LOG.warn(attributeType.getName() + " is a non-trivial attribute and should have the form <wherePart>... AND ...</wherePart> <attribute>...</attribute>: " + m_whereClause);
        //XXX
        String s = StringUtility.removeTag(m_whereClause, "attribute").trim();
        if (s.startsWith("AND ")) s = s.substring(4).trim();
        if (s.endsWith(" AND")) s = s.substring(0, s.length() - 4).trim();
        String t = StringUtility.getTag(m_whereClause, "attribute");
        System.out.println(attributeType.getSimpleName() + ": <wherePart>" + s + "</wherePart> <attribute>" + t + "</attribute>");
      }
    }
  }

  private static String autoCreateSelectClause(String whereClause) {
    if (whereClause == null) {
      return null;
    }
    if (Pattern.compile("[^a-zA-Z_$]SELECT[^a-zA-Z_$]").matcher(whereClause).find()) {
      return null;
    }
    return whereClause;
  }

  /**
   * @deprecated use {@link #getWhereClause()} instead
   */
  @Deprecated
  public String getSqlAttribute() {
    return m_whereClause;
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
   * attribute
   * itself as the complete statement part.
   * 
   * @param builder
   *          containging all binds and sql parts
   * @param attributeNodeData
   *          the form data object containing the runtime value {@link ComposerAttributeNodeData}
   * @param bindNames
   *          by default the names "a", "b", "c", ... representing then field bindValues in the same order as the fields
   * @param bindValues
   *          the values of the {@link AbstractValueFieldData}s
   * @param parentAliasMap
   *          the map of meta-alias to alias for this entity, for example @Person@ -> p1
   * @param includeWherePartTag
   *          applied to attributes only: some attributes in the having section contain wherePart tags that belong to
   *          the where section and not the having section.
   *          <p>
   *          when adding WHERE parts from attributes without aggregation then includeWherePartTag=true,
   *          includeAttributeTag=true
   *          <p>
   *          when adding WHERE parts from attributes with aggregation then includeWherePartTag=true,
   *          includeAttributeTag=false
   *          <p>
   *          when adding HAVING parts from attributes with aggregation then includeWherePartTag=false,
   *          includeAttributeTag=true
   * @param includeAttributeTag
   *          see includeWherePartTag
   * @return the result sql text; null if that part is to be ignored
   *         <p>
   *         normally calls
   *         {@link FormDataStatementBuilder#createComposerAttributeStatementPart(Integer, String, int, List, List, boolean, Map, boolean, boolean)}
   *         <p>
   *         Can make use of alias markers such as @Person@.LAST_NAME, these are resolved in the
   *         {@link FormDataStatementBuilder}
   *         <p>
   *         Additional bind values - other than the parameter values - must be added using
   *         {@link FormDataStatementBuilder#addBind(String, Object)}
   * @throws ProcessingException
   */
  public String createNewInstance(FormDataStatementBuilder builder, ComposerAttributeNodeData attributeNodeData, List<String> bindNames, List<Object> bindValues, Map<String, String> parentAliasMap, boolean includeWherePartTag, boolean includeAttributeTag) throws ProcessingException {
    return builder.createComposerAttributeStatementPart(attributeNodeData.getAggregationType(), this.getWhereClause(), attributeNodeData.getOperator(), bindNames, bindValues, this.isPlainBind(), parentAliasMap, includeWherePartTag, includeAttributeTag);
  }

  /**
   * @deprecated override
   *             {@link #createNewInstance(FormDataStatementBuilder, ComposerAttributeNodeData, List, List, Map, boolean, boolean)}
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  public String createNewInstance(FormDataStatementBuilder builder, ComposerAttributeNodeData attributeNodeData, List<String> bindNames, List<Object> bindValues, Map<String, String> parentAliasMap) throws ProcessingException {
    //ensure backward compatibility
    return builder.createComposerAttributeStatementPart(attributeNodeData.getAggregationType(), this.getWhereClause(), attributeNodeData.getOperator(), bindNames, bindValues, this.isPlainBind(), parentAliasMap);
  }

}
