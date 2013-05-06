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
package org.eclipse.scout.rt.ui.swing.form.fields.listbox;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.easymock.EasyMock;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.basic.table.SwingScoutTable;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.junit.Test;

/**
 * Test for {@link SwingScoutListBox}
 * 
 * @since 3.9.0
 */
public class SwingScoutListBoxUiTest {

  /**
   * Test method for {@link SwingScoutListBox#initializeSwing()}.
   */
  @Test
  public void testInitializeSwing() {

    ITable scoutTable = new P_Table();

    IListBox scoutObject = createNiceMock(IListBox.class);
    scoutObject.getTable();
    expectLastCall().andReturn(scoutTable);
    scoutObject.getGridData();
    expectLastCall().andReturn(new GridData(1, 1, 1, 1, 1.0, 1.0)).anyTimes();
    scoutObject.getFields();
    expectLastCall().andReturn(new IFormField[]{});
    scoutObject.getKeyStrokes();
    expectLastCall().andReturn(new IKeyStroke[]{}).anyTimes();

    ISwingEnvironment environment = createMock(ISwingEnvironment.class);
    environment.createStatusLabel(EasyMock.<IFormField> anyObject());
    expectLastCall().andReturn(new JStatusLabelEx());
    environment.createTable(scoutTable); //Bug 405354: creation of the table is delegated to ISwingEnvironment
    expectLastCall().andReturn(new SwingScoutTable());
    environment.getFieldLabelWidth();
    expectLastCall().andReturn(100).anyTimes();

    replay(scoutObject, environment);

    SwingScoutListBox field = new SwingScoutListBox();
    field.createField(scoutObject, environment); //initializeSwing() will be called in callInitializers().

    verify(environment);
  }

  public static class P_Table extends AbstractTable {
  }
}
