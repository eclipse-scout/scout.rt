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

import org.eclipse.scout.rt.client.extension.ui.form.AbstractFormHandlerExtension;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerDiscardChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerLoadChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerPostLoadChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerStoreChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerValidateChain;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.opentelemetry.ITracingHelper;

import io.opentelemetry.api.trace.Tracer;

public class TracingFormHandlerExtension extends AbstractFormHandlerExtension<AbstractFormHandler> {

  private Tracer m_tracer;

  public TracingFormHandlerExtension(AbstractFormHandler owner) {
    super(owner);
    m_tracer = BEANS.get(ITracingHelper.class).createTracer(TracingFormHandlerExtension.class);
  }

  @Override
  public void execLoad(FormHandlerLoadChain chain) {
    String name = getOwner().getClass().getSimpleName() + "#execLoad";
    BEANS.get(ITracingHelper.class).wrapInSpan(m_tracer, name, span -> {
      span.setAttribute("scout.client.formhandler.class", getOwner().getClass().getName());
      super.execLoad(chain);
    });
  }

  @Override
  public void execPostLoad(FormHandlerPostLoadChain chain) {
    String name = getOwner().getClass().getSimpleName() + "#execPostLoad";
    BEANS.get(ITracingHelper.class).wrapInSpan(m_tracer, name, span -> {
      span.setAttribute("scout.client.formhandler.class", getOwner().getClass().getName());
      super.execPostLoad(chain);
    });
  }

  @Override
  public boolean execValidate(FormHandlerValidateChain chain) {
    String name = getOwner().getClass().getSimpleName() + "#execValidate";
    return BEANS.get(ITracingHelper.class).wrapInSpan(m_tracer, name, span -> {
      span.setAttribute("scout.client.formhandler.class", getOwner().getClass().getName());
      return super.execValidate(chain);
    });
  }

  @Override
  public void execStore(FormHandlerStoreChain chain) {
    String name = getOwner().getClass().getSimpleName() + "#execStore";
    BEANS.get(ITracingHelper.class).wrapInSpan(m_tracer, name, span -> {
      span.setAttribute("scout.client.formhandler.class", getOwner().getClass().getName());
      super.execStore(chain);
    });
  }

  @Override
  public void execDiscard(FormHandlerDiscardChain chain) {
    String name = getOwner().getClass().getSimpleName() + "#execDiscard";
    BEANS.get(ITracingHelper.class).wrapInSpan(m_tracer, name, span -> {
      span.setAttribute("scout.client.formhandler.class", getOwner().getClass().getName());
      super.execDiscard(chain);
    });
  }
}
