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
package org.eclipse.scout.rt.client.extension.ui.label;

import java.util.List;

import org.eclipse.scout.rt.client.ui.label.AbstractLabel;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class LabelChains {

  private LabelChains() {
  }

  protected abstract static class AbstractLabelChain extends AbstractExtensionChain<ILabelExtension<? extends AbstractLabel>> {

    public AbstractLabelChain(List<? extends ILabelExtension<? extends AbstractLabel>> extensions) {
      super(extensions, ILabelExtension.class);
    }
  }

  public static class LabelAppLinkActionChain extends AbstractLabelChain {

    public LabelAppLinkActionChain(List<? extends ILabelExtension<? extends AbstractLabel>> extensions) {
      super(extensions);
    }

    public void execAppLinkAction(final String ref) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ILabelExtension<? extends AbstractLabel> next) {
          next.execAppLinkAction(LabelAppLinkActionChain.this, ref);
        }
      };
      callChain(methodInvocation, ref);
    }
  }
}
