package org.eclipse.scout.rt.shared.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * <h3>{@link DefaultUserAgentParser}</h3>
 */
public class DefaultUserAgentParserTest {
  private UserAgent m_defaultAgent;

  @Before
  public void before() {
    m_defaultAgent = UserAgents.createDefault();
  }

  @Test
  public void testInvalidId() {
    UserAgent parsedAgent = UserAgents.createByIdentifier(new DefaultUserAgentParser(), "XY|DESKTOP|UNKNOWN|Windows7");
    assertEquals(m_defaultAgent, parsedAgent);
  }

  @Test()
  public void testEmptyId() {
    UserAgent parsedAgent = UserAgents.createByIdentifier(new DefaultUserAgentParser(), "");
    assertEquals(m_defaultAgent, parsedAgent);
  }

  @Test
  public void testDefaultAgent() {
    DefaultUserAgentParser parser = new DefaultUserAgentParser();
    String id = m_defaultAgent.createIdentifier(parser);
    UserAgent parsedAgent = UserAgents.createByIdentifier(parser, id);
    assertEquals(m_defaultAgent, parsedAgent);
  }

  @Test
  public void testParsingCustomAgent() {
    UserAgent testUserAgent = UserAgents
        .create()
        .withUiLayer(UiLayer.HTML)
        .withUiDeviceType(UiDeviceType.MOBILE)
        .withUiEngineType(UiEngineType.ANDROID)
        .withUiSystem(UiSystem.ANDROID)
        .withDeviceId("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30")
        .build();
    DefaultUserAgentParser parser = new DefaultUserAgentParser();
    String id = testUserAgent.createIdentifier(parser);

    UserAgent parsedAgent = UserAgents.createByIdentifier(parser, id);
    assertEquals(testUserAgent, parsedAgent);
  }

  @Test
  public void testParseInvalidDeviceId() {
    UserAgent testUserAgent = UserAgents
        .create()
        .withUiLayer(UiLayer.HTML)
        .withUiDeviceType(UiDeviceType.MOBILE)
        .withUiEngineType(UiEngineType.ANDROID)
        .withUiSystem(UiSystem.ANDROID)
        .withDeviceId("xxx|yyy")
        .build();
    DefaultUserAgentParser parser = new DefaultUserAgentParser();
    String id = testUserAgent.createIdentifier(parser);

    UserAgent parsedAgent = UserAgents.createByIdentifier(parser, id);
    assertEquals(testUserAgent.getUiDeviceType(), parsedAgent.getUiDeviceType());
    assertEquals(testUserAgent.getUiLayer(), parsedAgent.getUiLayer());

  }

}
