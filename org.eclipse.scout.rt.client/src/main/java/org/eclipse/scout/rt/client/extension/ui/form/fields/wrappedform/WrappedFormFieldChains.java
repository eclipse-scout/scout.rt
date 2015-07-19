package org.eclipse.scout.rt.client.extension.ui.form.fields.wrappedform;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class WrappedFormFieldChains {

  private WrappedFormFieldChains() {
  }

  protected abstract static class AbstractWrappedFormFieldChain<FORM extends IForm> extends AbstractExtensionChain<IWrappedFormFieldExtension<FORM, ? extends AbstractWrappedFormField<FORM>>> {

    public AbstractWrappedFormFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IWrappedFormFieldExtension.class);
    }
  }

  public static class WrappedFormFieldInnerFormChangedChain<FORM extends IForm> extends AbstractWrappedFormFieldChain<FORM> {

    public WrappedFormFieldInnerFormChangedChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execInnerFormChanged(final FORM oldInnerForm, final FORM newInnerForm) throws ProcessingException {
      MethodInvocation<Void> methodInvocation = new MethodInvocation<Void>() {
        @Override
        protected void callMethod(IWrappedFormFieldExtension<FORM, ? extends AbstractWrappedFormField<FORM>> next) throws ProcessingException {
          next.execInnerFormChanged(WrappedFormFieldInnerFormChangedChain.this, oldInnerForm, newInnerForm);
        }
      };
      callChain(methodInvocation, oldInnerForm, newInnerForm);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
    }
  }
}
