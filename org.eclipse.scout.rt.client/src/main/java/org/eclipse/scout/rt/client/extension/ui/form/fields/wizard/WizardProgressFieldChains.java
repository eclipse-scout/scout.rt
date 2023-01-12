/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.wizard;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.wizard.AbstractWizardProgressField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class WizardProgressFieldChains {

  private WizardProgressFieldChains() {
  }

  protected abstract static class AbstractWizardProgressChain extends AbstractExtensionChain<IWizardProgressFieldExtension<? extends AbstractWizardProgressField>> {

    public AbstractWizardProgressChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IWizardProgressFieldExtension.class);
    }
  }

  public static class WizardProgressFieldStepActionChain extends AbstractWizardProgressChain {

    public WizardProgressFieldStepActionChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execStepIndex(final int stepIndex) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardProgressFieldExtension<? extends AbstractWizardProgressField> next) {
          next.execStepAction(WizardProgressFieldStepActionChain.this, stepIndex);
        }
      };
      callChain(methodInvocation);
    }
  }
}
