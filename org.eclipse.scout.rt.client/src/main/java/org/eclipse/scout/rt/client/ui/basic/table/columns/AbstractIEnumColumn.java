/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.dataobject.enumeration.IEnum;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("25538f8b-3433-40c1-808e-9c387866c4e7")
public class AbstractIEnumColumn<VALUE extends IEnum> extends AbstractColumn<VALUE> {

  @Override
  protected String formatValueInternal(ITableRow row, IEnum value) {
    return value != null ? value.text() : "";
  }
}
