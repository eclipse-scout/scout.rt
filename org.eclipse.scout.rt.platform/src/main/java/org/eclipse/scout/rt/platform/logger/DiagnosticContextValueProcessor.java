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
package org.eclipse.scout.rt.platform.logger;

import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.chain.callable.ICallableDecorator;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.slf4j.MDC;

/**
 * Processor to put a diagnostic context value into the current thread's diagnostic context map for the subsequent
 * sequence of actions.
 * <p>
 * Instances of this class are to be added to a {@link CallableChain} to participate in the execution of a
 * {@link Callable}.
 *
 * @see MDC
 * @since 5.1
 */
public class DiagnosticContextValueProcessor<RESULT> implements ICallableDecorator<RESULT> {

  private final IDiagnosticContextValueProvider m_mdcValueProvider;
  private final String m_mdcKey;

  public DiagnosticContextValueProcessor(final IDiagnosticContextValueProvider mdcValueProvider) {
    m_mdcValueProvider = mdcValueProvider;
    m_mdcKey = Assertions.assertNotNullOrEmpty(mdcValueProvider.key(), "MDC key must not be null");
  }

  @Override
  public IUndecorator<RESULT> decorate() throws Exception {
    final String originValue = MDC.get(m_mdcKey);
    MDC.put(m_mdcKey, m_mdcValueProvider.value());

    // Restore origin value upon completion of the command.
    return new IUndecorator<RESULT>() {

      @Override
      public void undecorate(final RESULT callableResult, final Throwable callableException) {
        if (originValue != null) {
          MDC.put(m_mdcKey, originValue);
        }
        else {
          MDC.remove(m_mdcKey);
        }
      }
    };
  }

  public String getMdcKey() {
    return m_mdcKey;
  }

  /**
   * This class provides a value to be set into the <code>diagnostic context map</code> for logging purpose.
   *
   * @see DiagnosticContextValueProcessor
   * @see MDC
   */
  public interface IDiagnosticContextValueProvider {

    /**
     * Returns the key for the {@link MDC value}.
     */
    String key();

    /**
     * Returns the value to be put into {@link MDC diagnostic context}.
     */
    String value();
  }
}
