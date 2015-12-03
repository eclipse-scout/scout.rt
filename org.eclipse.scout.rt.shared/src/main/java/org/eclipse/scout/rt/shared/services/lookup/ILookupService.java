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
package org.eclipse.scout.rt.shared.services.lookup;

import java.util.List;

import org.eclipse.scout.rt.platform.service.IService;

public interface ILookupService<KEY_TYPE> extends IService {

  /**
   * Lookup by performing a "key" filter and activating the <key> tags
   */
  List<? extends ILookupRow<KEY_TYPE>> getDataByKey(ILookupCall<KEY_TYPE> call);

  /**
   * Lookup by performing a "text" filter and activating the <text> tags
   */
  List<? extends ILookupRow<KEY_TYPE>> getDataByText(ILookupCall<KEY_TYPE> call);

  /**
   * Lookup by performing a "all" filter and activating the <all> tags
   */
  List<? extends ILookupRow<KEY_TYPE>> getDataByAll(ILookupCall<KEY_TYPE> call);

  /**
   * Lookup by performing a "recursion" filter and activating the <rec> tags
   */
  List<? extends ILookupRow<KEY_TYPE>> getDataByRec(ILookupCall<KEY_TYPE> call);
}
