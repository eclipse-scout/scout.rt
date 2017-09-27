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

  protected abstract static class AbstractSmartFieldChain<VALUE> extends AbstractExtensionChain<ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>>> {

    public AbstractSmartFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, ISmartFieldExtension.class);
    }
  }

  public static class SmartFieldFilterBrowseLookupResultChain<VALUE> extends AbstractSmartFieldChain<VALUE> {

    public SmartFieldFilterBrowseLookupResultChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execFilterBrowseLookupResult(final ILookupCall<VALUE> call, final List<ILookupRow<VALUE>> result) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>> next) {
          next.execFilterBrowseLookupResult(SmartFieldFilterBrowseLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation, call, result);
    }
  }

  public static class SmartFieldFilterKeyLookupResultChain<VALUE> extends AbstractSmartFieldChain<VALUE> {

    public SmartFieldFilterKeyLookupResultChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execFilterKeyLookupResult(final ILookupCall<VALUE> call, final List<ILookupRow<VALUE>> result) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>> next) {
          next.execFilterKeyLookupResult(SmartFieldFilterKeyLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation, call, result);
    }
  }

  public static class SmartFieldPrepareLookupChain<VALUE> extends AbstractSmartFieldChain<VALUE> {

    public SmartFieldPrepareLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPrepareLookup(final ILookupCall<VALUE> call) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>> next) {
          next.execPrepareLookup(SmartFieldPrepareLookupChain.this, call);
        }
      };
      callChain(methodInvocation, call);
    }
  }

  public static class SmartFieldPrepareTextLookupChain<VALUE> extends AbstractSmartFieldChain<VALUE> {

    public SmartFieldPrepareTextLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPrepareTextLookup(final ILookupCall<VALUE> call, final String text) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>> next) {
          next.execPrepareTextLookup(SmartFieldPrepareTextLookupChain.this, call, text);
        }
      };
      callChain(methodInvocation, call, text);
    }
  }

  public static class SmartFieldPrepareBrowseLookupChain<VALUE> extends AbstractSmartFieldChain<VALUE> {

    public SmartFieldPrepareBrowseLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPrepareBrowseLookup(final ILookupCall<VALUE> call) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>> next) {
          next.execPrepareBrowseLookup(SmartFieldPrepareBrowseLookupChain.this, call);
        }
      };
      callChain(methodInvocation, call);
    }
  }

  public static class SmartFieldFilterTextLookupResultChain<VALUE> extends AbstractSmartFieldChain<VALUE> {

    public SmartFieldFilterTextLookupResultChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execFilterTextLookupResult(final ILookupCall<VALUE> call, final List<ILookupRow<VALUE>> result) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>> next) {
          next.execFilterTextLookupResult(SmartFieldFilterTextLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation, call, result);
    }
  }

  public static class SmartFieldPrepareRecLookupChain<VALUE> extends AbstractSmartFieldChain<VALUE> {

    public SmartFieldPrepareRecLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPrepareRecLookup(final ILookupCall<VALUE> call, final VALUE parentKey) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>> next) {
          next.execPrepareRecLookup(SmartFieldPrepareRecLookupChain.this, call, parentKey);
        }
      };
      callChain(methodInvocation, call, parentKey);
    }
  }

  public static class SmartFieldFilterLookupResultChain<VALUE> extends AbstractSmartFieldChain<VALUE> {

    public SmartFieldFilterLookupResultChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execFilterLookupResult(final ILookupCall<VALUE> call, final List<ILookupRow<VALUE>> result) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>> next) {
          next.execFilterLookupResult(SmartFieldFilterLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation, call, result);
    }
  }

  public static class SmartFieldFilterRecLookupResultChain<VALUE> extends AbstractSmartFieldChain<VALUE> {

    public SmartFieldFilterRecLookupResultChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execFilterRecLookupResult(final ILookupCall<VALUE> call, final List<ILookupRow<VALUE>> result) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>> next) {
          next.execFilterRecLookupResult(SmartFieldFilterRecLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation, call, result);
    }
  }

  public static class SmartFieldPrepareKeyLookupChain<VALUE> extends AbstractSmartFieldChain<VALUE> {

    public SmartFieldPrepareKeyLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPrepareKeyLookup(final ILookupCall<VALUE> call, final VALUE key) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>> next) {
          next.execPrepareKeyLookup(SmartFieldPrepareKeyLookupChain.this, call, key);
        }
      };
      callChain(methodInvocation, call, key);
    }
  }
}
