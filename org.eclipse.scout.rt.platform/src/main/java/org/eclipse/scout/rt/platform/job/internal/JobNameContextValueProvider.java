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

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;
import org.slf4j.MDC;

/**
 * Provides the job name to be set into the <code>diagnostic context map</code> for logging purpose. This value provider
 * is expected to be invoked from within a job, meaning that {@link IFuture} is present.
 *
 * @see #KEY
 * @see DiagnosticContextValueProcessor
 * @see MDC
 */
@ApplicationScoped
public class JobNameContextValueProvider implements IDiagnosticContextValueProvider {

  public static final String KEY = "scout.job.name";

  @Override
  public String key() {
    return KEY;
  }

  @Override
  public String value() {
    IFuture<?> future = IFuture.CURRENT.get();
    return future != null && future.getJobInput() != null ? future.getJobInput().getName() : null;
  }
}
