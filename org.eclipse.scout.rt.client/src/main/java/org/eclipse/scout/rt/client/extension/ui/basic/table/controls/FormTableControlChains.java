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
