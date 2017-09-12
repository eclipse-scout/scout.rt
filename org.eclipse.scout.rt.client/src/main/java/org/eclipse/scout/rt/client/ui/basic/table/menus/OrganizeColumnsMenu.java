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
package org.eclipse.scout.rt.client.ui.basic.table.menus;

import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.IOrganizeColumnsForm;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.IOrganizeColumnsFormProvider;
import org.eclipse.scout.rt.client.ui.form.AbstractFormMenu;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.TEXTS;

@ClassId("dc8237d6-18b8-4406-91b0-bb4a95bf9fec")
public class OrganizeColumnsMenu extends AbstractFormMenu<IOrganizeColumnsForm> {
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
    return CollectionUtility.hashSet(TableMenuType.Header);
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
  protected IOrganizeColumnsForm createForm() {
    return BEANS.get(IOrganizeColumnsFormProvider.class).createOrganizeColumnsForm(m_table);
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
   * Whether or not new, delete or modify menus for custom columns must be displayed.
   *
   * @return
   */
  public boolean isColumnsCustomizable() {
    return getTable().getTableCustomizer() != null;
  }

}
