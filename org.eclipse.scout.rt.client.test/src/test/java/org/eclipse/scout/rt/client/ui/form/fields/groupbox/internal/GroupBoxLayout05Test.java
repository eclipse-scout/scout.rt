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
package org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBoxBodyGrid;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <h4>Vertical</h4>
 *
 * <pre>
 * -----------------------------------------
 *    Field01   |   Field01   |   Field02
 * -----------------------------------------
 *    Field01   |   Field01   |   Field03
 * -----------------------------------------
 * </pre>
 *
 * <h4>Horizontal</h4>
 *
 * <pre>
 * -----------------------------------------
 *    Field01   |   Field01   |   Field02
 * -----------------------------------------
 *    Field01   |   Field01   |   Field03
 * -----------------------------------------
 * </pre>
 *
 * @author Andreas Hoegger
 * @since 4.0.0 M6 25.02.2014
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class GroupBoxLayout05Test extends AbstractGroupBoxLayoutTest {

  @Test
  public void testVerticalLayout() throws Exception {
    MainBox groupBox = new MainBox();
    IGroupBoxBodyGrid grid = new VerticalSmartGroupBoxBodyGrid();
    grid.validate(groupBox);

    // groupbox
    assertEquals(3, grid.getGridColumnCount());
    assertEquals(2, grid.getGridRowCount());

    // field01
    assertGridData(0, 0, 2, 2, groupBox.getFieldByClass(GroupBoxLayout05Test.MainBox.Field01.class).getGridData());

    // field02
    assertGridData(2, 0, 1, 1, groupBox.getFieldByClass(GroupBoxLayout05Test.MainBox.Field02.class).getGridData());

    // field03
    assertGridData(2, 1, 1, 1, groupBox.getFieldByClass(GroupBoxLayout05Test.MainBox.Field03.class).getGridData());

  }

  @Test
  public void testHorizontalLayout() throws Exception {
    MainBox groupBox = new MainBox();
    IGroupBoxBodyGrid grid = new HorizontalGroupBoxBodyGrid();
    grid.validate(groupBox);

    // groupbox
    assertEquals(3, grid.getGridColumnCount());
    assertEquals(2, grid.getGridRowCount());

    // field01
    assertGridData(0, 0, 2, 2, groupBox.getFieldByClass(GroupBoxLayout05Test.MainBox.Field01.class).getGridData());

    // field02
    assertGridData(2, 0, 1, 1, groupBox.getFieldByClass(GroupBoxLayout05Test.MainBox.Field02.class).getGridData());

    // field03
    assertGridData(2, 1, 1, 1, groupBox.getFieldByClass(GroupBoxLayout05Test.MainBox.Field03.class).getGridData());

  }

  public class MainBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredGridColumnCount() {
      return 3;
    }

    @Order(10)
    public class Field01 extends AbstractStringField {
      @Override
      protected String getConfiguredLabel() {
        return "Field 01";
      }

      @Override
      protected int getConfiguredGridH() {
        return 2;
      }

      @Override
      protected int getConfiguredGridW() {
        return 2;
      }

    }

    @Order(20)
    public class Field02 extends AbstractStringField {
      @Override
      protected String getConfiguredLabel() {
        return "Field 02";
      }

    }

    @Order(30)
    public class Field03 extends AbstractStringField {

      @Override
      protected String getConfiguredLabel() {
        return "Field 03";
      }

    }

    @Order(200)
    public class CloseButton extends AbstractCloseButton {
    }
  }
}
