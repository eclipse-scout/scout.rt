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
   *     the rows fetched.
   */
  void onSuccess(List<? extends ILookupRow<T>> rows);

  /**
   * Method invoked if {@link ILookupRow} data fetching failed.
   *
   * @param exception
   *     the exception occurred.
   */
  void onFailure(RuntimeException exception);
}
