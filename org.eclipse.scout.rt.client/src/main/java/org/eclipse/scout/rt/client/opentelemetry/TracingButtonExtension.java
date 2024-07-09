/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.opentelemetry;

import org.eclipse.scout.rt.client.extension.ui.form.fields.button.AbstractButtonExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.button.ButtonChains.ButtonClickActionChain;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.opentelemetry.ITracingHelper;

import io.opentelemetry.api.trace.Tracer;

public class TracingButtonExtension extends AbstractButtonExtension<AbstractButton> {

  private Tracer m_tracer;

  public TracingButtonExtension(AbstractButton owner) {
    super(owner);
    m_tracer = BEANS.get(ITracingHelper.class).createTracer(TracingButtonExtension.class);
  }


  @Override
  public void execClickAction(ButtonClickActionChain chain) {
    String name = getOwner().getClass().getSimpleName() + "#execClickAction";
    BEANS.get(ITracingHelper.class).wrapInSpan(m_tracer, name, span -> {
      span.setAttribute("scout.client.button.class", getOwner().getClass().getName());
      span.setAttribute("scout.client.button.text", getOwner().getLabel());
      super.execClickAction(chain);
    });
  }
}
