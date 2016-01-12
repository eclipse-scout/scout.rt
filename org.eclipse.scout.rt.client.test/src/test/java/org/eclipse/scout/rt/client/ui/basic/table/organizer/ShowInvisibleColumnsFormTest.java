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
package org.eclipse.scout.rt.client.ui.basic.table.organizer;

import java.util.Collections;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.ColumnSet;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.mockito.Mockito;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ShowInvisibleColumnsFormTest {

  protected ShowInvisibleColumnsForm getForm() {
    ITable tableMock = Mockito.mock(ITable.class);
    ColumnSet mockColumnSet = Mockito.mock(ColumnSet.class);
    // FIXME asa: return columns, check some of them and test whether they are set visible in store
    Mockito.when(mockColumnSet.getAllColumnsInUserOrder()).thenReturn(Collections.<IColumn<?>> emptyList());
    Mockito.when(tableMock.getColumnSet()).thenReturn(mockColumnSet);
    return new ShowInvisibleColumnsForm(tableMock);
  }

  @Test
  public void testModifyHandler() {
    ShowInvisibleColumnsForm form = getForm();
    form.startModify();
    form.touch();
    form.doClose();
  }
}
