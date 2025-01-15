/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.chain.callable;

import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.chain.IChainable;

/**
 * A <code>Decorator</code> is an object to be used in {@link CallableChain} to decorate the execution of a
 * {@link Callable} with some <i>before</i>- and <i>after</i> actions, e.g. to log execution or to set some
 * thread-locals.
 *
 * @since 5.2
 */
@FunctionalInterface
public interface ICallableDecorator extends IChainable {

  /**
   * Method invoked prior to executing the {@link Callable command}.
   *
   * @return the {@link IUndecorator} to be invoked after execution in order to revert decoration, or <code>null</code>
   * to do nothing upon return of the command.
   * @throws Exception
   *     throw exception to stop chain processing. The exception is propagated to the caller.
   */
  @SuppressWarnings("squid:S00112")
  IUndecorator decorate() throws Exception;

  /**
   * Undecorator to restore decoration.
   */
  @FunctionalInterface
  interface IUndecorator {

    /**
     * Method invoked after executed the {@link Callable}, and is invoked regardless of success or failure.
     */
    void undecorate();
  }
}
