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

import org.eclipse.scout.rt.platform.chain.callable.ICallableDecorator;
import org.eclipse.scout.rt.platform.context.RunContext;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

public class OpenTelemetryContextProcessor implements ICallableDecorator {

  public static final IUndecorator NOOP = () -> {};

  public OpenTelemetryContextProcessor() {
  }

  @SuppressWarnings("resource")
  @Override
  public IUndecorator decorate() throws Exception {
    Context openTelemetryContext = RunContext.CURRENT.get().getOpenTelemetryContext();
    if (openTelemetryContext == null) {
      return NOOP;
    }
    Scope scope = openTelemetryContext.makeCurrent();

    return scope::close;
  }
}
