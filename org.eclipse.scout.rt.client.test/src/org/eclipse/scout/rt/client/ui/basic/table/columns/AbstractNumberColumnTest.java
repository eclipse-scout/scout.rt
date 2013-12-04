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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.DecimalFormat;

import org.easymock.EasyMock;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberField;
import org.junit.Test;

public class AbstractNumberColumnTest extends AbstractNumberColumn<Integer> {

  @Override
  protected Integer getConfiguredMinValue() {
    return null;
  }

  @Override
  protected Integer getConfiguredMaxValue() {
    return null;
  }

  @Override
  protected AbstractNumberField<Integer> getEditorField() {
    return new AbstractIntegerField() {
    };
  }

  @Test
  public void testDecimalFormatHandling() {
    DecimalFormat format = getFormat();
    assertTrue("expected groupingUsed-property set to true as default", format.isGroupingUsed());
    assertTrue("expected groupingUsed-property set to true as default", isGroupingUsed());

    format.setGroupingUsed(false);
    setFormat(format);
    format = getFormat();
    assertFalse("expected groupingUsed-property set to false after setting format", format.isGroupingUsed());
    assertFalse("expected groupingUsed-property set to false after setting format", isGroupingUsed());

    setGroupingUsed(true);
    format = getFormat();
    assertTrue("expected groupingUsed-property set to true after using convenience setter", format.isGroupingUsed());
    assertTrue("expected groupingUsed-property set to true after using convenience setter", isGroupingUsed());
  }

  @Test
  public void testPrepareEditInternal() throws ProcessingException {
    setGroupingUsed(false);
    ITableRow row = EasyMock.createMock(ITableRow.class);
    AbstractIntegerField field = (AbstractIntegerField) prepareEditInternal(row);
    assertFalse("expected groupingUsed property to be propagated to field", field.isGroupingUsed());

    setGroupingUsed(true);
    field = (AbstractIntegerField) prepareEditInternal(row);
    assertTrue("expected groupingUsed property to be propagated to field", field.isGroupingUsed());
  }
}
