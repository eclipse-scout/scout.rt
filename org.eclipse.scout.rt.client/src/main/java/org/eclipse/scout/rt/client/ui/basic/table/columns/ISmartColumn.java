/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

public interface ISmartColumn<VALUE> extends IColumn<VALUE> {

  /**
   * see {@link #setSortCodesByDisplayText(boolean)}
   *
   * @since 04.11.2009
   */
  boolean isSortCodesByDisplayText();

  /**
   * Sorting of columns with attached {@link ICodeType} can be based on the codes sort order or their display texts.
   * Default is sort by codes sort order.
   *
   * @since 04.11.2009 ticket 82478
   */
  void setSortCodesByDisplayText(boolean b);

  /**
   * code value decorator
   */
  Class<? extends ICodeType<?, VALUE>> getCodeTypeClass();

  void setCodeTypeClass(Class<? extends ICodeType<?, VALUE>> t);

  /**
   * custom value decorator
   */
  ILookupCall<VALUE> getLookupCall();

  void setLookupCall(ILookupCall<VALUE> c);

  ILookupCall<VALUE> prepareLookupCall(ITableRow row);

}
