package org.eclipse.scout.rt.client.deeplink;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
