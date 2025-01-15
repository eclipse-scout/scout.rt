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
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Column sorting string values with alpha numeric comparator.
 */
@ClassId("86f11c9b-cde4-4619-9b2a-6bbcdd330feb")
public abstract class AbstractAlphanumericSortingStringColumn extends AbstractStringColumn implements IAlphanumericSortingStringColumn {

  public AbstractAlphanumericSortingStringColumn() {
    this(true);
  }

  public AbstractAlphanumericSortingStringColumn(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  public int compareTableRows(ITableRow r1, ITableRow r2) {
    return StringUtility.ALPHANUMERIC_COMPARATOR_IGNORE_CASE.compare(getValue(r1), getValue(r2));
  }
}
