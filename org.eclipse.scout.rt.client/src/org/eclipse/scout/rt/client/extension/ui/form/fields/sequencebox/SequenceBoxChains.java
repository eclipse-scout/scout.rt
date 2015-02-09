package org.eclipse.scout.rt.client.extension.ui.form.fields.sequencebox;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class SequenceBoxChains {

  private SequenceBoxChains() {
  }

  protected abstract static class AbstractSequenceBoxChain extends AbstractExtensionChain<ISequenceBoxExtension<? extends AbstractSequenceBox>> {

    public AbstractSequenceBoxChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, ISequenceBoxExtension.class);
    }
  }

  public static class SequenceBoxIsLabelSuffixCandidateChain extends AbstractSequenceBoxChain {

    public SequenceBoxIsLabelSuffixCandidateChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public boolean execIsLabelSuffixCandidate(final IFormField formField) {
      MethodInvocation<Boolean> methodInvocation = new MethodInvocation<Boolean>() {
        @Override
        protected void callMethod(ISequenceBoxExtension<? extends AbstractSequenceBox> next) {
          setReturnValue(next.execIsLabelSuffixCandidate(SequenceBoxIsLabelSuffixCandidateChain.this, formField));
        }
      };
      callChain(methodInvocation, formField);
      return methodInvocation.getReturnValue();
    }
  }

  public static class SequenceBoxCheckFromToChain extends AbstractSequenceBoxChain {

    public SequenceBoxCheckFromToChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public <T extends Comparable<T>> void execCheckFromTo(final IValueField<T>[] valueFields, final int changedIndex) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISequenceBoxExtension<? extends AbstractSequenceBox> next) throws ProcessingException {
          next.execCheckFromTo(SequenceBoxCheckFromToChain.this, valueFields, changedIndex);
        }
      };
      callChain(methodInvocation, valueFields, changedIndex);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class SequenceBoxCreateLabelSuffixChain extends AbstractSequenceBoxChain {

    public SequenceBoxCreateLabelSuffixChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public String execCreateLabelSuffix() {
      MethodInvocation<String> methodInvocation = new MethodInvocation<String>() {
        @Override
        protected void callMethod(ISequenceBoxExtension<? extends AbstractSequenceBox> next) {
          setReturnValue(next.execCreateLabelSuffix(SequenceBoxCreateLabelSuffixChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }
}
