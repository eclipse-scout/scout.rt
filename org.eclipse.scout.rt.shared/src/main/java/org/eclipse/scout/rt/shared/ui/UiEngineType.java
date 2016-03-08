package org.eclipse.scout.rt.shared.ui;

/**
 * @since 6.0
 */
public enum UiEngineType implements IUiEngineType {
  ANDROID,
  CHROME,
  SAFARI,
  FIREFOX,
  IE,
  OPERA,
  KONQUEROR,
  UNKNOWN;

  @Override
  public String getIdentifier() {
    return name();
  }

  public static UiEngineType createByIdentifier(String identifier) {
    return valueOf(identifier);
  }

}
