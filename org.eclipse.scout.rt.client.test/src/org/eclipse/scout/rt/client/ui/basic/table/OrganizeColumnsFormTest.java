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
package org.eclipse.scout.rt.client.ui.basic.table;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.dnd.JavaTransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.junit.Test;

/**
 * Tests for {@link OrganizeColumnsForm}
 *
 * @since 4.1.0
 */
public class OrganizeColumnsFormTest {
  @Test
  public void testNoNPEInExecDrop() throws ProcessingException {
    ITable table = mock(ITable.class);
    List<ITableRow> list = new LinkedList<ITableRow>();
    list.add(mock(ITableRow.class));
    JavaTransferObject transfer = mock(JavaTransferObject.class);
    when(transfer.getLocalObjectAsList(ITableRow.class)).thenReturn(list);
    OrganizeColumnsForm form = new OrganizeColumnsForm(table);
    try {
      form.getColumnsTableField().getTable().execDrop(null, null);
      form.getColumnsTableField().getTable().execDrop(null, transfer);
      form.getColumnsTableField().getTable().execDrop(mock(ITableRow.class), null);
    }
    catch (NullPointerException e) {
      fail("Null-Argument should not lead to NullPointerException " + e);
    }
  }
}
