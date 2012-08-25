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
package org.eclipse.scout.rt.ui.swing.window.desktop.layout;

import java.awt.Component;

import org.eclipse.scout.commons.VerboseUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

class CellElement {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(CellElement.class);

  private Component m_comp;
  private float[][] m_distributionMatrix;

  public CellElement(Component comp, float[][] distributionMatrix3x3) {
    m_comp = comp;
    m_distributionMatrix = distributionMatrix3x3;
  }

  public Component getComponent() {
    return m_comp;
  }

  /**
   * @return distribution map
   */
  public float[][] getDistributionMatrix() {
    return m_distributionMatrix;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[map=" + VerboseUtility.dumpObject(m_distributionMatrix) + ", comp=" + m_comp.getName() + "]";
  }

}
