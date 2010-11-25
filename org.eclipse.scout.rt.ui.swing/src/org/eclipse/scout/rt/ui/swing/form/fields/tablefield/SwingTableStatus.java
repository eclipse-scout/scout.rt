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
package org.eclipse.scout.rt.ui.swing.form.fields.tablefield;

import javax.swing.JLabel;

/**
 * Default implementation using a target label
 */
public class SwingTableStatus implements ISwingTableStatus {
  private final JLabel m_label;

  public SwingTableStatus(JLabel label) {
    m_label = label;
  }

  @Override
  public void setStatusText(String s) {
    //bsi ticket 95826: eliminate newlines
    if (s != null) {
      s = s.replaceAll("[\\s]+", " ");
    }
    m_label.setText(s);
  }
}
