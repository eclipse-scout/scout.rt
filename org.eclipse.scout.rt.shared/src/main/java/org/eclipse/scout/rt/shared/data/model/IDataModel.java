/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.data.model;

import java.util.List;

public interface IDataModel {

  /**
   * call init before using the data model structure
   */
  void init();

  List<IDataModelAttribute> getAttributes();

  IDataModelAttribute getAttribute(Class<? extends IDataModelAttribute> attributeClazz);

  List<IDataModelEntity> getEntities();

  IDataModelEntity getEntity(Class<? extends IDataModelEntity> entityClazz);
}
