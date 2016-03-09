package org.eclipse.scout.rt.client.deeplink;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Base class for all classes that provide deep-link logic.
 */
public abstract class AbstractDeepLinkHandler implements IDeepLinkHandler {

  private final Pattern m_pattern;

  protected AbstractDeepLinkHandler(Pattern pattern) {
    m_pattern = pattern;
  }

  protected static Pattern defaultPattern(String handlerName) {
    return Pattern.compile("^" + handlerName + "/(\\d+)/(.*)$");
  }

  // FIXME awe: (deep-links) impl. pretty URL encoding -> rename to "slug", see:
  // http://stackoverflow.com/questions/1657193/java-code-library-for-generating-slugs-for-use-in-pretty-urls
  protected static String toSlug(String title) {
    title = StringUtility.substring(title, 0, 50);
    title = title.replaceAll("\\s+", "_");
    try {
      return URLEncoder.encode(title, StandardCharsets.UTF_8.name());
    }
    catch (UnsupportedEncodingException e) {
      throw new ProcessingException("Failed to encode URL", e);
    }
  }

  /**
   * @return The path prefix. Example: "/view/".
   */
  protected String getPathPrefix() {
    return "/" + DeepLinks.DEEP_LINK_PREFIX + "/";
  }

  /**
   * @return The prefix required to generate an absolute URL. Example: "http://localhost:8082/view/".
   */
  protected String getUrlPrefix() {
    // FIXME awe: (deep-links) must set that on DeepLinks when wep-app starts up
    String webAppContext = "http://localhost:8082";
    return webAppContext + getPathPrefix();
  }

  @Override
  public boolean matches(String path) {
    return m_pattern.matcher(path).matches();
  }

  @Override
  public boolean handle(String path) throws DeepLinkException {
    Matcher matcher = m_pattern.matcher(path);
    if (matcher.matches()) {
      handleImpl(matcher);
      return true;
    }
    else {
      return false;
    }
  }

  protected abstract void handleImpl(Matcher matcher) throws DeepLinkException;

}
