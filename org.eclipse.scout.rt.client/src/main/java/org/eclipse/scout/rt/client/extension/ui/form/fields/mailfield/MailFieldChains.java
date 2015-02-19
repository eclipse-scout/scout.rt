package org.eclipse.scout.rt.client.extension.ui.form.fields.mailfield;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.mailfield.AbstractMailField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class MailFieldChains {

  private MailFieldChains() {
  }

  protected abstract static class AbstractMailFieldChain extends AbstractExtensionChain<IMailFieldExtension<? extends AbstractMailField>> {

    public AbstractMailFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IMailFieldExtension.class);
    }
  }

  public static class MailFieldHyperlinkActionChain extends AbstractMailFieldChain {

    public MailFieldHyperlinkActionChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execHyperlinkAction(final URL url, final String path, final boolean local) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IMailFieldExtension<? extends AbstractMailField> next) throws ProcessingException {
          next.execHyperlinkAction(MailFieldHyperlinkActionChain.this, url, path, local);
        }
      };
      callChain(methodInvocation, url, path, local);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class MailFieldAttachementActionChain extends AbstractMailFieldChain {

    public MailFieldAttachementActionChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execAttachementAction(final File file) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IMailFieldExtension<? extends AbstractMailField> next) throws ProcessingException {
          next.execAttachementAction(MailFieldAttachementActionChain.this, file);
        }
      };
      callChain(methodInvocation, file);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
