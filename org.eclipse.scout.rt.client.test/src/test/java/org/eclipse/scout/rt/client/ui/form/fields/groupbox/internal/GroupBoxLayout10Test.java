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

import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
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
 * -----------------------------------------
 *    Field01   |   Field02   |   Field05
 * -----------------------------------------
 *    Field03   |   Field03   |
 * -----------------------------------------
 *    Field04   |   Field06   |   Field06
 * -----------------------------------------
 * </pre>
 *
 * <h4>Horizontal</h4>
 *
 * <pre>
 * -----------------------------------------
 *    Field01   |   Field02   |
 * -----------------------------------------
 *    Field03   |   Field03   |   Field04
 * -----------------------------------------
 *    Field05   |   Field06   |   Field06
 * -----------------------------------------
 * </pre>
 *
 * @author Andreas Hoegger
 * @since 4.0.0 M6 25.02.2014
 */
@RunWith(PlatformTestRunner.class)
public class GroupBoxLayout10Test extends AbstractGroupBoxLayoutTest {

  @Test
  public void testVerticalLayout() throws Exception {
    LayoutGroupBox groupBox = new LayoutGroupBox();
    IGroupBoxBodyGrid grid = new VerticalSmartGroupBoxBodyGrid();
    grid.validate(groupBox);

    // groupbox
    assertEquals(3, grid.getGridColumnCount());
    assertEquals(3, grid.getGridRowCount());

    // field01
    assertGridData(0, 0, 1, 1, groupBox.getFieldByClass(GroupBoxLayout10Test.LayoutGroupBox.Field01.class).getGridData());

    // field02
    assertGridData(1, 0, 1, 1, groupBox.getFieldByClass(GroupBoxLayout10Test.LayoutGroupBox.Field02.class).getGridData());

    // field03
    assertGridData(0, 1, 2, 1, groupBox.getFieldByClass(GroupBoxLayout10Test.LayoutGroupBox.Field03.class).getGridData());

    // field04
    assertGridData(0, 2, 1, 1, groupBox.getFieldByClass(GroupBoxLayout10Test.LayoutGroupBox.Field04.class).getGridData());

    // field05
    assertGridData(2, 0, 1, 1, groupBox.getFieldByClass(GroupBoxLayout10Test.LayoutGroupBox.Field05.class).getGridData());

    // field06
    assertGridData(1, 2, 2, 1, groupBox.getFieldByClass(GroupBoxLayout10Test.LayoutGroupBox.Field06.class).getGridData());
  }

  @Test
  public void testHorizontalLayout() throws Exception {
    LayoutGroupBox groupBox = new LayoutGroupBox();
    IGroupBoxBodyGrid grid = new HorizontalGroupBoxBodyGrid();
    grid.validate(groupBox);

    // groupbox
    assertEquals(3, grid.getGridColumnCount());
    assertEquals(3, grid.getGridRowCount());

    // field01
    assertGridData(0, 0, 1, 1, groupBox.getFieldByClass(GroupBoxLayout10Test.LayoutGroupBox.Field01.class).getGridData());

    // field02
    assertGridData(1, 0, 1, 1, groupBox.getFieldByClass(GroupBoxLayout10Test.LayoutGroupBox.Field02.class).getGridData());

    // field03
    assertGridData(0, 1, 2, 1, groupBox.getFieldByClass(GroupBoxLayout10Test.LayoutGroupBox.Field03.class).getGridData());

    // field04
    assertGridData(2, 1, 1, 1, groupBox.getFieldByClass(GroupBoxLayout10Test.LayoutGroupBox.Field04.class).getGridData());

    // field05
    assertGridData(0, 2, 1, 1, groupBox.getFieldByClass(GroupBoxLayout10Test.LayoutGroupBox.Field05.class).getGridData());

    // field06
    assertGridData(1, 2, 2, 1, groupBox.getFieldByClass(GroupBoxLayout10Test.LayoutGroupBox.Field06.class).getGridData());
  }

  public class LayoutGroupBox extends AbstractGroupBox {

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
      protected int getConfiguredGridW() {
        return 1;
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

      @Override
      protected int getConfiguredGridW() {
        return 2;
      }
    }

    @Order(40)
    public class Field04 extends AbstractStringField {

      @Override
      protected String getConfiguredLabel() {
        return "Field 04";
      }

    }

    @Order(50)
    public class Field05 extends AbstractStringField {

      @Override
      protected String getConfiguredLabel() {
        return "Field 05";
      }

    }

    @Order(60)
    public class Field06 extends AbstractStringField {

      @Override
      protected String getConfiguredLabel() {
        return "Field 06";
      }

      @Override
      protected int getConfiguredGridW() {
        return 2;
      }

    }

    @Order(100)
    public class F04VisToggleButton extends AbstractButton {
      @Override
      protected String getConfiguredLabel() {
        return "Field 03 visible";
      }

      @Override
      protected void execInitField() {
        setSelected(getFieldByClass(Field03.class).isVisible());
      }

      @Override
      protected int getConfiguredDisplayStyle() {
        return IButton.DISPLAY_STYLE_TOGGLE;
      }

      @Override
      protected void execSelectionChanged(boolean selection) {
        getFieldByClass(Field03.class).setVisible(selection);
      }
    }

    @Order(200)
    public class CloseButton extends AbstractCloseButton {
    }
  }
}
