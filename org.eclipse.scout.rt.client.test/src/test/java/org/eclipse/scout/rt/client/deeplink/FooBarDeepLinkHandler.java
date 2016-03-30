package org.eclipse.scout.rt.client.deeplink;

import java.util.regex.Matcher;

public class FooBarDeepLinkHandler extends AbstractDeepLinkHandler {

  public static final String HANDLER_NAME = "foobar";

  private String m_lastMatch;

  protected FooBarDeepLinkHandler() {
    super(defaultPattern(HANDLER_NAME, "[0-9]+"));
  }

  @Override
  public String getName() {
    return HANDLER_NAME;
  }

  @Override
  protected void handleImpl(Matcher matcher) throws DeepLinkException {
    m_lastMatch = matcher.group(1);
    if ("321".equals(m_lastMatch)) {
      throw new DeepLinkException();
    }
  }

  public String getLastMatch() {
    return m_lastMatch;
  }

}
