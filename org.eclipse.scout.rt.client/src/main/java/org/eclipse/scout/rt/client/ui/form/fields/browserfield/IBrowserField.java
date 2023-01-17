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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;

/**
 * This model represents a separate "website" inside the application.
 * <p>
 * The content is either a URL (location, 3rd party website or webapp) or a {@link BinaryResource} with attachments
 * (e.g. static HTML content with images, styles etc.).
 */
public interface IBrowserField extends IFormField {

  enum SandboxPermission {
    AllowForms("allow-forms"),
    AllowPointerLock("allow-pointer-lock"),
    AllowPopups("allow-popups"),
    AllowPopupsToEscapeSandbox("allow-popups-to-escape-sandbox"),
    AllowSameOrigin("allow-same-origin"),
    AllowScripts("allow-scripts"),
    AllowTopNavigation("allow-top-navigation"),
    AllowPresentation("allow-presentation"),
    AllowDownloads("allow-downloads"),
    AllowModals("allow-modals"),
    AllowOrientationLock("allow-orientation-lock"),
    AllowTopNavigationByUserActivation("allow-top-navigation-by-user-activation");

    private final String m_attribute;

    SandboxPermission(String attribute) {
      m_attribute = attribute;
    }

    public static EnumSet<SandboxPermission> none() {
      return EnumSet.noneOf(SandboxPermission.class);
    }

    public static EnumSet<SandboxPermission> all() {
      return EnumSet.allOf(SandboxPermission.class);
    }

    /**
     * @return the value to use in the HTML <code>sandbox</code> attribute
     */
    public String getAttribute() {
      return m_attribute;
    }
  }

  String PROP_LOCATION = "location";
  String PROP_BINARY_RESOURCE = "binaryResource";
  String PROP_ATTACHMENTS = "attachments";
  String PROP_SCROLL_BAR_ENABLED = "scrollBarEnabled";
  String PROP_SANDBOX_ENABLED = "sandboxEnabled";
  String PROP_SANDBOX_PERMISSIONS = "sandboxPermissions";
  String PROP_TRUSTED_MESSAGE_ORIGINS = "trustedMessageOrigins";
  String PROP_SHOW_IN_EXTERNAL_WINDOW = "showInExternalWindow";
  String PROP_EXTERNAL_WINDOW_FIELD_TEXT = "externalWindowFieldText";
  String PROP_EXTERNAL_WINDOW_BUTTON_TEXT = "externalWindowButtonText";
  String PROP_AUTO_CLOSE_EXTERNAL_WINDOW = "autoCloseExternalWindow";
  String PROP_TRACK_LOCATION = "trackLocation";

  IBrowserFieldUIFacade getUIFacade();

  IFastListenerList<BrowserFieldListener> browserFieldListeners();

  default void addBrowserFieldListener(BrowserFieldListener listener) {
    browserFieldListeners().add(listener);
  }

  default void removeBrowserFieldListener(BrowserFieldListener listener) {
    browserFieldListeners().remove(listener);
  }

  /**
   * Sets the content of the field to the given location. The location should be an URI. A validation is performed by
   * default.
   *
   * @throws RuntimeException
   *           when the internal validation fails
   */
  void setLocation(String location);

  String getLocation();

  /**
   * Stores the given resource in the field, generates a dynamic resource URL for it and sets the location (value) to
   * that URL.
   */
  void setBinaryResource(BinaryResource binaryResource);

  /**
   * Convenience method that sets the binary resource and the attachments in the same call.
   */
  void setBinaryResource(BinaryResource binaryResource, BinaryResource... attachments);

  /**
   * @see #setBinaryResource(BinaryResource)
   */
  BinaryResource getBinaryResource();

  /**
   * Adds additional resources that may be referenced by the main resource ({@link #getBinaryResource()}). To reference
   * an attachment, set the "filename" attribute correspondingly. The attachment's URL shares the same base path, with a
   * different filename at the end.
   */
  void setAttachments(Set<BinaryResource> attachments);

  /**
   * @see #setAttachments(Set)
   */
  Set<BinaryResource> getAttachments();

  /**
   * Sends a message to the embedded web page ({@code iframe}).
   *
   * @param message
   *          The message to send. Can be a {@link String}, a {@link Number}, a {@link Boolean} or an
   *          {@link IDataObject}. All other objects are ignored.
   * @param targetOrigin
   *          The expected origin of the receiving {@code window}. If the origin does not match, the browser will not
   *          dispatch the message for security reasons. See the
   *          <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/postMessage">documentation</a> for
   *          details.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/postMessage">window.postMessage (MDN)</a>
   */
  void postMessage(Object message, String targetOrigin);

  void setScrollBarEnabled(boolean scrollBarEnabled);

  boolean isScrollBarEnabled();

  /**
   * Enable or disable sandboxing for this field.
   * <p>
   * To display insecure content (for example, third-party content or HTML entered by a possibly malicious user) in a
   * browser field, you can enable sandboxing which places a set of extra restrictions on the content.
   * </p>
   * <p>
   * The implementation depends on the UI layer. In the case uf the HMTL5 UI layer, the implementation adds the
   * <code>sandbox</code> attribute to the <code>iframe</code> HTML tag in which the content is displayed.<br/>
   * You can lift specific restrictions with {@link #setSandboxPermissions(EnumSet)}. For more information of sandboxing
   * in HTML, refer to the HTML 5 specification.
   * </p>
   *
   * @param sandboxEnabled
   *          <code>true</code> if sandboxing is to be turned on
   * @see #isSandboxEnabled()
   * @see #setSandboxPermissions(EnumSet)
   * @see <a href="http://www.w3.org/TR/html5/embedded-content-0.html#attr-iframe-sandbox">HTML 5 specification: Iframe
   *      sandbox</a>
   */
  void setSandboxEnabled(boolean sandboxEnabled);

  /**
   * Returns true if sandboxing is enabled for this browser field.
   *
   * @return <code>true</code> if sandboxing is enabled for this browser field.
   * @see #setSandboxEnabled(boolean)
   * @see #getSandboxPermissions()
   */
  boolean isSandboxEnabled();

  /**
   * Set which sandbox restrictions on the content should be lifted.
   * <p>
   * Passing an empty set, or <code>null</code> as argument means all restrictions apply.
   * </p>
   *
   * @param sandboxPermission
   *          Sandbox permissions to lift restrictions on the content
   * @see #getSandboxPermissions()
   * @see #setSandboxEnabled(boolean)
   */
  void setSandboxPermissions(EnumSet<SandboxPermission> sandboxPermission);

  /**
   * Returns which restrictions on the sandbox are lifted.
   *
   * @return the currently lifted sandbox restrictions
   * @see #isSandboxEnabled()
   * @see #setSandboxPermissions(EnumSet)
   */
  EnumSet<SandboxPermission> getSandboxPermissions();

  /**
   * @return a list of origin URIs from which this field will receive messages posted via <i>postMessage</i>. If this is
   *         {@code null} or empty, messages from all origins are accepted.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/postMessage">window.postMessage (MDN)</a>
   */
  List<String> getTrustedMessageOrigins();

  /**
   * @param trustedMessageOrigins
   *          A list of origin URIs from which this field will receive messages posted via <i>postMessage</i>. If this
   *          is {@code null} or empty, messages from all origins are accepted.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/postMessage">window.postMessage (MDN)</a>
   */
  void setTrustedMessageOrigins(List<String> trustedMessageOrigins);

  /**
   * Configures the browser field general behavior. By default the content of the browser field is shown inline or in an
   * inline container (e.g. an &lt;iframe&gt; for the HTML5 UI layer), some very specific web pages (e.g. using
   * plug-ins, complex frames within the webpage) might not be displayed well or may even lead to a browser crash.
   * <p>
   * This property may be used to disable the inline container usage &lt;iframe&gt; usage. Fallback behavior for the
   * HTML5 UI layer is a separate browser window to show the content. Other UI layers may offer a different fallback,
   * might even decide not to offer a fallback behavior at all (property is just a hint for the UI layer).
   * <p>
   * Property can only be changed during initialization, it can not be changed during runtime.
   *
   * @param showInExternalWindow
   *          <code>false</code> to disable &lt;iframe&gt; usage, <code>true</code> otherwise.
   * @see #isShowInExternalWindow()
   */
  void setShowInExternalWindow(boolean showInExternalWindow);

  /**
   * Returns whether content should be shown inline.
   *
   * @return <code>false</code> to disable &lt;iframe&gt; usage, <code>true</code> otherwise.
   * @see #setShowInExternalWindow(boolean)
   */
  boolean isShowInExternalWindow();

  /**
   * Fallback text (shown in browser field itself), if content is shown in an external window.
   *
   * @see #isShowInExternalWindow()
   * @see #getExternalWindowButtonText()
   */
  String getExternalWindowFieldText();

  /**
   * @see #getExternalWindowFieldText()
   */
  void setExternalWindowFieldText(String externalWindowFieldText);

  /**
   * Fallback text for button (shown in browser field itself) to reopen external window, if content is shown in an
   * external window.
   *
   * @see #isShowInExternalWindow()
   * @see #getExternalWindowFieldText()
   */
  String getExternalWindowButtonText();

  /**
   * @see #getExternalWindowButtonText()
   */
  void setExternalWindowButtonText(String externalWindowButtonText);

  /**
   * Determines if the external window should be auto closed when this field gets removed.
   * <p>
   * NOTE: Auto closing only makes sense, if content is shown in an external window. (@see #isShowInExternalWindow())
   * </p>
   *
   * @return <code>true</code> when the external window will be auto closed, <code>false</code> otherwise.
   * @see #isShowInExternalWindow()
   */
  boolean isAutoCloseExternalWindow();

  /**
   * @see #isAutoCloseExternalWindow()
   */
  void setAutoCloseExternalWindow(boolean autoCloseExternalWindow);

  /**
   * Configures whether the location property should be updated whenever the location of the iframe changes. Default is
   * false.
   * <p>
   * Note: This does only work if the iframe and the iframe's parent document have the same origin (protocol, port and
   * host are the same).
   */
  boolean isTrackLocation();

  /**
   * @see #isTrackLocation()
   */
  void setTrackLocation(boolean trackLocation);
}
