package org.eclipse.scout.svg.client.extension.svgfield;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;
import org.eclipse.scout.svg.client.svgfield.AbstractSvgField;
import org.eclipse.scout.svg.client.svgfield.SvgFieldEvent;

public final class SvgFieldChains {

  private SvgFieldChains() {
  }

  protected abstract static class AbstractSvgFieldChain extends AbstractExtensionChain<ISvgFieldExtension<? extends AbstractSvgField>> {

    public AbstractSvgFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, ISvgFieldExtension.class);
    }
  }

  public static class SvgFieldClickedChain extends AbstractSvgFieldChain {

    public SvgFieldClickedChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execClicked(final SvgFieldEvent e) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISvgFieldExtension<? extends AbstractSvgField> next) throws ProcessingException {
          next.execClicked(SvgFieldClickedChain.this, e);
        }
      };
      callChain(methodInvocation, e);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class SvgFieldHyperlinkChain extends AbstractSvgFieldChain {

    public SvgFieldHyperlinkChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execHyperlink(final SvgFieldEvent e) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISvgFieldExtension<? extends AbstractSvgField> next) throws ProcessingException {
          next.execHyperlink(SvgFieldHyperlinkChain.this, e);
        }
      };
      callChain(methodInvocation, e);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
