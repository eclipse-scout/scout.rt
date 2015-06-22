package org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.AbstractBrowserField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class BrowserFieldChains {

  private BrowserFieldChains() {
  }

  protected abstract static class AbstractBrowserFieldChain extends AbstractExtensionChain<IBrowserFieldExtension<? extends AbstractBrowserField>> {

    public AbstractBrowserFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IBrowserFieldExtension.class);
    }
  }

  public static class BrowserFieldLocationChangedChain extends AbstractBrowserFieldChain {

    public BrowserFieldLocationChangedChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPostMessage(final String data, final String origin) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IBrowserFieldExtension<? extends AbstractBrowserField> next) throws ProcessingException {
          next.execPostMessage(BrowserFieldLocationChangedChain.this, data, origin);
        }
      };
      callChain(methodInvocation, data, origin);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
