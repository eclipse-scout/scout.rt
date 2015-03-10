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

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.shared.TEXTS;

/**
 * @since 5.1.0
 */
public class TableOrganizeMenu extends OrganizeColumnsMenu {

  public TableOrganizeMenu(ITable table) {
    super(table);
  }

  @Override
  protected String getConfiguredIconId() {
    return "font:\uE031";
  }

  @Override
  protected String getConfiguredTooltipText() {
    return TEXTS.get("TableOrganize");
  }

  @Override
  protected String getConfiguredText() {
    return null;
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
