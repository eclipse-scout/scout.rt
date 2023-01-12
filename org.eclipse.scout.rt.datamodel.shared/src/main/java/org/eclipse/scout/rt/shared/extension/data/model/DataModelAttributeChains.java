/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension.data.model;

import java.util.List;

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

    public void execInitAttribute() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDataModelAttributeExtension<? extends AbstractDataModelAttribute> next) {
          next.execInitAttribute(DataModelAttributeInitAttributeChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class DataModelAttributePrepareLookupChain extends AbstractDataModelAttributeChain {

    public DataModelAttributePrepareLookupChain(List<? extends IDataModelAttributeExtension<? extends AbstractDataModelAttribute>> extensions) {
      super(extensions);
    }

    public void execPrepareLookup(final ILookupCall<?> call) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDataModelAttributeExtension<? extends AbstractDataModelAttribute> next) {
          next.execPrepareLookup(DataModelAttributePrepareLookupChain.this, call);
        }
      };
      callChain(methodInvocation);
    }
  }
}
