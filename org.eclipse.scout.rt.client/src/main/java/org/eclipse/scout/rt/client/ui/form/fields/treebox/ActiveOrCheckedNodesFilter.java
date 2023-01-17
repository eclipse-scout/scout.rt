/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
    if (nodeValue == null || filterValue == null || nodeValue.booleanValue() == filterValue.booleanValue()) {
      return true;
    }
    else {
      // active mismatch, test checked
      return super.accept(node, level);
    }
  }

}
