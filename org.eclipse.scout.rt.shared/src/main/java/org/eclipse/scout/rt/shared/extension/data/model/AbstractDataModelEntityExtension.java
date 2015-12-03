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
