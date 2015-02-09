package org.eclipse.scout.rt.client.extension.ui.form.fields.button;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class ButtonChains {

  private ButtonChains() {
  }

  protected abstract static class AbstractButtonChain extends AbstractExtensionChain<IButtonExtension<? extends AbstractButton>> {

    public AbstractButtonChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IButtonExtension.class);
    }
  }

  public static class ButtonSelectionChangedChain extends AbstractButtonChain {

    public ButtonSelectionChangedChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execSelectionChanged(final boolean selection) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IButtonExtension<? extends AbstractButton> next) throws ProcessingException {
          next.execSelectionChanged(ButtonSelectionChangedChain.this, selection);
        }
      };
      callChain(methodInvocation, selection);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ButtonClickActionChain extends AbstractButtonChain {

    public ButtonClickActionChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execClickAction() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IButtonExtension<? extends AbstractButton> next) throws ProcessingException {
          next.execClickAction(ButtonClickActionChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
