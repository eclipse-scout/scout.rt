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

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineTableField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class OutlineTableFieldChains {

  private OutlineTableFieldChains() {
  }

  protected abstract static class AbstractOutlineTableFieldChain extends AbstractExtensionChain<IOutlineTableFieldExtension<? extends AbstractOutlineTableField>> {

    public AbstractOutlineTableFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IOutlineTableFieldExtension.class);
    }
  }

  public static class OutlineTableFieldTableTitleChangedChain extends AbstractOutlineTableFieldChain {

    public OutlineTableFieldTableTitleChangedChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execTableTitleChanged() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IOutlineTableFieldExtension<? extends AbstractOutlineTableField> next) {
          next.execTableTitleChanged(OutlineTableFieldTableTitleChangedChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }
}
