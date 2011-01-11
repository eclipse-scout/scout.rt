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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerEntityNodeData;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;

/**
 * Definition of a entity-to-sql mapping for {@link IDataModelEntity}
 */
public class DataModelEntityPartDefinition {
  private final Class<? extends IDataModelEntity> m_entityType;
  private final String m_whereClause;
  private final String m_selectClause;

  /**
   * see {@link #DataModelEntityPartDefinition(Class, String, String)}
   */
  public DataModelEntityPartDefinition(Class<? extends IDataModelEntity> entityType, String whereClause) {
    this(entityType, whereClause, null);
  }

  /**
   * @param whereClause
   *          is the clause used to find data by constraining the where clause. This is normally something like
   * 
   *          <pre>
   * EXISTS (   *          SELECT 1 FROM ... &lt;whereParts/&gt; ... &lt;groupBy&gt; ... &lt;havingParts/&gt; ... &lt;/groupBy&gt;)
   * </pre>
   *          <p>
   *          whereParts and havingParts is replaced in the build process by the corresponding attributes with/without
   *          aggregation.
   * @param selectClause
   *          is the clause to add n attributes of an entity to the base entity.
   *          This parameter is the join clause used to join the additional entity to the
   *          base entity in the select part. It must have the form (SELECT <column/> FROM ... ) or a similar form
   *          containing a <column/> tag and a @parent.TableAlias@.PRIMARY_KEY_ID
   *          <p>
   *          Example for a Person-Contact-Entity-join where-clause:
   * 
   *          <pre>
   * @parent.Person@.PERSON_ID!=0
   * AND EXISTS (
   *   SELECT 1 " +
   *   FROM MY_CONTACT @Contact@
   *   WHERE @Contact@.PERSON_ID=@parent.Person@.PERSON_ID
   *   &lt;whereParts/&gt;
   *   &lt;groupBy&gt;
   *     GROUP BY @parent.Person@.PERSON_ID
   *     HAVING 1=1
   *     &lt;havingParts/&gt;
   *   &lt;/groupBy&gt;
   * )
   * </pre>
   *          <p>
   *          Example for a Person-Contact-Entity-join select-clause:
   * 
   *          <pre>
   * SELECT &lt;column/&gt;
   * FROM MY_CONTACT @Contact@
   * WHERE @Contact@.PERSON_FK=@parent.Person@.PERSON_ID
   * &lt;whereParts/&gt;
   * &lt;groupBy/&gt;
   * </pre>
   *          <p>
   */
  public DataModelEntityPartDefinition(Class<? extends IDataModelEntity> entityType, String whereClause, String selectClause) {
    m_entityType = entityType;
    m_whereClause = whereClause;
    if (selectClause == null || selectClause.indexOf("<column/>") >= 0) {
      m_selectClause = selectClause;
    }
    else {
      throw new IllegalArgumentException("select clause must be of form SELECT <column/> FROM ... or a similar that contains a <column/> tag");
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
