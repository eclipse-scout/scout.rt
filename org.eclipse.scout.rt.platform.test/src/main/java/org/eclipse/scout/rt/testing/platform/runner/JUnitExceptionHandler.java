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
package org.eclipse.scout.rt.testing.platform.runner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.testing.platform.runner.statement.ThrowHandledExceptionStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code ExceptionHandler} to not silently swallow exceptions during JUnit test execution. In
 * {@link ThrowHandledExceptionStatement}, the first handled exception will be re-thrown for JUnit assertion.
 * <p/>
 * Do not annotate this class with {@link Replace} because registered programmatically for the time of executing a test
 * in {@link PlatformTestRunner}.
 *
 * @see PlatformTestRunner
 */
@IgnoreBean
public class JUnitExceptionHandler extends ExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(JUnitExceptionHandler.class);

  private final List<Throwable> m_errors = Collections.synchronizedList(new ArrayList<Throwable>());

  @Override
  public void handle(final Throwable t) {
    if (t instanceof ProcessingException && ((ProcessingException) t).isConsumed()) {
      LOG.info("Exception will not be re-thrown for JUnit assertion because already consumed. [exception={}]", t.getMessage());
    }
    else {
      m_errors.add(t);
      if (m_errors.size() == 1) {
        LOG.info("Exception will be re-thrown for JUnit assertion. [exception={}]", t.getMessage());
      }
      else {
        LOG.info("Exception will not be re-thrown for JUnit assertion because another exception was already handled. [current exception={}, other exception={}]", t, m_errors.get(0).getMessage());
      }
    }
  }

  /**
   * @return the mutable list of collected errors. The list may by modified before {@link #throwOnError()} is called.
   */
  public List<Throwable> getErrors() {
    return m_errors;
  }

  /**
   * Throws the first exception handled by this {@code ExceptionHandler} and resets this handler.<br/>
   * This method call has no effect if no exception was handled.
   */
  public void throwOnError() throws Throwable {
    if (m_errors.isEmpty()) {
      return;
    }
    final Throwable throwable = m_errors.get(0);
    m_errors.clear();
    throw throwable;
  }
}
