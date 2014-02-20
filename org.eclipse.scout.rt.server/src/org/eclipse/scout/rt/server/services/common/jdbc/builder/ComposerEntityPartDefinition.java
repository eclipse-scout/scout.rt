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
 * @deprecated use {@link DataModelEntityPartDefinition}. Will be removed in the M-Release
 */
@Deprecated
@SuppressWarnings("deprecation")
public class ComposerEntityPartDefinition extends DataModelEntityPartDefinition {

  public ComposerEntityPartDefinition(Class<? extends IDataModelEntity> entityType, String whereClause) {
    super(entityType, whereClause);
  }

  public ComposerEntityPartDefinition(Class<? extends IDataModelEntity> entityType, String whereClause, String selectClause) {
    super(entityType, whereClause, selectClause);
  }

  public String createNewInstance(FormDataStatementBuilder builder, ComposerEntityNodeData entityNodeData, Map<String, String> parentAliasMap) throws ProcessingException {
    return getWhereClause();
  }

}
