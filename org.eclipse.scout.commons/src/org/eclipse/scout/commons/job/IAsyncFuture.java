/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.job;

import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * Future to be notified upon completion of a computation.
 *
 * @param <R>
 *          type of the computation result.
 * @since 5.0
 */
public interface IAsyncFuture<R> {

  /**
   * Is called upon successful computation.
   *
   * @param result
   *          the computation result.
   */
  void onSuccess(R result);

  /**
   * Is called upon failed computation.
   *
   * @param e
   *          computation error.
   */
  void onError(ProcessingException e);

  /**
   * Is always called after {@link #onSuccess(Object)} or {@link #onError(ProcessingException)}.
   *
   * @param result
   *          the computation result; is <code>null</code> in case of an error or a <code>null</code>-result.
   * @param e
   *          the computation error; is <code>null</code> in case of success.
   */
  void onDone(R result, ProcessingException e);
}
