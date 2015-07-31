package org.eclipse.scout.rt.client.ui.desktop;

/**
 * Enum with hints that may alter the behavior of the desktop's "open URI" feature on the UI.
 * <p>
 * By default, the UI handles all URIs using an "auto detection" algorithm. Hey should be opened in the browser, without
 * replacing the current application. Normally, a new browser window or tab is opened and the URI is set as location for
 * that window. The UI may chose to handle some URIs differently, depending on various factors such as the operating
 * system. Examples for special behavior URIs: "mailto:...", "tel:..."
 */
public enum OpenUriHint implements IOpenUriHint {

  /**
   * The URI represents a downloadable object which should not be handled by the browser's rendering engine.
   * Instead the "Save as..." dialog should appear which allows the user to store the resource to his
   * local file system. The application's location does not change, and no browser windows or tabs should
   * be opened.
   */
  DOWNLOAD("download"),
  /**
   * The URI should be opened in an external application. This is similar to {@link #DOWNLOAD} but should launch
   * an external application to handle the URI. The handler is registered with the browser, usually using
   * a special URI protocol (such as "tel:...", "mailto:..." etc.)
   */
  OPEN_APPLICATION("open-application"),
  /**
   * The URI represents content that is displayable by the browser's rendering engine. A new window or tab should be
   * opened to show this content. Note that this may be prevented by a popup blocker.
   */
  NEW_WINDOW("new-window");

  private final String m_identifier;

  private OpenUriHint(String identifier) {
    m_identifier = identifier;
  }

  @Override
  public String getIdentifier() {
    return m_identifier;
  }
}
