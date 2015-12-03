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
package org.eclipse.scout.rt.client.extension.ui.form;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class FormHandlerChains {

  private FormHandlerChains() {
  }

  protected abstract static class AbstractFormHandlerChain extends AbstractExtensionChain<IFormHandlerExtension<? extends AbstractFormHandler>> {

    public AbstractFormHandlerChain(List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions) {
      super(extensions, IFormHandlerExtension.class);
    }
  }

  public static class FormHandlerPostLoadChain extends AbstractFormHandlerChain {

    public FormHandlerPostLoadChain(List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions) {
      super(extensions);
    }

    public void execPostLoad() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormHandlerExtension<? extends AbstractFormHandler> next) {
          next.execPostLoad(FormHandlerPostLoadChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class FormHandlerValidateChain extends AbstractFormHandlerChain {

    public FormHandlerValidateChain(List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions) {
      super(extensions);
    }

    public boolean execValidate() {
      MethodInvocation<Boolean> methodInvocation = new MethodInvocation<Boolean>() {
        @Override
        protected void callMethod(IFormHandlerExtension<? extends AbstractFormHandler> next) {
          setReturnValue(next.execValidate(FormHandlerValidateChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  public static class FormHandlerLoadChain extends AbstractFormHandlerChain {

    public FormHandlerLoadChain(List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions) {
      super(extensions);
    }

    public void execLoad() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormHandlerExtension<? extends AbstractFormHandler> next) {
          next.execLoad(FormHandlerLoadChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class FormHandlerStoreChain extends AbstractFormHandlerChain {

    public FormHandlerStoreChain(List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions) {
      super(extensions);
    }

    public void execStore() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormHandlerExtension<? extends AbstractFormHandler> next) {
          next.execStore(FormHandlerStoreChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class FormHandlerDiscardChain extends AbstractFormHandlerChain {

    public FormHandlerDiscardChain(List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions) {
      super(extensions);
    }

    public void execDiscard() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormHandlerExtension<? extends AbstractFormHandler> next) {
          next.execDiscard(FormHandlerDiscardChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class FormHandlerCheckFieldsChain extends AbstractFormHandlerChain {

    public FormHandlerCheckFieldsChain(List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions) {
      super(extensions);
    }

    public boolean execCheckFields() {
      MethodInvocation<Boolean> methodInvocation = new MethodInvocation<Boolean>() {
        @Override
        protected void callMethod(IFormHandlerExtension<? extends AbstractFormHandler> next) {
          setReturnValue(next.execCheckFields(FormHandlerCheckFieldsChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  public static class FormHandlerFinallyChain extends AbstractFormHandlerChain {

    public FormHandlerFinallyChain(List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions) {
      super(extensions);
    }

    public void execFinally() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormHandlerExtension<? extends AbstractFormHandler> next) {
          next.execFinally(FormHandlerFinallyChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }
}
