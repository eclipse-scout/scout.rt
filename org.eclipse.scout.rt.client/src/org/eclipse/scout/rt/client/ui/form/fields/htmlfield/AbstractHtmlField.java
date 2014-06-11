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
package org.eclipse.scout.rt.client.ui.form.fields.htmlfield;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.HTMLUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.AbstractBrowserField;
import org.eclipse.scout.rt.client.ui.form.fields.documentfield.AbstractDocumentField;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;
import org.eclipse.scout.service.SERVICES;

/**
 * This model represents a UI specific browser, in swing it is a JEditorPane html viewer/editor, in swt it is the native
 * browser.
 * <p>
 * See also {@link AbstractBrowserField} for html viewing and {@link AbstractDocumentField} for html editing (requires a
 * fragment such as microsoft word editor)
 */
@ClassId("99301bfb-cccc-431f-b687-dc0bf73ff789")
public abstract class AbstractHtmlField extends AbstractValueField<String> implements IHtmlField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractHtmlField.class);

  private IHtmlFieldUIFacade m_uiFacade;
  private boolean m_htmlEditor;
  private boolean m_scrollBarEnabled;
  private Set<RemoteFile> m_attachments;
  private Boolean m_monitorSpelling = null; // If null the application-wide

  public AbstractHtmlField() {
    this(true);
  }

  public AbstractHtmlField(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(230)
  @ValidationRule(ValidationRule.MAX_LENGTH)
  protected int getConfiguredMaxLength() {
    return Integer.MAX_VALUE;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(240)
  protected boolean getConfiguredHtmlEditor() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(250)
  protected boolean getConfiguredScrollBarEnabled() {
    return false;
  }

  /**
   * @param url
   * @param path
   *          {@link URL#getPath()}
   * @param local
   *          true if the url is not a valid external url but a local model url
   *          (http://local/...)
   */
  @ConfigOperation
  @Order(230)
  protected void execHyperlinkAction(URL url, String path, boolean local) throws ProcessingException {
    LOG.info("execHyperlinkAction " + url + " (in " + getClass().getName() + ")");
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
    m_htmlEditor = getConfiguredHtmlEditor();
    m_scrollBarEnabled = getConfiguredScrollBarEnabled();
    setMaxLength(getConfiguredMaxLength());
  }

  @Override
  public int getMaxLength() {
    int len = propertySupport.getPropertyInt(PROP_MAX_LENGTH);
    if (len <= 0) {
      len = 200;
    }
    return len;
  }

  @Override
  public void setMaxLength(int len) {
    if (len > 0) {
      propertySupport.setPropertyInt(PROP_MAX_LENGTH, len);
    }
    if (isInitialized()) {
      setValue(getValue());
    }
  }

  @Override
  public void doHyperlinkAction(URL url) throws ProcessingException {
    if (url != null) {
      execHyperlinkAction(url, url.getPath(), "local".equals(url.getHost()));
    }
  }

  public void setValueFromURL(URL url, String encoding) throws ProcessingException {
    if (url != null) {
      try {
        setValue(IOUtility.getContent(new InputStreamReader(url.openStream(), encoding)));
      }
      catch (ProcessingException e) {
        throw e;
      }
      catch (Throwable t) {
        throw new ProcessingException("URL " + url, t);
      }
    }
    else {
      setValue(null);
    }
  }

  @Override
  public String getPlainText() {
    String s = getValue();
    if (s != null) {
      return HTMLUtility.getPlainText(HTMLUtility.toHtmlDocument(s));
    }
    return "";
  }

  @Override
  public boolean isHtmlEditor() {
    return m_htmlEditor;
  }

  @Override
  protected String validateValueInternal(String rawValue) throws ProcessingException {
    String validValue = null;
    rawValue = super.validateValueInternal(rawValue);
    validValue = rawValue;
    if (validValue != null && validValue.length() == 0) {
      validValue = null;
    }
    if (validValue != null) {
      if (validValue.length() > getMaxLength()) {
        validValue = validValue.substring(0, getMaxLength());
      }
    }
    return validValue;
  }

  @Override
  public IHtmlFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  // convert string to a real string
  @Override
  protected String parseValueInternal(String text) throws ProcessingException {
    if (text != null && text.length() == 0) {
      text = null;
    }
    return text;
  }

  @Override
  public void insertImage(String imageUrl) {
    if (imageUrl != null) {
      propertySupport.firePropertyChange(PROP_INSERT_IMAGE, null, imageUrl);
    }
  }

  /**
   * local images and local resources bound to the html text
   */
  @Override
  public Set<RemoteFile> getAttachments() {
    return CollectionUtility.hashSet(m_attachments);
  }

  @Override
  public void setAttachments(Collection<? extends RemoteFile> attachments) {
    m_attachments = CollectionUtility.<RemoteFile> hashSetWithoutNullElements(attachments);
  }

  @Override
  public boolean isScrollBarEnabled() {
    return m_scrollBarEnabled;
  }

  private class P_UIFacade implements IHtmlFieldUIFacade {

    @Override
    public boolean setTextFromUI(String htmlText) {
      if (isHtmlEditor()) {
        if (htmlText != null && htmlText.length() == 0) {
          htmlText = null;
        }
        // parse always, validity might change even if text is same
        return parseValue(htmlText);
      }
      return true;
    }

    @Override
    public void setAttachmentsFromUI(Collection<? extends RemoteFile> attachments) {
      setAttachments(attachments);
    }

    @Override
    public void fireHyperlinkActionFromUI(URL url) {
      try {
        doHyperlinkAction(url);
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }
  }

  @Override
  public void setScrollToAnchor(String anchorName) {
    propertySupport.setPropertyString(PROP_SCROLLBAR_SCROLL_TO_ANCHOR, anchorName);
  }

  @Override
  public String getScrollToAnchor() {
    return propertySupport.getPropertyString(PROP_SCROLLBAR_SCROLL_TO_ANCHOR);
  }

  @Override
  public void scrollToEnd() {
    propertySupport.setPropertyAlwaysFire(PROP_SCROLLBAR_SCROLL_TO_END, null);
  }

  /**
   * Returns whether this html component is spell checkable.
   */
  @Override
  public boolean isSpellCheckEnabled() {
    return (this.isEnabled() && this.isEnabledGranted() && (!(this.getForm() instanceof ISearchForm)));
  }

  /**
   * Returns whether this html component should be monitored for spelling errors
   * in the background ("check as you type").<br>
   * If it is not defined, null is returned, then the application default is
   * used.
   */
  @Override
  public Boolean isSpellCheckAsYouTypeEnabled() {
    return m_monitorSpelling;
  }

  /**
   * Sets whether to monitor this html component for spelling errors in the
   * background ("check as you type").<br>
   * Use null for application default.
   */
  public void setSpellCheckAsYouTypeEnabled(boolean monitorSpelling) {
    m_monitorSpelling = new Boolean(monitorSpelling);
  }
}
