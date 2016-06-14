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
package org.eclipse.scout.rt.shared.services.common.code;

import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

/**
 * @since Scout 4.0.0
 */
public interface ICodeRow<KEY_TYPE> extends ILookupRow<KEY_TYPE> {
  /**
   * @return {@link ICodeRow} with given text
   */
  @Override
  ICodeRow<KEY_TYPE> withText(String text);

  /**
   * @return {@link ICodeRow} with given icon id
   */
  @Override
  ICodeRow<KEY_TYPE> withIconId(String iconId);

  /**
   * @return {@link ICodeRow} with given tooltipText
   */
  @Override
  ICodeRow<KEY_TYPE> withTooltipText(String tooltipText);

  /**
   * @return {@link ICodeRow} with given foregroundColor
   */
  @Override
  ICodeRow<KEY_TYPE> withForegroundColor(String foregroundColor);

  /**
   * @return {@link ICodeRow} with given backgroundColor
   */
  @Override
  ICodeRow<KEY_TYPE> withBackgroundColor(String backgroundColor);

  /**
   * @return {@link ICodeRow} with given font
   */
  @Override
  ICodeRow<KEY_TYPE> withFont(FontSpec font);

  /**
   * @return {@link ICodeRow} with given css class
   */
  @Override
  ICodeRow<KEY_TYPE> withCssClass(String cssClass);

  /**
   * @return {@link ICodeRow} with active set to the given value.
   */
  @Override
  ICodeRow<KEY_TYPE> withActive(boolean active);

  /**
   * @return {@link ICodeRow} with enabled set to the given value.
   */
  @Override
  ICodeRow<KEY_TYPE> withEnabled(boolean enabled);

  /**
   * @return {@link ICodeRow} with the given parent key.
   */
  @Override
  ICodeRow<KEY_TYPE> withParentKey(KEY_TYPE parentKey);

  /**
   * Lookup row with some additional data associated with it.
   */
  @Override
  ICodeRow<KEY_TYPE> withAdditionalTableRowData(AbstractTableRowData bean);

  long getPartitionId();

  ICodeRow<KEY_TYPE> withPartitionId(long partitionId);

  Number getValue();

  ICodeRow<KEY_TYPE> withValue(Number value);

  String getExtKey();

  ICodeRow<KEY_TYPE> withExtKey(String extKey);

  /**
   * @return
   */
  double getOrder();

  ICodeRow<KEY_TYPE> withOrder(double order);

}
