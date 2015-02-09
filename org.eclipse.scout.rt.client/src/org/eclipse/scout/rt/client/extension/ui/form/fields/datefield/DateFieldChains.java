package org.eclipse.scout.rt.client.extension.ui.form.fields.datefield;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class DateFieldChains {

  private DateFieldChains() {
  }

  protected abstract static class AbstractDateFieldChain extends AbstractExtensionChain<IDateFieldExtension<? extends AbstractDateField>> {

    public AbstractDateFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IDateFieldExtension.class);
    }
  }

  public static class DateFieldShiftTimeChain extends AbstractDateFieldChain {

    public DateFieldShiftTimeChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execShiftTime(final int level, final int value) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDateFieldExtension<? extends AbstractDateField> next) throws ProcessingException {
          next.execShiftTime(DateFieldShiftTimeChain.this, level, value);
        }
      };
      callChain(methodInvocation, level, value);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class DateFieldShiftDateChain extends AbstractDateFieldChain {

    public DateFieldShiftDateChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execShiftDate(final int level, final int value) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDateFieldExtension<? extends AbstractDateField> next) throws ProcessingException {
          next.execShiftDate(DateFieldShiftDateChain.this, level, value);
        }
      };
      callChain(methodInvocation, level, value);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
