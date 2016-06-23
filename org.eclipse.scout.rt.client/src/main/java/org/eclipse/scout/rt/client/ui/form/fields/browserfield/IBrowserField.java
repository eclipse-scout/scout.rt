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

import java.util.EnumSet;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

/**
 * This model represents a separate "website" inside the application.
 * <p>
 * The content is either a URL (location, 3rd party website or webapp) or a {@link BinaryResource} with attachments
 * (e.g. static HTML content with images, styles etc.).
 */
public interface IBrowserField extends IFormField {

  public enum SandboxPermission {
    AllowForms("allow-forms"),
    AllowPointerLock("allow-pointer-lock"),
    AllowPopups("allow-popups"),
    AllowPopupsToEscapeSandbox("allow-popups-to-escape-sandbox"),
    AllowSameOrigin("allow-same-origin"),
    AllowScripts("allow-scripts"),
    AllowTopNavigation("allow-top-navigation");

    private String m_attribute;

    private SandboxPermission(String attribute) {
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

  IBrowserFieldUIFacade getUIFacade();

  void addBrowserFieldListener(BrowserFieldListener listener);

  void removeBrowserFieldListener(BrowserFieldListener listener);

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
}
