package org.eclipse.scout.rt.shared.extension.data.model;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelAttribute;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

public final class DataModelAttributeChains {

  private DataModelAttributeChains() {
  }

  protected abstract static class AbstractDataModelAttributeChain extends AbstractExtensionChain<IDataModelAttributeExtension<? extends AbstractDataModelAttribute>> {

    public AbstractDataModelAttributeChain(List<? extends IDataModelAttributeExtension<? extends AbstractDataModelAttribute>> extensions) {
      super(extensions, IDataModelAttributeExtension.class);
    }
  }

  public static class DataModelAttributeInitAttributeChain extends AbstractDataModelAttributeChain {

    public DataModelAttributeInitAttributeChain(List<? extends IDataModelAttributeExtension<? extends AbstractDataModelAttribute>> extensions) {
      super(extensions);
    }

    public void execInitAttribute() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDataModelAttributeExtension<? extends AbstractDataModelAttribute> next) throws ProcessingException {
          next.execInitAttribute(DataModelAttributeInitAttributeChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class DataModelAttributePrepareLookupChain extends AbstractDataModelAttributeChain {

    public DataModelAttributePrepareLookupChain(List<? extends IDataModelAttributeExtension<? extends AbstractDataModelAttribute>> extensions) {
      super(extensions);
    }

    public void execPrepareLookup(final ILookupCall<?> call) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDataModelAttributeExtension<? extends AbstractDataModelAttribute> next) throws ProcessingException {
          next.execPrepareLookup(DataModelAttributePrepareLookupChain.this, call);
        }
      };
      callChain(methodInvocation, call);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
