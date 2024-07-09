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

import org.eclipse.scout.rt.client.extension.ui.action.AbstractActionExtension;
import org.eclipse.scout.rt.client.extension.ui.action.ActionChains.ActionActionChain;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.opentelemetry.ITracingHelper;

import io.opentelemetry.api.trace.Tracer;

public class TracingActionExtension extends AbstractActionExtension<AbstractAction> {

  private Tracer m_tracer;

  public TracingActionExtension(AbstractAction owner) {
    super(owner);
    m_tracer = BEANS.get(ITracingHelper.class).createTracer(TracingActionExtension.class);
  }

  @Override
  public void execAction(ActionActionChain chain) {
    String name = getOwner().getClass().getSimpleName() + "#execAction";
    BEANS.get(ITracingHelper.class).wrapInSpan(m_tracer, name, span -> {
      span.setAttribute("scout.client.action.id", getOwner().getActionId());
      span.setAttribute("scout.client.action.class", getOwner().getClass().getName());
      span.setAttribute("scout.client.action.text", getOwner().getText());
      super.execAction(chain);
    });
  }
}
