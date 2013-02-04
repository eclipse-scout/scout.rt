/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client.ui.basic.table;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.extension.client.ExtensionUtility;
import org.eclipse.scout.rt.extension.client.IExtensibleScoutObject;
import org.eclipse.scout.rt.extension.client.Replace;
import org.eclipse.scout.rt.extension.client.ui.action.menu.MenuExtensionUtility;

/**
 * Table supporting the following Scout extension features:
 * <ul>
 * <li>adding, removing and modifying statically configured menus</li>
 * <li>{@link Replace} annotation on columns and menus</li>
 * </ul>
 * 
 * @since 3.9.0
 */
@SuppressWarnings("deprecation")
public abstract class AbstractExtensibleTable extends AbstractTable implements IExtensibleScoutObject {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractExtensibleTable.class);

  private Map<Class<? extends IMenu>, IMenu> m_replacementMenuMap;

  public AbstractExtensibleTable() {
    super();
  }

  public AbstractExtensibleTable(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void injectColumnsInternal(List<IColumn<?>> columnList) {
    super.injectColumnsInternal(columnList);
    ExtensionUtility.processReplaceAnnotations(columnList);
  }

  @Override
  protected void injectMenusInternal(List<IMenu> menuList) {
    super.injectMenusInternal(menuList);
    Object enclosingObject = ExtensionUtility.getEnclosingObject(this);
    if (enclosingObject != null) {
      MenuExtensionUtility.adaptMenus(enclosingObject, this, menuList);
    }
    Map<IMenu, IMenu> replacements = ExtensionUtility.processReplaceAnnotations(menuList);
    if (replacements.isEmpty()) {
      m_replacementMenuMap = Collections.emptyMap();
    }
    else {
      m_replacementMenuMap = new HashMap<Class<? extends IMenu>, IMenu>(replacements.size());
      for (Map.Entry<IMenu, IMenu> mapping : replacements.entrySet()) {
        m_replacementMenuMap.put(mapping.getKey().getClass(), mapping.getValue());
      }
    }
  }

  @Override
  protected Class<? extends IMenu> getDefaultMenuInternal() {
    Class<? extends IMenu> configuredDefaultMenu = super.getDefaultMenuInternal();
    if (configuredDefaultMenu == null) {
      // no default menu configured
      return null;
    }
    IMenu replacementDefaultMenu = m_replacementMenuMap.get(configuredDefaultMenu);
    if (replacementDefaultMenu == null) {
      // configured default menu is not replaced
      return configuredDefaultMenu;
    }

    // sanity check
    if (!configuredDefaultMenu.isInstance(replacementDefaultMenu)) {
      if (LOG.isErrorEnabled()) {
        LOG.error("The replacement menu on table [" + getClass().getName() + "] is not assinable to the configured default menu. " +
            "configured default menu [" + configuredDefaultMenu.getName() + "], " +
            "replacement menu [" + replacementDefaultMenu.getClass().getName() + "]");
      }
      return null;
    }
    return replacementDefaultMenu.getClass();
  }
}
