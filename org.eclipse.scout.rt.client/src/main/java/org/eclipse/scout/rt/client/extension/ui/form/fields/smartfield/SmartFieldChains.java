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
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public final class SmartFieldChains {

  private SmartFieldChains() {
  }

  protected abstract static class AbstractSmartField2Chain<VALUE> extends AbstractExtensionChain<ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>>> {

    public AbstractSmartField2Chain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, ISmartFieldExtension.class);
    }
  }

  public static class SmartField2FilterBrowseLookupResultChain<VALUE> extends AbstractSmartField2Chain<VALUE> {

    public SmartField2FilterBrowseLookupResultChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execFilterBrowseLookupResult(final ILookupCall<VALUE> call, final List<ILookupRow<VALUE>> result) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>> next) {
          next.execFilterBrowseLookupResult(SmartField2FilterBrowseLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation, call, result);
    }
  }

  public static class SmartField2FilterKeyLookupResultChain<VALUE> extends AbstractSmartField2Chain<VALUE> {

    public SmartField2FilterKeyLookupResultChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execFilterKeyLookupResult(final ILookupCall<VALUE> call, final List<ILookupRow<VALUE>> result) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>> next) {
          next.execFilterKeyLookupResult(SmartField2FilterKeyLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation, call, result);
    }
  }

  public static class SmartField2PrepareLookupChain<VALUE> extends AbstractSmartField2Chain<VALUE> {

    public SmartField2PrepareLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPrepareLookup(final ILookupCall<VALUE> call) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>> next) {
          next.execPrepareLookup(SmartField2PrepareLookupChain.this, call);
        }
      };
      callChain(methodInvocation, call);
    }
  }

  public static class SmartField2PrepareTextLookupChain<VALUE> extends AbstractSmartField2Chain<VALUE> {

    public SmartField2PrepareTextLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPrepareTextLookup(final ILookupCall<VALUE> call, final String text) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>> next) {
          next.execPrepareTextLookup(SmartField2PrepareTextLookupChain.this, call, text);
        }
      };
      callChain(methodInvocation, call, text);
    }
  }

  public static class SmartField2PrepareBrowseLookupChain<VALUE> extends AbstractSmartField2Chain<VALUE> {

    public SmartField2PrepareBrowseLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPrepareBrowseLookup(final ILookupCall<VALUE> call, final String browseHint) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>> next) {
          next.execPrepareBrowseLookup(SmartField2PrepareBrowseLookupChain.this, call, browseHint);
        }
      };
      callChain(methodInvocation, call, browseHint);
    }
  }

  public static class SmartField2FilterTextLookupResultChain<VALUE> extends AbstractSmartField2Chain<VALUE> {

    public SmartField2FilterTextLookupResultChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execFilterTextLookupResult(final ILookupCall<VALUE> call, final List<ILookupRow<VALUE>> result) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>> next) {
          next.execFilterTextLookupResult(SmartField2FilterTextLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation, call, result);
    }
  }

  public static class SmartField2PrepareRecLookupChain<VALUE> extends AbstractSmartField2Chain<VALUE> {

    public SmartField2PrepareRecLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPrepareRecLookup(final ILookupCall<VALUE> call, final VALUE parentKey) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>> next) {
          next.execPrepareRecLookup(SmartField2PrepareRecLookupChain.this, call, parentKey);
        }
      };
      callChain(methodInvocation, call, parentKey);
    }
  }

  public static class SmartField2FilterLookupResultChain<VALUE> extends AbstractSmartField2Chain<VALUE> {

    public SmartField2FilterLookupResultChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execFilterLookupResult(final ILookupCall<VALUE> call, final List<ILookupRow<VALUE>> result) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>> next) {
          next.execFilterLookupResult(SmartField2FilterLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation, call, result);
    }
  }

  public static class SmartField2FilterRecLookupResultChain<VALUE> extends AbstractSmartField2Chain<VALUE> {

    public SmartField2FilterRecLookupResultChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execFilterRecLookupResult(final ILookupCall<VALUE> call, final List<ILookupRow<VALUE>> result) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>> next) {
          next.execFilterRecLookupResult(SmartField2FilterRecLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation, call, result);
    }
  }

  public static class SmartField2PrepareKeyLookupChain<VALUE> extends AbstractSmartField2Chain<VALUE> {

    public SmartField2PrepareKeyLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPrepareKeyLookup(final ILookupCall<VALUE> call, final VALUE key) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>> next) {
          next.execPrepareKeyLookup(SmartField2PrepareKeyLookupChain.this, call, key);
        }
      };
      callChain(methodInvocation, call, key);
    }
  }
}
