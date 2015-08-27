/*******************************************************************************
 * Copyright (c) 2012,2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.svg.ui.rap;

import java.io.ByteArrayOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.util.SVGConstants;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.ext.browser.BrowserExtension;
import org.eclipse.scout.rt.ui.rap.ext.browser.IHyperlinkCallback;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFieldComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.svg.SVGDocument;

public abstract class AbstractRwtScoutSvgComposite<T extends IFormField> extends RwtScoutFieldComposite<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractRwtScoutSvgComposite.class);

  private static final String DOCUMENT_ENCODING = "UTF-8";

  private BrowserExtension m_browserExtension;

  private void setBrowserExtension(BrowserExtension browserExtension) {
    m_browserExtension = browserExtension;
  }

  protected BrowserExtension getBrowserExtension() {
    return m_browserExtension;
  }

  @Override
  protected void initializeUi(Composite parent) {
    super.initializeUi(parent);

    // create container for label and svg browser
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    setUiContainer(container);

    // create label
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());
    setUiLabel(label);

    // create browser that shows the SVG
    Browser browser = getUiEnvironment().getFormToolkit().createBrowser(container, SWT.NO_SCROLL);
    setUiField(browser);

    browser.addDisposeListener(new DisposeListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetDisposed(DisposeEvent e) {
        getBrowserExtension().detach();
      }
    });
    browser.addControlListener(new ControlListener() {

      private static final long serialVersionUID = 1L;

      @Override
      public void controlResized(ControlEvent e) {
        updateSvgDocument();
      }

      @Override
      public void controlMoved(ControlEvent e) {
        updateSvgDocument();
      }
    });
    attachBrowserExtension(browser);

    // layout
    getUiContainer().setLayout(new LogicalGridLayout(1, 0));
  }

  protected void attachBrowserExtension(Browser browser) {
    detachBrowserExtension();
    BrowserExtension browserExtension = new BrowserExtension(browser, getUiEnvironment(), new IHyperlinkCallback() {

      @Override
      public void execute(String url) {
        hyperlinkActivatedFromUi(url);
      }

    });
    browser.addDisposeListener(new DisposeListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetDisposed(DisposeEvent e) {
        detachBrowserExtension();
      }
    });
    adaptBrowserExtension(browserExtension);
    browserExtension.attach();
    setBrowserExtension(browserExtension);
  }

  protected void adaptBrowserExtension(BrowserExtension browserExtension) {
    browserExtension.setDefaultHyperlinkTarget("_top");
  }

  protected void detachBrowserExtension() {
    if (m_browserExtension != null) {
      m_browserExtension.detach();
      m_browserExtension = null;
    }
  }

  protected static String getSvgContentFromDocument(SVGDocument doc) throws ProcessingException {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      DOMSource domSource = new DOMSource(doc.getRootElement());
      StreamResult streamResult = new StreamResult(out);
      Transformer t = TransformerFactory.newInstance().newTransformer();
      t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      t.setOutputProperty(OutputKeys.ENCODING, DOCUMENT_ENCODING);
      t.transform(domSource, streamResult);
      out.close();
      return new String(out.toByteArray(), DOCUMENT_ENCODING);
    }
    catch (Exception e) {
      throw new ProcessingException("Writing SVG Failed", e);
    }
  }

  protected void updateSvgDocument() {
    getBrowserExtension().clearLocalHyperlinkCache();
    getBrowserExtension().clearResourceCache();

    SVGDocument doc = getSvgDocument();
    if (doc == null) {
      getUiField().setText("");
      return;
    }
    try {
      // set the dimensions of the svg element to the size of the browser field
      Rectangle browserBounds = getAbsoluteBrowserBounds();
      doc.getRootElement().setAttribute(SVGConstants.SVG_HEIGHT_ATTRIBUTE, browserBounds.height + "px");
      doc.getRootElement().setAttribute(SVGConstants.SVG_WIDTH_ATTRIBUTE, browserBounds.width + "px");

      // get the svg code as string and rewrite the links
      String svgText = getBrowserExtension().adaptHyperlinks(getSvgContentFromDocument(doc));

      // bugfix for SVG fields to ensure all context menus are closed when the user clicks into the svg field
      String contextMenuHideScript = "parent.parent.rwt.widgets.util.MenuManager.getInstance().update(null, 'mousedown');";

      // bugfix so that the svg field inherits the color of the parent container. otherwise it is always defined white.
      String backgroundColorInheritScript = null;
      if (getScoutObject().getBackgroundColor() == null) {
        backgroundColorInheritScript = "var iframes = parent.document.getElementsByTagName('iframe');" +
            "for(var i=0;i<iframes.length;i++) {" +
            "  var field=iframes[i].parentNode;" +
            "  var color=field.style.backgroundColor;" +
            "  if(color && color.toLowerCase() === 'rgb(255, 255, 255)') " +
            "    field.style.backgroundColor='';" +
            "}";
      }
      else {
        backgroundColorInheritScript = "";
      }

      // assume that html content is encoded properly. set the html content to the browser
      getUiField().setText("<html><body style=\"overflow: hidden;\" width=\"100%\" height=\"100%\" onload=\"" + backgroundColorInheritScript + "\" onclick=\"" + contextMenuHideScript + "\">" + svgText + "</body></html>");
    }
    catch (Exception e) {
      LOG.error("preparing svg browser content", e);
      getUiField().setText("");
    }
  }

  protected Rectangle getAbsoluteBrowserBounds() {
    Point pt = getUiField().getDisplay().map(getUiField(), null, new Point(0, 0));
    return new Rectangle(pt.x, pt.y, getUiField().getBounds().width, getUiField().getBounds().height);
  }

  @Override
  public Browser getUiField() {
    return (Browser) super.getUiField();
  }

  protected abstract SVGDocument getSvgDocument();

  protected abstract void hyperlinkActivatedFromUi(String url);

  @Override
  protected void setEnabledFromScout(boolean b) {
    //nop
  }
}
