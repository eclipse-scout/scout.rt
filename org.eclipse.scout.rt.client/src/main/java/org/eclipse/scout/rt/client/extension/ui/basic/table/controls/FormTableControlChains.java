/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.basic.table.controls;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.action.IActionExtension;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.basic.table.controls.AbstractTableControl;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class FormTableControlChains {

  private FormTableControlChains() {
  }

  protected abstract static class AbstractTableControlChain extends AbstractExtensionChain<IFormTableControlExtension<? extends AbstractTableControl>> {

    public AbstractTableControlChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions, IFormTableControlExtension.class);
    }
  }

  public static class TableControlInitFormChain extends AbstractTableControlChain {

    public TableControlInitFormChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions);
    }

    public void execInitForm() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {

        @Override
        protected void callMethod(IFormTableControlExtension<? extends AbstractTableControl> next) {
          next.execInitForm(TableControlInitFormChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }
}
