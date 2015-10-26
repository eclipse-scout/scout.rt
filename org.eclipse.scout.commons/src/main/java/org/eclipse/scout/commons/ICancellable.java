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
package org.eclipse.scout.commons;

/**
 * This is any kind of object interested in active cancellation.
 *
 * @since 5.1
 */
public interface ICancellable {

  /**
   * Attempts to cancel the execution of this {@link ICancellable}.
   *
   * @param interruptIfRunning
   *          <code>true</code> if the thread executing this {@link ICancellable} should be interrupted.
   * @return <code>false</code> if cancellation failed, typically because it has already cancelled or completed.
   */
  boolean cancel(boolean interruptIfRunning);

  /**
   * @return <code>true</code> if this {@link ICancellable} was cancelled.
   */
  boolean isCancelled();
}
