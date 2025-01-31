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
import org.eclipse.scout.rt.shared.extension.AbstractSerializableExtension;
import org.eclipse.scout.rt.shared.extension.data.model.DataModelEntityChains.DataModelEntityInitEntityChain;

public abstract class AbstractDataModelEntityExtension<OWNER extends AbstractDataModelEntity> extends AbstractSerializableExtension<OWNER> implements IDataModelEntityExtension<OWNER> {
  private static final long serialVersionUID = 1L;

  public AbstractDataModelEntityExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execInitEntity(DataModelEntityInitEntityChain chain) {
    chain.execInitEntity();
  }
}
