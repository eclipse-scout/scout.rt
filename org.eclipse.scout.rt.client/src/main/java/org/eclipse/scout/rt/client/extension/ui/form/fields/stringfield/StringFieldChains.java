package org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class StringFieldChains {

  private StringFieldChains() {
  }

  protected abstract static class AbstractStringFieldChain extends AbstractExtensionChain<IStringFieldExtension<? extends AbstractStringField>> {

    public AbstractStringFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IStringFieldExtension.class);
    }
  }

  public static class StringFieldDropRequestChain extends AbstractStringFieldChain {

    public StringFieldDropRequestChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execDropRequest(final TransferObject transferObject) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IStringFieldExtension<? extends AbstractStringField> next) {
          next.execDropRequest(StringFieldDropRequestChain.this, transferObject);
        }
      };
      callChain(methodInvocation, transferObject);
    }
  }

  public static class StringFieldLinkActionChain extends AbstractStringFieldChain {

    public StringFieldLinkActionChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execAction() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IStringFieldExtension<? extends AbstractStringField> next) {
          next.execAction(StringFieldLinkActionChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class StringFieldDragRequestChain extends AbstractStringFieldChain {

    public StringFieldDragRequestChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public TransferObject execDragRequest() {
      MethodInvocation<TransferObject> methodInvocation = new MethodInvocation<TransferObject>() {
        @Override
        protected void callMethod(IStringFieldExtension<? extends AbstractStringField> next) {
          setReturnValue(next.execDragRequest(StringFieldDragRequestChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }
}
