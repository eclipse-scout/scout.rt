/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
