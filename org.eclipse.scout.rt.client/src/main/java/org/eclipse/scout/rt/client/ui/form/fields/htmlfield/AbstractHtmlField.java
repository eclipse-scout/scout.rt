/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.htmlfield;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.htmlfield.HtmlFieldChains.HtmlFieldAppLinkActionChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.htmlfield.IHtmlFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.html.HtmlHelper;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("99301bfb-cccc-431f-b687-dc0bf73ff789")
public abstract class AbstractHtmlField extends AbstractValueField<String> implements IHtmlField {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractHtmlField.class);

  private IHtmlFieldUIFacade m_uiFacade;
  private Set<BinaryResource> m_attachments;

  public AbstractHtmlField() {
    this(true);
  }

  public AbstractHtmlField(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
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
   *          true if the url is not a valid external url but a local model url (http://local/...)
   * @deprecated use {@link #execAppLinkAction(String)} instead
   */
  @ConfigOperation
  @Order(230)
  @Deprecated
  protected void execHyperlinkAction(URL url, String path, boolean local) {
    LOG.info("execHyperlinkAction {} (in {})", url, getClass().getName());
  }

  /**
   * Called when an app link has been clicked.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(230)
  protected void execAppLinkAction(String ref) {
    //FIXME cgu: remove this code when execpHyperlinkAction has been removed
    URL url = null;
    boolean local = false;
    if (ref != null) {
      try {
        url = new URL(ref);
        local = "local".equals(url.getHost());
      }
      catch (MalformedURLException e) {
        LOG.error("Malformed URL '{}'", ref, e);
      }
    }
    execHyperlinkAction(url, ref, local);
  }

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    super.initConfig();
    setScrollBarEnabled(getConfiguredScrollBarEnabled());
    setHtmlEnabled(true);
  }

  @Override
  public void doAppLinkAction(String ref) {
    interceptAppLinkAction(ref);
  }

  public void setValueFromURL(URL url, String encoding) {
    if (url != null) {
      try {
        setValue(IOUtility.getContent(new InputStreamReader(url.openStream(), encoding)));
      }
      catch (IOException e) {
        throw new ProcessingException("URL " + url, e);
      }
    }
    else {
      setValue(null);
    }
  }

  @Override
  public String getPlainText() {
    String s = getValue();
    if (s == null) {
      return "";
    }
    return BEANS.get(HtmlHelper.class).toPlainText(s);
  }

  @Override
  protected String validateValueInternal(String rawValue) {
    String validValue = null;
    rawValue = super.validateValueInternal(rawValue);
    validValue = rawValue;
    if (validValue != null && validValue.length() == 0) {
      validValue = null;
    }
    return validValue;
  }

  @Override
  public IHtmlFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  // convert string to a real string
  @Override
  protected String parseValueInternal(String text) {
    if (text != null && text.length() == 0) {
      text = null;
    }
    return text;
  }

  /**
   * local images and local resources bound to the html text
   */
  @Override
  public Set<BinaryResource> getAttachments() {
    return CollectionUtility.hashSet(m_attachments);
  }

  @Override
  public void setAttachments(Collection<? extends BinaryResource> attachments) {
    m_attachments = CollectionUtility.<BinaryResource> hashSetWithoutNullElements(attachments);
  }

  @Override
  public void setScrollBarEnabled(boolean scrollBarEnabled) {
    propertySupport.setPropertyBool(PROP_SCROLL_BAR_ENABLED, scrollBarEnabled);
  }

  @Override
  public boolean isScrollBarEnabled() {
    return propertySupport.getPropertyBool(PROP_SCROLL_BAR_ENABLED);
  }

  protected class P_UIFacade implements IHtmlFieldUIFacade {

    @Override
    public void fireAppLinkActionFromUI(String ref) {
      doAppLinkAction(ref);
    }
  }

  @Override
  public void setScrollToAnchor(String anchorName) {
    propertySupport.setPropertyString(PROP_SCROLL_TO_ANCHOR, anchorName);
  }

  @Override
  public String getScrollToAnchor() {
    return propertySupport.getPropertyString(PROP_SCROLL_TO_ANCHOR);
  }

  @Override
  public void scrollToEnd() {
    propertySupport.setPropertyAlwaysFire(PROP_SCROLL_TO_END, null);
  }

  @Override
  public void setHtmlEnabled(boolean enabled) {
    propertySupport.setPropertyBool(PROP_HTML_ENABLED, enabled);
  }

  @Override
  public boolean isHtmlEnabled() {
    return propertySupport.getPropertyBool(PROP_HTML_ENABLED);
  }

  protected final void interceptAppLinkAction(String ref) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    HtmlFieldAppLinkActionChain chain = new HtmlFieldAppLinkActionChain(extensions);
    chain.execAppLinkAction(ref);
  }

  protected static class LocalHtmlFieldExtension<OWNER extends AbstractHtmlField> extends LocalValueFieldExtension<String, OWNER> implements IHtmlFieldExtension<OWNER> {

    public LocalHtmlFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execAppLinkAction(HtmlFieldAppLinkActionChain chain, String ref) {
      getOwner().execAppLinkAction(ref);
    }
  }

  @Override
  protected IHtmlFieldExtension<? extends AbstractHtmlField> createLocalExtension() {
    return new LocalHtmlFieldExtension<AbstractHtmlField>(this);
  }
}
