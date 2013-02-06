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
package org.eclipse.scout.svg.ui.swing.internal;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.SVGUserAgent;
import org.apache.batik.swing.svg.SVGUserAgentGUIAdapter;
import org.eclipse.scout.svg.client.JSVGCanvasEx;
import org.eclipse.scout.svg.client.SVGUtility;
import org.w3c.dom.svg.SVGDocument;

public class SwingViewer {

  public static void show() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          new SwingViewer().runSwing();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  private void runSwing() throws Exception {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    final SVGUserAgent ua = new SVGUserAgentGUIAdapter(frame) {
      @Override
      public void openLink(String uri, boolean newc) {
        System.out.println("USER_AGENT.openLink(" + uri + "," + newc + ")");
      }
    };
    final JSVGCanvas canvas = new JSVGCanvasEx(ua, true, false);
    frame.getContentPane().add(canvas, BorderLayout.CENTER);
    frame.pack();
    frame.setSize(800, 600);
    frame.setVisible(true);
    //
    final SVGDocument doc = SVGUtility.readSVGDocument(SwingViewer.class.getResourceAsStream("sample.svg"));
    canvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);//IMPORTANT TO do "getIntersectionList" and dom changes (DYNAMIC)!
    canvas.setSVGDocument(doc);
  }
}
