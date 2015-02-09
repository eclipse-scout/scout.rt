package org.eclipse.scout.rt.client.extension.ui.form.fields.tabbox;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class TabBoxChains {

  private TabBoxChains() {
  }

  protected abstract static class AbstractTabBoxChain extends AbstractExtensionChain<ITabBoxExtension<? extends AbstractTabBox>> {

    public AbstractTabBoxChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, ITabBoxExtension.class);
    }
  }

  public static class TabBoxTabSelectedChain extends AbstractTabBoxChain {

    public TabBoxTabSelectedChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execTabSelected(final IGroupBox selectedBox) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITabBoxExtension<? extends AbstractTabBox> next) throws ProcessingException {
          next.execTabSelected(TabBoxTabSelectedChain.this, selectedBox);
        }
      };
      callChain(methodInvocation, selectedBox);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
