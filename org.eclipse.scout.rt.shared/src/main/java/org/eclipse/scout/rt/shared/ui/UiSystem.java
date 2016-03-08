package org.eclipse.scout.rt.shared.ui;

public enum UiSystem implements IUiSystem {
  WINDOWS,
  UNIX,
  OSX,
  IOS,
  ANDROID,
  UNKNOWN;

  @Override
  public String getIdentifier() {
    return name();
  }

  public static UiSystem createByIdentifier(String identifier) {
    return valueOf(identifier);
  }

}
