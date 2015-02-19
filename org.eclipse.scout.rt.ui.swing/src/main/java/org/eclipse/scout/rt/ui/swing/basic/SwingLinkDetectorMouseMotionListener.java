/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.basic;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;

/**
 * MouseMotionListener for a JTable or JTree which detects a click on a hyperlink
 *
 * @since 4.0.0-RC1
 */
public class SwingLinkDetectorMouseMotionListener<T extends JComponent> extends MouseMotionAdapter {
  private final AbstractHtmlLinkDetector<T> m_detector;

  public SwingLinkDetectorMouseMotionListener(AbstractHtmlLinkDetector<T> detector) {
    m_detector = detector;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void mouseMoved(MouseEvent e) {
    if (m_detector.detect((T) e.getComponent(), e.getPoint())) {
      e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    else {
      e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
  }
}
