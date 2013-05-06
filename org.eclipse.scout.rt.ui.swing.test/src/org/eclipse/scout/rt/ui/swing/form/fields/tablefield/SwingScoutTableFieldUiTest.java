/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.form.fields.tablefield;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.easymock.EasyMock;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.basic.table.SwingScoutTable;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.junit.Test;

/**
 * Junit test for {@link SwingScoutTableField}
 * 
 * @since 3.9.0
 */
public class SwingScoutTableFieldUiTest {

  /**
   * Test method for {@link SwingScoutTableField#setTableFromScout()}.
   */
  @Test
  public void testSetTableFromScout() {
    IForm form = createNiceMock(IForm.class);
    form.getDisplayViewId();
    expectLastCall().andReturn(IForm.VIEW_ID_CENTER);
    form.getOuterForm();
    expectLastCall().andReturn(form);

    ITable scoutTable = new P_Table();

    ITableField<?> scoutObject = createNiceMock(ITableField.class);
    scoutObject.getGridData();
    expectLastCall().andReturn(new GridData(1, 1, 1, 1, 1.0, 1.0)).anyTimes();
    scoutObject.getKeyStrokes();
    expectLastCall().andReturn(new IKeyStroke[]{}).anyTimes();
    scoutObject.getTable();
    expectLastCall().andReturn(scoutTable);
    scoutObject.getLabelPosition();
    expectLastCall().andReturn(IFormField.LABEL_POSITION_DEFAULT);
    scoutObject.getForm();
    expectLastCall().andReturn(form).anyTimes();

    ISwingEnvironment environment = createMock(ISwingEnvironment.class);
    environment.createStatusLabel(EasyMock.<IFormField> anyObject());
    expectLastCall().andReturn(new JStatusLabelEx());
    environment.getFieldLabelWidth();
    expectLastCall().andReturn(100).anyTimes();
    environment.createTable(scoutTable); //Bug 405354: creation of the table is delegated to ISwingEnvironment
    expectLastCall().andReturn(new SwingScoutTable());

    replay(form, scoutObject, environment);

    SwingScoutTableField field = new SwingScoutTableField();
    field.createField(scoutObject, environment); //setTableFromScout() will be called in attachScout().

    verify(environment);
  }

  public static class P_Table extends AbstractTable {
  }
}
