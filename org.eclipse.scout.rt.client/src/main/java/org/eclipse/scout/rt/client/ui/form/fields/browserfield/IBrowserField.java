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

import java.util.EnumSet;
import java.util.Set;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.resource.BinaryResource;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * This model represents a separate "website" inside the application.
 * <p>
 * The content is either a URL (location, 3rd party website or webapp) or a {@link BinaryResource} with attachments
 * (e.g. static HTML content with images, styles etc.).
 */
public interface IBrowserField extends IFormField {

  public enum SandboxPermissions {
    AllowForms("allow-forms"),
    AllowPointerLock("allow-pointer-lock"),
    AllowPopups("allow-popups"),
    AllowSameOrigin("allow-same-origin"),
    AllowScripts("allow-scripts"),
    AllowTopNavigation("allow-top-navigation");

    private String m_attribute;

    private SandboxPermissions(String attribute) {
      m_attribute = attribute;
    }

    public static EnumSet<SandboxPermissions> none() {
      return EnumSet.noneOf(SandboxPermissions.class);
    }

    public static Set<SandboxPermissions> all() {
      return EnumSet.allOf(SandboxPermissions.class);
    }

    /**
     * @return the value to use in the HTML <code>sandbox</code> attribute
     */
    public String getAttribute() {
      return m_attribute;
    }
  }

  // TODO BSH doLocationChange()

  String PROP_LOCATION = "location";
  String PROP_BINARY_RESOURCE = "binaryResource";
  String PROP_ATTACHMENTS = "attachments";
  // FIXME AWE: fix name-inconsistency, constant-name VS property-name VS method-name SCROLLBARS, scrollBars, scrollBar
  String PROP_SCROLLBARS_ENABLED = "scrollBarsEnabled";
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
   * Stores the given resource in the field, generates a dynamic resource URL for it
   * and sets the location (value) to that URL.
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
   * Adds additional resources that may be referenced by the main resource ({@link #getBinaryResource()}).
   * To reference an attachment, set the "filename" attribute correspondingly. The attachment's URL
   * shares the same base path, with a different filename at the end.
   */
  void setAttachments(Set<BinaryResource> attachments);

  /**
   * @see #setAttachments(Set)
   */
  Set<BinaryResource> getAttachments();

  boolean isScrollBarEnabled();

  /**
   * emulate a location change in order to handle it in the model
   */
  void doLocationChange(String location) throws ProcessingException;

  void setSandboxEnabled(boolean sandboxEnabled);

  boolean isSandboxEnabled();

  void setSandboxPermissions(EnumSet<SandboxPermissions> sandboxPermissions);

  EnumSet<SandboxPermissions> getSandboxPermissions();
}
