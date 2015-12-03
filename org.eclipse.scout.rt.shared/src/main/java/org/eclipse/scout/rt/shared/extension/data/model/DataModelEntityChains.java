/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
