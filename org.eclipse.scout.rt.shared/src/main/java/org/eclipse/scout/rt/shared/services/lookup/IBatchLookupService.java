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

public interface IBatchLookupService extends IService {

  /**
   * Lookup by performing a "key" filter and activating the {@code<key>} tags<br>
   * Batch processing
   */
  List<List<ILookupRow<?>>> getBatchDataByKey(BatchLookupCall call);

  /**
   * Lookup by performing a "text" filter and activating the {@code<text>} tags<br>
   * Batch processing
   */
  List<List<ILookupRow<?>>> getBatchDataByText(BatchLookupCall call);

  /**
   * Lookup by performing a "all" filter and activating the {@code<all>} tags<br>
   * Batch processing
   */
  List<List<ILookupRow<?>>> getBatchDataByAll(BatchLookupCall call);

  /**
   * Lookup by performing a "recursion" filter and activating the {@code<rec>} tags<br>
   * Batch processing
   */
  List<List<ILookupRow<?>>> getBatchDataByRec(BatchLookupCall call);
}
