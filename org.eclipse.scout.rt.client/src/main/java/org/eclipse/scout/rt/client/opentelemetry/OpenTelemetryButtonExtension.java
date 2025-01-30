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
import org.eclipse.scout.rt.client.opentelemetry.OpenTelemetryExtensionInstrumenterFactory.OpenTelemetryExtensionRequest;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.platform.BEANS;

import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;

public class OpenTelemetryButtonExtension extends AbstractButtonExtension<AbstractButton> {

  private Instrumenter<OpenTelemetryExtensionRequest<AbstractButton>, Void> m_instrumenter;

  public OpenTelemetryButtonExtension(AbstractButton owner) {
    super(owner);
    m_instrumenter = BEANS.get(OpenTelemetryExtensionInstrumenterFactory.class).createInstrumenter(
        getClass(),
        (request) -> request.getOwner().getLabel());
  }

  @Override
  public void execClickAction(ButtonClickActionChain chain) {
    new OpenTelemetryExtensionRequest<>(getOwner(), "execClickAction")
        .wrapCall(() -> super.execClickAction(chain), m_instrumenter);
  }
}
