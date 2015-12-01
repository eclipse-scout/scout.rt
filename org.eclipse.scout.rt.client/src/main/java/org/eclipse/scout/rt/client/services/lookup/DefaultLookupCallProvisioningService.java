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
package org.eclipse.scout.rt.client.services.lookup;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;

/**
 * @since 3.8.1
 */
@Order(5100)
public class DefaultLookupCallProvisioningService implements ILookupCallProvisioningService {

  @SuppressWarnings("unchecked")
  @Override
  public <T> ILookupCall<T> newClonedInstance(ILookupCall<T> templateCall, IProvisioningContext context) {
    if (templateCall instanceof LookupCall<?>) {
      return (ILookupCall<T>) ((LookupCall<?>) templateCall).clone();
    }
    return null;
  }

}
