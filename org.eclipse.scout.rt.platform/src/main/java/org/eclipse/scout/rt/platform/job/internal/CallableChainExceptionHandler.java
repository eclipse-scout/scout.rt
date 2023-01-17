/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.internal;

import org.eclipse.scout.rt.platform.chain.callable.CallableChain.Chain;
import org.eclipse.scout.rt.platform.chain.callable.ICallableDecorator;
import org.eclipse.scout.rt.platform.chain.callable.ICallableInterceptor;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.concurrent.AbstractInterruptionError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This exception handler is only for infrastructure reasons. It handles exceptions occurring in
 * {@link ICallableDecorator} and {@link ICallableInterceptor} which are before {@link ExceptionProcessor} in the
 * callable chain. Exceptions handled by the {@link ExceptionProcessor} will not be handled of this handler.
 */
public class CallableChainExceptionHandler<RESULT> implements ICallableInterceptor<RESULT> {

  private static final Logger LOG = LoggerFactory.getLogger(CallableChainExceptionHandler.class);

  @Override
  public RESULT intercept(Chain<RESULT> chain) throws Exception {
    try {
      return chain.continueChain();
    }
    // do not handle exceptions twice.
    catch (CallableChainHandledException e) { // NOSONAR
      if (e.getCause() instanceof Error) {
        throw (Error) e.getCause();
      }
      else if (e.getCause() instanceof Exception) {
        throw (Exception) e.getCause();
      }
      throw e;
    }
    catch (AbstractInterruptionError | InterruptedException e) {
      LOG.debug("Exception in callable chain due to interruption.", e);
      throw e;
    }
    catch (Exception | Error e) { // NOSONAR
      if (Thread.currentThread().isInterrupted()) {
        LOG.debug("Exception in callable chain due to interruption.", e);
      }
      else if (e instanceof VetoException) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("{} in callable chain.", e.getClass().getSimpleName(), e);
        }
        else {
          LOG.info("{} in callable chain: {}", e.getClass().getSimpleName(), e.getMessage());
        }
      }
      else {
        LOG.warn("Exception in callable chain.", e);
      }
      throw e;
    }
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
