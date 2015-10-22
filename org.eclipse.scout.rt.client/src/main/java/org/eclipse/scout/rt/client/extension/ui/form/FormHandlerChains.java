package org.eclipse.scout.rt.client.extension.ui.form;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class FormHandlerChains {

  private FormHandlerChains() {
  }

  protected abstract static class AbstractFormHandlerChain extends AbstractExtensionChain<IFormHandlerExtension<? extends AbstractFormHandler>> {

    public AbstractFormHandlerChain(List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions) {
      super(extensions, IFormHandlerExtension.class);
    }
  }

  public static class FormHandlerPostLoadChain extends AbstractFormHandlerChain {

    public FormHandlerPostLoadChain(List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions) {
      super(extensions);
    }

    public void execPostLoad() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormHandlerExtension<? extends AbstractFormHandler> next) {
          next.execPostLoad(FormHandlerPostLoadChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class FormHandlerValidateChain extends AbstractFormHandlerChain {

    public FormHandlerValidateChain(List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions) {
      super(extensions);
    }

    public boolean execValidate() {
      MethodInvocation<Boolean> methodInvocation = new MethodInvocation<Boolean>() {
        @Override
        protected void callMethod(IFormHandlerExtension<? extends AbstractFormHandler> next) {
          setReturnValue(next.execValidate(FormHandlerValidateChain.this));
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }

  public static class FormHandlerLoadChain extends AbstractFormHandlerChain {

    public FormHandlerLoadChain(List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions) {
      super(extensions);
    }

    public void execLoad() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormHandlerExtension<? extends AbstractFormHandler> next) {
          next.execLoad(FormHandlerLoadChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class FormHandlerStoreChain extends AbstractFormHandlerChain {

    public FormHandlerStoreChain(List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions) {
      super(extensions);
    }

    public void execStore() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormHandlerExtension<? extends AbstractFormHandler> next) {
          next.execStore(FormHandlerStoreChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class FormHandlerDiscardChain extends AbstractFormHandlerChain {

    public FormHandlerDiscardChain(List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions) {
      super(extensions);
    }

    public void execDiscard() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormHandlerExtension<? extends AbstractFormHandler> next) {
          next.execDiscard(FormHandlerDiscardChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class FormHandlerCheckFieldsChain extends AbstractFormHandlerChain {

    public FormHandlerCheckFieldsChain(List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions) {
      super(extensions);
    }

    public boolean execCheckFields() {
      MethodInvocation<Boolean> methodInvocation = new MethodInvocation<Boolean>() {
        @Override
        protected void callMethod(IFormHandlerExtension<? extends AbstractFormHandler> next) {
          setReturnValue(next.execCheckFields(FormHandlerCheckFieldsChain.this));
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }

  public static class FormHandlerFinallyChain extends AbstractFormHandlerChain {

    public FormHandlerFinallyChain(List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions) {
      super(extensions);
    }

    public void execFinally() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormHandlerExtension<? extends AbstractFormHandler> next) {
          next.execFinally(FormHandlerFinallyChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
