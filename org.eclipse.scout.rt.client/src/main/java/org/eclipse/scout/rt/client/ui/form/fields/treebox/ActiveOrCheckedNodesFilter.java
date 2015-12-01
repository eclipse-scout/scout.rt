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
package org.eclipse.scout.rt.client.ui.form.fields.treebox;

import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.platform.util.TriState;

/**
 * The filter accepts all active rows and in addition all checked rows
 */
class ActiveOrCheckedNodesFilter extends CheckedNodesFilter {
  private final ITreeBox m_box;
  private final TriState m_filterValue;

  public ActiveOrCheckedNodesFilter(ITreeBox box, TriState filterValue) {
    m_box = box;
    m_filterValue = filterValue;
  }

  @Override
  public boolean accept(ITreeNode node, int level) {
    Boolean nodeValue = m_box.isNodeActive(node);
    Boolean filterValue = m_filterValue.getBooleanValue();
    if (nodeValue == null || filterValue == null || nodeValue == filterValue) {
      return true;
    }
    else {
      // active mismatch, test checked
      return super.accept(node, level);
    }
  }

}
