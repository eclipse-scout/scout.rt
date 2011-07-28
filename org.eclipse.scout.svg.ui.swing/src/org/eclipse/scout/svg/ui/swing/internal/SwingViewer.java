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
import java.awt.event.MouseAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.batik.dom.svg.SVGOMRect;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.SVGUserAgent;
import org.apache.batik.swing.svg.SVGUserAgentGUIAdapter;
import org.eclipse.scout.svg.client.SVGUtility;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGGElement;
import org.w3c.dom.svg.SVGTransform;
import org.w3c.dom.svg.SVGTransformList;

public class SwingViewer {
  private static final String FOLDER = "D:\\dev\\svg";

  public static void show() throws Exception {
    SwingUtilities.invokeAndWait(new Runnable() {
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
    final JSVGCanvas canvas = new JSVGCanvas(ua, true, false);
    frame.getContentPane().add(canvas, BorderLayout.CENTER);
    frame.pack();
    frame.setSize(800, 600);
    frame.setVisible(true);
    //
    System.out.println("loading...");
    File f = new File(FOLDER, "test.svg");
    final SVGDocument doc = SVGUtility.readSVGDocument(new FileInputStream(f));
    System.out.println("loaded ");
    canvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);//IMPORTANT TO do "getIntersectionList" and dom changes (DYNAMIC)!
    canvas.setSVGDocument(doc);
    System.out.println("showing");
    //XXX adaptTachos(doc);
    //Swing listener
    canvas.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        try {
          AffineTransform vbTransform = canvas.getViewBoxTransform().createInverse();
          Point2D p = new Point2D.Double(e.getX(), e.getY());
          p = vbTransform.transform(p, null);
          SVGOMRect svgOMRect = new SVGOMRect((float) p.getX(), (float) p.getY(), 1, 1);
          System.out.println("click " + p.getX() + "," + p.getY());
          NodeList intersectedElements = doc.getRootElement().getIntersectionList(svgOMRect, null);
          int n = intersectedElements.getLength();
          System.out.println("n: " + n);
          for (int i = 0; i < n; i++) {
            Node node = intersectedElements.item(i);
            handleNode(node);
          }
        }
        catch (NoninvertibleTransformException e1) {
          e1.printStackTrace();
        }
      }
    });
  }

  private void adaptTachos(SVGDocument doc) {
    SVGGElement g = (SVGGElement) doc.getElementById("tacho1.hand");
    SVGTransformList tlist = g.getTransform().getBaseVal();
    SVGTransform t = tlist.getItem(0);
    t.setMatrix(t.getMatrix().rotate(30));
  }

  private void handleNode(Node node) {
    dump(node);
  }

  private void dump(Node e) {
    System.out.println("Node " + e.getLocalName());
    NamedNodeMap nnm = e.getAttributes();
    for (int i = 0; i < nnm.getLength(); i++) {
      System.out.println(" " + nnm.item(i).getNodeName() + "=" + nnm.item(i).getNodeValue());
    }
  }
}
