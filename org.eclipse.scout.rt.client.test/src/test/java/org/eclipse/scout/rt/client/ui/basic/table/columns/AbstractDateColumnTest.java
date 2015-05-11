/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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
import static org.mockito.Mockito.mock;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.junit.Test;

/**
 * Tests for {@link AbstractDateColumn}
 */
public class AbstractDateColumnTest {

  @Test
  public void testPrepareEditInternal() throws ProcessingException {
    AbstractDateColumn column = new AbstractDateColumn() {
    };
    column.setMandatory(true);
    column.setHasTime(true);
    ITableRow row = mock(ITableRow.class);
    IDateField field = (IDateField) column.prepareEditInternal(row);
    assertEquals("mandatory property to be progagated to field", column.isMandatory(), field.isMandatory());
    assertEquals("mandatory property to be progagated to field", column.isHasTime(), field.isHasTime());
  }

}
