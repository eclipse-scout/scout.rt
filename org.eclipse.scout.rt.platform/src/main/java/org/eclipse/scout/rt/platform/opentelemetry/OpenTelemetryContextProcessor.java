/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.opentelemetry;

import org.eclipse.scout.rt.platform.chain.callable.CallableChain.Chain;
import org.eclipse.scout.rt.platform.chain.callable.ICallableInterceptor;

import io.opentelemetry.context.Context;

public class OpenTelemetryContextProcessor<RESULT> implements ICallableInterceptor<RESULT> {

  protected final Context m_openTelemetryContext;

  public OpenTelemetryContextProcessor(final Context openTelemetryContext) {
    m_openTelemetryContext = openTelemetryContext;
  }

  @Override
  public RESULT intercept(Chain<RESULT> chain) throws Exception {
    if (m_openTelemetryContext != null) {
      return m_openTelemetryContext.wrap(() -> chain.continueChain()).call();
    }
    else {
      return chain.continueChain();
    }
  }

  @Override
  public boolean isEnabled() {
    return m_openTelemetryContext != null;
  }
}
