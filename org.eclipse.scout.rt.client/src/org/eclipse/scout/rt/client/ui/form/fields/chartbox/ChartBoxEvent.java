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
package org.eclipse.scout.rt.client.ui.form.fields.chartbox;

import java.util.EventObject;

public class ChartBoxEvent extends EventObject {
  private static final long serialVersionUID = 1L;
  public static final int TYPE_DATA_CHANGED = 100;

  private int m_type;

  public ChartBoxEvent(IChartBox source, int type) {
    super(source);
    m_type = type;
  }

  public IChartBox getChartBox() {
    return (IChartBox) getSource();
  }

  public int getType() {
    return m_type;
  }
}
