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

import java.util.List;

import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public interface ILookupCallResult<LOOKUP_KEY> {

  IQueryParam<LOOKUP_KEY> getQueryParam();

  Throwable getException();

  List<ILookupRow<LOOKUP_KEY>> getLookupRows();
}
