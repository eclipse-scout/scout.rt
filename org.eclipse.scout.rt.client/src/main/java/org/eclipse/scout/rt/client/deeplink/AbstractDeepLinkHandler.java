package org.eclipse.scout.rt.client.deeplink;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.BEANS;

/**
 * Base class for all classes that provide deep-link logic.
 */
public abstract class AbstractDeepLinkHandler implements IDeepLinkHandler {

  private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
  private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

  private final Pattern m_pattern;

  protected AbstractDeepLinkHandler(Pattern pattern) {
    m_pattern = pattern;
  }

  protected static Pattern defaultPattern(String handlerName) {
    return Pattern.compile("^" + handlerName + "/(\\d+)/(.*)$");
  }

  /**
   * Slug implementation as proposed from Stackoverflow.
   *
   * @see http://stackoverflow.com/questions/1657193/java-code-library-for-generating-slugs-for-use-in-pretty-urls
   */
  protected static String toSlug(String input) {
    String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
    String normalized = Normalizer.normalize(nowhitespace, Form.NFD);
    String slug = NONLATIN.matcher(normalized).replaceAll("");
    return slug.toLowerCase(Locale.ENGLISH);
  }

  /**
   * @return The prefix required to generate an absolute URL (including trailing slash). Example:
   *         "http://scou.eclipse.org:8080/widgets/view/".
   */
  protected String getUrlPrefix() {
    String webRoot = BEANS.get(IDeepLinks.class).getWebRoot();
    return webRoot + "/" + DeepLinks.DEEP_LINK_PREFIX + "/";
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
