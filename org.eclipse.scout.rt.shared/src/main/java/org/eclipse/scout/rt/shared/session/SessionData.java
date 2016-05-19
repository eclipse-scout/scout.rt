package org.eclipse.scout.rt.shared.session;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.shared.ISession;

/**
 * This class is used by {@link ISession}s for holding arbitrary data in a {@link ConcurrentMap}. Hence operations are
 * atomic and concurrent modifications may occur. It implements the {@link Serializable} interface, but it is only
 * serializable if all the referenced objects are serializable as well.
 * <p/>
 * <code>null</code>-keys are not supported and will throw an {@link AssertionException}. <code>null</code>-values are
 * considered as remove request.
 *
 * @since 5.2
 */
public class SessionData implements Serializable {

  private static final long serialVersionUID = 1L;

  private final ConcurrentMap<String, Object> m_dataMap;

  public SessionData() {
    m_dataMap = new ConcurrentHashMap<>();
  }

  public void set(String key, Object value) {
    Assertions.assertNotNull(key, "key must not be null");
    if (value == null) {
      m_dataMap.remove(key);
    }
    else {
      m_dataMap.put(key, value);
    }
  }

  /**
   * Returns the already assigned value for the given key or computes a new one, if there is none exists.
   * <p/>
   * <b>Note</b>: the producer could be called multiple times but only the first value is used.
   */
  public Object computeIfAbsent(String key, Callable<?> producer) {
    Assertions.assertNotNull(producer, "producer must not be null");
    Assertions.assertNotNull(key, "key must not be null");

    Object existingValue = m_dataMap.get(key);
    if (existingValue != null) {
      return existingValue;
    }

    Object newValue;
    try {
      newValue = producer.call();
    }
    catch (Exception e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e);
    }
    if (newValue == null) {
      return null;
    }

    existingValue = m_dataMap.putIfAbsent(key, newValue);
    if (existingValue != null) {
      return existingValue;
    }
    return newValue;
  }

  public Object get(String key) {
    Assertions.assertNotNull(key, "key must not be null");
    return m_dataMap.get(key);
  }
}
