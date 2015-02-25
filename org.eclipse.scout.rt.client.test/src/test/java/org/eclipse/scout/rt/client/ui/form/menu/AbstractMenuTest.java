package org.eclipse.scout.rt.client.ui.form.menu;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.imagebox.AbstractImageField;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that abstract menus won't be instantiated
 */
@RunWith(ScoutClientTestRunner.class)
public class AbstractMenuTest {

  @Test
  public void testAbstractMenuInImageField() {
    ImageField imageField = new ImageField();
    List<IMenu> menus = imageField.getMenus();
    assertEquals(2, menus.size());
    assertEquals(menus.get(0).getText(), "Menu1");
    assertEquals(menus.get(1).getText(), "Menu2");
  }

  public class ImageField extends AbstractImageField {

    @Order(10.0)
    public class Menu1 extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return "Menu1";
      }
    }

    @Order(20.0)
    public abstract class MyAbstractMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return "AbstractMenu";
      }
    }

    @Order(30.0)
    public class Menu2 extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return "Menu2";
      }
    }
  }
}
