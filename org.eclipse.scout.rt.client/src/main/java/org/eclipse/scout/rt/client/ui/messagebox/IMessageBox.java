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
package org.eclipse.scout.rt.client.ui.messagebox;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.html.IHtmlContent;
import org.eclipse.scout.commons.status.IStatus;
import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * Interface for message box.<br/>
 * Use {@link MessageBoxes} to create a message box.
 */
public interface IMessageBox extends IPropertyObserver {

  /**
   * Result status YES
   */
  int YES_OPTION = 0;
  /**
   * Result status NO
   */
  int NO_OPTION = 1;
  /**
   * Result status CANCEL
   */
  int CANCEL_OPTION = 2;

  /**
   * @return the {@link IDisplayParent} to attach this {@link IMessageBox} to; is never <code>null</code>.
   */
  IDisplayParent displayParent();

  /**
   * Sets the display parent to attach this {@link IMessageBox} to.
   * <p>
   * A display parent is the anchor to attach this {@link IMessageBox} to, and affects its accessibility and modality
   * scope. Possible parents are {@link IDesktop}, {@link IOutline}, or {@link IForm}:
   * <ul>
   * <li>Desktop: {@link IMessageBox} is always accessible; blocks the entire desktop;</li>
   * <li>Outline: {@link IMessageBox} is only accessible when the given outline is active; only blocks the outline;</li>
   * <li>Form: {@link IMessageBox} is only accessible when the given Form is active; only blocks the Form;</li>
   * </ul>
   *
   * @param displayParent
   *          like {@link IDesktop}, {@link IOutline}, {@link IForm}, or <code>null</code> to derive the
   *          {@link IDisplayParent} from the current calling context.
   */
  IMessageBox displayParent(IDisplayParent displayParent);

  /*
   * Model observer
   */
  void addMessageBoxListener(MessageBoxListener listener);

  void removeMessageBoxListener(MessageBoxListener listener);

  /*
   * ui messaging
   */
  IMessageBoxUIFacade getUIFacade();

  String iconId();

  IMessageBox iconId(String iconId);

  /**
   * @see {@link #severity(int)}
   */
  int severity();

  /**
   * Sets the severity.
   *
   * @param severity
   *          One of the {@link IStatus} constants.
   * @return
   */
  IMessageBox severity(int severity);

  /**
   * @see {@link #header(String)}
   */
  String header();

  /**
   * Sets the header.
   * <p>
   * The header is by default represented in bold font as first entry in the message box.
   */
  IMessageBox header(String header);

  /**
   * @see {@link #body(String)}
   */
  String body();

  /**
   * Sets the body.
   * <p>
   * The body is by default represented in normal font as second entry after the header (if available).
   */
  IMessageBox body(String body);

  /**
   * @see {@link #html(IHtmlContent)}
   */
  IHtmlContent html();

  /**
   * Sets the html.
   * <p>
   * The html allows to use custom html and is positioned as third entry after the body (if available).
   */
  IMessageBox html(IHtmlContent html);

  /**
   * @see {@link #hiddenText(String)}
   */
  String hiddenText();

  /**
   * Sets the hidden text.
   * <p>
   * The hidden text is used for the default copy paste text, thus not visible in the UI directly.<br/>
   * Examples usages are stacktraces or more detailed information that should not be directly presented to the user.
   */
  IMessageBox hiddenText(String hiddenText);

  /**
   * @see {@link #yesButtonText(String)}
   */
  String yesButtonText();

  /**
   * Sets the text for the yes / ok button.
   */
  IMessageBox yesButtonText(String yesButtonText);

  /**
   * @see {@link #noButtonText(String)}
   */
  String noButtonText();

  /**
   * Sets the text for the no button.
   */
  IMessageBox noButtonText(String noButtonText);

  /**
   * @see {@link #cancelButtonText(String)}
   */
  String cancelButtonText();

  /**
   * Sets the text for the cancel button.
   */
  IMessageBox cancelButtonText(String cancelButtonText);

  /**
   * @see {@link #autoCloseMillis(long)}
   */
  long autoCloseMillis();

  /**
   * To close the message box automatically after the specified period of time. By default, the result
   * {@link #CANCEL_OPTION} is returned after being closed. This can be changed by using {@link #show(int)} to
   * construct the message box.
   *
   * @param autoCloseMillis
   *          timeout [ms]
   */
  IMessageBox autoCloseMillis(long autoCloseMillis);

  /**
   * @see {@link #copyPasteText(String)}
   */
  String copyPasteText();

  /**
   * Sets the copy paste text.
   * <p>
   * The text is used if the user applies the copy shortcut on the message box.
   * <p>
   * If not explicitly set, the default copy paste text is used, which is a combination of header, body, html (plain
   * text) and hidden text (separated by newline).
   */
  IMessageBox copyPasteText(String copyPasteText);

  /**
   * To query whether the message box is open or closed.
   *
   * @return <code>true</code> if the message box is open, <code>false</code> if closed.
   */
  boolean isOpen();

  /**
   * Opens a message box. This call blocks until the message box is closed.
   *
   * @return The close result ({@link #YES_OPTION}, {@link #NO_OPTION}, {@link #CANCEL_OPTION}).
   */
  int show();

  /**
   * Opens a message box. This call blocks until the message box is closed.
   *
   * @param defaultResult
   *          default result to return if not closed by the user (e.g. by auto-close timer).
   * @return The close result ({@link #YES_OPTION}, {@link #NO_OPTION}, {@link #CANCEL_OPTION}).
   */
  int show(int defaultResult);
}
