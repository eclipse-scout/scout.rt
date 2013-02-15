/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.svg.client.svgfield;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.EventListener;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.scout.svg.client.SVGUtility;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGPoint;

public abstract class AbstractSvgField extends AbstractFormField implements ISvgField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSvgField.class);

  private ISvgFieldUIFacade m_uiFacade;
  private final EventListenerList m_listenerList = new EventListenerList();

  public AbstractSvgField() {
    this(true);
  }

  public AbstractSvgField(boolean callInitializer) {
    super(callInitializer);
  }

  @ConfigPropertyValue("0")
  @Override
  protected int getConfiguredVerticalAlignment() {
    return 0;
  }

  @ConfigPropertyValue("0")
  @Override
  protected int getConfiguredHorizontalAlignment() {
    return 0;
  }

  /**
   * called when a svg item was clicked, use {@link SVGUtility#getElementsAt(SVGDocument, SVGPoint)} with
   * {@link #getSelection()} to find out which elements
   */
  @ConfigOperation
  @Order(10)
  protected void execClicked(SvgFieldEvent e) throws ProcessingException {
  }

  /**
   * called when a svg hyperlink was clicked
   */
  @ConfigOperation
  @Order(20)
  protected void execHyperlink(SvgFieldEvent e) throws ProcessingException {
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
  }

  @Override
  public void addSvgFieldListener(ISvgFieldListener listener) {
    m_listenerList.add(ISvgFieldListener.class, listener);
  }

  @Override
  public void removeSvgFieldListener(ISvgFieldListener listener) {
    m_listenerList.remove(ISvgFieldListener.class, listener);
  }

  @Override
  public SVGDocument getSvgDocument() {
    return (SVGDocument) propertySupport.getProperty(PROP_SVG_DOCUMENT);
  }

  @Override
  public void setSvgDocument(SVGDocument doc) {
    setSelection(null);
    if (LOG.isTraceEnabled()) {
      try {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SVGUtility.writeSVGDocument(doc, out, "UTF-8");
        LOG.trace(getClass().getName() + ".setSvgDocument\n" + new String(out.toByteArray(), "UTF-8"));
      }
      catch (Throwable t) {
        //nop
      }
    }
    propertySupport.setProperty(PROP_SVG_DOCUMENT, doc);
  }

  @Override
  public SVGPoint getSelection() {
    return (SVGPoint) propertySupport.getProperty(PROP_SELECTION);
  }

  @Override
  public void setSelection(SVGPoint point) {
    propertySupport.setProperty(PROP_SELECTION, point);
  }

  /*
   * UI accessible
   */
  @Override
  public ISvgFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  private void fireHyperlink(URL url) {
    SvgFieldEvent e = new SvgFieldEvent(this, SvgFieldEvent.TYPE_HYPERLINK, null, url);
    // single observer
    try {
      execHyperlink(e);
    }
    catch (ProcessingException pe) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(pe);
    }
    catch (Throwable t) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
    }
    fireSvgFieldEventInternal(e);
  }

  private void fireClick() {
    SvgFieldEvent e = new SvgFieldEvent(this, SvgFieldEvent.TYPE_CLICKED, getSelection(), null);
    // single observer
    try {
      execClicked(e);
    }
    catch (ProcessingException pe) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(pe);
    }
    catch (Throwable t) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
    }
    fireSvgFieldEventInternal(e);
  }

  private void fireSvgFieldEventInternal(SvgFieldEvent e) {
    EventListener[] a = m_listenerList.getListeners(ISvgFieldListener.class);
    if (a != null) {
      for (int i = 0; i < a.length; i++) {
        ((ISvgFieldListener) a[i]).handleSvgFieldEvent(e);
      }
    }
  }

  private class P_UIFacade implements ISvgFieldUIFacade {
    @Override
    public void fireHyperlinkFromUI(URL url) {
      if (url == null) {
        return;
      }
      fireHyperlink(url);
    }

    @Override
    public void fireClickFromUI(SVGPoint point) {
      setSelection(point);
      if (point == null) {
        return;
      }
      fireClick();
    }
  }
}
