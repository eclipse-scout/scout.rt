/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

/**
 *
 */
public class FormFieldChains {

  public static abstract class AbstractFormFieldChain extends AbstractExtensionChain<IFormFieldExtension<? extends AbstractFormField>> {

    public AbstractFormFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IFormFieldExtension.class);
    }

  }

  public static class FormFieldDataChangedChain extends AbstractFormFieldChain {

    public FormFieldDataChangedChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execDataChanged(final Object... dataTypes) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormFieldExtension<? extends AbstractFormField> next) throws ProcessingException {
          next.execDataChanged(FormFieldDataChangedChain.this, dataTypes);
        }
      };
      callChain(methodInvocation, dataTypes);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

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

    public void execChangedMasterValue(final Object newMasterValue) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormFieldExtension<? extends AbstractFormField> next) throws ProcessingException {
          next.execChangedMasterValue(FormFieldChangedMasterValueChain.this, newMasterValue);
        }
      };
      callChain(methodInvocation, newMasterValue);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class FormFieldDisposeFieldChain extends AbstractFormFieldChain {

    public FormFieldDisposeFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execDisposeField() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormFieldExtension<? extends AbstractFormField> next) throws ProcessingException {
          next.execDisposeField(FormFieldDisposeFieldChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class FormFieldInitFieldChain extends AbstractFormFieldChain {

    public FormFieldInitFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execInitField() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormFieldExtension<? extends AbstractFormField> next) throws ProcessingException {
          next.execInitField(FormFieldInitFieldChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

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

    public void execMarkSaved() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormFieldExtension<? extends AbstractFormField> next) throws ProcessingException {
          next.execMarkSaved(FormFieldMarkSavedChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class FormFieldIsEmptyChain extends AbstractFormFieldChain {

    public FormFieldIsEmptyChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public boolean execIsEmpty() throws ProcessingException {
      MethodInvocation<Boolean> methodInvocation = new MethodInvocation<Boolean>() {
        @Override
        protected void callMethod(IFormFieldExtension<? extends AbstractFormField> next) throws ProcessingException {
          setReturnValue(next.execIsEmpty(FormFieldIsEmptyChain.this));
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }

  public static class FormFieldIsSaveNeededChain extends AbstractFormFieldChain {

    public FormFieldIsSaveNeededChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public boolean execIsSaveNeeded() throws ProcessingException {
      MethodInvocation<Boolean> methodInvocation = new MethodInvocation<Boolean>() {
        @Override
        protected void callMethod(IFormFieldExtension<? extends AbstractFormField> next) throws ProcessingException {
          setReturnValue(next.execIsSaveNeeded(FormFieldIsSaveNeededChain.this));
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }
}
