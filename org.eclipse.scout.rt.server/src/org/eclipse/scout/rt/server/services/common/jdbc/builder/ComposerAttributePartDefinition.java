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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerAttributeNodeData;
import org.eclipse.scout.rt.shared.data.model.DataModelConstants;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;

/**
 * Definition of a attribute-to-sql mapping for {@link IDataModelAttribute}
 */
public class ComposerAttributePartDefinition {
  private final String m_whereClause;
  private final String m_selectClause;
  private final boolean m_plainBind;
  private final Class<? extends IDataModelAttribute> m_attributeType;

  /**
   * @param whereClause
   *          is normally something like @Person@.LAST_NAME or in a special case EXISTS(...<attribute>...)
   */
  public ComposerAttributePartDefinition(Class<? extends IDataModelAttribute> attributeType, String whereClause, boolean plainBind) {
    this(attributeType, whereClause, whereClause, plainBind);
  }

  /**
   * @param whereClause
   *          is normally something like @Person@.LAST_NAME or in a special case EXISTS(...<attribute>...)
   * @param selectClause
   *          is by default the same as the where clause, but sometimes it is necessary to have a different select
   *          clause than the where clause.
   */
  public ComposerAttributePartDefinition(Class<? extends IDataModelAttribute> attributeType, String whereClause, String selectClause, boolean plainBind) {
    m_attributeType = attributeType;
    m_whereClause = whereClause;
    m_selectClause = selectClause;
    m_plainBind = plainBind;
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
   * @return the result sql text; null if that part is to be ignored
   *         <p>
   *         normally calls
   *         {@link FormDataStatementBuilder#createComposerAttributeStatementPart(Integer, String, int, String[], Object[], boolean, Map)}
   *         <p>
   *         Can make use of alias markers such as @Person@.LAST_NAME, these are resolved in the
   *         {@link FormDataStatementBuilder}
   *         <p>
   *         Additional bind values - other than the parameter values - must be added using
   *         {@link FormDataStatementBuilder#addBind(String, Object)}
   * @throws ProcessingException
   */
  public String createNewInstance(FormDataStatementBuilder builder, ComposerAttributeNodeData attributeNodeData, List<String> bindNames, List<Object> bindValues, Map<String, String> parentAliasMap) throws ProcessingException {
    return builder.createComposerAttributeStatementPart(attributeNodeData.getAggregationType(), this.getWhereClause(), attributeNodeData.getOperator(), bindNames, bindValues, this.isPlainBind(), parentAliasMap);
  }
}
