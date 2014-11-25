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

import org.eclipse.scout.rt.shared.data.model.IDataModel;

public class AnalysisTableControl extends AbstractTableControl implements IAnalysisTableControl {

  public AnalysisTableControl() {
    this(true);
  }

  public AnalysisTableControl(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setTooltipText("Analyse");
    setIconId("\uE020"); //Icons.Target
    setGroup("Suche");
  }

  @Override
  public IDataModel getDataModel() {
    return (IDataModel) propertySupport.getProperty(PROP_DATA_MODEL);
  }

  public void setDataModel(IDataModel dataModel) {
    propertySupport.setProperty(PROP_DATA_MODEL, dataModel);
  }
}
