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
package org.eclipse.scout.rt.client.ui.form.fields.browserfield;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.resource.BinaryResource;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield.BrowserFieldChains.BrowserFieldAcceptLocationChangeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield.BrowserFieldChains.BrowserFieldLocationChangedChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield.IBrowserFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.shared.data.form.fields.browserfield.AbstractBrowserFieldData;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

@ClassId("6402e68c-abd1-42b8-8da2-b4a12f910c98")
@FormData(value = AbstractBrowserFieldData.class, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE, sdkCommand = SdkCommand.USE)
public abstract class AbstractBrowserField extends AbstractFormField implements IBrowserField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractBrowserField.class);

  private IBrowserFieldUIFacade m_uiFacade;
  private final EventListenerList m_listenerList = new EventListenerList();

  public AbstractBrowserField() {
    this(true);
  }

  public AbstractBrowserField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected boolean getConfiguredStatusVisible() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(250)
  protected boolean getConfiguredScrollBarEnabled() {
    return false;
  }

  @Order(220)
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredSandboxEnabled() {
    return true;
  }

  @Order(220)
  @ConfigProperty(ConfigProperty.OBJECT)
  protected Set<SandboxPermissions> getConfiguredSandboxRestrictions() {
    return null;
  }

  /**
   * This callback is invoked before the link is followed, it can be used as handler and vetoer. The default returns
   * true.<br>
   *
   * @return true to accept this location, false to prevent the browser from going to that location (equal to browser
   *         esc/stop button)
   * @param location
   * @param path
   *          may be null for locations like about:blank or javascript:... {@link URL#getPath()}
   * @param local
   *          true if the url is not a valid external url but a local model url
   *          (http://local/...)
   */
  @ConfigOperation
  @Order(230)
  protected boolean execAcceptLocationChange(String location, String path, boolean local) throws ProcessingException {
    return true;
  }

  /**
   * This callback is invoked after the link was followed, thus it is already at that location
   * <p>
   * The default does noting.
   *
   * @param location
   * @param path
   *          may be null for locations like about:blank or javascript:... {@link URL#getPath()}
   * @param local
   *          true if the url is not a valid external url but a local model url
   *          (http://local/...)
   */
  @ConfigOperation
  @Order(230)
  protected void execLocationChanged(String location, String path, boolean local) throws ProcessingException {
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
    setScrollBarEnabled(getConfiguredScrollBarEnabled());
    setSandboxEnabled(getConfiguredSandboxEnabled());
    setSandboxPermissions(getSandboxPermissions());
  }

  @Override
  protected void applySearchInternal(SearchFilter search) {
    //nop
  }

  @Override
  public IBrowserFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public void addBrowserFieldListener(BrowserFieldListener listener) {
    m_listenerList.add(BrowserFieldListener.class, listener);
  }

  @Override
  public void removeBrowserFieldListener(BrowserFieldListener listener) {
    m_listenerList.remove(BrowserFieldListener.class, listener);
  }

  protected void fireContentChanged() {
    fireBrowserFieldEvent(new BrowserFieldEvent(this, BrowserFieldEvent.TYPE_CONTENT_CHANGED));
  }

  protected void fireBrowserFieldEvent(BrowserFieldEvent e) {
    EventListener[] listeners = m_listenerList.getListeners(BrowserFieldListener.class);
    if (listeners != null && listeners.length > 0) {
      for (int i = 0; i < listeners.length; i++) {
        ((BrowserFieldListener) listeners[i]).browserFieldChanged(e);
      }
    }
  }

  @Override
  public void setLocation(String location) {
    setLocationInternal(location);
    setBinaryResourceInternal(null);
    setAttachmentsInternal(null);
    fireContentChanged();
  }

  @Internal
  protected void setLocationInternal(String location) {
    validateLocation(location);
    propertySupport.setProperty(PROP_LOCATION, location);
  }

  /**
   * If this method returns without throwing in exception, the location is considered a valid URI.
   * By default, {@link URI} is used to check the location for syntax errors. If no scheme is defined,
   * a {@link RuntimeException} is thrown, <i>unless</i> the location starts with <code>//</code>. An URL
   * starting with <code>//</code> is considered a "protocol relative URL", i.e. it re-uses the current
   * scheme, without explicitly specifying it. See also: http://www.paulirish.com/2010/the-protocol-relative-url
   */
  protected void validateLocation(String location) {
    try {
      if (location == null) {
        return;
      }
      URI uri = new URI(location);
      // Prevent
      if (uri.getScheme() == null && !location.startsWith("//")) {
        throw new IllegalArgumentException("Missing scheme in URI: " + location);
      }
    }
    catch (URISyntaxException e) {
      throw new IllegalArgumentException("Invalid URI: " + location, e);
    }
  }

  @Override
  public String getLocation() {
    return propertySupport.getPropertyString(PROP_LOCATION);
  }

  @Override
  public void setBinaryResource(BinaryResource binaryResource) {
    setLocationInternal(null);
    setBinaryResourceInternal(binaryResource);
    fireContentChanged();
  }

  @Override
  public void setBinaryResource(BinaryResource binaryResource, BinaryResource... attachments) {
    setLocationInternal(null);
    setBinaryResourceInternal(binaryResource);
    if (attachments == null) {
      setAttachmentsInternal(null);
    }
    else {
      Set<BinaryResource> attachmentSet = new HashSet<>();
      for (BinaryResource attachment : attachments) {
        if (attachment != null) {
          attachmentSet.add(attachment);
        }
      }
      setAttachmentsInternal(attachmentSet);
    }
    fireContentChanged();
  }

  @Internal
  protected void setBinaryResourceInternal(BinaryResource binaryResource) {
    propertySupport.setProperty(PROP_BINARY_RESOURCE, binaryResource);
  }

  @Override
  public BinaryResource getBinaryResource() {
    return (BinaryResource) propertySupport.getProperty(PROP_BINARY_RESOURCE);
  }

  @Override
  public void setAttachments(Set<BinaryResource> attachments) {
    setAttachmentsInternal(attachments);
  }

  @Internal
  protected void setAttachmentsInternal(Set<BinaryResource> attachments) {
    propertySupport.setProperty(PROP_ATTACHMENTS, attachments);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<BinaryResource> getAttachments() {
    return (Set<BinaryResource>) propertySupport.getProperty(PROP_ATTACHMENTS);
  }

  protected void setScrollBarEnabled(boolean scrollBarEnabled) {
    propertySupport.setProperty(PROP_SCROLLBARS_ENABLED, scrollBarEnabled);
  }

  @Override
  public boolean isScrollBarEnabled() {
    return propertySupport.getPropertyBool(PROP_SCROLLBARS_ENABLED);
  }

  protected BinaryResource resolveBinaryResource(String filename) {
    if (filename == null) {
      return null;
    }
    BinaryResource binaryResource = getBinaryResource();
    if (binaryResource != null && CompareUtility.equals(binaryResource.getFilename(), filename)) {
      return binaryResource;
    }
    Set<BinaryResource> attachments = getAttachments();
    if (attachments != null) {
      for (BinaryResource attachment : attachments) {
        if (CompareUtility.equals(attachment.getFilename(), filename)) {
          return attachment;
        }
      }
    }
    LOG.warn("Could not resolve binary resource for filename: " + filename);
    return null;
  }

  @Override
  public void doLocationChange(String location) throws ProcessingException {
    if (getUIFacade().fireBeforeLocationChangedFromUI(location)) {
      getUIFacade().fireAfterLocationChangedFromUI(location);
    }
  }

  @Override
  public void setSandboxEnabled(boolean sandboxEnabled) {
    propertySupport.setProperty(PROP_SANDBOX_ENABLED, sandboxEnabled);
  }

  @Override
  public boolean isSandboxEnabled() {
    return propertySupport.getPropertyBool(PROP_SANDBOX_ENABLED);
  }

  @Override
  public void setSandboxPermissions(EnumSet<SandboxPermissions> sandboxPermissions) {
    propertySupport.setProperty(PROP_SANDBOX_PERMISSIONS, sandboxPermissions);
  }

  @SuppressWarnings("unchecked")
  @Override
  public EnumSet<SandboxPermissions> getSandboxPermissions() {
    return (EnumSet<SandboxPermissions>) propertySupport.getProperty(PROP_SANDBOX_PERMISSIONS);
  }

  private class P_UIFacade implements IBrowserFieldUIFacade {

    @Override
    public boolean fireBeforeLocationChangedFromUI(String location) {
      try {
        URL url = null;
        try {
          url = new URL(location);
        }
        catch (Exception t) {
          //nop
        }
        return interceptAcceptLocationChange(location, url != null ? url.getPath() : null, url != null && "local".equals(url.getHost()));
      }
      catch (Exception t) {
        LOG.error("location: " + location, t);
      }
      return false;
    }

    @Override
    public void fireAfterLocationChangedFromUI(String location) {
      try {
        URL url = null;
        try {
          url = new URL(location);
        }
        catch (Exception t) {
          //nop
        }
        interceptLocationChanged(location, url != null ? url.getPath() : null, url != null && "local".equals(url.getHost()));
      }
      catch (Exception t) {
        LOG.error("location: " + location, t);
      }
    }

    @Override
    public BinaryResource requestBinaryResourceFromUI(String filename) {
      return resolveBinaryResource(filename);
    }
  }

  protected final void interceptLocationChanged(String location, String path, boolean local) throws ProcessingException {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    BrowserFieldLocationChangedChain chain = new BrowserFieldLocationChangedChain(extensions);
    chain.execLocationChanged(location, path, local);
  }

  protected final boolean interceptAcceptLocationChange(String location, String path, boolean local) throws ProcessingException {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    BrowserFieldAcceptLocationChangeChain chain = new BrowserFieldAcceptLocationChangeChain(extensions);
    return chain.execAcceptLocationChange(location, path, local);
  }

  protected static class LocalBrowserFieldExtension<OWNER extends AbstractBrowserField> extends LocalFormFieldExtension<OWNER> implements IBrowserFieldExtension<OWNER> {

    public LocalBrowserFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execLocationChanged(BrowserFieldLocationChangedChain chain, String location, String path, boolean local) throws ProcessingException {
      getOwner().execLocationChanged(location, path, local);
    }

    @Override
    public boolean execAcceptLocationChange(BrowserFieldAcceptLocationChangeChain chain, String location, String path, boolean local) throws ProcessingException {
      return getOwner().execAcceptLocationChange(location, path, local);
    }
  }

  @Override
  protected IBrowserFieldExtension<? extends AbstractBrowserField> createLocalExtension() {
    return new LocalBrowserFieldExtension<AbstractBrowserField>(this);
  }
}
