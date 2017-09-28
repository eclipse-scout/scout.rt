/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.form;

import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

public final class FormChains {

  private FormChains() {
  }

  protected abstract static class AbstractFormChain extends AbstractExtensionChain<IFormExtension<? extends AbstractForm>> {

    public AbstractFormChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions, IFormExtension.class);
    }
  }

  public static class FormCloseTimerChain extends AbstractFormChain {

    public FormCloseTimerChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execCloseTimer() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) {
          next.execCloseTimer(FormCloseTimerChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class FormInactivityTimerChain extends AbstractFormChain {

    public FormInactivityTimerChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execInactivityTimer() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) {
          next.execInactivityTimer(FormInactivityTimerChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class FormStoredChain extends AbstractFormChain {

    public FormStoredChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execStored() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) {
          next.execStored(FormStoredChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class IsSaveNeededFieldsChain extends AbstractFormChain {

    public IsSaveNeededFieldsChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public boolean execIsSaveNeeded() {
      MethodInvocation<Boolean> methodInvocation = new MethodInvocation<Boolean>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) {
          setReturnValue(next.execIsSaveNeeded(IsSaveNeededFieldsChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  public static class FormCheckFieldsChain extends AbstractFormChain {

    public FormCheckFieldsChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public boolean execCheckFields() {
      MethodInvocation<Boolean> methodInvocation = new MethodInvocation<Boolean>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) {
          setReturnValue(next.execCheckFields(FormCheckFieldsChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  public static class FormResetSearchFilterChain extends AbstractFormChain {

    public FormResetSearchFilterChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execResetSearchFilter(final SearchFilter searchFilter) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) {
          next.execResetSearchFilter(FormResetSearchFilterChain.this, searchFilter);
        }
      };
      callChain(methodInvocation, searchFilter);
    }
  }

  public static class FormAddSearchTermsChain extends AbstractFormChain {

    public FormAddSearchTermsChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execAddSearchTerms(final SearchFilter search) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) {
          next.execAddSearchTerms(FormAddSearchTermsChain.this, search);
        }
      };
      callChain(methodInvocation, search);
    }
  }

  public static class FormOnVetoExceptionChain extends AbstractFormChain {

    public FormOnVetoExceptionChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execOnVetoException(final VetoException e, final int code) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) {
          next.execOnVetoException(FormOnVetoExceptionChain.this, e, code);
        }
      };
      callChain(methodInvocation, e, code);
    }
  }

  public static class FormFormActivatedChain extends AbstractFormChain {

    public FormFormActivatedChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execFormActivated() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) {
          next.execFormActivated(FormFormActivatedChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class FormDisposeFormChain extends AbstractFormChain {

    public FormDisposeFormChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execDisposeForm() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) {
          next.execDisposeForm(FormDisposeFormChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class FormTimerChain extends AbstractFormChain {

    public FormTimerChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execTimer(final String timerId) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) {
          next.execTimer(FormTimerChain.this, timerId);
        }
      };
      callChain(methodInvocation, timerId);
    }
  }

  public static class FormCreateFormDataChain extends AbstractFormChain {

    public FormCreateFormDataChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public AbstractFormData execCreateFormData() {
      MethodInvocation<AbstractFormData> methodInvocation = new MethodInvocation<AbstractFormData>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) {
          setReturnValue(next.execCreateFormData(FormCreateFormDataChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  public static class FormInitFormChain extends AbstractFormChain {

    public FormInitFormChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execInitForm() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) {
          next.execInitForm(FormInitFormChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class FormValidateChain extends AbstractFormChain {

    public FormValidateChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public boolean execValidate() {
      MethodInvocation<Boolean> methodInvocation = new MethodInvocation<Boolean>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) {
          setReturnValue(next.execValidate(FormValidateChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  public static class FormOnCloseRequestChain extends AbstractFormChain {

    public FormOnCloseRequestChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execOnCloseRequest(final boolean kill, final Set<Integer> enabledButtonSystemTypes) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) {
          next.execOnCloseRequest(FormOnCloseRequestChain.this, kill, enabledButtonSystemTypes);
        }
      };
      callChain(methodInvocation, kill, enabledButtonSystemTypes);
    }
  }

  public static class FormDataChangedChain extends AbstractFormChain {

    public FormDataChangedChain(List<? extends IFormExtension<? extends AbstractForm>> extensions) {
      super(extensions);
    }

    public void execDataChanged(final Object... dataTypes) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormExtension<? extends AbstractForm> next) {
          next.execDataChanged(FormDataChangedChain.this, dataTypes);
        }
      };
      callChain(methodInvocation, dataTypes);
    }
  }
}
