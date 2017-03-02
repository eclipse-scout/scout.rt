package org.eclipse.scout.rt.shared.ui;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.ConfigUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory methods for {@link UserAgent}
 *
 * @see UserAgent
 * @since 6.0.0
 */
public final class UserAgents {
  private static final Logger LOG = LoggerFactory.getLogger(UserAgents.class);

  private IUiLayer m_uiLayer = UiLayer.UNKNOWN;
  private IUiDeviceType m_uiDeviceType = UiDeviceType.UNKNOWN;
  private IUiEngineType m_uiEngineType = UiEngineType.UNKNOWN;
  private IUiSystem m_uiSystem = UiSystem.UNKNOWN;
  private String m_uiDeviceId = "n/a";
  private boolean m_touch = false;
  private boolean m_standalone = false;

  private UserAgents() {
  }

  public UserAgents withUiDeviceType(IUiDeviceType uiDeviceType) {
    m_uiDeviceType = uiDeviceType;
    return this;
  }

  public UserAgents withUiEngineType(IUiEngineType uiEngineType) {
    m_uiEngineType = uiEngineType;
    return this;
  }

  public UserAgents withUiSystem(IUiSystem uiSystem) {
    m_uiSystem = uiSystem;
    return this;
  }

  public UserAgents withUiLayer(IUiLayer uiLayer) {
    m_uiLayer = uiLayer;
    return this;
  }

  public UserAgents withDeviceId(String uiDeviceId) {
    m_uiDeviceId = uiDeviceId;
    return this;
  }

  public UserAgents withDefaultDeviceId() {
    return withDeviceId(ConfigUtility.getProperty("os.name"));
  }

  public UserAgents withTouch(boolean touch) {
    m_touch = touch;
    return this;
  }

  public UserAgents withStandalone(boolean standalone) {
    m_standalone = standalone;
    return this;
  }

  public static UserAgents create() {
    return new UserAgents();
  }

  public static UserAgents create(UserAgent other) {
    return new UserAgents()
        .withUiDeviceType(other.getUiDeviceType())
        .withUiEngineType(other.getUiEngineType())
        .withUiLayer(other.getUiLayer())
        .withUiSystem(other.getUiSystem())
        .withDeviceId(other.getUiDeviceId())
        .withTouch(other.isTouch())
        .withStandalone(other.isStandalone());
  }

  public UserAgent build() {
    return new UserAgent(m_uiLayer, m_uiDeviceType, m_uiEngineType, m_uiSystem, m_uiDeviceId, m_touch, m_standalone);
  }

  public static UserAgent createDefault() {
    return create()
        .withDefaultDeviceId()
        .build();
  }

  public static UserAgent createByIdentifier(IUserAgentParser parser, String userAgent) {
    try {
      return parser.parseIdentifier(userAgent);
    }
    catch (RuntimeException e) {
      LOG.error("UserAgentIdentifier could not be parsed. Exception occured while parsing. UserAgent: {}", userAgent, e);
    }
    return createDefault();
  }

  public static UserAgent createByIdentifier(String userAgent) {
    return createByIdentifier(BEANS.get(IUserAgentParser.class), userAgent);
  }

}
