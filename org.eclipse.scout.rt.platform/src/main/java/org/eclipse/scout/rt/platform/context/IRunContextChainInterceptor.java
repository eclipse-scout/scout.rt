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
package org.eclipse.scout.rt.platform.context;

import org.eclipse.scout.rt.platform.chain.callable.ICallableInterceptor;

/**
 * <h3>{@link IRunContextChainInterceptor}</h3><br>
 * A {@link IRunContextChainInterceptor} should always be produced of a {@link IRunContextChainInterceptorProducer} to
 * add a certain variable (e.g. ThreadLocal) to a {@link RunContext}.
 */
public interface IRunContextChainInterceptor<RESULT> extends ICallableInterceptor<RESULT> {

  /**
   * is called when from {@link RunContext#fillEmpty()} to apply default values to the current {@link RunContext}. This
   * method is called in the caller environment most likely a outer {@link RunContext}.
   */
  void fillEmtpy();

  /**
   * is called from {@link RunContext#fillCurrentValues()} to apply current environment variables to the current
   * {@link RunContext}. This method is called in the caller environment most likely a outer {@link RunContext}.
   */
  void fillCurrent();

}
