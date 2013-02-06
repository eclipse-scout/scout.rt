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
package org.eclipse.scout.rt.ui.swing.ext;

import java.awt.BorderLayout;

public class JStatusLabelTop extends JStatusLabelEx {
  private static final long serialVersionUID = 1L;

  public JStatusLabelTop() {
    setStatusHidesMandatoryIconEnabled(false);
  }

  /**
   * <p>
   * Creates a panel for the label and one for the statusLabel (icon).
   * </p>
   * Compared to the default implementation the status Panel is on the left.
   */
  @Override
  protected void createPanels() {
    JPanelEx iconPanel = new JPanelEx();
    iconPanel.setLayout(new FlowLayoutEx(FlowLayoutEx.HORIZONTAL, FlowLayoutEx.RIGHT, 0, 0));
    add(iconPanel, BorderLayout.WEST);
    setIconPanel(iconPanel);

    JPanelEx labelPanel = new JPanelEx();
    labelPanel.setLayout(new FlowLayoutEx(FlowLayoutEx.HORIZONTAL, FlowLayoutEx.RIGHT, 0, 0));
    add(labelPanel, BorderLayout.CENTER);
    setLabelPanel(labelPanel);
  }

  /**
   * Returns the label panel so that the mandatory icon is shown right after the text.
   */
  @Override
  protected JPanelEx getMandatoryIconPanel() {
    return getLabelPanel();
  }

}
