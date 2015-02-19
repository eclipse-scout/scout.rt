package org.eclipse.scout.rt.client.extension.ui.wizard;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
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

    public void execDeactivate(final int stepKind) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardStepExtension<? extends IForm, ? extends AbstractWizardStep<? extends IForm>> next) throws ProcessingException {
          next.execDeactivate(WizardStepDeactivateChain.this, stepKind);
        }
      };
      callChain(methodInvocation, stepKind);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class WizardStepDisposeChain<FORM extends IForm> extends AbstractWizardStepChain<FORM> {

    public WizardStepDisposeChain(List<? extends IWizardStepExtension<FORM, ? extends AbstractWizardStep<? extends IForm>>> extensions) {
      super(extensions);
    }

    public void execDispose() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardStepExtension<? extends IForm, ? extends AbstractWizardStep<? extends IForm>> next) throws ProcessingException {
          next.execDispose(WizardStepDisposeChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class WizardStepFormClosedChain<FORM extends IForm> extends AbstractWizardStepChain<FORM> {

    public WizardStepFormClosedChain(List<? extends IWizardStepExtension<FORM, ? extends AbstractWizardStep<? extends IForm>>> extensions) {
      super(extensions);
    }

    public void execFormClosed(final boolean activation) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardStepExtension<? extends IForm, ? extends AbstractWizardStep<? extends IForm>> next) throws ProcessingException {
          next.execFormClosed(WizardStepFormClosedChain.this, activation);
        }
      };
      callChain(methodInvocation, activation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class WizardStepActivateChain<FORM extends IForm> extends AbstractWizardStepChain<FORM> {

    public WizardStepActivateChain(List<? extends IWizardStepExtension<FORM, ? extends AbstractWizardStep<? extends IForm>>> extensions) {
      super(extensions);
    }

    public void execActivate(final int stepKind) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardStepExtension<? extends IForm, ? extends AbstractWizardStep<? extends IForm>> next) throws ProcessingException {
          next.execActivate(WizardStepActivateChain.this, stepKind);
        }
      };
      callChain(methodInvocation, stepKind);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class WizardStepFormDiscardedChain<FORM extends IForm> extends AbstractWizardStepChain<FORM> {

    public WizardStepFormDiscardedChain(List<? extends IWizardStepExtension<FORM, ? extends AbstractWizardStep<? extends IForm>>> extensions) {
      super(extensions);
    }

    public void execFormDiscarded(final boolean activation) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardStepExtension<? extends IForm, ? extends AbstractWizardStep<? extends IForm>> next) throws ProcessingException {
          next.execFormDiscarded(WizardStepFormDiscardedChain.this, activation);
        }
      };
      callChain(methodInvocation, activation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class WizardStepFormStoredChain<FORM extends IForm> extends AbstractWizardStepChain<FORM> {

    public WizardStepFormStoredChain(List<? extends IWizardStepExtension<FORM, ? extends AbstractWizardStep<? extends IForm>>> extensions) {
      super(extensions);
    }

    public void execFormStored(final boolean activation) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardStepExtension<? extends IForm, ? extends AbstractWizardStep<? extends IForm>> next) throws ProcessingException {
          next.execFormStored(WizardStepFormStoredChain.this, activation);
        }
      };
      callChain(methodInvocation, activation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
