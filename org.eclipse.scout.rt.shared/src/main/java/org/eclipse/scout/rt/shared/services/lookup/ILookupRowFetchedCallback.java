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

import org.eclipse.scout.rt.platform.context.RunContext;

/**
 * Callback for asynchronous lookup row fetching.
 * <p>
 * Typically, the methods of this callback are not invoked from the submitting thread, meaning that the implementor is
 * responsible to handle the event in the proper {@link RunContext}.
 */
public interface ILookupRowFetchedCallback<T> {

  /**
   * Method invoked upon successful {@link ILookupRow} data fetching.
   *
   * @param rows
   *          the rows fetched.
   */
  void onSuccess(List<? extends ILookupRow<T>> rows);

  /**
   * Method invoked if {@link ILookupRow} data fetching failed.
   *
   * @param exception
   *          the exception occurred.
   */
  void onFailure(RuntimeException exception);
}
