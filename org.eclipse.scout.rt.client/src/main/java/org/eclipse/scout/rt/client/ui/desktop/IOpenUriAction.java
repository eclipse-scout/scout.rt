package org.eclipse.scout.rt.client.ui.desktop;

/**
 * Describes the action that should be used by the UI to handle the URI in the desktop's "open URI" feature.
 */
public interface IOpenUriAction {

  /**
   * @return the identifier (known to the UI) for this action.
   */
  String getIdentifier();
}
