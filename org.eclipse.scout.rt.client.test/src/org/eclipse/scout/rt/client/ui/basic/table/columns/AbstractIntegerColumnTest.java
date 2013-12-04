/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import static org.junit.Assert.assertEquals;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.easymock.EasyMock;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.junit.Test;

public class AbstractIntegerColumnTest extends AbstractIntegerColumn {

  @Test
  public void testFormattingInDecorateCellInternal() throws ProcessingException {
    ITableRow row = EasyMock.createMock(ITableRow.class);
    Cell cell = new Cell();
    Integer testValue = Integer.valueOf(-123456789);
    cell.setValue(testValue);

    DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.CANADA_FRENCH);
    setFormat(df);

    decorateCellInternal(cell, row);
    assertEquals("cell text not formatted as expected", df.format(testValue), cell.getText());
  }

}
