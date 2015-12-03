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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

public interface IContentAssistColumn<VALUE, LOOKUP_TYPE> extends IColumn<VALUE> {

  /**
   * code value decorator
   */
  Class<? extends ICodeType<?, LOOKUP_TYPE>> getCodeTypeClass();

  void setCodeTypeClass(Class<? extends ICodeType<?, LOOKUP_TYPE>> t);

  /**
   * custom value decorator
   */
  ILookupCall<LOOKUP_TYPE> getLookupCall();

  void setLookupCall(ILookupCall<LOOKUP_TYPE> c);

  ILookupCall<LOOKUP_TYPE> prepareLookupCall(ITableRow row);
}
