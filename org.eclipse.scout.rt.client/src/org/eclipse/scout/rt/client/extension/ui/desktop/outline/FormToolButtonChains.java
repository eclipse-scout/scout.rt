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

  protected abstract static class AbstractFormToolButtonChain<FORM extends IForm> extends AbstractExtensionChain<IFormToolButtonExtension<? extends IForm, ? extends AbstractFormToolButton<? extends IForm>>> {

    public AbstractFormToolButtonChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions, IFormToolButtonExtension.class);
    }
  }

  public static class FormToolButtonStartFormChain<FORM extends IForm> extends AbstractFormToolButtonChain<FORM> {

    public FormToolButtonStartFormChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions);
    }

    public void execStartForm() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormToolButtonExtension<? extends IForm, ? extends AbstractFormToolButton<? extends IForm>> next) throws ProcessingException {
          next.execStartForm(FormToolButtonStartFormChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
