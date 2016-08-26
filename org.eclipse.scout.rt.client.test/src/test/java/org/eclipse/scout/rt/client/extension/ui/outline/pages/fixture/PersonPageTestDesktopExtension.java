package org.eclipse.scout.rt.client.extension.ui.outline.pages.fixture;

import org.eclipse.scout.rt.client.extension.ui.action.menu.AbstractMenuExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.AbstractDesktopExtension;
import org.eclipse.scout.rt.client.extension.ui.outline.pages.fixture.PersonPageTestDesktop.DevMenu;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.platform.Order;

/**
 * @since 6.0
 */
public class PersonPageTestDesktopExtension extends AbstractDesktopExtension<PersonPageTestDesktop> {

  public PersonPageTestDesktopExtension(PersonPageTestDesktop desktop) {
    super(desktop);
  }

  public class DevMenuExtension extends AbstractMenuExtension<DevMenu> {

    public DevMenuExtension(final DevMenu owner) {
      super(owner);
    }

    @Order(100)
    public class TestMenu extends AbstractMenu {

      @Override
      protected String getConfiguredText() {
        return "Test";
      }
    }
  }
}
