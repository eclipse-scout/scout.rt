package org.eclipse.scout.rt.shared.deeplink;

public final class DeepLinkUrlParameter {

  /**
   * Name of the URL parameter which contains the deep-link path in the format
   * <code>[handler name]-[handler data]</code>.
   */
  public final static String DEEP_LINK = "dl";

  /**
   * Name of the optional URL parameter which contains a human readable, informative text about the deep-link.
   */
  public final static String INFO = "i";

  /**
   * Name of the parameter used to enable/disable deep link handling
   */
  public static final String HANDLE_DEEP_LINK = "handleDeepLink";

  private DeepLinkUrlParameter() {
  }

}
