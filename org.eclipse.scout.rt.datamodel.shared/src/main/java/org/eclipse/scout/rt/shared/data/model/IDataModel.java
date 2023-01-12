/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
