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
import org.eclipse.scout.rt.client.opentelemetry.OpenTelemetryExtensionInstrumenterFactory.OpenTelemetryExtensionRequest;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.platform.BEANS;

import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;

public class OpenTelemetryFormHandlerExtension extends AbstractFormHandlerExtension<AbstractFormHandler> {

  private Instrumenter<OpenTelemetryExtensionRequest<AbstractFormHandler>, Void> m_instrumenter;

  public OpenTelemetryFormHandlerExtension(AbstractFormHandler owner) {
    super(owner);
    m_instrumenter = BEANS.get(OpenTelemetryExtensionInstrumenterFactory.class).createInstrumenter(
        getClass(),
        (request) -> request.getOwner().getForm().getTitle());
  }

  @Override
  public void execLoad(FormHandlerLoadChain chain) {
    new OpenTelemetryExtensionRequest<>(getOwner(), "execLoad")
        .wrapCall(() -> super.execLoad(chain), m_instrumenter);
  }

  @Override
  public void execPostLoad(FormHandlerPostLoadChain chain) {
    new OpenTelemetryExtensionRequest<>(getOwner(), "execPostLoad")
        .wrapCall(() -> super.execPostLoad(chain), m_instrumenter);
  }

  @Override
  public boolean execValidate(FormHandlerValidateChain chain) {
    return new OpenTelemetryExtensionRequest<>(getOwner(), "execValidate")
        .wrapCall(() -> super.execValidate(chain), m_instrumenter);
  }

  @Override
  public void execStore(FormHandlerStoreChain chain) {
    new OpenTelemetryExtensionRequest<>(getOwner(), "execStore")
        .wrapCall(() -> super.execStore(chain), m_instrumenter);
  }

  @Override
  public void execDiscard(FormHandlerDiscardChain chain) {
    new OpenTelemetryExtensionRequest<>(getOwner(), "execDiscard")
        .wrapCall(() -> super.execDiscard(chain), m_instrumenter);
  }
}
