/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.browserfield;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield.BrowserFieldChains.BrowserFieldExternalWindowStateChangedChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield.BrowserFieldChains.BrowserFieldPostMessageChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield.IBrowserFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.event.FastListenerList;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.browserfield.AbstractBrowserFieldData;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("6402e68c-abd1-42b8-8da2-b4a12f910c98")
@FormData(value = AbstractBrowserFieldData.class, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE, sdkCommand = SdkCommand.USE)
public abstract class AbstractBrowserField extends AbstractFormField implements IBrowserField {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractBrowserField.class);

  private IBrowserFieldUIFacade m_uiFacade;
  private final FastListenerList<BrowserFieldListener> m_listenerList = new FastListenerList<>();

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
   * @return Sandbox permissions to enabled or {@code null} / {@link SandboxPermission#none()} if no permissions should
   * be enabled.
   */
  @Order(220)
  @ConfigProperty(ConfigProperty.OBJECT)
  protected EnumSet<SandboxPermission> getConfiguredSandboxPermissions() {
    return null;
  }

  /**
   * @return a list of origin URIs from which this field will receive messages posted via <i>postMessage</i>. If this is
   * {@code null} or empty, messages from all origins are accepted. The default is empty.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/postMessage">window.postMessage (MDN)</a>
   */
  protected List<String> getConfiguredTrustedMessageOrigins() {
    return Collections.emptyList();
  }

  /**
   * Configures the browser field general behavior. By default the content of the browser field is shown inline or in an
   * inline container (e.g. an &lt;iframe&gt; for the HTML5 UI layer), some very specific web pages (e.g. using
   * plug-ins, complex frames within the webpage) might not be displayed well or may even lead to a browser crash.
   * <p>
   * This property may be used to disable the inline container (&lt;iframe&gt; usage). Fallback behavior for the HTML5
   * UI layer is a separate browser window to show the content. Other UI layers may offer a different fallback, might
   * even decide not to offer a fallback behavior at all (property is just a hint for the UI layer).
   * <p>
   * Property can only be changed during initialization, it can not be changed during runtime.
   *
   * @return <code>true</code> to enable &lt;iframe&gt; usage (default), <code>false</code> otherwise.
   */
  @Order(230)
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredShowInExternalWindow() {
    return false;
  }

  /**
   * @see IBrowserField#getExternalWindowFieldText()
   */
  @Order(230)
  @ConfigProperty(ConfigProperty.STRING)
  protected String getConfiguredExternalWindowFieldText() {
    return TEXTS.get("ExternalWindowFieldText");
  }

  /**
   * @see IBrowserField#getExternalWindowButtonText()
   */
  @Order(230)
  @ConfigProperty(ConfigProperty.STRING)
  protected String getConfiguredExternalWindowButtonText() {
    return TEXTS.get("ExternalWindowButtonText");
  }

  /**
   * @see IBrowserField#isAutoCloseExternalWindow()
   */
  @Order(230)
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredAutoCloseExternalWindow() {
    return false;
  }

  /**
   * If true, the location property is updated whenever the location of the iframe changes. Default is false.
   * <p>
   * Note: This does only work if the iframe and the iframe's parent document have the same origin (protocol, port and
   * host are the same).
   */
  @Order(240)
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredTrackLocation() {
    return false;
  }

  /**
   * This callback is invoked when the field has received a message from the embedded page ({@code iframe}) or external
   * window.
   * <p>
   * The {@code data} can either be a {@link String}, a {@link Number}, a {@link Boolean} or an {@link IDataObject}.
   * <p>
   * If {@link #getTrustedMessageOrigins()} is set, the UI should already have checked that the sender is one of the
   * trusted origins. However, for security reasons, the {@code origin} should be checked again here.
   * <p>
   * The default does nothing.
   * <p>
   * Possible reasons why this method is not called:
   * <ul>
   * <li>The embedded page use the wrong target {@code window}.
   * <li>The browser blocked the message for some unknown reason (check the F12 developer console).
   * <li>The sandbox is enabled and does not allow sending messages.
   * <li>The embedded page specified the wrong {@code targetOrigin} when calling <i>postMessage</i>.
   * <li>The sender origin does not match the list {@link #getTrustedMessageOrigins()}.
   * <li>The browser field is disabled.
   * </ul>
   *
   * @param data
   *     Message received from the {@code iframe}. Can be a {@link String}, a {@link Number}, a {@link Boolean} or
   *     an {@link IDataObject}
   * @param origin
   *     The origin of the window that sent the message.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/postMessage">window.postMessage (MDN)</a>
   */
  @ConfigOperation
  @Order(261)
  protected void execPostMessage(Object data, String origin) {
  }

  /**
   * If page is opened in an external window this method is called whenever the external window is opened (also
   * re-opened) and closed.
   *
   * @param windowState
   *     <code>true</code> for external window has been opened (also should be called immediately after the page is
   *     displayed), <code>false</code> for external window has been closed.
   * @see #getConfiguredShowInExternalWindow()
   */
  @ConfigOperation
  @Order(270)
  protected void execExternalWindowStateChanged(boolean windowState) {
  }

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    super.initConfig();
    setScrollBarEnabled(getConfiguredScrollBarEnabled());
    setSandboxEnabled(getConfiguredSandboxEnabled());
    setSandboxPermissions(getConfiguredSandboxPermissions());
    setTrustedMessageOrigins(getConfiguredTrustedMessageOrigins());
    setShowInExternalWindow(getConfiguredShowInExternalWindow());
    setExternalWindowButtonText(getConfiguredExternalWindowButtonText());
    setExternalWindowFieldText(getConfiguredExternalWindowFieldText());
    setAutoCloseExternalWindow(getConfiguredAutoCloseExternalWindow());
    setTrackLocation(getConfiguredTrackLocation());
  }

  @Override
  public void importFormFieldData(AbstractFormFieldData source, boolean valueChangeTriggersEnabled) {
    Assertions.assertNotNull(source);
    AbstractBrowserFieldData fd = (AbstractBrowserFieldData) source;

    if (source.isValueSet()) {
      try {
        if (!valueChangeTriggersEnabled) {
          setValueChangeTriggerEnabled(false);
        }

        setLocationInternal(fd.getLocation());
        setBinaryResourceInternal(fd.getBinaryResource());
        setAttachmentsInternal(fd.getAttachments());
        fireContentChanged();
      }
      finally {
        if (!valueChangeTriggersEnabled) {
          setValueChangeTriggerEnabled(true);
        }
      }
    }
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
  public IFastListenerList<BrowserFieldListener> browserFieldListeners() {
    return m_listenerList;
  }

  protected void fireContentChanged() {
    fireBrowserFieldEvent(new BrowserFieldEvent(this, BrowserFieldEvent.TYPE_CONTENT_CHANGED));
  }

  protected void fireBrowserFieldEvent(BrowserFieldEvent e) {
    browserFieldListeners().list().forEach(listener -> listener.browserFieldChanged(e));
  }

  @Override
  public void setLocation(String location) {
    setLocationInternal(location);
    setBinaryResourceInternal(null);
    setAttachmentsInternal(null);
    fireContentChanged();
  }

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

  protected void setAttachmentsInternal(Set<BinaryResource> attachments) {
    propertySupport.setProperty(PROP_ATTACHMENTS, attachments);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<BinaryResource> getAttachments() {
    return (Set<BinaryResource>) propertySupport.getProperty(PROP_ATTACHMENTS);
  }

  protected void firePostMessage(Object message, String targetOrigin) {
    fireBrowserFieldEvent(new BrowserFieldEvent(this, BrowserFieldEvent.TYPE_POST_MESSAGE)
        .withMessage(message)
        .withTargetOrigin(targetOrigin));
  }

  @Override
  public void postMessage(Object message, String targetOrigin) {
    firePostMessage(message, targetOrigin);
  }

  @Override
  public void setScrollBarEnabled(boolean scrollBarEnabled) {
    propertySupport.setProperty(PROP_SCROLL_BAR_ENABLED, scrollBarEnabled);
  }

  @Override
  public boolean isScrollBarEnabled() {
    return propertySupport.getPropertyBool(PROP_SCROLL_BAR_ENABLED);
  }

  protected BinaryResource resolveBinaryResource(String filename) {
    if (filename == null) {
      return null;
    }
    BinaryResource binaryResource = getBinaryResource();
    if (binaryResource != null && ObjectUtility.equals(binaryResource.getFilename(), filename)) {
      return binaryResource;
    }
    Set<BinaryResource> attachments = getAttachments();
    if (attachments != null) {
      for (BinaryResource attachment : attachments) {
        if (ObjectUtility.equals(attachment.getFilename(), filename)) {
          return attachment;
        }
      }
    }
    LOG.warn("Could not resolve binary resource for filename: {}. Origin: {}", filename, getClass());
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

  @Override
  public List<String> getTrustedMessageOrigins() {
    return propertySupport.getPropertyList(PROP_TRUSTED_MESSAGE_ORIGINS);
  }

  @Override
  public void setTrustedMessageOrigins(List<String> trustedMessageOrigins) {
    propertySupport.setPropertyList(PROP_TRUSTED_MESSAGE_ORIGINS, trustedMessageOrigins);
  }

  @SuppressWarnings("unchecked")
  @Override
  public EnumSet<SandboxPermission> getSandboxPermissions() {
    return (EnumSet<SandboxPermission>) propertySupport.getProperty(PROP_SANDBOX_PERMISSIONS);
  }

  @Override
  public void setShowInExternalWindow(boolean showInExternalWindow) {
    propertySupport.setPropertyBool(PROP_SHOW_IN_EXTERNAL_WINDOW, showInExternalWindow);
  }

  @Override
  public boolean isShowInExternalWindow() {
    return propertySupport.getPropertyBool(PROP_SHOW_IN_EXTERNAL_WINDOW);
  }

  @Override
  public void setExternalWindowButtonText(String externalWindowButtonText) {
    propertySupport.setPropertyString(PROP_EXTERNAL_WINDOW_BUTTON_TEXT, externalWindowButtonText);
  }

  @Override
  public String getExternalWindowButtonText() {
    return propertySupport.getPropertyString(PROP_EXTERNAL_WINDOW_BUTTON_TEXT);
  }

  @Override
  public void setExternalWindowFieldText(String externalWindowFieldText) {
    propertySupport.setPropertyString(PROP_EXTERNAL_WINDOW_FIELD_TEXT, externalWindowFieldText);
  }

  @Override
  public String getExternalWindowFieldText() {
    return propertySupport.getPropertyString(PROP_EXTERNAL_WINDOW_FIELD_TEXT);
  }

  @Override
  public boolean isAutoCloseExternalWindow() {
    return propertySupport.getPropertyBool(PROP_AUTO_CLOSE_EXTERNAL_WINDOW);
  }

  @Override
  public void setAutoCloseExternalWindow(boolean autoCloseExternalWindow) {
    propertySupport.setPropertyBool(PROP_AUTO_CLOSE_EXTERNAL_WINDOW, autoCloseExternalWindow);
  }

  @Override
  public boolean isTrackLocation() {
    return propertySupport.getPropertyBool(PROP_TRACK_LOCATION);
  }

  @Override
  public void setTrackLocation(boolean trackLocation) {
    propertySupport.setPropertyBool(PROP_TRACK_LOCATION, trackLocation);
  }

  protected class P_UIFacade implements IBrowserFieldUIFacade {

    @Override
    public void firePostExternalWindowStateFromUI(boolean state) {
      interceptExternalWindowStateChanged(state);
    }

    @Override
    public void firePostMessageFromUI(Object data, String origin) {
      if (!isEnabledIncludingParents() || !isVisibleIncludingParents()) {
        return;
      }
      interceptPostMessage(data, origin);
    }

    @Override
    public BinaryResource requestBinaryResourceFromUI(String filename) {
      return resolveBinaryResource(filename);
    }

    @Override
    public void setLocationFromUI(String location) {
      setLocation(location);
    }
  }

  protected final void interceptPostMessage(Object data, String origin) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    BrowserFieldPostMessageChain chain = new BrowserFieldPostMessageChain(extensions);
    chain.execPostMessage(data, origin);
  }

  protected final void interceptExternalWindowStateChanged(boolean state) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    BrowserFieldExternalWindowStateChangedChain chain = new BrowserFieldExternalWindowStateChangedChain(extensions);
    chain.execExternalWindowStateChanged(state);
  }

  protected static class LocalBrowserFieldExtension<OWNER extends AbstractBrowserField> extends LocalFormFieldExtension<OWNER> implements IBrowserFieldExtension<OWNER> {

    public LocalBrowserFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execPostMessage(BrowserFieldPostMessageChain chain, Object data, String origin) {
      getOwner().execPostMessage(data, origin);
    }

    @Override
    public void execExternalWindowStateChanged(BrowserFieldExternalWindowStateChangedChain chain, boolean state) {
      getOwner().execExternalWindowStateChanged(state);
    }
  }

  @Override
  protected IBrowserFieldExtension<? extends AbstractBrowserField> createLocalExtension() {
    return new LocalBrowserFieldExtension<>(this);
  }
}
