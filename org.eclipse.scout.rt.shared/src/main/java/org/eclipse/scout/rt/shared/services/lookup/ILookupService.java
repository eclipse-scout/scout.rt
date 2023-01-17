/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.lookup;

import java.util.List;

import org.eclipse.scout.rt.platform.service.IService;

/**
 * Service for querying data in content-assist fields/smartfields.
 */
public interface ILookupService<KEY_TYPE> extends IService {

  /**
   * Lookup by key using {@link ILookupCall#getKey()}
   */
  List<? extends ILookupRow<KEY_TYPE>> getDataByKey(ILookupCall<KEY_TYPE> call);

  /**
   * Lookup by text using {@link ILookupCall#getText()}
   */
  List<? extends ILookupRow<KEY_TYPE>> getDataByText(ILookupCall<KEY_TYPE> call);

  /**
   * Lookup all rows using the browse hint{@link ILookupCall#getAll()}
   */
  List<? extends ILookupRow<KEY_TYPE>> getDataByAll(ILookupCall<KEY_TYPE> call);

  /**
   * Lookup child rows by parent for lazy loading hierarchical data, using {@link ILookupCall#getRec()} (parent)
   */
  List<? extends ILookupRow<KEY_TYPE>> getDataByRec(ILookupCall<KEY_TYPE> call);
}
