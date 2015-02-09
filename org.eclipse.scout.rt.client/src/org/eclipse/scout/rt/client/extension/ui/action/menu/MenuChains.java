package org.eclipse.scout.rt.client.extension.ui.action.menu;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.action.IActionExtension;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class MenuChains {

  private MenuChains() {
  }

  protected abstract static class AbstractMenuChain extends AbstractExtensionChain<IMenuExtension<? extends AbstractMenu>> {

    public AbstractMenuChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions, IMenuExtension.class);
    }
  }

  public static class MenuAboutToShowChain extends AbstractMenuChain {

    public MenuAboutToShowChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions);
    }

    public void execAboutToShow() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IMenuExtension<? extends AbstractMenu> next) throws ProcessingException {
          next.execAboutToShow(MenuAboutToShowChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class MenuOwnerValueChangedChain extends AbstractMenuChain {

    public MenuOwnerValueChangedChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions);
    }

    public void execOwnerValueChanged(final Object newOwnerValue) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IMenuExtension<? extends AbstractMenu> next) throws ProcessingException {
          next.execOwnerValueChanged(MenuOwnerValueChangedChain.this, newOwnerValue);
        }
      };
      callChain(methodInvocation, newOwnerValue);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
