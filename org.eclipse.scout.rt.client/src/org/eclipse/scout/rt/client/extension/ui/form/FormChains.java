package org.eclipse.scout.rt.client.extension.ui.form;

import java.util.HashSet;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

public final class FormChains {

  private FormChains() {
  }

  protected abstract static class AbstractFormChain extends AbstractExtensionChain<IFormExtension<? extends AbstractForm>> {

    public AbstractFormChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions, IFormExtension.class);
    }
  }

  public static class FormCloseTimerChain extends AbstractFormChain {

    public FormCloseTimerChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execCloseTimer() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) throws ProcessingException {
          next.execCloseTimer(FormCloseTimerChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class FormInactivityTimerChain extends AbstractFormChain {

    public FormInactivityTimerChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execInactivityTimer() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) throws ProcessingException {
          next.execInactivityTimer(FormInactivityTimerChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class FormStoredChain extends AbstractFormChain {

    public FormStoredChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execStored() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) throws ProcessingException {
          next.execStored(FormStoredChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class FormCheckFieldsChain extends AbstractFormChain {

    public FormCheckFieldsChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public boolean execCheckFields() throws ProcessingException {
      MethodInvocation<Boolean> methodInvocation = new MethodInvocation<Boolean>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) throws ProcessingException {
          setReturnValue(next.execCheckFields(FormCheckFieldsChain.this));
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }

  public static class FormResetSearchFilterChain extends AbstractFormChain {

    public FormResetSearchFilterChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execResetSearchFilter(final SearchFilter searchFilter) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) throws ProcessingException {
          next.execResetSearchFilter(FormResetSearchFilterChain.this, searchFilter);
        }
      };
      callChain(methodInvocation, searchFilter);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class FormAddSearchTermsChain extends AbstractFormChain {

    public FormAddSearchTermsChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execAddSearchTerms(final SearchFilter search) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) {
          next.execAddSearchTerms(FormAddSearchTermsChain.this, search);
        }
      };
      callChain(methodInvocation, search);
    }
  }

  public static class FormOnVetoExceptionChain extends AbstractFormChain {

    public FormOnVetoExceptionChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execOnVetoException(final VetoException e, final int code) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) throws ProcessingException {
          next.execOnVetoException(FormOnVetoExceptionChain.this, e, code);
        }
      };
      callChain(methodInvocation, e, code);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class FormFormActivatedChain extends AbstractFormChain {

    public FormFormActivatedChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execFormActivated() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) throws ProcessingException {
          next.execFormActivated(FormFormActivatedChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class FormDisposeFormChain extends AbstractFormChain {

    public FormDisposeFormChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execDisposeForm() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) throws ProcessingException {
          next.execDisposeForm(FormDisposeFormChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class FormTimerChain extends AbstractFormChain {

    public FormTimerChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execTimer(final String timerId) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) throws ProcessingException {
          next.execTimer(FormTimerChain.this, timerId);
        }
      };
      callChain(methodInvocation, timerId);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class FormCreateFormDataChain extends AbstractFormChain {

    public FormCreateFormDataChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public AbstractFormData execCreateFormData() throws ProcessingException {
      MethodInvocation<AbstractFormData> methodInvocation = new MethodInvocation<AbstractFormData>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) throws ProcessingException {
          setReturnValue(next.execCreateFormData(FormCreateFormDataChain.this));
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }

  public static class FormInitFormChain extends AbstractFormChain {

    public FormInitFormChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execInitForm() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) throws ProcessingException {
          next.execInitForm(FormInitFormChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class FormValidateChain extends AbstractFormChain {

    public FormValidateChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public boolean execValidate() throws ProcessingException {
      MethodInvocation<Boolean> methodInvocation = new MethodInvocation<Boolean>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) throws ProcessingException {
          setReturnValue(next.execValidate(FormValidateChain.this));
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }

  public static class FormOnCloseRequestChain extends AbstractFormChain {

    public FormOnCloseRequestChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execOnCloseRequest(final boolean kill, final HashSet<Integer> enabledButtonSystemTypes) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) throws ProcessingException {
          next.execOnCloseRequest(FormOnCloseRequestChain.this, kill, enabledButtonSystemTypes);
        }
      };
      callChain(methodInvocation, kill, enabledButtonSystemTypes);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class FormDataChangedChain extends AbstractFormChain {

    public FormDataChangedChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execDataChanged(final Object... dataTypes) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) throws ProcessingException {
          next.execDataChanged(FormDataChangedChain.this, dataTypes);
        }
      };
      callChain(methodInvocation, dataTypes);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
