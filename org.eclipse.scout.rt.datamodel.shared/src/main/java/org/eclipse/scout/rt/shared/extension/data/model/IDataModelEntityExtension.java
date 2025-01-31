/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension.data.model;

import org.eclipse.scout.rt.shared.data.model.AbstractDataModelEntity;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.data.model.DataModelEntityChains.DataModelEntityInitEntityChain;

public interface IDataModelEntityExtension<OWNER extends AbstractDataModelEntity> extends IExtension<OWNER> {

  void execInitEntity(DataModelEntityInitEntityChain chain);
}
