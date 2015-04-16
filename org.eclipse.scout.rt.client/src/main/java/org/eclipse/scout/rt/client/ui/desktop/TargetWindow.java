package org.eclipse.scout.rt.client.ui.desktop;

/**
 * Defines the target window for a link or resource.
 */
public enum TargetWindow implements ITargetWindow {

  /**
   * The client UI decides where to open a given resource.
   */
  AUTO,
  /**
   * Resource is opened in the same window.
   */
  SELF,
  /**
   * Resource is opened in a new window.
   */
  BLANK;

  @Override
  public String getIdentifier() {
    return name();
  }

  public static ITargetWindow createByIdentifier(String identifier) {
    return valueOf(identifier);
  }

}
