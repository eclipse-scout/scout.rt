package org.eclipse.scout.rt.client.deeplink;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all classes that provide deep-link logic.
 */
public abstract class AbstractDeepLinkHandler implements IDeepLinkHandler {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractDeepLinkHandler.class);

  protected final Pattern m_pattern;

  protected AbstractDeepLinkHandler(Pattern pattern) {
    m_pattern = pattern;
  }

  /**
   * Creates a regexp pattern to validate/parse the value of the deep-link URL parameter.
   */
  protected static Pattern defaultPattern(String handlerName, String dataGroup) {
    return Pattern.compile("^" + handlerName + "-(" + dataGroup + ")$");
  }

  /**
   * This method creates a deep-link path to be used in the URL parameter. The returned value has the format:
   * <code>[handler name]-[handler data]</code>. Example <code>outline-12345</code>.
   *
   * @param handlerData
   * @return
   */
  protected String toDeepLinkPath(String handlerData) {
    return getName() + "-" + handlerData;
  }

  @Override
  public boolean matches(String path) {
    return m_pattern.matcher(path).matches();
  }

  @Override
  public boolean handle(String path) throws DeepLinkException {
    Matcher matcher = m_pattern.matcher(path);
    if (matcher.matches()) {
      LOG.debug("Handling deep-link name={} path={}", getName(), path);
      handleImpl(matcher);
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Implement logic to display the deep-link's corresponding entity. If the handler is not able to process the
   * deep-link URL a {@link DeepLinkException} must be thrown to ensure correct handling in the UI.
   *
   * @param matcher
   * @throws DeepLinkException
   */
  protected abstract void handleImpl(Matcher matcher) throws DeepLinkException;

}
