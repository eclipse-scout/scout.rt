/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
public class EveryPersonTablePageExtension<T extends AbstractPersonTablePage<T>.Table> extends AbstractPageWithTableExtension<T, AbstractPersonTablePage<T>> {

  private Date m_date;

  public EveryPersonTablePageExtension(AbstractPersonTablePage<T> owner) {
    super(owner);
  }

  @Override
  public void execInitPage(PageInitPageChain chain) {
    m_date = new Date();
    chain.execInitPage();
  }

  public class EditPersonMenuExtension extends AbstractMenuExtension<AbstractPersonTablePage<?>.Table.EditMenu> {

    private final Logger LOG = LoggerFactory.getLogger(EveryPersonTablePageExtension.EditPersonMenuExtension.class);

    public EditPersonMenuExtension(AbstractPersonTablePage<?>.Table.EditMenu owner) {
      super(owner);
    }

    @Override
    public void execAction(ActionActionChain chain) {
      LOG.info("Table extension was generated on {}", m_date);
      super.execAction(chain);
    }
  }

  public class TableExtension extends AbstractTableExtension<AbstractPersonTablePage<T>.Table> {

    public TableExtension(AbstractPersonTablePage<T>.Table owner) {
      super(owner);
    }

    @Order(20)
    public class TestMenu extends AbstractInitializableMenu {
    }
  }
}
