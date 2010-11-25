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
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;

/**
 * Example HTML content: <xmp> <html> <head> <style type="text/css"> <!-- body {
 * background-color: white; color: #ff0000; font-size: 10pt; vertical-align:
 * top; font-family: Arial; text-align: left } a { color: #999999;
 * text-decoration: none } .textsmal { background-color: #ffffff; padding-right:
 * 5px; color: #999999; padding-top: 0; font-size: 10pt; padding-bottom: 0;
 * padding-left: 10px; font-family: Verdana, Arial, Helvetica, sans-serif }
 * .dreipx { font-size: 3pt } .news { line-height: 18px; color: #b4b4b4;
 * font-size: 22pt; font-family: Arial, Helvetica, sans-serif; font-weight: bold
 * } .text2 { padding-right: 5px; color: #999999; padding-top: 0; font-size:
 * 12pt; padding-bottom: 0; padding-left: 10px; font-family: Verdana, Arial,
 * Helvetica, sans-serif; font-weight: bold } .titel { background-color:
 * #999999; padding-right: 5px; color: #ffffff; padding-top: 0; padding-bottom:
 * 0; padding-left: 5px; font-family: Arial, Helvetica, sans-serif } .text {
 * background-color: #ffffff; padding-right: 10px; color: #000000; padding-top:
 * 0; font-size: 12pt; padding-bottom: 0; padding-left: 10px; font-family:
 * Verdana, Arial, Helvetica, sans-serif } .editLink { color: #000000;
 * text-decoration: underline } --> </style> </head> <body> Guten Tag Herr X,
 * Ihr Auftrag wird bearbeitet. </body> </html> </xmp>
 */
public interface IHtmlField extends IValueField<String> {

  String PROP_MAX_LENGTH = "maxLength";
  String PROP_SCROLLBARS_ENABLED = "scrollBarsEnabled";
  String PROP_INSERT_IMAGE = "insertImage";

  void setMaxLength(int len);

  int getMaxLength();

  boolean isHtmlEditor();

  boolean isScrollBarEnabled();

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
