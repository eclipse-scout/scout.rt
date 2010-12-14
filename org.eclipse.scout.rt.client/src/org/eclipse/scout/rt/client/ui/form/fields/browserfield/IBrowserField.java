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

import java.net.URL;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;

/**
 * This model represents a UI specific browser, in swing it is a JEditorPane html viewer/editor, in swt it is the native
 * browser.
 * This may be changed by adding a fragment that uses the swt Browser in swing
 * (org.eclipse.scout.rt.ui.swing.browser.swt.fragment)
 * <p>
 * The content is either the value (remote file) or the {@link #setExternalURL()}
 * <p>
 * The content of the website is contained in the remote file that is the value of this field. The remote file may be a
 * html file or a zip file containing a html file (with same name) and additional resources such as images, styles etc.
 * <p>
 * Uses {@link RemoteFile#writeZipContentToDirectory(java.io.File)} to unpack zipped content for viewing
 */
public interface IBrowserField extends IValueField<RemoteFile> {

  String PROP_SCROLLBARS_ENABLED = "scrollBarsEnabled";
  String PROP_EXTERNAL_URL = "externalUrl";

  boolean isScrollBarEnabled();

  IBrowserFieldUIFacade getUIFacade();

  void doHyperlinkAction(URL url) throws ProcessingException;

  /**
   * instead of using direct content, show an external url
   */
  void setExternalURL(URL url);

  URL getExternalURL();

}
