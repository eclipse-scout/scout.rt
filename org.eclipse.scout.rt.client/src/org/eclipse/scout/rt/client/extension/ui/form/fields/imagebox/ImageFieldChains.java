package org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox;

import java.util.List;

import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.imagebox.AbstractImageField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class ImageFieldChains {

  private ImageFieldChains() {
  }

  protected abstract static class AbstractImageFieldChain extends AbstractExtensionChain<IImageFieldExtension<? extends AbstractImageField>> {

    public AbstractImageFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IImageFieldExtension.class);
    }
  }

  public static class ImageFieldDragRequestChain extends AbstractImageFieldChain {

    public ImageFieldDragRequestChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public TransferObject execDragRequest() throws ProcessingException {
      MethodInvocation<TransferObject> methodInvocation = new MethodInvocation<TransferObject>() {
        @Override
        protected void callMethod(IImageFieldExtension<? extends AbstractImageField> next) throws ProcessingException {
          setReturnValue(next.execDragRequest(ImageFieldDragRequestChain.this));
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }

  public static class ImageFieldDropRequestChain extends AbstractImageFieldChain {

    public ImageFieldDropRequestChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execDropRequest(final TransferObject transferObject) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IImageFieldExtension<? extends AbstractImageField> next) throws ProcessingException {
          next.execDropRequest(ImageFieldDropRequestChain.this, transferObject);
        }
      };
      callChain(methodInvocation, transferObject);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
