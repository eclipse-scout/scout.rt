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
package org.eclipse.scout.svg.ui.swing.svgfield;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.net.URI;
import java.net.URL;

import org.apache.batik.dom.svg.SVGOMPoint;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.SVGUserAgent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFieldComposite;
import org.eclipse.scout.svg.client.JSVGCanvasEx;
import org.eclipse.scout.svg.client.SilentSVGUserAgentAdapter;
import org.eclipse.scout.svg.client.svgfield.ISvgField;
import org.w3c.dom.svg.SVGDocument;

public class SwingScoutSvgField extends SwingScoutFieldComposite<ISvgField> implements ISwingScoutSvgField {

  public SwingScoutSvgField() {
  }

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    //
    if (getScoutObject().isLabelVisible()) {
      JStatusLabelEx label = getSwingEnvironment().createStatusLabel(getScoutObject());
      container.add(label);
      setSwingLabel(label);
    }
    //setup svg viewer
    final SVGUserAgent ua = new P_SVGUserAgent();
    JSVGCanvas canvas = new JSVGCanvasEx(ua, true, false);
    //IMPORTANT for supporting hyperlinks
    canvas.setDocumentState(JSVGCanvas.ALWAYS_INTERACTIVE);
    canvas.addMouseListener(new P_SwingMouseListener());
    SwingUtility.installDefaultFocusHandling(canvas);
    container.add(canvas);
    setSwingField(canvas);
    //
    setSwingContainer(container);
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  @Override
  public JSVGCanvas getSwingField() {
    return (JSVGCanvas) super.getSwingField();
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    updateSvgDocumentFromScout();
    setHorizontalAlignmentFromScout(getScoutObject().getGridData().horizontalAlignment);
    setVerticalAlignmentFromScout(getScoutObject().getGridData().verticalAlignment);
  }

  @Override
  protected void detachScout() {
    super.detachScout();
    getSwingField().stopProcessing();
    getSwingField().dispose();
  }

  protected void updateSvgDocumentFromScout() {
    SVGDocument doc = getScoutObject().getSvgDocument();
    JSVGCanvas canvas = getSwingField();
    canvas.setSVGDocument(doc);
  }

  @Override
  protected void setHorizontalAlignmentFromScout(int scoutAlign) {
    float swingAlignX = SwingUtility.createAlignmentX(scoutAlign);
    getSwingField().setAlignmentX(swingAlignX);
  }

  @Override
  protected void setVerticalAlignmentFromScout(int scoutAlign) {
    float swingAlignY = SwingUtility.createAlignmentY(scoutAlign);
    getSwingField().setAlignmentY(swingAlignY);
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(ISvgField.PROP_SVG_DOCUMENT)) {
      updateSvgDocumentFromScout();
    }
  }

  protected void handleSwingClick(MouseEvent e) {
    try {
      JSVGCanvas canvas = getSwingField();
      AffineTransform vbTransform = canvas.getViewBoxTransform().createInverse();
      Point2D p = new Point2D.Double(e.getX(), e.getY());
      p = vbTransform.transform(p, null);
      final SVGOMPoint svgPoint = new SVGOMPoint((float) p.getX(), (float) p.getY());
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().fireClickFromUI(svgPoint);
        }
      };
      getSwingEnvironment().invokeScoutLater(t, 5678);
      // end notify
    }
    catch (NoninvertibleTransformException e1) {
      //nop
    }
  }

  private class P_SwingMouseListener extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent e) {
      if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
        handleSwingClick(e);
      }
    }
  }

  private class P_SVGUserAgent extends SilentSVGUserAgentAdapter {
    @Override
    public void openLink(String uri, boolean newc) {
      try {
        final URL url = new URI(uri).toURL();
        //notify scout later
        new ClientSyncJob("Hyperlink", getSwingEnvironment().getScoutSession()) {
          @Override
          protected void runVoid(IProgressMonitor monitor) throws Throwable {
            getScoutObject().getUIFacade().fireHyperlinkFromUI(url);
          }
        }.schedule();
        // end notify
      }
      catch (Throwable t) {
        //nop
      }
    }
  }
}
