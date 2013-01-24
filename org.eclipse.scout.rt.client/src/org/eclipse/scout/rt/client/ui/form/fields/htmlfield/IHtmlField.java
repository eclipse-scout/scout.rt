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

import java.net.URL;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField;
import org.eclipse.scout.rt.client.ui.form.fields.documentfield.IDocumentField;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;

/**
 * This model represents a UI specific browser, in swing it is a JEditorPane html viewer/editor, in swt it is the native
 * browser.
 * <p>
 * You can use local urls that call back to the field itself and can be handled by overriding
 * {@link AbstractHtmlField#execHyperlinkAction(URL, String, boolean)} A local URL is one of the form http://local/...
 * <p>
 * See also the {@link IBrowserField} for html viewing and {@link IDocumentField} for html editing (requires a fragment
 * such as microsoft word editor)
 */
public interface IHtmlField extends IValueField<String> {

  String PROP_MAX_LENGTH = "maxLength";
  String PROP_SCROLLBARS_ENABLED = "scrollBarsEnabled";
  String PROP_INSERT_IMAGE = "insertImage";
  String PROP_SCROLLBAR_SCROLL_TO_END = "scrollToEnd";
  String PROP_SCROLLBAR_SCROLL_TO_ANCHOR = "scrollToPosition";

  void setMaxLength(int len);

  void setScrollToAnchor(String anchorName);

  String getScrollToAnchor();

  int getMaxLength();

  boolean isHtmlEditor();

  boolean isScrollBarEnabled();

  void scrollToEnd();

  /*
   * Runtime
   */

  IHtmlFieldUIFacade getUIFacade();

  void doHyperlinkAction(URL url) throws ProcessingException;

  /**
   * local images and local resources bound to the html text
   */
  RemoteFile[] getAttachments();

  void setAttachments(RemoteFile[] attachments);

  String getPlainText();

  /** Insert a new image at the caret position. */
  void insertImage(String imageUrl);

  /**
   * Returns whether this field is spell checkable.
   */
  boolean isSpellCheckEnabled();

  /**
   * Returns whether this field should be monitored for spelling errors in the
   * background ("check as you type"). If it is not defined, null is returned,
   * then the application default is used.
   */
  Boolean isSpellCheckAsYouTypeEnabled();

}
