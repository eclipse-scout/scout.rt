/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.wizard;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.wizard.AbstractWizardStep;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class WizardStepChains {

  private WizardStepChains() {
  }

  protected abstract static class AbstractWizardStepChain<FORM extends IForm> extends AbstractExtensionChain<IWizardStepExtension<? extends IForm, ? extends AbstractWizardStep<? extends IForm>>> {

    public AbstractWizardStepChain(List<? extends IWizardStepExtension<? extends IForm, ? extends AbstractWizardStep<? extends IForm>>> extensions) {
      super(extensions, IWizardStepExtension.class);
    }
  }

  public static class WizardStepDeactivateChain<FORM extends IForm> extends AbstractWizardStepChain<FORM> {

    public WizardStepDeactivateChain(List<? extends IWizardStepExtension<FORM, ? extends AbstractWizardStep<? extends IForm>>> extensions) {
      super(extensions);
    }

    public void execDeactivate(final int stepKind) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardStepExtension<? extends IForm, ? extends AbstractWizardStep<? extends IForm>> next) {
          next.execDeactivate(WizardStepDeactivateChain.this, stepKind);
        }
      };
      callChain(methodInvocation, stepKind);
    }
  }

  public static class WizardStepDisposeChain<FORM extends IForm> extends AbstractWizardStepChain<FORM> {

    public WizardStepDisposeChain(List<? extends IWizardStepExtension<FORM, ? extends AbstractWizardStep<? extends IForm>>> extensions) {
      super(extensions);
    }

    public void execDispose() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardStepExtension<? extends IForm, ? extends AbstractWizardStep<? extends IForm>> next) {
          next.execDispose(WizardStepDisposeChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class WizardStepFormClosedChain<FORM extends IForm> extends AbstractWizardStepChain<FORM> {

    public WizardStepFormClosedChain(List<? extends IWizardStepExtension<FORM, ? extends AbstractWizardStep<? extends IForm>>> extensions) {
      super(extensions);
    }

    public void execFormClosed(final boolean activation) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardStepExtension<? extends IForm, ? extends AbstractWizardStep<? extends IForm>> next) {
          next.execFormClosed(WizardStepFormClosedChain.this, activation);
        }
      };
      callChain(methodInvocation, activation);
    }
  }

  public static class WizardStepActivateChain<FORM extends IForm> extends AbstractWizardStepChain<FORM> {

    public WizardStepActivateChain(List<? extends IWizardStepExtension<FORM, ? extends AbstractWizardStep<? extends IForm>>> extensions) {
      super(extensions);
    }

    public void execActivate(final int stepKind) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardStepExtension<? extends IForm, ? extends AbstractWizardStep<? extends IForm>> next) {
          next.execActivate(WizardStepActivateChain.this, stepKind);
        }
      };
      callChain(methodInvocation, stepKind);
    }
  }

  public static class WizardStepFormDiscardedChain<FORM extends IForm> extends AbstractWizardStepChain<FORM> {

    public WizardStepFormDiscardedChain(List<? extends IWizardStepExtension<FORM, ? extends AbstractWizardStep<? extends IForm>>> extensions) {
      super(extensions);
    }

    public void execFormDiscarded(final boolean activation) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardStepExtension<? extends IForm, ? extends AbstractWizardStep<? extends IForm>> next) {
          next.execFormDiscarded(WizardStepFormDiscardedChain.this, activation);
        }
      };
      callChain(methodInvocation, activation);
    }
  }

  public static class WizardStepFormStoredChain<FORM extends IForm> extends AbstractWizardStepChain<FORM> {

    public WizardStepFormStoredChain(List<? extends IWizardStepExtension<FORM, ? extends AbstractWizardStep<? extends IForm>>> extensions) {
      super(extensions);
    }

    public void execFormStored(final boolean activation) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardStepExtension<? extends IForm, ? extends AbstractWizardStep<? extends IForm>> next) {
          next.execFormStored(WizardStepFormStoredChain.this, activation);
        }
      };
      callChain(methodInvocation, activation);
    }
  }

  public static class WizardStepActionChain<FORM extends IForm> extends AbstractWizardStepChain<FORM> {

    public WizardStepActionChain(List<? extends IWizardStepExtension<FORM, ? extends AbstractWizardStep<? extends IForm>>> extensions) {
      super(extensions);
    }

    public void execAction() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardStepExtension<? extends IForm, ? extends AbstractWizardStep<? extends IForm>> next) {
          next.execAction(WizardStepActionChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }
}
