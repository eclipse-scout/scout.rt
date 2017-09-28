/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util.concurrent;

/**
 * Represents a {@link Runnable} to run a computation. Unlike {@link Runnable}, the run method is allowed to throw a
 * checked exception.
 *
 * @see Runnable
 * @see 5.1
 */
@FunctionalInterface
public interface IRunnable {

  /**
   * Computes a result, or throws an exception if unable to do so.
   */
  @SuppressWarnings("squid:S00112")
  void run() throws Exception;
}
