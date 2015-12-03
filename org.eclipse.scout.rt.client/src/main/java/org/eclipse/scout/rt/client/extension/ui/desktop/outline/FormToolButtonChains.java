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
package org.eclipse.scout.rt.client.extension.ui.desktop.outline;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.action.IActionExtension;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractFormToolButton;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class FormToolButtonChains {

  private FormToolButtonChains() {
  }

  protected abstract static class AbstractFormToolButtonChain<FORM extends IForm> extends AbstractExtensionChain<IFormToolButtonExtension<FORM, ? extends AbstractFormToolButton<FORM>>> {

    public AbstractFormToolButtonChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions, IFormToolButtonExtension.class);
    }
  }

  public static class FormToolButtonInitFormChain<FORM extends IForm> extends AbstractFormToolButtonChain<FORM> {

    public FormToolButtonInitFormChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions);
    }

    public void execInitForm(final FORM form) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormToolButtonExtension<FORM, ? extends AbstractFormToolButton<FORM>> next) throws Exception {
          next.execInitForm(FormToolButtonInitFormChain.this, form);
        }
      };
      callChain(methodInvocation, form);
    }
  }
}
