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
package org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractMixedSmartField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class MixedSmartFieldChains {

  private MixedSmartFieldChains() {
  }

  protected abstract static class AbstractMixedSmartFieldChain<VALUE, LOOKUP_KEY> extends AbstractExtensionChain<IMixedSmartFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractMixedSmartField<VALUE, LOOKUP_KEY>>> {

    public AbstractMixedSmartFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IMixedSmartFieldExtension.class);
    }
  }

  public static class MixedSmartFieldConvertValueToKeyChain<VALUE, LOOKUP_KEY> extends AbstractMixedSmartFieldChain<VALUE, LOOKUP_KEY> {

    public MixedSmartFieldConvertValueToKeyChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public LOOKUP_KEY execConvertValueToKey(final VALUE value) {
      MethodInvocation<LOOKUP_KEY> methodInvocation = new MethodInvocation<LOOKUP_KEY>() {
        @Override
        protected void callMethod(IMixedSmartFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractMixedSmartField<VALUE, LOOKUP_KEY>> next) {
          setReturnValue(next.execConvertValueToKey(MixedSmartFieldConvertValueToKeyChain.this, value));
        }
      };
      callChain(methodInvocation, value);
      return methodInvocation.getReturnValue();
    }
  }

  public static class MixedSmartFieldConvertKeyToValueChain<VALUE, LOOKUP_KEY> extends AbstractMixedSmartFieldChain<VALUE, LOOKUP_KEY> {

    public MixedSmartFieldConvertKeyToValueChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public VALUE execConvertKeyToValue(final LOOKUP_KEY key) {
      MethodInvocation<VALUE> methodInvocation = new MethodInvocation<VALUE>() {
        @Override
        protected void callMethod(IMixedSmartFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractMixedSmartField<VALUE, LOOKUP_KEY>> next) {
          setReturnValue(next.execConvertKeyToValue(MixedSmartFieldConvertKeyToValueChain.this, key));
        }
      };
      callChain(methodInvocation, key);
      return methodInvocation.getReturnValue();
    }
  }
}
