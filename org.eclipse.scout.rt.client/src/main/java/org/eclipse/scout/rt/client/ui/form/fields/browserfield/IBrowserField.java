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

import java.util.Collections;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.resource.BinaryResource;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;

/**
 * This model represents a UI specific browser, in swing it is a JEditorPane html viewer/editor.
 * This may be changed by adding a fragment that uses the swt Browser in swing
 * (org.eclipse.scout.rt.ui.swing.browser.swt.fragment)
 * <p>
 * The content is either the value (remote file) or the {@link #setExternalURL()}
 * <p>
 * The content of the website is contained in the remote file that is the value of this field. The remote file may be a
 * html file or a zip file containing a html file (with same name) and additional resources such as images, styles etc.
 * <p>
 * Uses {@link RemoteFile#writeZipContentToDirectory(java.io.File)} to unpack zipped content for viewing
 * <p>
 * You can use local urls that call back to the field itself and can be handled by overriding
 * {@link AbstractBrowserField#execLocationChanged(String, String, boolean)} A local URL is one of the form
 * http://local/...
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

    public static Set<SandboxPermissions> none() {
      return Collections.emptySet();
    }

    public static Set<SandboxPermissions> all() {
      return CollectionUtility.hashSet(values());
    }

    /**
     * @return the value to use in the HTML <code>sandbox</code> attribute
     */
    public String getAttribute() {
      return m_attribute;
    }
  }

  // TODO BSH Update javaDoc
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

  void setSandboxPermissions(Set<SandboxPermissions> sandboxPermissions);

  Set<SandboxPermissions> getSandboxPermissions();
}
