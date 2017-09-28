/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
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
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

public final class FormFieldChains {

  private FormFieldChains() {
  }

  public abstract static class AbstractFormFieldChain extends AbstractExtensionChain<IFormFieldExtension<? extends AbstractFormField>> {

    public AbstractFormFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IFormFieldExtension.class);
    }

  }

  public static class FormFieldDataChangedChain extends AbstractFormFieldChain {

    public FormFieldDataChangedChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execDataChanged(final Object... dataTypes) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormFieldExtension<? extends AbstractFormField> next) {
          next.execDataChanged(FormFieldDataChangedChain.this, dataTypes);
        }
      };
      callChain(methodInvocation, dataTypes);
    }
  }

  public static class FormFieldAddSearchTermsChain extends AbstractFormFieldChain {

    public FormFieldAddSearchTermsChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execAddSearchTerms(final SearchFilter search) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormFieldExtension<? extends AbstractFormField> next) {
          next.execAddSearchTerms(FormFieldAddSearchTermsChain.this, search);
        }
      };
      callChain(methodInvocation, search);
    }
  }

  public static class FormFieldChangedMasterValueChain extends AbstractFormFieldChain {

    public FormFieldChangedMasterValueChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execChangedMasterValue(final Object newMasterValue) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormFieldExtension<? extends AbstractFormField> next) {
          next.execChangedMasterValue(FormFieldChangedMasterValueChain.this, newMasterValue);
        }
      };
      callChain(methodInvocation, newMasterValue);
    }
  }

  public static class FormFieldDisposeFieldChain extends AbstractFormFieldChain {

    public FormFieldDisposeFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execDisposeField() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormFieldExtension<? extends AbstractFormField> next) {
          next.execDisposeField(FormFieldDisposeFieldChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class FormFieldInitFieldChain extends AbstractFormFieldChain {

    public FormFieldInitFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execInitField() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormFieldExtension<? extends AbstractFormField> next) {
          next.execInitField(FormFieldInitFieldChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class FormFieldCalculateVisibleChain extends AbstractFormFieldChain {

    public FormFieldCalculateVisibleChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public boolean execCalculateVisible() {
      MethodInvocation<Boolean> methodInvocation = new MethodInvocation<Boolean>() {
        @Override
        protected void callMethod(IFormFieldExtension<? extends AbstractFormField> next) {
          setReturnValue(next.execCalculateVisible(FormFieldCalculateVisibleChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  public static class FormFieldMarkSavedChain extends AbstractFormFieldChain {

    public FormFieldMarkSavedChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execMarkSaved() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormFieldExtension<? extends AbstractFormField> next) {
          next.execMarkSaved(FormFieldMarkSavedChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class FormFieldIsEmptyChain extends AbstractFormFieldChain {

    public FormFieldIsEmptyChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public boolean execIsEmpty() {
      MethodInvocation<Boolean> methodInvocation = new MethodInvocation<Boolean>() {
        @Override
        protected void callMethod(IFormFieldExtension<? extends AbstractFormField> next) {
          setReturnValue(next.execIsEmpty(FormFieldIsEmptyChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  public static class FormFieldIsSaveNeededChain extends AbstractFormFieldChain {

    public FormFieldIsSaveNeededChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public boolean execIsSaveNeeded() {
      MethodInvocation<Boolean> methodInvocation = new MethodInvocation<Boolean>() {
        @Override
        protected void callMethod(IFormFieldExtension<? extends AbstractFormField> next) {
          setReturnValue(next.execIsSaveNeeded(FormFieldIsSaveNeededChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }
}
