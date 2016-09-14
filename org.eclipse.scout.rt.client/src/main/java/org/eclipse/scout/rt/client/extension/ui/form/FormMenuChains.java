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

import org.eclipse.scout.rt.client.extension.ui.action.IActionExtension;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.form.AbstractFormMenu;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class FormMenuChains {

  private FormMenuChains() {
  }

  public abstract static class AbstractFormMenuChain<FORM extends IForm> extends AbstractExtensionChain<IFormMenuExtension<FORM, ? extends AbstractFormMenu<FORM>>> {

    public AbstractFormMenuChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions, IFormMenuExtension.class);
    }
  }

  public static class FormMenuInitFormChain<FORM extends IForm> extends AbstractFormMenuChain<FORM> {

    public FormMenuInitFormChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions);
    }

    public void execInitForm(final FORM form) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormMenuExtension<FORM, ? extends AbstractFormMenu<FORM>> next) {
          next.execInitForm(FormMenuInitFormChain.this, form);
        }
      };
      callChain(methodInvocation, form);
    }
  }
}
