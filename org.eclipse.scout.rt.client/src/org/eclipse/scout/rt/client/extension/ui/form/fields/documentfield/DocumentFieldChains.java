package org.eclipse.scout.rt.client.extension.ui.form.fields.documentfield;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.documentfield.AbstractDocumentField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class DocumentFieldChains {

  private DocumentFieldChains() {
  }

  protected abstract static class AbstractDocumentFieldChain extends AbstractExtensionChain<IDocumentFieldExtension<? extends AbstractDocumentField>> {

    public AbstractDocumentFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IDocumentFieldExtension.class);
    }
  }

  public static class DocumentFieldComReadyStatusChangedChain extends AbstractDocumentFieldChain {

    public DocumentFieldComReadyStatusChangedChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execComReadyStatusChanged(final boolean ready) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDocumentFieldExtension<? extends AbstractDocumentField> next) throws ProcessingException {
          next.execComReadyStatusChanged(DocumentFieldComReadyStatusChangedChain.this, ready);
        }
      };
      callChain(methodInvocation, ready);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
