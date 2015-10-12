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

import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.data.basic.graph.GraphModel;

public class GraphTableControl extends AbstractTableControl implements IGraphTableControl {

  public GraphTableControl() {
    this(true);
  }

  public GraphTableControl(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setTooltipText(TEXTS.get("ui.Network"));
    setIconId(AbstractIcons.Graph);
  }

  @Override
  public void setGraphModel(GraphModel graphModel) {
    propertySupport.setProperty(PROP_GRAPH, graphModel);
  }

  @Override
  public GraphModel getGraphModel() {
    return (GraphModel) propertySupport.getProperty(PROP_GRAPH);
  }
}
