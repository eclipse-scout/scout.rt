package org.eclipse.scout.rt.shared.extension.data.model;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelEntity;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class DataModelEntityChains {

  private DataModelEntityChains() {
  }

  protected abstract static class AbstractDataModelEntityChain extends AbstractExtensionChain<IDataModelEntityExtension<? extends AbstractDataModelEntity>> {

    public AbstractDataModelEntityChain(List<? extends IDataModelEntityExtension<? extends AbstractDataModelEntity>> extensions) {
      super(extensions, IDataModelEntityExtension.class);
    }
  }

  public static class DataModelEntityInitEntityChain extends AbstractDataModelEntityChain {

    public DataModelEntityInitEntityChain(List<? extends IDataModelEntityExtension<? extends AbstractDataModelEntity>> extensions) {
      super(extensions);
    }

    public void execInitEntity() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDataModelEntityExtension<? extends AbstractDataModelEntity> next) throws ProcessingException {
          next.execInitEntity(DataModelEntityInitEntityChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
