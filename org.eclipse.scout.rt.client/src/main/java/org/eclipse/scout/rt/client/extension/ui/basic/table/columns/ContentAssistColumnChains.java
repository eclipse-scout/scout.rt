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
package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractContentAssistColumn;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

public final class ContentAssistColumnChains {

  private ContentAssistColumnChains() {
  }

  protected abstract static class AbstractContentAssistColumnChain<VALUE, LOOKUP_TYPE> extends AbstractExtensionChain<IContentAssistColumnExtension<VALUE, LOOKUP_TYPE, ? extends AbstractContentAssistColumn<VALUE, LOOKUP_TYPE>>> {

    public AbstractContentAssistColumnChain(List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions) {
      super(extensions, IContentAssistColumnExtension.class);
    }
  }

  public static class ContentAssistColumnConvertValueToKeyChain<VALUE, LOOKUP_TYPE> extends AbstractContentAssistColumnChain<VALUE, LOOKUP_TYPE> {

    public ContentAssistColumnConvertValueToKeyChain(List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions) {
      super(extensions);
    }

    public LOOKUP_TYPE execConvertValueToKey(final VALUE value) {
      MethodInvocation<LOOKUP_TYPE> methodInvocation = new MethodInvocation<LOOKUP_TYPE>() {
        @Override
        protected void callMethod(IContentAssistColumnExtension<VALUE, LOOKUP_TYPE, ? extends AbstractContentAssistColumn<VALUE, LOOKUP_TYPE>> next) {
          setReturnValue(next.execConvertValueToKey(ContentAssistColumnConvertValueToKeyChain.this, value));
        }
      };
      callChain(methodInvocation, value);
      return methodInvocation.getReturnValue();
    }
  }

  public static class ContentAssistColumnPrepareLookupChain<VALUE, LOOKUP_TYPE> extends AbstractContentAssistColumnChain<VALUE, LOOKUP_TYPE> {

    public ContentAssistColumnPrepareLookupChain(List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions) {
      super(extensions);
    }

    public void execPrepareLookup(final ILookupCall<LOOKUP_TYPE> call, final ITableRow row) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IContentAssistColumnExtension<VALUE, LOOKUP_TYPE, ? extends AbstractContentAssistColumn<VALUE, LOOKUP_TYPE>> next) {
          next.execPrepareLookup(ContentAssistColumnPrepareLookupChain.this, call, row);
        }
      };
      callChain(methodInvocation, call, row);
    }
  }
}
