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
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.svg.client.JSVGCanvasEx;
import org.eclipse.scout.svg.client.SVGUtility;
import org.w3c.dom.svg.SVGDocument;

public class SwingViewer {
  private final static IScoutLogger LOG = ScoutLogManager.getLogger(SwingViewer.class);

  private static final int DEFAULT_FRAME_WIDTH = 800;
  private static final int DEFAULT_FRAME_HEIGHT = 600;

  private SwingViewer() {
  }

  public static void show() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          new SwingViewer().runSwing();
        }
        catch (ProcessingException e) {
          LOG.error("Problem running SwingViewer: ", e);
        }
      }
    });
  }

  private void runSwing() throws ProcessingException {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    final SVGUserAgent ua = new SVGUserAgentGUIAdapter(frame) {
      @Override
      public void openLink(String uri, boolean newc) {
        LOG.debug("USER_AGENT.openLink({0}, {1})", uri, newc);
      }
    };
    final JSVGCanvas canvas = new JSVGCanvasEx(ua, true, false);
    frame.getContentPane().add(canvas, BorderLayout.CENTER);
    frame.pack();
    frame.setSize(DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT);
    frame.setVisible(true);
    //
    final SVGDocument doc = SVGUtility.readSVGDocument(SwingViewer.class.getResourceAsStream("sample.svg"));
    canvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);//IMPORTANT TO do "getIntersectionList" and dom changes (DYNAMIC)!
    canvas.setSVGDocument(doc);
  }
}
