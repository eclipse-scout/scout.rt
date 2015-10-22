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
package org.eclipse.scout.rt.client.ui.basic.table.menus;

import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractFormToolButton;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.TEXTS;

public class OrganizeColumnsMenu extends AbstractFormToolButton<OrganizeColumnsForm> {
  private final ITable m_table;

  public OrganizeColumnsMenu(ITable table) {
    m_table = table;
  }

  @Override
  protected boolean getConfiguredInheritAccessibility() {
    return false;
  }

  @Override
  protected Set<IMenuType> getConfiguredMenuTypes() {
    return CollectionUtility.<IMenuType> hashSet(TableMenuType.Header);
  }

  public ITable getTable() {
    return m_table;
  }

  @Override
  protected void execSelectionChanged(boolean selected) {
    super.execSelectionChanged(selected);
    if (selected) {
      getForm().reload();
    }
  }

  @Override
  protected OrganizeColumnsForm createForm() {
    return new OrganizeColumnsForm(m_table);
  }

  @Override
  protected String getConfiguredIconId() {
    return AbstractIcons.Gear;
  }

  @Override
  protected String getConfiguredTooltipText() {
    return TEXTS.get("TableOrganize");
  }

  /**
   * Whether or not development menus must be displayed (copy columns width).
   *
   * @return
   */
  public boolean isDevelopment() {
    // FIXME AWE/CGU: (dev-mode) send this flag with JsonSession - global
    return Platform.get().inDevelopmentMode();
  }

  /**
   * Whether or not new, delete or modify menus for custom columns must be displayed.
   *
   * @return
   */
  public boolean isColumnsCustomizable() {
    return getTable().getTableCustomizer() != null;
  }

}
