/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.beans.PropertyChangeListener;

import org.eclipse.scout.rt.client.services.lookup.ILookupCallResult;
import org.eclipse.scout.rt.client.services.lookup.IQueryParam;

public interface ISmartFieldLookupRowFetcher<LOOKUP_KEY> {

  String PROP_SEARCH_RESULT = "searchResult";

  void addPropertyChangeListener(PropertyChangeListener listener);

  void removePropertyChangeListener(PropertyChangeListener listener);

  void update(IQueryParam<LOOKUP_KEY> queryParam, boolean synchronous);

  ILookupCallResult<LOOKUP_KEY> getResult();
}
