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
import org.eclipse.scout.rt.dataobject.enumeration.IEnum;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("25538f8b-3433-40c1-808e-9c387866c4e7")
public abstract class AbstractIEnumColumn<VALUE extends IEnum> extends AbstractColumn<VALUE> {

  @Override
  protected String formatValueInternal(ITableRow row, IEnum value) {
    return value != null ? value.text() : "";
  }
}
