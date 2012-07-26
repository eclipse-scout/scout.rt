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

import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.extension.client.ExtensionClientUtility;
import org.eclipse.scout.rt.extension.client.Replace;

/**
 * Table supporting the following scout extension features:
 * <ul>
 * <li>{@link Replace} annotation on columns and menus</li>
 * </ul>
 * 
 * @since 3.9.0
 */
public class AbstractExtensibleTable extends AbstractTable {

  public AbstractExtensibleTable() {
    super();
  }

  public AbstractExtensibleTable(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void injectColumnsInternal(List<IColumn<?>> columnList) {
    super.injectColumnsInternal(columnList);
    ExtensionClientUtility.processReplaceAnnotations(columnList);
  }

  @Override
  protected void injectMenusInternal(List<IMenu> menuList) {
    super.injectMenusInternal(menuList);
    ExtensionClientUtility.processReplaceAnnotations(menuList);
  }
}
