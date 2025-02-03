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

import org.eclipse.scout.rt.shared.data.model.AbstractDataModelAttribute;
import org.eclipse.scout.rt.shared.extension.AbstractSerializableExtension;
import org.eclipse.scout.rt.shared.extension.data.model.DataModelAttributeChains.DataModelAttributeInitAttributeChain;
import org.eclipse.scout.rt.shared.extension.data.model.DataModelAttributeChains.DataModelAttributePrepareLookupChain;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

public abstract class AbstractDataModelAttributeExtension<OWNER extends AbstractDataModelAttribute> extends AbstractSerializableExtension<OWNER> implements IDataModelAttributeExtension<OWNER> {
  private static final long serialVersionUID = 1L;

  public AbstractDataModelAttributeExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execInitAttribute(DataModelAttributeInitAttributeChain chain) {
    chain.execInitAttribute();
  }

  @Override
  public void execPrepareLookup(DataModelAttributePrepareLookupChain chain, ILookupCall<?> call) {
    chain.execPrepareLookup(call);
  }
}
