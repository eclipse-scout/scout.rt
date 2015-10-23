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
package org.eclipse.scout.commons.chain;

import java.util.concurrent.Callable;

/**
 * A <code>Decorator</code> is an object to be used in {@link InvocationChain} to decorate the execution of a
 * {@link Callable} with some <i>before</i>- and <i>post</i> actions, e.g. to log execution or to set some
 * Thread-Locals.
 *
 * @see IInvocationInterceptor
 * @since 5.2
 */
public interface IInvocationDecorator extends IChainable {

  /**
   * Method invoked prior to executing the {@link Callable}.
   */
  void onBefore();

  /**
   * Method invoked after executed the {@link Callable}, and is invoked regardless of success or failure.
   */
  void onAfter();
}
