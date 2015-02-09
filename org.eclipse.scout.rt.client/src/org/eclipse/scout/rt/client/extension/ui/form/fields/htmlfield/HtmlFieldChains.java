package org.eclipse.scout.rt.client.extension.ui.form.fields.htmlfield;

import java.net.URL;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class HtmlFieldChains {

  private HtmlFieldChains() {
  }

  protected abstract static class AbstractHtmlFieldChain extends AbstractExtensionChain<IHtmlFieldExtension<? extends AbstractHtmlField>> {

    public AbstractHtmlFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IHtmlFieldExtension.class);
    }
  }

  public static class HtmlFieldHyperlinkActionChain extends AbstractHtmlFieldChain {

    public HtmlFieldHyperlinkActionChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execHyperlinkAction(final URL url, final String path, final boolean local) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IHtmlFieldExtension<? extends AbstractHtmlField> next) throws ProcessingException {
          next.execHyperlinkAction(HtmlFieldHyperlinkActionChain.this, url, path, local);
        }
      };
      callChain(methodInvocation, url, path, local);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
