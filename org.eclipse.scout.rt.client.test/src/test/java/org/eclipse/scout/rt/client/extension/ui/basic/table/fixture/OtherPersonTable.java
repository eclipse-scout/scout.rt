/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.basic.table.fixture;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;

public class OtherPersonTable extends AbstractPersonTable {

  public PhoneNumberColumn getPhoneNumberColumn() {
    return getColumnSet().getColumnByClass(PhoneNumberColumn.class);
  }

  @Order(30)
  public class PhoneNumberColumn extends AbstractStringColumn {
  }
}
