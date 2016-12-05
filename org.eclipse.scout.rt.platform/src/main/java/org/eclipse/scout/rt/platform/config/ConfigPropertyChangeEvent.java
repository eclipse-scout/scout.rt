package org.eclipse.scout.rt.platform.config;

/**
 * <h3>{@link ConfigPropertyChangeEvent}</h3><br>
 * Describes the change of a {@link IConfigProperty}.
 */
public class ConfigPropertyChangeEvent {

  /**
   * Event describing a manual change of the property by using {@link IConfigProperty#setValue(Object)};
   */
  public static final int TYPE_VALUE_CHANGED = 1;

  /**
   * Event describing that the property value was invalidated. After the next access to the property value an event of
   * type {@link #TYPE_VALUE_INITIALIZED} will be fired.
   */
  public static final int TYPE_INVALIDATE = 2;

  /**
   * Event describing that the property initialized its value because it was accessed and has not yet initialized its
   * value or has been invalidated before ({@link IConfigProperty#invalidate()}).
   */
  public static final int TYPE_VALUE_INITIALIZED = 3;

  private final IConfigProperty<?> m_configProperty;
  private final Object m_oldValue;
  private final Object m_newValue;
  private final int m_type;

  public ConfigPropertyChangeEvent(IConfigProperty<?> configProperty, Object oldValue, Object newValue, int type) {
    m_configProperty = configProperty;
    m_oldValue = oldValue;
    m_newValue = newValue;
    m_type = type;
  }

  /**
   * @return The {@link IConfigProperty} instance that changed.
   */
  public IConfigProperty<?> getConfigProperty() {
    return m_configProperty;
  }

  /**
   * @return The old value of the property before the change.
   */
  public Object getOldValue() {
    return m_oldValue;
  }

  /**
   * @return The new (and now current) value of the property.
   */
  public Object getNewValue() {
    return m_newValue;
  }

  /**
   * @return The type of event. One of {@link #TYPE_VALUE_CHANGED}, {@link #TYPE_INVALIDATE},
   *         {@link #TYPE_VALUE_INITIALIZED}.
   */
  public int getType() {
    return m_type;
  }
}
