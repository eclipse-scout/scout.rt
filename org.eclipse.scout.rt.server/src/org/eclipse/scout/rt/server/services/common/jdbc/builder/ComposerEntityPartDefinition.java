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
import org.eclipse.scout.rt.shared.data.form.fields.composer.AbstractComposerEntityData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerEntityNodeData;

/**
 * Definition of a entity-to-sql mapping for {@link AbstractComposerEntityData}
 */
public class ComposerEntityPartDefinition {
  private final String m_sql;
  private final Class<? extends AbstractComposerEntityData> m_entityType;

  public ComposerEntityPartDefinition(Class<? extends AbstractComposerEntityData> entityType, String sql) {
    m_entityType = entityType;
    m_sql = sql;
  }

  public String getSql() {
    return m_sql;
  }

  public Class<? extends AbstractComposerEntityData> getEntityType() {
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
    return getSql();
  }
}
