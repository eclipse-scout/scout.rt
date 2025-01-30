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

import org.eclipse.scout.rt.client.extension.ui.wizard.AbstractWizardExtension;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardCancelChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardFinishChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardNextStepChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardPostStartChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardPreviousStepChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardStartChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardSuspendChain;
import org.eclipse.scout.rt.client.opentelemetry.OpenTelemetryExtensionInstrumenterFactory.OpenTelemetryExtensionRequest;
import org.eclipse.scout.rt.client.ui.wizard.AbstractWizard;
import org.eclipse.scout.rt.platform.BEANS;

import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;

public class OpenTelemetryWizardExtension extends AbstractWizardExtension<AbstractWizard> {

  private Instrumenter<OpenTelemetryExtensionRequest<AbstractWizard>, Void> m_instrumenter;

  public OpenTelemetryWizardExtension(AbstractWizard owner) {
    super(owner);
    m_instrumenter = BEANS.get(OpenTelemetryExtensionInstrumenterFactory.class).createInstrumenter(
        getClass(),
        (request) -> request.getOwner().getTitle());
  }

  @Override
  public void execStart(WizardStartChain chain) {
    new OpenTelemetryExtensionRequest<>(getOwner(), "execStart")
        .wrapCall(() -> super.execStart(chain), m_instrumenter);
  }

  @Override
  public void execPostStart(WizardPostStartChain chain) {
    new OpenTelemetryExtensionRequest<>(getOwner(), "execPostStart")
        .wrapCall(() -> super.execPostStart(chain), m_instrumenter);
  }

  @Override
  public void execPreviousStep(WizardPreviousStepChain chain) {
    new OpenTelemetryExtensionRequest<>(getOwner(), "execPreviousStep")
        .wrapCall(() -> super.execPreviousStep(chain), m_instrumenter);
  }

  @Override
  public void execNextStep(WizardNextStepChain chain) {
    new OpenTelemetryExtensionRequest<>(getOwner(), "execNextStep")
        .wrapCall(() -> super.execNextStep(chain), m_instrumenter);
  }

  @Override
  public void execSuspend(WizardSuspendChain chain) {
    new OpenTelemetryExtensionRequest<>(getOwner(), "execSuspend")
        .wrapCall(() -> super.execSuspend(chain), m_instrumenter);
  }

  @Override
  public void execCancel(WizardCancelChain chain) {
    new OpenTelemetryExtensionRequest<>(getOwner(), "execCancel")
        .wrapCall(() -> super.execCancel(chain), m_instrumenter);
  }

  @Override
  public void execFinish(WizardFinishChain chain) {
    new OpenTelemetryExtensionRequest<>(getOwner(), "execFinish")
        .wrapCall(() -> super.execFinish(chain), m_instrumenter);
  }
}
