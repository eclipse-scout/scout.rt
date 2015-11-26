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
package org.eclipse.scout.rt.server.jdbc.builder;

import java.util.Map;

import org.eclipse.scout.rt.server.jdbc.builder.FormDataStatementBuilder.EntityStrategy;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerEntityNodeData;
import org.eclipse.scout.rt.shared.data.model.DataModelConstants;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;

/**
 * Definition of a entity-to-sql mapping for {@link IDataModelEntity}
 */
public class DataModelEntityPartDefinition implements DataModelConstants {
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
   *        EXISTS (
   *          SELECT 1
   *          FROM ... &lt;fromParts/&gt;
   *          WHERE 1=1 &lt;whereParts/&gt;
   *          &lt;groupBy&gt;
   *            GROUP BY &lt;groupByParts/&gt;
   *            HAVING 1=1 &lt;havingParts/&gt;
   *          &lt;/groupBy&gt;
   *        )
   *          </pre>
   *          <p>
   *          whereParts and havingParts is replaced in the build process by the corresponding attributes with/without
   *          aggregation.
   * @param selectClause
   *          is the clause to add n attributes of an entity to the base entity. This parameter is the join clause used
   *          to join the additional entity to the base entity in the select part. It must have the form (SELECT
   *          <selectParts/> FROM ... ) or a similar form containing a <column/> tag and
   *          a @parent.TableAlias@.PRIMARY_KEY_ID
   *          <p>
   *          Example for a Person-Contact-Entity-join select-clause:
   *
   *          <pre>
   * SELECT &lt;selectParts/&gt;
   * FROM MY_CONTACT @Contact@ &lt;fromParts/&gt;
   * WHERE @Contact@.PERSON_FK=@parent.Person@.PERSON_ID
   * &lt;whereParts/&gt;
   * &lt;groupBy&gt;
   *  GROUP BY &lt;groupByParts/&gt;
   *   HAVING 1=1 &lt;havingParts/&gt;
   *  &lt;/groupBy&gt;
   *          </pre>
   *          <p>
   */
  public DataModelEntityPartDefinition(Class<? extends IDataModelEntity> entityType, String whereClause, String selectClause) {
    m_entityType = entityType;
    m_whereClause = autoCompleteTags(whereClause);
    m_selectClause = autoCompleteTags(selectClause);
    check();
  }

  protected String autoCompleteTags(String s) {
    return s;
  }

  /**
   * Override to do customized checks on select and where clause or to deactivate default checks.
   * <p>
   * Called by the constructor after calling {@link #autoCompleteTags(String)} and setting its member fields
   */
  protected void check() {
    /*
    if (m_whereClause != null) {
      for (String tag : new String[]{"whereParts", "groupBy", "groupByParts", "havingParts"}) {
        if (StringUtility.getTag(m_whereClause, tag) == null) {
          throw new IllegalArgumentException("whereClause must contain a " + tag + " tag");
        }
      }
    }
    if (m_selectClause != null) {
      for (String tag : new String[]{"selectParts", "fromParts", "whereParts", "groupBy", "groupByParts", "havingParts"}) {
        if (StringUtility.getTag(m_selectClause, tag) == null) {
          throw new IllegalArgumentException("selectClause must contain a " + tag + " tag");
        }
      }
    }
     */
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
   * @param strategy
   *          is one of the {@link EntityStrategy} enums and decides whether {@link #getSelectClause()} or
   *          {@link #getWhereClause()} is used
   * @param stm
   *          is either {@link #getSelectClause()} or {@link #getWhereClause()} depending on the strategy
   * @param parentAliasMap
   *          the map of meta-alias to alias for this entity, for example @Person@ -> p1
   * @return the result sql text; null if that part is to be ignored
   *         <p>
   *         default just returns the incoming stm (either {@link #getSelectClause()} or {@link #getWhereClause()})
   */
  public String createInstance(FormDataStatementBuilder builder, ComposerEntityNodeData entityNodeData, EntityStrategy strategy, String stm, Map<String, String> parentAliasMap) {
    return stm;
  }
}
