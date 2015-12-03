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
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.wizard.AbstractWizard;
import org.eclipse.scout.rt.client.ui.wizard.IWizardContainerForm;
import org.eclipse.scout.rt.client.ui.wizard.IWizardStep;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class WizardChains {

  private WizardChains() {
  }

  protected abstract static class AbstractWizardChain extends AbstractExtensionChain<IWizardExtension<? extends AbstractWizard>> {

    public AbstractWizardChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions, IWizardExtension.class);
    }
  }

  public static class WizardActiveStepChangedChain extends AbstractWizardChain {

    public WizardActiveStepChangedChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public void execActiveStepChanged() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) {
          next.execActiveStepChanged(WizardActiveStepChangedChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class WizardSuspendChain extends AbstractWizardChain {

    public WizardSuspendChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public void execSuspend() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) {
          next.execSuspend(WizardSuspendChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class WizardRefreshButtonPolicyChain extends AbstractWizardChain {

    public WizardRefreshButtonPolicyChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public void execRefreshButtonPolicy() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) {
          next.execRefreshButtonPolicy(WizardRefreshButtonPolicyChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class WizardCancelChain extends AbstractWizardChain {

    public WizardCancelChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public void execCancel() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) {
          next.execCancel(WizardCancelChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class WizardStartChain extends AbstractWizardChain {

    public WizardStartChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public void execStart() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) {
          next.execStart(WizardStartChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class WizardCreateContainerFormChain extends AbstractWizardChain {

    public WizardCreateContainerFormChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public IWizardContainerForm execCreateContainerForm() {
      MethodInvocation<IWizardContainerForm> methodInvocation = new MethodInvocation<IWizardContainerForm>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) {
          setReturnValue(next.execCreateContainerForm(WizardCreateContainerFormChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  public static class WizardDecorateContainerFormChain extends AbstractWizardChain {

    public WizardDecorateContainerFormChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public IWizardContainerForm execDecorateContainerForm() {
      MethodInvocation<IWizardContainerForm> methodInvocation = new MethodInvocation<IWizardContainerForm>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) {
          next.execDecorateContainerForm(WizardDecorateContainerFormChain.this);
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  public static class WizardAnyFieldChangedChain extends AbstractWizardChain {

    public WizardAnyFieldChangedChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public void execAnyFieldChanged(final IFormField source) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) {
          next.execAnyFieldChanged(WizardAnyFieldChangedChain.this, source);
        }
      };
      callChain(methodInvocation, source);
    }
  }

  public static class WizardResetChain extends AbstractWizardChain {

    public WizardResetChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public void execReset() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) {
          next.execReset(WizardResetChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class WizardAppLinkActionChain extends AbstractWizardChain {

    public WizardAppLinkActionChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public void execAppLinkAction(final String ref) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) {
          next.execAppLinkAction(WizardAppLinkActionChain.this, ref);
        }
      };
      callChain(methodInvocation, ref);
    }
  }

  public static class WizardStepActionChain extends AbstractWizardChain {

    public WizardStepActionChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public void execStepAction(final IWizardStep<? extends IForm> step) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) {
          next.execStepAction(WizardStepActionChain.this, step);
        }
      };
      callChain(methodInvocation, step);
    }
  }

  public static class WizardPreviousStepChain extends AbstractWizardChain {

    public WizardPreviousStepChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public void execPreviousStep() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) {
          next.execPreviousStep(WizardPreviousStepChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class WizardNextStepChain extends AbstractWizardChain {

    public WizardNextStepChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public void execNextStep() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) {
          next.execNextStep(WizardNextStepChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class WizardFinishChain extends AbstractWizardChain {

    public WizardFinishChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public void execFinish() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) {
          next.execFinish(WizardFinishChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }
}
