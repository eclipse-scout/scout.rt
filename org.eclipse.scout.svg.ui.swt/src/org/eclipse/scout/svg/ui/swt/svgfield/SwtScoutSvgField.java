/*******************************************************************************
 * Copyright (c) 2010,2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.svg.ui.swt.svgfield;

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
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFieldComposite;
import org.eclipse.scout.svg.client.SilentSVGUserAgentAdapter;
import org.eclipse.scout.svg.client.svgfield.ISvgField;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.svg.SVGDocument;

public class SwtScoutSvgField extends SwtScoutFieldComposite<ISvgField> implements ISwtScoutSvgField {

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    if (getScoutObject().isLabelVisible()) {
      int labelStyle = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldLabelAlignment();
      StatusLabelEx label = new StatusLabelEx(container, labelStyle, getEnvironment());
      getEnvironment().getFormToolkit().getFormToolkit().adapt(label, false, false);
      setSwtLabel(label);
    }
    //
    //setup svg viewer
    final SVGUserAgent ua = new P_SVGUserAgent();
    JSVGCanvasSwtWrapper canvas = new JSVGCanvasSwtWrapper(container, SWT.NONE, ua, true, false);
    //IMPORTANT for supporting "getIntersectionList" and doing dynamic dom changes
    canvas.getJSVGCanvas().setDocumentState(JSVGCanvas.ALWAYS_INTERACTIVE);
    canvas.getJSVGCanvas().addMouseListener(new P_SwingMouseListener());
    getEnvironment().getFormToolkit().adapt(canvas, false, false);
    setSwtField(canvas);
    //
    setSwtContainer(container);
    // layout
    getSwtContainer().setLayout(new LogicalGridLayout(1, 0));
  }

  @Override
  public JSVGCanvasSwtWrapper getSwtField() {
    return (JSVGCanvasSwtWrapper) super.getSwtField();
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    updateSvgDocumentFromScout();
  }

  @Override
  protected void detachScout() {
    super.detachScout();
    getSwtField().getJSVGCanvas().stopProcessing();
    getSwtField().getJSVGCanvas().dispose();
  }

  protected void updateSvgDocumentFromScout() {
    SVGDocument doc = getScoutObject().getSvgDocument();
    JSVGCanvas canvas = getSwtField().getJSVGCanvas();
    canvas.setSVGDocument(doc);
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
      JSVGCanvas canvas = getSwtField().getJSVGCanvas();
      AffineTransform vbTransform = canvas.getViewBoxTransform().createInverse();
      Point2D p = new Point2D.Double(e.getX(), e.getY());
      p = vbTransform.transform(p, null);
      final SVGOMPoint svgPoint = new SVGOMPoint((float) p.getX(), (float) p.getY());
      // notify scout later
      new ClientSyncJob("Click", getEnvironment().getClientSession()) {
        @Override
        protected void runVoid(IProgressMonitor monitor) {
          getScoutObject().getUIFacade().fireClickFromUI(svgPoint);
        }
      }.schedule();
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
        new ClientSyncJob("Hyperlink", getEnvironment().getClientSession()) {
          @Override
          protected void runVoid(IProgressMonitor monitor) {
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
