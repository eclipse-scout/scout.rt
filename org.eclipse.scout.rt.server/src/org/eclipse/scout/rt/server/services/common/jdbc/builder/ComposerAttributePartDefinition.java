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

import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;

/**
 * @deprecated use {@link DataModelAttributePartDefinition}
 */
@Deprecated
public class ComposerAttributePartDefinition extends DataModelAttributePartDefinition {

  public ComposerAttributePartDefinition(Class<? extends IDataModelAttribute> attributeType, String whereClause, boolean plainBind) {
    super(attributeType, whereClause, plainBind);
  }

  public ComposerAttributePartDefinition(Class<? extends IDataModelAttribute> attributeType, String whereClause, String selectClause, boolean plainBind) {
    super(attributeType, whereClause, selectClause, plainBind);
  }
}
