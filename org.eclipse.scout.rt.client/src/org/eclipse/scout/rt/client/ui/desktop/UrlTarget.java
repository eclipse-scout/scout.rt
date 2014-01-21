package org.eclipse.scout.rt.client.ui.desktop;

public enum UrlTarget implements IUrlTarget {
  AUTO, SELF, BLANK;

  @Override
  public String getIdentifier() {
    return name();
  }

  public static IUrlTarget createByIdentifier(String identifier) {
    return valueOf(identifier);
  }
}
