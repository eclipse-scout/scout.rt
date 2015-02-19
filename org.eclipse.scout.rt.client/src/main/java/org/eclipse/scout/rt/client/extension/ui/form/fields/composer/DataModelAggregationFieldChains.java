package org.eclipse.scout.rt.client.extension.ui.form.fields.composer;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractDataModelAggregationField;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class DataModelAggregationFieldChains {

  private DataModelAggregationFieldChains() {
  }

  protected abstract static class AbstractDataModelAggregationFieldChain extends AbstractExtensionChain<IDataModelAggregationFieldExtension<? extends AbstractDataModelAggregationField>> {

    public AbstractDataModelAggregationFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IDataModelAggregationFieldExtension.class);
    }
  }

  public static class DataModelAggregationFieldAttributeChangedChain extends AbstractDataModelAggregationFieldChain {

    public DataModelAggregationFieldAttributeChangedChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execAttributeChanged(final IDataModelAttribute attribute) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDataModelAggregationFieldExtension<? extends AbstractDataModelAggregationField> next) throws ProcessingException {
          next.execAttributeChanged(DataModelAggregationFieldAttributeChangedChain.this, attribute);
        }
      };
      callChain(methodInvocation, attribute);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
