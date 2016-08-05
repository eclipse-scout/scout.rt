package org.eclipse.scout.rt.client.extension.ui.outline.pages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.action.menu.IMenuExtension;
import org.eclipse.scout.rt.client.extension.ui.outline.pages.fixture.AbstractPersonTablePage;
import org.eclipse.scout.rt.client.extension.ui.outline.pages.fixture.AbstractPersonTablePage.Table.EditMenu;
import org.eclipse.scout.rt.client.extension.ui.outline.pages.fixture.AllPersonTablePage;
import org.eclipse.scout.rt.client.extension.ui.outline.pages.fixture.AllPersonTablePageExtension;
import org.eclipse.scout.rt.client.extension.ui.outline.pages.fixture.EveryPersonTablePageExtension;
import org.eclipse.scout.rt.client.extension.ui.outline.pages.fixture.OtherPersonTablePage;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.menus.OrganizeColumnsMenu;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 6.0
 */
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
@RunWithSubject("anna")
public class TablePageExtensionTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testSetup() {
    assertOriginalTablePage(new AllPersonTablePage());
    assertOriginalTablePage(new OtherPersonTablePage());
  }

  @Test
  public void testEveryPersonTablePageExtensionRegisteredOnAbstractPersonTablePage() {
    BEANS.get(IExtensionRegistry.class).register(EveryPersonTablePageExtension.class, AbstractPersonTablePage.class);
    assertExtendedTablePage(new AllPersonTablePage(), EveryPersonTablePageExtension.TableExtension.TestMenu.class, EveryPersonTablePageExtension.EditPersonMenuExtension.class);
    assertExtendedTablePage(new OtherPersonTablePage(), EveryPersonTablePageExtension.TableExtension.TestMenu.class, EveryPersonTablePageExtension.EditPersonMenuExtension.class);
  }

  @Test
  public void testEveryPersonTablePageExtensionRegisteredOnAllPersonTablePage() {
    BEANS.get(IExtensionRegistry.class).register(EveryPersonTablePageExtension.class, AllPersonTablePage.class);
    assertExtendedTablePage(new AllPersonTablePage(), EveryPersonTablePageExtension.TableExtension.TestMenu.class, EveryPersonTablePageExtension.EditPersonMenuExtension.class);
    assertOriginalTablePage(new OtherPersonTablePage());
  }

  @Test
  public void testEveryPersonTablePageExtensionRegisteredOnOtherPersonTablePage() {
    BEANS.get(IExtensionRegistry.class).register(EveryPersonTablePageExtension.class, OtherPersonTablePage.class);
    assertOriginalTablePage(new AllPersonTablePage());
    assertExtendedTablePage(new OtherPersonTablePage(), EveryPersonTablePageExtension.TableExtension.TestMenu.class, EveryPersonTablePageExtension.EditPersonMenuExtension.class);
  }

  @Test
  public void testAllPersonTablePageExtension() {
    BEANS.get(IExtensionRegistry.class).register(AllPersonTablePageExtension.class);
    assertExtendedTablePage(new AllPersonTablePage(), AllPersonTablePageExtension.TableExtension.TestMenu.class, AllPersonTablePageExtension.EditPersonMenuExtension.class);
    assertOriginalTablePage(new OtherPersonTablePage());
  }

  protected void assertOriginalTablePage(AbstractPersonTablePage<?> page) {
    AbstractPersonTablePage<?>.Table table = page.getTable();
    assertEquals(2, table.getColumnCount());
    assertSame(table.getNameColumn(), table.getColumnSet().getColumn(0));
    assertSame(table.getAgeColumn(), table.getColumnSet().getColumn(1));

    assertEquals(2, table.getMenus().size());
    assertSame(table.getMenuByClass(EditMenu.class), table.getMenus().get(0));
    assertSame(table.getMenuByClass(OrganizeColumnsMenu.class), table.getMenus().get(1));

    assertEquals(1, table.getMenuByClass(EditMenu.class).getAllExtensions().size());
  }

  protected void assertExtendedTablePage(AbstractPersonTablePage<?> page, Class<? extends IMenu> expectedTestMenuClass, Class<? extends IMenuExtension> expectedMenuExtensionClass) {
    AbstractPersonTablePage<?>.Table table = page.getTable();
    assertEquals(2, table.getColumnCount());
    assertSame(table.getNameColumn(), table.getColumnSet().getColumn(0));
    assertSame(table.getAgeColumn(), table.getColumnSet().getColumn(1));

    assertEquals(3, table.getMenus().size());
    EditMenu editMenu = table.getMenuByClass(EditMenu.class);
    assertSame(editMenu, table.getMenus().get(0));
    assertSame(table.getMenuByClass(expectedTestMenuClass), table.getMenus().get(1));
    assertSame(table.getMenuByClass(OrganizeColumnsMenu.class), table.getMenus().get(2));

    assertEquals(2, editMenu.getAllExtensions().size());
    assertSame(editMenu.getExtension(expectedMenuExtensionClass), editMenu.getAllExtensions().get(0));
  }
}
