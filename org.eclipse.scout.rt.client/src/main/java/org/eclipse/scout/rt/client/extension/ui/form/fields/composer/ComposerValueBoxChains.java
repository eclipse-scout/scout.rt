package org.eclipse.scout.rt.client.extension.ui.form.fields.composer;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerValueBox;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class ComposerValueBoxChains {

  private ComposerValueBoxChains() {
  }

  protected abstract static class AbstractComposerValueBoxChain extends AbstractExtensionChain<IComposerValueBoxExtension<? extends AbstractComposerValueBox>> {

    public AbstractComposerValueBoxChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IComposerValueBoxExtension.class);
    }
  }

  public static class ComposerValueBoxChangedValueChain extends AbstractComposerValueBoxChain {

    public ComposerValueBoxChangedValueChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execChangedValue() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IComposerValueBoxExtension<? extends AbstractComposerValueBox> next) throws ProcessingException {
          next.execChangedValue(ComposerValueBoxChangedValueChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
