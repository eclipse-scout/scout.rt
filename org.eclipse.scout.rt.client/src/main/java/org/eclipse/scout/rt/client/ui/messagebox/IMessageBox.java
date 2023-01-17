/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.messagebox;

import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.html.IHtmlContent;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;

/**
 * Interface for message box.<br/>
 * Use {@link MessageBoxes} to create a message box.
 */
public interface IMessageBox extends IWidget {

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
  IDisplayParent getDisplayParent();

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
  IMessageBox withDisplayParent(IDisplayParent displayParent);

  /*
   * Model observer
   */
  IFastListenerList<MessageBoxListener> messageBoxListeners();

  default void addMessageBoxListener(MessageBoxListener listener) {
    messageBoxListeners().add(listener);
  }

  default void removeMessageBoxListener(MessageBoxListener listener) {
    messageBoxListeners().remove(listener);
  }

  /*
   * ui messaging
   */
  IMessageBoxUIFacade getUIFacade();

  String getIconId();

  IMessageBox withIconId(String iconId);

  /**
   * @see #withSeverity(int)
   */
  int getSeverity();

  /**
   * Sets the severity.
   *
   * @param severity
   *          One of the {@link IStatus} constants.
   */
  IMessageBox withSeverity(int severity);

  /**
   * @see #withHeader(String)
   */
  String getHeader();

  /**
   * Sets the header.
   * <p>
   * The header is by default represented in bold font as first entry in the message box.
   */
  IMessageBox withHeader(String header);

  /**
   * @see #withBody(String)
   */
  String getBody();

  /**
   * Sets the body.
   * <p>
   * The body is by default represented in normal font as second entry after the header (if available).
   */
  IMessageBox withBody(String body);

  /**
   * @see #withHtml(IHtmlContent)
   */
  IHtmlContent getHtml();

  /**
   * Sets the html.
   * <p>
   * The html allows to use custom html and is positioned as third entry after the body (if available).
   */
  IMessageBox withHtml(IHtmlContent html);

  /**
   * @see #withHiddenText(String)
   */
  String getHiddenText();

  /**
   * Sets the hidden text.
   * <p>
   * The hidden text is used for the default copy paste text, thus not visible in the UI directly.<br/>
   * Examples usages are stack-traces or more detailed information that should not be directly presented to the user.
   */
  IMessageBox withHiddenText(String hiddenText);

  /**
   * @see #withYesButtonText(String)
   */
  String getYesButtonText();

  /**
   * Sets the text for the yes / ok button.
   */
  IMessageBox withYesButtonText(String yesButtonText);

  /**
   * @see #withNoButtonText(String)
   */
  String getNoButtonText();

  /**
   * Sets the text for the no button.
   */
  IMessageBox withNoButtonText(String noButtonText);

  /**
   * @see #withCancelButtonText(String)
   */
  String getCancelButtonText();

  /**
   * Sets the text for the cancel button.
   */
  IMessageBox withCancelButtonText(String cancelButtonText);

  /**
   * @see #withAutoCloseMillis(long)
   */
  long getAutoCloseMillis();

  /**
   * To close the message box automatically after the specified period of time. By default, the result
   * {@link #CANCEL_OPTION} is returned after being closed. This can be changed by using {@link #show(int)} to construct
   * the message box.
   *
   * @param autoCloseMillis
   *          timeout [ms]
   */
  IMessageBox withAutoCloseMillis(long autoCloseMillis);

  /**
   * @see #withCopyPasteText(String)
   */
  String getCopyPasteText();

  /**
   * Sets the copy paste text.
   * <p>
   * The text is used if the user applies the copy shortcut on the message box.
   * <p>
   * If not explicitly set, the default copy paste text is used, which is a combination of header, body, html (plain
   * text) and hidden text (separated by newline).
   */
  IMessageBox withCopyPasteText(String copyPasteText);

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

  /**
   * Closes the message box and resolves the blocking call. The close result is set according to the available buttons
   * on the message box with the following priority: {@link #YES_OPTION}, {@link #NO_OPTION}, {@link #CANCEL_OPTION}.
   */
  void doOk();

  /**
   * Closes the message box and resolves the blocking call. The close result is set according to the available buttons
   * on the message box with the following priority: {@link #CANCEL_OPTION}, {@link #NO_OPTION}, {@link #YES_OPTION}.
   */
  void doClose();
}
