package org.eclipse.scout.rt.shared.deeplink;

public final class DeepLinkUrlParameter {

  /**
   * Name of the URL parameter which contains the deep-link path in the format
   * <code>[handler name]-[handler data]</code>.
   */
  public static final String DEEP_LINK = "dl";

  /**
   * Name of the optional URL parameter which contains a human readable, informative text about the deep-link.
   */
  public static final String INFO = "i";

  private DeepLinkUrlParameter() {
  }

}
