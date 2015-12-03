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
package org.eclipse.scout.rt.client.extension.ui.form.fields.radiobuttongroup;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public final class RadioButtonGroupChains {

  private RadioButtonGroupChains() {
  }

  protected abstract static class AbstractRadioButtonGroupChain<T> extends AbstractExtensionChain<IRadioButtonGroupExtension<T, ? extends AbstractRadioButtonGroup<T>>> {

    public AbstractRadioButtonGroupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IRadioButtonGroupExtension.class);
    }
  }

  public static class RadioButtonGroupPrepareLookupChain<T> extends AbstractRadioButtonGroupChain<T> {

    public RadioButtonGroupPrepareLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPrepareLookup(final ILookupCall<T> call) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IRadioButtonGroupExtension<T, ? extends AbstractRadioButtonGroup<T>> next) {
          next.execPrepareLookup(RadioButtonGroupPrepareLookupChain.this, call);
        }
      };
      callChain(methodInvocation, call);
    }
  }

  public static class RadioButtonGroupFilterLookupResultChain<T> extends AbstractRadioButtonGroupChain<T> {

    public RadioButtonGroupFilterLookupResultChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execFilterLookupResult(final ILookupCall<T> call, final List<ILookupRow<T>> result) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IRadioButtonGroupExtension<T, ? extends AbstractRadioButtonGroup<T>> next) {
          next.execFilterLookupResult(RadioButtonGroupFilterLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation, call, result);
    }
  }
}
