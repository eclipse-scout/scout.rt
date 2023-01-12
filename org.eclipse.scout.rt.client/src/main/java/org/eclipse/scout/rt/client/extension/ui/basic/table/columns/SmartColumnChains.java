/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
      callChain(methodInvocation);
    }
  }
}
