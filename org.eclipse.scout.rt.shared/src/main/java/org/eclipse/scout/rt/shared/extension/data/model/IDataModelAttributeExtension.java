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

import org.eclipse.scout.rt.shared.data.model.AbstractDataModelAttribute;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.data.model.DataModelAttributeChains.DataModelAttributeInitAttributeChain;
import org.eclipse.scout.rt.shared.extension.data.model.DataModelAttributeChains.DataModelAttributePrepareLookupChain;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

public interface IDataModelAttributeExtension<OWNER extends AbstractDataModelAttribute> extends IExtension<OWNER> {

  void execInitAttribute(DataModelAttributeInitAttributeChain chain);

  void execPrepareLookup(DataModelAttributePrepareLookupChain chain, ILookupCall<?> call);

}
