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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerEntityNodeData;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;

/**
 * Definition of a entity-to-sql mapping for {@link IDataModelEntity}
 */
public class ComposerEntityPartDefinition {
  private final Class<? extends IDataModelEntity> m_entityType;
  private final String m_whereClause;
  private final String m_selectClause;

  /**
   * @param whereClause
   *          is the clause used to find data by constraining the where clause. This is normally something like EXISTS (
   *          SELECT 1 FROM
   *          <p>
   *          This adds a default selectClause iff it has the exact form EXISTS ( SELECT 1 FROM ...)
   *          <p>
   *          Then the EXISTS is omitted and the 1 is replaced by &lt;column/&gt;.
   */
  public ComposerEntityPartDefinition(Class<? extends IDataModelEntity> entityType, String whereClause) {
    m_entityType = entityType;
    m_whereClause = whereClause;
    String selectClause = null;
    if (whereClause != null) {
      Matcher m = Pattern.compile("EXISTS\\s*\\(\\s*SELECT\\s+1\\s+FROM").matcher(whereClause.toUpperCase());
      if (m.find() && m.start() == 0) {
        String template = "( SELECT <column/> FROM " + whereClause.substring(m.end()).trim();
        template = StringUtility.removeTag(template, "groupBy");
        selectClause = template;
      }
    }
    m_selectClause = selectClause;
  }

  /**
   * @param whereClause
   *          is the clause used to find data by constraining the where clause. This is normally something like EXISTS (
   *          SELECT 1...
   * @param selectClause
   *          is the clause to add n attributes of an entity to the base entity.
   *          This parameter is the join clause used to join the additional entity to the
   *          base entity in the select part. It must have the form (SELECT <column/> FROM ... ) or a similar form
   *          containing a <column/> tag.
   */
  public ComposerEntityPartDefinition(Class<? extends IDataModelEntity> entityType, String whereClause, String selectClause) {
    m_entityType = entityType;
    m_whereClause = whereClause;
    if (selectClause == null || selectClause.indexOf("<column/>") >= 0) {
      m_selectClause = selectClause;
    }
    else {
      throw new IllegalArgumentException("select clause must be of form (SELECT <column/> FROM ... ) or a similar that contains a <column/> tag");
    }
  }

  public String getSelectClause() {
    return m_selectClause;
  }

  public String getWhereClause() {
    return m_whereClause;
  }

  public Class<? extends IDataModelEntity> getEntityType() {
    return m_entityType;
  }

  /**
   * Override this method to intercept and change the sql fragment that is created by this entity.<br>
   * Sometimes it is convenient to have dynamic joins depending on what attributes are contained within the
   * {@link ComposerEntityNodeData#getContainingAttributeNodes()}.
   * 
   * @param builder
   *          containging all binds and sql parts
   * @param entityNodeData
   *          the form data object containing the runtime value {@link ComposerEntityNodeData}
   * @param parentAliasMap
   *          the map of meta-alias to alias for this entity, for example @Person@ -> p1
   * @return the result sql text; null if that part is to be ignored
   *         <p>
   *         default just returns {@link #getSql()}
   * @throws ProcessingException
   */
  public String createNewInstance(FormDataStatementBuilder builder, ComposerEntityNodeData entityNodeData, Map<String, String> parentAliasMap) throws ProcessingException {
    return getWhereClause();
  }
}
