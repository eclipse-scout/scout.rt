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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link AbstractStringColumn}
 */
public class AbstractStringColumnTest {

  @Test
  public void testPrepareEditInternal() throws ProcessingException {
    AbstractStringColumn column = new AbstractStringColumn() {
    };
    column.setCssClass("myCSSClass");
    column.setInputMasked(true);
    column.setMandatory(true);
    ITableRow row = Mockito.mock(ITableRow.class);
    IStringField field = (IStringField) column.prepareEditInternal(row);
    assertEquals("input masked property to be progagated to field", column.isInputMasked(), field.isInputMasked());
    assertEquals("mandatory property to be progagated to field", column.isMandatory(), field.isMandatory());
    //TODO jgu
//    assertEquals("css class property to be progagated to field", column.getCssClass(), field.getCssClass());
  }

}
