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
import org.eclipse.scout.rt.client.opentelemetry.OpenTelemetryExtensionInstrumenterFactory.OpenTelemetryExtensionRequest;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.platform.BEANS;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;

public class OpenTelemetryActionExtension extends AbstractActionExtension<AbstractAction> {
  private Instrumenter<OpenTelemetryExtensionRequest<AbstractAction>, Void> m_instrumenter;
  private static final AttributeKey<String> OWNER_ACTION_ID = AttributeKey.stringKey("scout.extension.owner.action.id");

  public OpenTelemetryActionExtension(AbstractAction owner) {
    super(owner);
    m_instrumenter = BEANS.get(OpenTelemetryExtensionInstrumenterFactory.class).createInstrumenter(
        getClass(),
        (request) -> request.getOwner().getText());
  }

  @Override
  public void execAction(ActionActionChain chain) {
    new OpenTelemetryExtensionRequest<>(getOwner(), "execAction")
        .withAdditionalAttributesProvider(attributes -> attributes.put(OWNER_ACTION_ID, getOwner().getActionId()))
        .wrapCall(() -> super.execAction(chain), m_instrumenter);
  }
}
