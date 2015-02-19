package org.eclipse.scout.rt.client.extension.ui.wizard;

import java.net.URL;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.wizard.AbstractWizard;
import org.eclipse.scout.rt.client.ui.wizard.IWizardContainerForm;
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

    public void execActiveStepChanged() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) throws ProcessingException {
          next.execActiveStepChanged(WizardActiveStepChangedChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class WizardSuspendChain extends AbstractWizardChain {

    public WizardSuspendChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public void execSuspend() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) throws ProcessingException {
          next.execSuspend(WizardSuspendChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class WizardRefreshButtonPolicyChain extends AbstractWizardChain {

    public WizardRefreshButtonPolicyChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public void execRefreshButtonPolicy() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) throws ProcessingException {
          next.execRefreshButtonPolicy(WizardRefreshButtonPolicyChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class WizardCancelChain extends AbstractWizardChain {

    public WizardCancelChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public void execCancel() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) throws ProcessingException {
          next.execCancel(WizardCancelChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class WizardStartChain extends AbstractWizardChain {

    public WizardStartChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public void execStart() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) throws ProcessingException {
          next.execStart(WizardStartChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class WizardCreateContainerFormChain extends AbstractWizardChain {

    public WizardCreateContainerFormChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public IWizardContainerForm execCreateContainerForm() throws ProcessingException {
      MethodInvocation<IWizardContainerForm> methodInvocation = new MethodInvocation<IWizardContainerForm>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) throws ProcessingException {
          setReturnValue(next.execCreateContainerForm(WizardCreateContainerFormChain.this));
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }

  public static class WizardAnyFieldChangedChain extends AbstractWizardChain {

    public WizardAnyFieldChangedChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public void execAnyFieldChanged(final IFormField source) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) throws ProcessingException {
          next.execAnyFieldChanged(WizardAnyFieldChangedChain.this, source);
        }
      };
      callChain(methodInvocation, source);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class WizardResetChain extends AbstractWizardChain {

    public WizardResetChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public void execReset() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) throws ProcessingException {
          next.execReset(WizardResetChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class WizardHyperlinkActionChain extends AbstractWizardChain {

    public WizardHyperlinkActionChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public void execHyperlinkAction(final URL url, final String path, final boolean local) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) throws ProcessingException {
          next.execHyperlinkAction(WizardHyperlinkActionChain.this, url, path, local);
        }
      };
      callChain(methodInvocation, url, path, local);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class WizardPreviousStepChain extends AbstractWizardChain {

    public WizardPreviousStepChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public void execPreviousStep() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) throws ProcessingException {
          next.execPreviousStep(WizardPreviousStepChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class WizardNextStepChain extends AbstractWizardChain {

    public WizardNextStepChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public void execNextStep() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) throws ProcessingException {
          next.execNextStep(WizardNextStepChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class WizardFinishChain extends AbstractWizardChain {

    public WizardFinishChain(List<? extends IWizardExtension<? extends AbstractWizard>> extensions) {
      super(extensions);
    }

    public void execFinish() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardExtension<? extends AbstractWizard> next) throws ProcessingException {
          next.execFinish(WizardFinishChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
