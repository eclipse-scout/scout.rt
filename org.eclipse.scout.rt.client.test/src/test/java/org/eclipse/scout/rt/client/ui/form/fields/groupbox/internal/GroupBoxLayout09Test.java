/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBoxBodyGrid;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <h4>Vertical</h4>
 *
 * <pre>
 * ---------------------------
 *    Field01   |
 * ---------------------------
 *    Field02   |   Field02
 * ---------------------------
 *    Field03   |   Field04
 * ---------------------------
 *              |   Field04
 * ---------------------------
 * </pre>
 *
 * <h4>Horizontal</h4>
 *
 * <pre>
 * ---------------------------
 *    Field01   |
 * ---------------------------
 *    Field02   |   Field02
 * ---------------------------
 *    Field03   |   Field04
 * ---------------------------
 *              |   Field04
 * ---------------------------
 * </pre>
 *
 * @author Andreas Hoegger
 * @since 4.0.0 M6 25.02.2014
 */
@RunWith(PlatformTestRunner.class)
public class GroupBoxLayout09Test extends AbstractGroupBoxLayoutTest {

  @Test
  public void testVerticalLayout() {
    LayoutGroupBox groupBox = new LayoutGroupBox();
    IGroupBoxBodyGrid grid = new VerticalSmartGroupBoxBodyGrid();
    grid.validate(groupBox);

    // groupbox
    assertEquals(2, grid.getGridColumnCount());
    assertEquals(4, grid.getGridRowCount());

    // field01
    assertGridData(0, 0, 1, 1, groupBox.getFieldByClass(GroupBoxLayout09Test.LayoutGroupBox.Field01.class).getGridData());

    // field02
    assertGridData(0, 1, 2, 1, groupBox.getFieldByClass(GroupBoxLayout09Test.LayoutGroupBox.Field02.class).getGridData());

    // field03
    assertGridData(0, 2, 1, 1, groupBox.getFieldByClass(GroupBoxLayout09Test.LayoutGroupBox.Field03.class).getGridData());

    // field04
    assertGridData(1, 2, 1, 2, groupBox.getFieldByClass(GroupBoxLayout09Test.LayoutGroupBox.Field04.class).getGridData());

  }

  @Test
  public void testHorizontalLayout() {
    LayoutGroupBox groupBox = new LayoutGroupBox();
    IGroupBoxBodyGrid grid = new HorizontalGroupBoxBodyGrid();
    grid.validate(groupBox);
    // groupbox
    assertEquals(2, grid.getGridColumnCount());
    assertEquals(4, grid.getGridRowCount());

    // field01
    assertGridData(0, 0, 1, 1, groupBox.getFieldByClass(GroupBoxLayout09Test.LayoutGroupBox.Field01.class).getGridData());

    // field02
    assertGridData(0, 1, 2, 1, groupBox.getFieldByClass(GroupBoxLayout09Test.LayoutGroupBox.Field02.class).getGridData());

    // field03
    assertGridData(0, 2, 1, 1, groupBox.getFieldByClass(GroupBoxLayout09Test.LayoutGroupBox.Field03.class).getGridData());

    // field04
    assertGridData(1, 2, 1, 2, groupBox.getFieldByClass(GroupBoxLayout09Test.LayoutGroupBox.Field04.class).getGridData());

  }

  public class LayoutGroupBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredGridColumnCount() {
      return 2;
    }

    @Order(10)
    public class Field01 extends AbstractStringField {
      @Override
      protected String getConfiguredLabel() {
        return "Field 01";
      }

    }

    @Order(20)
    public class Field02 extends AbstractStringField {
      @Override
      protected String getConfiguredLabel() {
        return "Field 02";
      }

      @Override
      protected int getConfiguredGridW() {
        return 2;
      }

    }

    @Order(30)
    public class Field03 extends AbstractStringField {

      @Override
      protected String getConfiguredLabel() {
        return "Field 03";
      }

    }

    @Order(40)
    public class Field04 extends AbstractStringField {

      @Override
      protected String getConfiguredLabel() {
        return "Field 04";
      }

      @Override
      protected int getConfiguredGridH() {
        return 2;
      }

    }

    @Order(200)
    public class CloseButton extends AbstractCloseButton {
    }
  }
}
