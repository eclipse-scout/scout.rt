/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.lookup;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

/**
 * @since 3.8.1
 */
@Order(5100)
public class DefaultLookupCallProvisioningService implements ILookupCallProvisioningService {

  @Override
  public <T> ILookupCall<T> newClonedInstance(ILookupCall<T> templateCall, IProvisioningContext context) {
    return templateCall != null ? templateCall.copy() : null;
  }
}
