package org.eclipse.scout.rt.client.extension.ui.desktop.outline;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.action.IActionExtension;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractFormToolButton;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class FormToolButtonChains {

  private FormToolButtonChains() {
  }

  protected abstract static class AbstractFormToolButtonChain<FORM extends IForm> extends AbstractExtensionChain<IFormToolButtonExtension<FORM, ? extends AbstractFormToolButton<FORM>>> {

    public AbstractFormToolButtonChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions, IFormToolButtonExtension.class);
    }
  }

  public static class FormToolButtonInitFormChain<FORM extends IForm> extends AbstractFormToolButtonChain<FORM> {

    public FormToolButtonInitFormChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions);
    }

    public void execInitForm(final FORM form) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormToolButtonExtension<FORM, ? extends AbstractFormToolButton<FORM>> next) throws Exception {
          next.execInitForm(FormToolButtonInitFormChain.this, form);
        }
      };
      callChain(methodInvocation, form);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
    }
  }
}
