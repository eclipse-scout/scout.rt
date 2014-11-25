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
package org.eclipse.scout.rt.client.ui.basic.table.control;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

public class MapTableControl extends AbstractTableControl implements IMapTableControl {

  public MapTableControl() {
    this(true);
  }

  public MapTableControl(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    setTooltipText("Karte");
    setIconId("\uE021"); //Icons.World
    setGroup("Anzeige");
    setColumns(getConfiguredColumns());
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<? extends IColumn<?>> getColumns() {
    return (List<? extends IColumn<?>>) propertySupport.getProperty(PROP_COLUMNS);
  }

  public void setColumns(List<? extends IColumn<?>> columns) {
    propertySupport.setProperty(PROP_COLUMNS, columns);
  }

  protected List<? extends IColumn<?>> getConfiguredColumns() {
    return new ArrayList<IColumn<?>>();
  }
}
