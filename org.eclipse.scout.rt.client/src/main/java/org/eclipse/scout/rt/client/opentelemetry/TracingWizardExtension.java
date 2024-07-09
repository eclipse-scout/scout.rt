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
import org.eclipse.scout.rt.client.ui.wizard.AbstractWizard;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.opentelemetry.ITracingHelper;

import io.opentelemetry.api.trace.Tracer;

public class TracingWizardExtension extends AbstractWizardExtension<AbstractWizard> {

  private Tracer m_tracer;

  public TracingWizardExtension(AbstractWizard owner) {
    super(owner);
    m_tracer = BEANS.get(ITracingHelper.class).createTracer(TracingWizardExtension.class);
  }

  @Override
  public void execStart(WizardStartChain chain) {
    String name = getOwner().getClass().getSimpleName() + "#execStart";
    BEANS.get(ITracingHelper.class).wrapInSpan(m_tracer, name, span -> {
      span.setAttribute("scout.client.wizard.class", getOwner().getClass().getName());
      span.setAttribute("scout.client.wizard.text", getOwner().getTitle());
      super.execStart(chain);
    });
  }

  @Override
  public void execPostStart(WizardPostStartChain chain) {
    String name = getOwner().getClass().getSimpleName() + "#execPostStart";
    BEANS.get(ITracingHelper.class).wrapInSpan(m_tracer, name, span -> {
      span.setAttribute("scout.client.wizard.class", getOwner().getClass().getName());
      span.setAttribute("scout.client.wizard.text", getOwner().getTitle());
      super.execPostStart(chain);
    });
  }

  @Override
  public void execPreviousStep(WizardPreviousStepChain chain) {
    String name = getOwner().getClass().getSimpleName() + "#execPreviousStep";
    BEANS.get(ITracingHelper.class).wrapInSpan(m_tracer, name, span -> {
      span.setAttribute("scout.client.wizard.class", getOwner().getClass().getName());
      span.setAttribute("scout.client.wizard.text", getOwner().getTitle());
      super.execPreviousStep(chain);
    });
  }

  @Override
  public void execNextStep(WizardNextStepChain chain) {
    String name = getOwner().getClass().getSimpleName() + "#execNextStep";
    BEANS.get(ITracingHelper.class).wrapInSpan(m_tracer, name, span -> {
      span.setAttribute("scout.client.wizard.class", getOwner().getClass().getName());
      span.setAttribute("scout.client.wizard.text", getOwner().getTitle());
      super.execNextStep(chain);
    });
  }

  @Override
  public void execSuspend(WizardSuspendChain chain) {
    String name = getOwner().getClass().getSimpleName() + "#execSuspend";
    BEANS.get(ITracingHelper.class).wrapInSpan(m_tracer, name, span -> {
      span.setAttribute("scout.client.wizard.class", getOwner().getClass().getName());
      span.setAttribute("scout.client.wizard.text", getOwner().getTitle());
      super.execSuspend(chain);
    });
  }

  @Override
  public void execCancel(WizardCancelChain chain) {
    String name = getOwner().getClass().getSimpleName() + "#execCancel";
    BEANS.get(ITracingHelper.class).wrapInSpan(m_tracer, name, span -> {
      span.setAttribute("scout.client.wizard.class", getOwner().getClass().getName());
      span.setAttribute("scout.client.wizard.text", getOwner().getTitle());
      super.execCancel(chain);
    });
  }

  @Override
  public void execFinish(WizardFinishChain chain) {
    String name = getOwner().getClass().getSimpleName() + "#execFinish";
    BEANS.get(ITracingHelper.class).wrapInSpan(m_tracer, name, span -> {
      span.setAttribute("scout.client.wizard.class", getOwner().getClass().getName());
      span.setAttribute("scout.client.wizard.text", getOwner().getTitle());
      super.execFinish(chain);
    });
  }
}
