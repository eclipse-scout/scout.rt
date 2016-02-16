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
package org.eclipse.scout.rt.client.extension.ui.basic.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.basic.table.fixture.AbstractPersonTable;
import org.eclipse.scout.rt.client.extension.ui.basic.table.fixture.AbstractPersonTable.NameColumn;
import org.eclipse.scout.rt.client.extension.ui.basic.table.fixture.AllPersonTable;
import org.eclipse.scout.rt.client.extension.ui.basic.table.fixture.FirstNameColumn;
import org.eclipse.scout.rt.client.extension.ui.basic.table.fixture.OtherPersonTable;
import org.eclipse.scout.rt.client.extension.ui.basic.table.fixture.PersonTableExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.table.fixture.PersonTableExtension.NameColumnExtension;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
@RunWithSubject("anna")
public class TableExtensionTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testExtendAbstractPersonTableAddFirstNameColumnExplicit() {
    BEANS.get(IExtensionRegistry.class).register(FirstNameColumn.class, AbstractPersonTable.class);
    doTestAddFirstnameField();
  }

  @Test
  public void testExtendAbstractPersonTableAddFirstNameColumnAnnotation() {
    BEANS.get(IExtensionRegistry.class).register(FirstNameColumn.class);
    doTestAddFirstnameField();
  }

  private void doTestAddFirstnameField() {
    AllPersonTable allPersonTable = new AllPersonTable();
    assertEquals(4, allPersonTable.getColumnCount());
    assertSame(allPersonTable.getNameColumn(), allPersonTable.getColumnSet().getColumn(0));
    assertSame(allPersonTable.getColumnSet().getColumnByClass(FirstNameColumn.class), allPersonTable.getColumnSet().getColumn(1));
    assertSame(allPersonTable.getCityColumn(), allPersonTable.getColumnSet().getColumn(2));
    assertSame(allPersonTable.getAgeColumn(), allPersonTable.getColumnSet().getColumn(3));

    OtherPersonTable otherPersonTable = new OtherPersonTable();
    assertEquals(4, otherPersonTable.getColumnCount());
    assertSame(otherPersonTable.getNameColumn(), otherPersonTable.getColumnSet().getColumn(0));
    assertSame(otherPersonTable.getColumnSet().getColumnByClass(FirstNameColumn.class), otherPersonTable.getColumnSet().getColumn(1));
    assertSame(otherPersonTable.getAgeColumn(), otherPersonTable.getColumnSet().getColumn(2));
    assertSame(otherPersonTable.getPhoneNumberColumn(), otherPersonTable.getColumnSet().getColumn(3));
  }

  @Test
  public void testExtendAbstractPersonTablePersonTableExtensionExplicit() {
    BEANS.get(IExtensionRegistry.class).register(PersonTableExtension.class, AbstractPersonTable.class);
    verifyExtendedAllPersonTable(new AllPersonTable());
    verifyExtendedOtherPersonTable(new OtherPersonTable());
  }

  @Test
  public void testExtendAbstractPersonTablePersonTableExtensionAnnotation() {
    BEANS.get(IExtensionRegistry.class).register(PersonTableExtension.class);
    verifyExtendedAllPersonTable(new AllPersonTable());
    verifyExtendedOtherPersonTable(new OtherPersonTable());
  }

  protected void verifyExtendedAllPersonTable(AllPersonTable allPersonTable) {
    assertEquals(4, allPersonTable.getColumnCount());
    assertSame(allPersonTable.getNameColumn(), allPersonTable.getColumnSet().getColumn(0));
    assertSame(allPersonTable.getCityColumn(), allPersonTable.getColumnSet().getColumn(1));
    assertSame(allPersonTable.getAgeColumn(), allPersonTable.getColumnSet().getColumn(2));

    PersonTableExtension extension = allPersonTable.getExtension(PersonTableExtension.class);
    assertNotNull(extension);
    assertSame(extension.getStreetColumn(), allPersonTable.getColumnSet().getColumn(3));

    NameColumnExtension columnExtension = allPersonTable.getNameColumn().getExtension(NameColumnExtension.class);
    assertNotNull(columnExtension);
  }

  protected void verifyExtendedOtherPersonTable(OtherPersonTable otherPersonTable) {
    assertEquals(4, otherPersonTable.getColumnCount());
    assertSame(otherPersonTable.getNameColumn(), otherPersonTable.getColumnSet().getColumn(0));
    assertSame(otherPersonTable.getAgeColumn(), otherPersonTable.getColumnSet().getColumn(1));
    assertSame(otherPersonTable.getPhoneNumberColumn(), otherPersonTable.getColumnSet().getColumn(2));

    PersonTableExtension extension = otherPersonTable.getExtension(PersonTableExtension.class);
    assertNotNull(extension);
    assertSame(extension.getStreetColumn(), otherPersonTable.getColumnSet().getColumn(3));

    NameColumnExtension columnExtension = otherPersonTable.getNameColumn().getExtension(NameColumnExtension.class);
    assertNotNull(columnExtension);
  }

  @Test
  public void testResetColumnConfigurationOnExtendedTable() {
    BEANS.get(IExtensionRegistry.class).register(PersonTableExtension.class);

    AllPersonTable allPersonTable = new AllPersonTable();
    verifyExtendedAllPersonTable(allPersonTable);
    NameColumn nameColumn = allPersonTable.getNameColumn();
    allPersonTable.resetColumnConfiguration();
    verifyExtendedAllPersonTable(allPersonTable);
    assertNotSame(nameColumn, allPersonTable.getNameColumn());

    OtherPersonTable otherPersonTable = new OtherPersonTable();
    nameColumn = otherPersonTable.getNameColumn();
    verifyExtendedOtherPersonTable(otherPersonTable);
    otherPersonTable.resetColumnConfiguration();
    verifyExtendedOtherPersonTable(otherPersonTable);
    assertNotSame(nameColumn, allPersonTable.getNameColumn());
  }
}
