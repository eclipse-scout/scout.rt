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
package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

public final class SmartColumnChains {

  private SmartColumnChains() {
  }

  protected abstract static class AbstractSmartColumnChain<VALUE> extends AbstractExtensionChain<ISmartColumnExtension<VALUE, ? extends AbstractSmartColumn<VALUE>>> {

    public AbstractSmartColumnChain(List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions) {
      super(extensions, ISmartColumnExtension.class);
    }
  }

  public static class SmartColumnPrepareLookupChain<VALUE> extends AbstractSmartColumnChain<VALUE> {

    public SmartColumnPrepareLookupChain(List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions) {
      super(extensions);
    }

    public void execPrepareLookup(final ILookupCall<VALUE> call, final ITableRow row) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISmartColumnExtension<VALUE, ? extends AbstractSmartColumn<VALUE>> next) {
          next.execPrepareLookup(SmartColumnPrepareLookupChain.this, call, row);
        }
      };
      callChain(methodInvocation, call, row);
    }
  }
}
