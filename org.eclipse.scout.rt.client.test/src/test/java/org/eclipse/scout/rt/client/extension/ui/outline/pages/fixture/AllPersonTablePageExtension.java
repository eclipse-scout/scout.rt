package org.eclipse.scout.rt.client.extension.ui.outline.pages.fixture;

import java.util.Date;

import org.eclipse.scout.rt.client.extension.ui.action.ActionChains.ActionActionChain;
import org.eclipse.scout.rt.client.extension.ui.action.menu.AbstractMenuExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.table.AbstractTableExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.AbstractPageWithTableExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitPageChain;
import org.eclipse.scout.rt.platform.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 6.0
 */
public class AllPersonTablePageExtension extends AbstractPageWithTableExtension<AllPersonTablePage.Table, AllPersonTablePage> {

  private Date m_date;

  public AllPersonTablePageExtension(AllPersonTablePage owner) {
    super(owner);
  }

  @Override
  public void execInitPage(PageInitPageChain chain) {
    m_date = new Date();
    chain.execInitPage();
  }

  public class EditPersonMenuExtension extends AbstractMenuExtension<AbstractPersonTablePage<?>.Table.EditMenu> {

    private final Logger LOG = LoggerFactory.getLogger(AllPersonTablePageExtension.EditPersonMenuExtension.class);

    public EditPersonMenuExtension(AbstractPersonTablePage<?>.Table.EditMenu owner) {
      super(owner);
    }

    @Override
    public void execAction(ActionActionChain chain) {
      LOG.info("Table extension was generated on {}", m_date);
      super.execAction(chain);
    }
  }

  public class TableExtension extends AbstractTableExtension<AllPersonTablePage.Table> {

    public TableExtension(AllPersonTablePage.Table owner) {
      super(owner);
    }

    @Order(20)
    public class TestMenu extends AbstractInitializableMenu {
    }
  }
}
