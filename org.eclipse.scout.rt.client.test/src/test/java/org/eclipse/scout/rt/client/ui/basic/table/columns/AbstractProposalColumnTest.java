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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.fixture.TestCodeType;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IProposalField;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * Tests for {@link AbstractProposalColumn}
 */
@RunWith(PlatformTestRunner.class)
public class AbstractProposalColumnTest {

  @Test
  public void testPrepareEditInternal() {
    AbstractProposalColumn<Long> column = new AbstractProposalColumn<Long>() {
    };
    column.setCodeTypeClass(TestCodeType.class);
    column.setMandatory(true);
    ITableRow row = Mockito.mock(ITableRow.class);
    @SuppressWarnings("unchecked")
    IProposalField<Long> field = (IProposalField<Long>) column.prepareEditInternal(row);
    assertEquals("mandatory property to be progagated to field", column.isMandatory(), field.isMandatory());
    assertEquals("code type class property to be progagated to field", column.getCodeTypeClass(), field.getCodeTypeClass());
  }

  /**
   * Tests that {@link AbstractProposalColumn#execPrepareLookup(ILookupCall, ITableRow)} is called when
   * {@link IProposalField#prepareKeyLookup(ILookupCall, Object)} is called on the editor field.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testPrepareLookupCallback() {
    TestProposalColumn column = new TestProposalColumn();
    ITableRow row = Mockito.mock(ITableRow.class);
    IProposalField<Long> field = (IProposalField<Long>) column.prepareEditInternal(row);
    ILookupCall call = mock(ILookupCall.class);
    field.prepareKeyLookup(call, 10L);
    assertEquals(row, column.lastRow);
    assertEquals(call, column.lastCall);
  }

  class TestProposalColumn extends AbstractProposalColumn<Long> {
    ILookupCall<Long> lastCall;
    ITableRow lastRow;

    @Override
    protected void execPrepareLookup(ILookupCall<Long> call, ITableRow row) {
      lastCall = call;
      lastRow = row;
    }
  }

}
