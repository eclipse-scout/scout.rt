/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.svg.client.svgfield;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.event.FastListenerList;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;
import org.eclipse.scout.rt.svg.client.SVGUtility;
import org.eclipse.scout.rt.svg.client.extension.svgfield.ISvgFieldExtension;
import org.eclipse.scout.rt.svg.client.extension.svgfield.SvgFieldChains.SvgFieldAppLinkActionChain;
import org.eclipse.scout.rt.svg.client.extension.svgfield.SvgFieldChains.SvgFieldClickedChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGPoint;

@ClassId("20ed1036-9314-4bd8-bad6-c66e778f281f")
public abstract class AbstractSvgField extends AbstractFormField implements ISvgField {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractSvgField.class);

  private ISvgFieldUIFacade m_uiFacade;
  private final FastListenerList<ISvgFieldListener> m_listenerList = new FastListenerList<>();
  // only do one action at a time
  private boolean m_actionRunning;

  public AbstractSvgField() {
    this(true);
  }

  public AbstractSvgField(boolean callInitializer) {
    super(callInitializer);
    m_actionRunning = false;
  }

  @Override
  protected int getConfiguredVerticalAlignment() {
    return 0;
  }

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
  protected void execClicked(SvgFieldEvent e) {
  }

  /**
   * Called when an app link has been clicked.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(230)
  protected void execAppLinkAction(String ref) {
  }

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    super.initConfig();
  }

  @Override
  public IFastListenerList<ISvgFieldListener> svgFieldListeners() {
    return m_listenerList;
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
        SVGUtility.writeSVGDocument(doc, out, StandardCharsets.UTF_8.name());
        LOG.trace("{}.setSvgDocument\n{}", getClass().getName(), new String(out.toByteArray(), StandardCharsets.UTF_8));
      }
      catch (RuntimeException e) {
        LOG.warn("Could not set svg document", e);
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

  @Override
  public void doAppLinkAction(String ref) {
    if (!m_actionRunning) {
      try {
        m_actionRunning = true;
        SvgFieldEvent e = new SvgFieldEvent(this, SvgFieldEvent.TYPE_HYPERLINK, null, ref);
        // single observer
        interceptAppLinkAction(ref);
        fireSvgFieldEventInternal(e);
      }
      finally {
        m_actionRunning = false;
      }
    }
  }

  private void fireClick() {
    if (!m_actionRunning) {
      try {
        m_actionRunning = true;
        SvgFieldEvent e = new SvgFieldEvent(this, SvgFieldEvent.TYPE_CLICKED, getSelection(), null);
        // single observer
        interceptClicked(e);
        fireSvgFieldEventInternal(e);
      }
      finally {
        m_actionRunning = false;
      }
    }
  }

  private void fireSvgFieldEventInternal(SvgFieldEvent e) {
    svgFieldListeners().list().forEach(listener -> listener.handleSvgFieldEvent(e));
  }

  protected class P_UIFacade implements ISvgFieldUIFacade {
    @Override
    public void fireAppLinkActionFromUI(String ref) {
      doAppLinkAction(ref);
    }

    @Override
    public void fireClickFromUI(SVGPoint point) {
      if (!isEnabledIncludingParents() || !isVisibleIncludingParents()) {
        return;
      }
      setSelection(point);
      if (point == null) {
        return;
      }
      fireClick();
    }
  }

  protected final void interceptClicked(SvgFieldEvent e) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SvgFieldClickedChain chain = new SvgFieldClickedChain(extensions);
    chain.execClicked(e);
  }

  protected final void interceptAppLinkAction(String ref) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SvgFieldAppLinkActionChain chain = new SvgFieldAppLinkActionChain(extensions);
    chain.execAppLinkAction(ref);
  }

  protected static class LocalSvgFieldExtension<OWNER extends AbstractSvgField> extends LocalFormFieldExtension<OWNER> implements ISvgFieldExtension<OWNER> {

    public LocalSvgFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execClicked(SvgFieldClickedChain chain, SvgFieldEvent e) {
      getOwner().execClicked(e);
    }

    @Override
    public void execAppLinkAction(SvgFieldAppLinkActionChain chain, String ref) {
      getOwner().execAppLinkAction(ref);
    }
  }

  @Override
  protected ISvgFieldExtension<? extends AbstractSvgField> createLocalExtension() {
    return new LocalSvgFieldExtension<>(this);
  }
}
