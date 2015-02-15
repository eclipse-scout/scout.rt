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
 * This class provides an empty implementation for the methods described by the {@link IAsyncFuture} interface.
 * <p/>
 * Classes that wish to deal with {@link IAsyncFuture} can extend this class and override only the methods which they
 * are interested in.
 *
 * @since 5.1
 */
public class AsyncFuture<R> implements IAsyncFuture<R> {

  @Override
  public void onSuccess(final R result) {
    // NOOP by default.
  }

  @Override
  public void onError(final ProcessingException e) {
    // NOOP by default.
  }

  @Override
  public void onDone(final R result, final ProcessingException e) {
    // NOOP by default.
  }
}
