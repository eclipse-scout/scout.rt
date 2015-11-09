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
package org.eclipse.scout.rt.client.ui.form.fields.browserfield;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield.BrowserFieldChains.BrowserFieldLocationChangedChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield.IBrowserFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.annotations.Internal;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.EventListenerList;
import org.eclipse.scout.rt.shared.data.form.fields.browserfield.AbstractBrowserFieldData;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("6402e68c-abd1-42b8-8da2-b4a12f910c98")
@FormData(value = AbstractBrowserFieldData.class, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE, sdkCommand = SdkCommand.USE)
public abstract class AbstractBrowserField extends AbstractFormField implements IBrowserField {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractBrowserField.class);

  private IBrowserFieldUIFacade m_uiFacade;
  private final EventListenerList m_listenerList = new EventListenerList();

  public AbstractBrowserField() {
    this(true);
  }

  public AbstractBrowserField(boolean callInitializer) {
    super(callInitializer);
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(250)
  protected boolean getConfiguredScrollBarEnabled() {
    return false;
  }

  /**
   * Configures whether the sandbox mode is enabled.
   *
   * @return {@code true} if the sandbox mode of the embedded browser (iframe) must be enabled, {@code false} otherwise.
   */
  @Order(220)
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredSandboxEnabled() {
    return true;
  }

  /**
   * Configures the sandbox permissions.
   * <p>
   * This property is only relevant when sandbox is enabled (see {@link #getConfiguredSandboxEnabled()}).
   *
   * @return Sandbox permissions to enabled or {@code null} / {@link IBrowserField.SandboxPermission#none()} if no
   *         permissions should be enabled.
   */
  @Order(220)
  @ConfigProperty(ConfigProperty.OBJECT)
  protected EnumSet<SandboxPermission> getConfiguredSandboxPermissions() {
    return null;
  }

  /**
   * This callback is invoked when the application has received a post-message from the embedded browser (IFRAME).
   * <p>
   * The default does nothing.
   * <p>
   * <b>Important:</b> this callback is only invoked when the IFRAME is not restricted by the sandbox property. You must
   * either disable sandbox completely ({@link #getConfiguredSandboxEnabled()} returns false) or grant the required
   * permissions ({@link SandboxPermission#AllowScripts}).
   * <p>
   * Example java script call:
   *
   * <pre>
   * window.parent.postMessage('hello application!', 'http://localhost:8082')
   * </pre>
   *
   * @param data
   * @param orgin
   *          The origin of the window that sent the message at the time postMessage was called
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/postMessage">Window.postMessage()</a>
   */
  @ConfigOperation
  @Order(230)
  protected void execPostMessage(String data, String orgin) {
  }

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    super.initConfig();
    setScrollBarEnabled(getConfiguredScrollBarEnabled());
    setSandboxEnabled(getConfiguredSandboxEnabled());
    setSandboxPermissions(getConfiguredSandboxPermissions());
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
   * If this method returns without throwing in exception, the location is considered a valid URI. By default,
   * {@link URI} is used to check the location for syntax errors. If no scheme is defined, a {@link RuntimeException} is
   * thrown, <i>unless</i> the location starts with <code>//</code>. An URL starting with <code>//</code> is considered
   * a "protocol relative URL", i.e. it re-uses the current scheme, without explicitly specifying it. See also:
   * http://www.paulirish.com/2010/the-protocol-relative-url
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
    LOG.warn("Could not resolve binary resource for filename: {}", filename);
    return null;
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
  public void setSandboxPermissions(EnumSet<SandboxPermission> sandboxPermission) {
    propertySupport.setProperty(PROP_SANDBOX_PERMISSIONS, sandboxPermission);
  }

  @SuppressWarnings("unchecked")
  @Override
  public EnumSet<SandboxPermission> getSandboxPermissions() {
    return (EnumSet<SandboxPermission>) propertySupport.getProperty(PROP_SANDBOX_PERMISSIONS);
  }

  protected class P_UIFacade implements IBrowserFieldUIFacade {

    @Override
    public void firePostMessageFromUI(String data, String origin) {
      if (!isEnabled() || !isVisible()) {
        return;
      }
      interceptPostMessage(data, origin);
    }

    @Override
    public BinaryResource requestBinaryResourceFromUI(String filename) {
      return resolveBinaryResource(filename);
    }
  }

  protected final void interceptPostMessage(String data, String origin) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    BrowserFieldLocationChangedChain chain = new BrowserFieldLocationChangedChain(extensions);
    chain.execPostMessage(data, origin);
  }

  protected static class LocalBrowserFieldExtension<OWNER extends AbstractBrowserField> extends LocalFormFieldExtension<OWNER> implements IBrowserFieldExtension<OWNER> {

    public LocalBrowserFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execPostMessage(BrowserFieldLocationChangedChain chain, String data, String origin) {
      getOwner().execPostMessage(data, origin);
    }
  }

  @Override
  protected IBrowserFieldExtension<? extends AbstractBrowserField> createLocalExtension() {
    return new LocalBrowserFieldExtension<AbstractBrowserField>(this);
  }
}
