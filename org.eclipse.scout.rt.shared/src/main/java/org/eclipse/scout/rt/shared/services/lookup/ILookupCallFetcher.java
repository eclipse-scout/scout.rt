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

import org.eclipse.scout.rt.platform.exception.ProcessingException;

/**
 * Asynchronous LookupCall fetcher.<br>
 * Used to get lookup data from a backend service using a background thread.
 * <p>
 * It calls for example {@link LookupCall#getDataByTextInBackground(ILookupCallFetcher)} in the normal model thread and
 * passes his callback as an argument. The framework is then loading the data in the background.<br>
 * Once data is loaded, the callback method {@link ILookupCallFetcher#dataFetched(LookupRow[], ProcessingException)} is
 * called back with either failed==null which signals successful processing or failed!=null which signals a failure.
 */
public interface ILookupCallFetcher<T> {

  /**
   * This method may be called in a background thread out of the scout session context
   *
   * @param rows
   *          that were fetched from the data provider
   * @param failed
   *          null if ok, not null if any error occured during fetch
   */
  void dataFetched(List<? extends ILookupRow<T>> rows, RuntimeException failed);

}
