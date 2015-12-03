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
package org.eclipse.scout.rt.client.extension.ui.form.fields;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class ValueFieldChains {

  private ValueFieldChains() {
  }

  public static abstract class AbstractValueFieldChain<VALUE> extends AbstractExtensionChain<IValueFieldExtension<VALUE, ? extends AbstractValueField<VALUE>>> {

    public AbstractValueFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IValueFieldExtension.class);
    }

  }

  public static class ValueFieldExecValidateChain<VALUE> extends AbstractValueFieldChain<VALUE> {

    /**
     * @param extensions
     *          the list of all extension sorted reverse considering the execution order. The list must be immutable.
     */
    public ValueFieldExecValidateChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public VALUE execValidateValue(final VALUE rawValue) {
      MethodInvocation<VALUE> methodInvocation = new MethodInvocation<VALUE>() {
        @Override
        protected void callMethod(IValueFieldExtension<VALUE, ? extends AbstractValueField<VALUE>> next) {
          setReturnValue(next.execValidateValue(ValueFieldExecValidateChain.this, rawValue));
        }
      };
      callChain(methodInvocation, rawValue);
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
      callChain(methodInvocation, validValue);
      return methodInvocation.getReturnValue();
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
      callChain(methodInvocation, rawValue);
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
      callChain(methodInvocation, text);
      return methodInvocation.getReturnValue();
    }
  }
}
