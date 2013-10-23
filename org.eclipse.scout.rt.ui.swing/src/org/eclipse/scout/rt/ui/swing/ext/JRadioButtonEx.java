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

import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import org.eclipse.scout.rt.ui.swing.SwingUtility;

/**
 * Extends JRadioButton to enable multiline behavior
 * 
 * @since 3.10.0-M4
 */
public class JRadioButtonEx extends JRadioButton {
  private static final long serialVersionUID = 1L;

  public JRadioButtonEx() {
    super();
  }

  // override for mutiline texts
  @Override
  public void setText(String text) {
    if (SwingUtility.isMultilineLabelText(text)) {
      setVerticalTextPosition(SwingConstants.TOP);
      text = SwingUtility.createHtmlLabelText(text, false);
    }
    else {
      setVerticalTextPosition(SwingConstants.CENTER);
    }
    super.setText(text);
  }
}
