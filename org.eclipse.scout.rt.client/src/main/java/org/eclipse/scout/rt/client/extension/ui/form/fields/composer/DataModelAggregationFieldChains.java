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
package org.eclipse.scout.rt.client.extension.ui.form.fields.composer;

import java.util.List;

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

    public void execAttributeChanged(final IDataModelAttribute attribute) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDataModelAggregationFieldExtension<? extends AbstractDataModelAggregationField> next) {
          next.execAttributeChanged(DataModelAggregationFieldAttributeChangedChain.this, attribute);
        }
      };
      callChain(methodInvocation, attribute);
    }
  }
}
