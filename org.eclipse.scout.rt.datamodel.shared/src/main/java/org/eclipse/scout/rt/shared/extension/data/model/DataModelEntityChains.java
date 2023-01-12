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

    public void execInitEntity() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDataModelEntityExtension<? extends AbstractDataModelEntity> next) {
          next.execInitEntity(DataModelEntityInitEntityChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }
}
