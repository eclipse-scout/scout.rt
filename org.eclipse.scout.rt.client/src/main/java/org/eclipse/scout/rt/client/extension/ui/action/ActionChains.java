package org.eclipse.scout.rt.client.extension.ui.action;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class ActionChains {

  private ActionChains() {
  }

  protected abstract static class AbstractActionChain extends AbstractExtensionChain<IActionExtension<? extends AbstractAction>> {

    public AbstractActionChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions, IActionExtension.class);
    }
  }

  public static class ActionSelectionChangedChain extends AbstractActionChain {

    public ActionSelectionChangedChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions);
    }

    public void execSelectionChanged(final boolean selection) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IActionExtension<? extends AbstractAction> next) throws ProcessingException {
          next.execSelectionChanged(ActionSelectionChangedChain.this, selection);
        }
      };
      callChain(methodInvocation, selection);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ActionActionChain extends AbstractActionChain {

    public ActionActionChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions);
    }

    public void execAction() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IActionExtension<? extends AbstractAction> next) throws ProcessingException {
          next.execAction(ActionActionChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ActionInitActionChain extends AbstractActionChain {

    public ActionInitActionChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions);
    }

    public void execInitAction() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IActionExtension<? extends AbstractAction> next) throws ProcessingException {
          next.execInitAction(ActionInitActionChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
