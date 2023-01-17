/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop;

/**
 * This enum's elements describe the action that should be performed on the UI when handling an "open URI" event.
 */
public enum OpenUriAction implements IOpenUriAction {

  /**
   * The URI represents a downloadable object which must not be handled by the browser's rendering engine. Instead the
   * "Save as..." dialog appears which allows the user to store the resource to his local file system. The application's
   * location does not change, and no browser windows or tabs are opened.
   */
  DOWNLOAD("download"),

  /**
   * The URI represents an object that will be "opened" on the user's system.
   * <ul>
   * <li><b>Documents with external viewers / editors</b> (e.g. <code>*.pdf</code>, <code>*.docx</code>).
   * <li><b>Executables</b> (e.g. <code>setup.exe</code>)
   * <li>URIs with <b>special protocols</b> that are registered in the user's system and and delegated to some "protocol
   * handler". This handler may then perform actions in a third party application (e.g. <i>mailto:xyz@example.com</i>
   * would open the system's mail application).
   * <p>
   * Example regular expression to check for some common special URI protocols:<br>
   * <code>/^(callto|facetime|fax|geo|mailto|maps|notes|sip|skype|tel):/</code>
   * </ul>
   * <p>
   * The application's location does not change.
   */
  OPEN("open"),

  /**
   * The URI represents content that is displayable by the browser's rendering engine. A new window or tab will be
   * opened to show this content. The application's location does not change. Note that this action may be prevented by
   * the browser's popup blocker mechanism.
   */
  NEW_WINDOW("newWindow"),

  /**
   * The URI represents content that is displayable by the browser's rendering engine. Always a new non-modal popup
   * window will be opened to show this content. Unlike {@link OpenUriAction#NEW_WINDOW} the newly opened window is
   * limited, i.e. it does not contain the location, the toolbar and the menubar.<br/>
   * The application's location does not change. Note that this action may be prevented by the browser's popup blocker
   * mechanism.
   */
  POPUP_WINDOW("popupWindow"),

  /**
   * The URI represents another web page or application that will replace the running application. The current window's
   * location will be changed to the URI, causing the application to be unloaded. The associated session will not be
   * destroyed immediately, but housekeeping will shut it down eventually, due to it's imminent inactivity.
   */
  SAME_WINDOW("sameWindow");

  private final String m_identifier;

  OpenUriAction(String identifier) {
    m_identifier = identifier;
  }

  @Override
  public String getIdentifier() {
    return m_identifier;
  }
}
