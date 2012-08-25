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
package org.eclipse.scout.svg.ui.swt.svgfield;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Frame;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.JSVGComponent;
import org.apache.batik.swing.svg.SVGUserAgent;
import org.eclipse.scout.svg.client.JSVGCanvasEx;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

public class JSVGCanvasSwtWrapper extends Composite {
  static {
    System.setProperty("sun.awt.noerasebackground", "true");
  }

  private final Frame m_awtFrame;
  private final JSVGCanvas m_svgCanvas;

  public JSVGCanvasSwtWrapper(Composite parent, int style, SVGUserAgent ua, boolean eventsEnabled, boolean selectableText) {
    super(parent, style | SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.EMBEDDED);
    m_svgCanvas = new JSVGCanvasEx(ua, eventsEnabled, selectableText);
    m_svgCanvas.setDocumentState(JSVGComponent.ALWAYS_DYNAMIC);
    m_svgCanvas.setLayout(new BorderLayout());
    m_svgCanvas.setDoubleBuffered(true);
    m_svgCanvas.setDoubleBufferedRendering(true);
    m_awtFrame = SWT_AWT.new_Frame(this);
    m_awtFrame.setLayout(new BorderLayout());
    m_awtFrame.add(BorderLayout.CENTER, m_svgCanvas);
    m_awtFrame.setEnabled(true);
    m_awtFrame.pack();
    m_awtFrame.setVisible(true);
    //dispose in awt thread
    addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        EventQueue.invokeLater(new Runnable() {
          @Override
          public void run() {
            m_svgCanvas.dispose();
            m_awtFrame.dispose();
          }
        });
      }
    });
  }

  public JSVGCanvas getJSVGCanvas() {
    return m_svgCanvas;
  }
}
