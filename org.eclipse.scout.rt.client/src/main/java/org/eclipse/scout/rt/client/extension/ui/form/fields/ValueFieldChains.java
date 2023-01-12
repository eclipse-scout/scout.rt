/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class ValueFieldChains {

  private ValueFieldChains() {
  }

  public abstract static class AbstractValueFieldChain<VALUE> extends AbstractExtensionChain<IValueFieldExtension<VALUE, ? extends AbstractValueField<VALUE>>> {

    public AbstractValueFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IValueFieldExtension.class);
    }
  }

  public static class ValueFieldValidateValueChain<VALUE> extends AbstractValueFieldChain<VALUE> {

    public ValueFieldValidateValueChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public VALUE execValidateValue(final VALUE rawValue) {
      MethodInvocation<VALUE> methodInvocation = new MethodInvocation<VALUE>() {
        @Override
        protected void callMethod(IValueFieldExtension<VALUE, ? extends AbstractValueField<VALUE>> next) {
          setReturnValue(next.execValidateValue(ValueFieldValidateValueChain.this, rawValue));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  public static class ValueFieldFormatValueChain<VALUE> extends AbstractValueFieldChain<VALUE> {

    public ValueFieldFormatValueChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public String execFormatValue(final VALUE validValue) {
      MethodInvocation<String> methodInvocation = new MethodInvocation<String>() {
        @Override
        protected void callMethod(IValueFieldExtension<VALUE, ? extends AbstractValueField<VALUE>> next) {
          setReturnValue(next.execFormatValue(ValueFieldFormatValueChain.this, validValue));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  public static class ValueFieldChangedValueChain<VALUE> extends AbstractValueFieldChain<VALUE> {

    public ValueFieldChangedValueChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execChangedValue() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IValueFieldExtension<VALUE, ? extends AbstractValueField<VALUE>> next) {
          next.execChangedValue(ValueFieldChangedValueChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class ValueFieldParseValueChain<VALUE> extends AbstractValueFieldChain<VALUE> {

    public ValueFieldParseValueChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public VALUE execParseValue(final String text) {
      MethodInvocation<VALUE> methodInvocation = new MethodInvocation<VALUE>() {
        @Override
        protected void callMethod(IValueFieldExtension<VALUE, ? extends AbstractValueField<VALUE>> next) {
          setReturnValue(next.execParseValue(ValueFieldParseValueChain.this, text));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }
}
