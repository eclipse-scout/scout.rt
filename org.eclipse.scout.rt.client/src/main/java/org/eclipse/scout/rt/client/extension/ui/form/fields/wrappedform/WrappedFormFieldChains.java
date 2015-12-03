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
package org.eclipse.scout.rt.client.extension.ui.form.fields.wrappedform;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class WrappedFormFieldChains {

  private WrappedFormFieldChains() {
  }

  protected abstract static class AbstractWrappedFormFieldChain<FORM extends IForm> extends AbstractExtensionChain<IWrappedFormFieldExtension<FORM, ? extends AbstractWrappedFormField<FORM>>> {

    public AbstractWrappedFormFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IWrappedFormFieldExtension.class);
    }
  }

  public static class WrappedFormFieldInnerFormChangedChain<FORM extends IForm> extends AbstractWrappedFormFieldChain<FORM> {

    public WrappedFormFieldInnerFormChangedChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execInnerFormChanged(final FORM oldInnerForm, final FORM newInnerForm) {
      MethodInvocation<Void> methodInvocation = new MethodInvocation<Void>() {
        @Override
        protected void callMethod(IWrappedFormFieldExtension<FORM, ? extends AbstractWrappedFormField<FORM>> next) {
          next.execInnerFormChanged(WrappedFormFieldInnerFormChangedChain.this, oldInnerForm, newInnerForm);
        }
      };
      callChain(methodInvocation, oldInnerForm, newInnerForm);
    }
  }
}
