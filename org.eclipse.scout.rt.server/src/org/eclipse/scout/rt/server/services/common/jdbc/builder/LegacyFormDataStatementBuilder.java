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
import org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;

/**
 * @deprecated Will be removed in the 3.11.0 Release
 */
@Deprecated
@SuppressWarnings("deprecation")
public class LegacyFormDataStatementBuilder extends FormDataStatementBuilder {

  /**
   * @param sqlStyle
   */
  public LegacyFormDataStatementBuilder(ISqlStyle sqlStyle) {
    super(sqlStyle);
  }

  /**
   * @deprecated use {@link #getDataModelAttributePartDefinitions()}
   */
  @Deprecated
  public Map<Class<?>, DataModelAttributePartDefinition> getComposerAttributePartDefinitions() {
    return getDataModelAttributePartDefinitions();
  }

  /**
   * @deprecated use {@link #getDataModelEntityPartDefinitions()}
   */
  @Deprecated
  public Map<Class<?>, DataModelEntityPartDefinition> getComposerEntityPartDefinitions() {
    return getDataModelEntityPartDefinitions();
  }

  /**
   * @deprecated use {@link #setDataModelAttributeDefinition(Class, String)}
   */
  @Deprecated
  public void setComposerAttributeDefinition(Class<? extends IDataModelAttribute> attributeType, String sqlAttribute) {
    setDataModelAttributeDefinition(attributeType, sqlAttribute);
  }

  /**
   * @deprecated use {@link #setDataModelAttributeDefinition(Class, String, boolean)}
   */
  @Deprecated
  public void setComposerAttributeDefinition(Class<? extends IDataModelAttribute> attributeType, String sqlAttribute, boolean plainBind) {
    setDataModelAttributeDefinition(attributeType, sqlAttribute, plainBind);
  }

  /**
   * @deprecated use {@link #setDataModelAttributeDefinition(Class, String, String, boolean)}
   */
  @Deprecated
  public void setComposerAttributeDefinition(Class<? extends IDataModelAttribute> attributeType, String whereClause, String selectClause, boolean plainBind) {
    setDataModelAttributeDefinition(attributeType, whereClause, selectClause, plainBind);
  }

  /**
   * @deprecated use {@link #setDataModelAttributeDefinition(DataModelAttributePartDefinition)}
   */
  @Deprecated
  public void setComposerAttributeDefinition(ComposerAttributePartDefinition def) {
    setDataModelAttributeDefinition(def);
  }

  /**
   * @deprecated use {@link #setDataModelEntityDefinition(Class, String)}
   */
  @Deprecated
  public void setComposerEntityDefinition(Class<? extends IDataModelEntity> entityType, String whereClause) {
    setDataModelEntityDefinition(entityType, whereClause);
  }

  /**
   * @deprecated use {@link #setDataModelEntityDefinition(Class, String, String)}
   */
  @Deprecated
  public void setComposerEntityDefinition(Class<? extends IDataModelEntity> entityType, String whereClause, String selectClause) {
    setDataModelEntityDefinition(entityType, whereClause, selectClause);
  }

  /**
   * @deprecated use {@link #setDataModelEntityDefinition(DataModelEntityPartDefinition)}
   */
  @Deprecated
  public void setComposerEntityDefinition(ComposerEntityPartDefinition def) {
    setDataModelEntityDefinition(def);
  }

  /**
   * @deprecated use {@link #createAttributePart(AtributeStrategy,Integer, String, int, List, List, boolean, Map)}
   */
  @Deprecated
  public String createComposerAttributeStatementPart(final Integer aggregationType, String stm, final int operation, List<String> bindNames, List<Object> bindValues, final boolean plainBind, Map<String, String> parentAliasMap) throws ProcessingException {
    return createAttributePartSimple(AttributeStrategy.BuildConstraintOfAttributeWithContext, aggregationType, stm, operation, bindNames, bindValues, plainBind, parentAliasMap);
  }

  /**
   * @deprecated use {@link #createSqlPart(Integer, String, int, List, List, boolean, Map)}
   */
  @Deprecated
  public String createStatementPart(final Integer aggregationType, String sql, final int operation, List<String> bindNames, List<Object> bindValues, final boolean plainBind, Map<String, String> parentAliasMap) throws ProcessingException {
    return createSqlPart(aggregationType, sql, operation, bindNames, bindValues, plainBind, parentAliasMap);
  }

}
