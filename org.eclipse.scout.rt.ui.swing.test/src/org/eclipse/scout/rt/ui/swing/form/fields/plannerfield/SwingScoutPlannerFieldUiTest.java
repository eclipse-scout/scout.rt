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
package org.eclipse.scout.rt.ui.swing.form.fields.plannerfield;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMap;
import org.eclipse.scout.rt.client.ui.basic.activitymap.TimeScale;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.IPlannerField;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.basic.table.SwingScoutTable;
import org.junit.Test;

/**
 * Test for {@link SwingScoutPlannerFieldUiTest}
 * 
 * @since 3.9.0
 */
public class SwingScoutPlannerFieldUiTest {

  /**
   * Test method for {@link SwingScoutPlannerField#initializeSwing()}.
   */
  @Test
  public void testInitializeSwing() {
    ITable scoutTable = new P_Table();

    IActivityMap<?, ?> activityMap = createNiceMock(IActivityMap.class);
    activityMap.getResourceIds();
    expectLastCall().andReturn(new Object[]{}).anyTimes();
    activityMap.getSelectedResourceIds();
    expectLastCall().andReturn(new Object[]{});
    activityMap.getTimeScale();
    expectLastCall().andReturn(new TimeScale()).anyTimes();

    IPlannerField<?, ?, ?, ?> scoutObject = createNiceMock(IPlannerField.class);
    scoutObject.getResourceTable();
    expectLastCall().andReturn(scoutTable);
    scoutObject.getGridData();
    expectLastCall().andReturn(new GridData(1, 1, 1, 1, 1.0, 1.0)).anyTimes();
    scoutObject.getActivityMap();
    expectLastCall().andReturn(activityMap);
    scoutObject.getKeyStrokes();
    expectLastCall().andReturn(new IKeyStroke[]{}).anyTimes();

    ISwingEnvironment environment = createMock(ISwingEnvironment.class);
    environment.createTable(scoutTable); //Bug 405354: creation of the table is delegated to ISwingEnvironment
    expectLastCall().andReturn(new SwingScoutTable());
    environment.getFormColumnWidth();
    expectLastCall().andReturn(400).anyTimes();
    environment.getFormColumnGap();
    expectLastCall().andReturn(14).anyTimes();
    environment.getFormRowHeight();
    expectLastCall().andReturn(20).anyTimes();
    environment.getFormRowGap();
    expectLastCall().andReturn(5).anyTimes();

    replay(activityMap, scoutObject, environment);

    SwingScoutPlannerField field = new SwingScoutPlannerField();
    field.createField(scoutObject, environment); //initializeSwing() will be called in callInitializers().

    verify(environment);
  }

  public static class P_Table extends AbstractTable {
  }

}
