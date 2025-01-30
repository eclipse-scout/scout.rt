/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.opentelemetry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import io.opentelemetry.instrumentation.api.instrumenter.ContextCustomizer;

public class SpanNamePropagationFromDownstream implements ContextCustomizer<HttpServletRequest> {

  private static final ContextKey<List<String>> NAME_CONTEXT_KEY = ContextKey.named("scout-opentelemetry-span-name-propagation-key");

  @Override
  public Context onStart(Context context, HttpServletRequest httpServletRequest, Attributes startAttributes) {
    return context.with(NAME_CONTEXT_KEY, new ArrayList<>(1));
  }

  public static void updateSpanName(String spanName) {
    List<String> o = Context.current().get(NAME_CONTEXT_KEY);
    if (CollectionUtility.hasElements(o)) {
      Span.current().updateName(spanName + " " + StringUtility.join(", ", o));
    }
  }

  public static void addNameToContext(Supplier<String> nameSupplier) {
    List<String> o = Context.current().get(NAME_CONTEXT_KEY);
    if (o != null) {
      o.add(nameSupplier.get());
    }
  }
}
