package org.eclipse.scout.rt.client.extension.ui.form.fields.graphfield;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.graphfield.AbstractGraphField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class GraphFieldChains {

  private GraphFieldChains() {
  }

  protected abstract static class AbstractGraphFieldChain extends AbstractExtensionChain<IGraphFieldExtension<? extends AbstractGraphField>> {

    public AbstractGraphFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IGraphFieldExtension.class);
    }
  }

  public static class GraphFieldAppLinkActionChain extends AbstractGraphFieldChain {

    public GraphFieldAppLinkActionChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execAppLinkAction(final String ref) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IGraphFieldExtension<? extends AbstractGraphField> next) {
          next.execAppLinkAction(GraphFieldAppLinkActionChain.this, ref);
        }
      };
      callChain(methodInvocation, ref);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
    }
  }
}
