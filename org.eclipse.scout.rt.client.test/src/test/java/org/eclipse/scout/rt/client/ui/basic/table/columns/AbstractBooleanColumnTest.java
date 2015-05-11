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
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link AbstractBooleanColumn}
 */
public class AbstractBooleanColumnTest {

  @Test
  public void testPrepareEditInternal() throws ProcessingException {
    AbstractBooleanColumn column = new AbstractBooleanColumn() {
    };
    column.setMandatory(true);
    ITableRow row = Mockito.mock(ITableRow.class);
    IBooleanField field = (IBooleanField) column.prepareEditInternal(row);
    assertEquals("mandatory property to be progagated to field", column.isMandatory(), field.isMandatory());
  }

}
