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
package org.eclipse.scout.rt.ui.swing.form.fields.chartbox;

import javax.swing.JComponent;

/**
 * Implement this interface to provide a new chart
 */
public interface ISwingChartProvider {

  JComponent/* chartComponent */createChart(ISwingScoutChartBox swingParent);

  JComponent refreshChart(JComponent chartPane);

}
