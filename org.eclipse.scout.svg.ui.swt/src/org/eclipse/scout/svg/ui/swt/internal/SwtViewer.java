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
package org.eclipse.scout.svg.ui.swt.internal;

import org.apache.batik.swing.svg.SVGUserAgent;
import org.eclipse.scout.svg.client.SVGUtility;
import org.eclipse.scout.svg.client.SilentSVGUserAgentAdapter;
import org.eclipse.scout.svg.ui.swt.svgfield.JSVGCanvasSwtWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.svg.SVGDocument;

public class SwtViewer extends Composite {

  static {
    System.setProperty("sun.awt.noerasebackground", "true");
  }

  public static void show() {
    Display display = new Display();
    Shell shell = new Shell(display);
    shell.setText("SWT SVG Viewer");
    shell.setLayout(new FillLayout());
    SwtViewer instance = new SwtViewer(shell, SWT.NO_REDRAW_RESIZE | SWT.NO_BACKGROUND);
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    instance.dispose();
  }

  public SwtViewer(Composite parent, int mode) {
    super(parent, mode);
    setLayout(new FillLayout());
    SVGUserAgent ua = new SilentSVGUserAgentAdapter() {
      @Override
      public void openLink(String uri, boolean newc) {
        System.out.println("USER_AGENT.openLink(" + uri + "," + newc + ")");
      }
    };
    JSVGCanvasSwtWrapper wrapper = new JSVGCanvasSwtWrapper(this, SWT.NONE, ua, true, false);
    //set document
    try {
      SVGDocument svgDocument = SVGUtility.readSVGDocument(SwtViewer.class.getResourceAsStream("sample.svg"));
      wrapper.getJSVGCanvas().setDocument(svgDocument);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
