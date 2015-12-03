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
package org.eclipse.scout.rt.client.ui.form.fields.tablefield;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData;

/**
 * Convenience Table Field that use an 'Array based TableData' (was the default with Eclipse Luna). See Bug 455282
 *
 * @since 4.3.0 (Mars-M5)
 */
@ClassId("f60194a6-f2a8-4744-947a-21b19f82c723")
@FormData(value = AbstractTableFieldData.class, sdkCommand = SdkCommand.USE, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE)
public class AbstractArrayTableField<T extends ITable> extends AbstractTableField<T> {

  public AbstractArrayTableField() {
    this(true);
  }

  public AbstractArrayTableField(boolean callInitializer) {
    super(callInitializer);
  }

}
