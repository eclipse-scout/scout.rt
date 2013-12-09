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
import org.eclipse.scout.rt.server.services.common.jdbc.builder.FormDataStatementBuilder.AttributeStrategy;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerAttributeNodeData;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;

/**
 * @deprecated use {@link DataModelAttributePartDefinition}. Will be removed in the 3.11.0 Release
 */
@Deprecated
public class ComposerAttributePartDefinition extends DataModelAttributePartDefinition {

  public ComposerAttributePartDefinition(Class<? extends IDataModelAttribute> attributeType, String whereClause, boolean plainBind) {
    super(attributeType, whereClause, plainBind);
  }

  public ComposerAttributePartDefinition(Class<? extends IDataModelAttribute> attributeType, String whereClause, String selectClause, boolean plainBind) {
    super(attributeType, whereClause, selectClause, plainBind);
  }

  public String createNewInstance(FormDataStatementBuilder builder, ComposerAttributeNodeData attributeNodeData, List<String> bindNames, List<Object> bindValues, Map<String, String> parentAliasMap) throws ProcessingException {
    return builder.createAttributePartSimple(AttributeStrategy.BuildConstraintOfAttributeWithContext, attributeNodeData.getAggregationType(), getWhereClause(), attributeNodeData.getOperator(), bindNames, bindValues, this.isPlainBind(), parentAliasMap);
  }

  /**
   * @deprecated use {@link #getWhereClause() }
   */
  @Deprecated
  public String getSqlAttribute() {
    return super.getWhereClause();
  }
}
