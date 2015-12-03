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
package org.eclipse.scout.rt.client.extension.ui.form.fields.tabbox;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class TabBoxChains {

  private TabBoxChains() {
  }

  protected abstract static class AbstractTabBoxChain extends AbstractExtensionChain<ITabBoxExtension<? extends AbstractTabBox>> {

    public AbstractTabBoxChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, ITabBoxExtension.class);
    }
  }

  public static class TabBoxTabSelectedChain extends AbstractTabBoxChain {

    public TabBoxTabSelectedChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execTabSelected(final IGroupBox selectedBox) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITabBoxExtension<? extends AbstractTabBox> next) {
          next.execTabSelected(TabBoxTabSelectedChain.this, selectedBox);
        }
      };
      callChain(methodInvocation, selectedBox);
    }
  }
}
