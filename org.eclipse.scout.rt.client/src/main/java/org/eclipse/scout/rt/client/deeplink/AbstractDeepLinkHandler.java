package org.eclipse.scout.rt.client.deeplink;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  /**
   * Creates a regexp pattern to validate/parse the value of the deep-link URL parameter.
   */
  protected static Pattern defaultPattern(String handlerName, String dataGroup) {
    return Pattern.compile("^" + handlerName + "-(" + dataGroup + ")$");
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
   * This method creates a value to be used in the deep-link URL parameter. The returned value has the format:
   * <code>[handler name]-[data]</code>.
   *
   * @param handlerData
   * @return
   */
  protected String toParameter(String handlerData) {
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
      handleImpl(matcher);
      return true;
    }
    else {
      return false;
    }
  }

  protected abstract void handleImpl(Matcher matcher) throws DeepLinkException;

}
