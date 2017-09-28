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
