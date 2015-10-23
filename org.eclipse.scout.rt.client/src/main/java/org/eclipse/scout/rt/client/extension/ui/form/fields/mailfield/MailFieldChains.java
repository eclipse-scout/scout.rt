package org.eclipse.scout.rt.client.extension.ui.form.fields.mailfield;

import java.io.File;
import java.util.List;

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

    public void execHyperlinkAction(final String ref) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IMailFieldExtension<? extends AbstractMailField> next) {
          next.execHyperlinkAction(MailFieldHyperlinkActionChain.this, ref);
        }
      };
      callChain(methodInvocation, ref);
    }
  }

  public static class MailFieldAttachementActionChain extends AbstractMailFieldChain {

    public MailFieldAttachementActionChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execAttachementAction(final File file) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IMailFieldExtension<? extends AbstractMailField> next) {
          next.execAttachementAction(MailFieldAttachementActionChain.this, file);
        }
      };
      callChain(methodInvocation, file);
    }
  }
}
