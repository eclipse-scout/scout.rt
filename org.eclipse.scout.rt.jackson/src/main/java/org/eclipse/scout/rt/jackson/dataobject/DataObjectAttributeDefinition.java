package org.eclipse.scout.rt.jackson.dataobject;

import java.lang.reflect.Method;
import java.util.Optional;

import org.eclipse.scout.rt.platform.dataobject.IDoEntity;

import com.fasterxml.jackson.databind.JavaType;

/**
 * Holder for name, type, format definition and accessor method of one {@link IDoEntity} attribute.
 */
public class DataObjectAttributeDefinition {
  private final String m_name;
  private final JavaType m_type;
  private final Optional<String> m_formatPattern;
  private final Method m_accessor;

  public DataObjectAttributeDefinition(String name, JavaType type, Optional<String> formatPattern, Method accessor) {
    m_name = name;
    m_type = type;
    m_formatPattern = formatPattern;
    m_accessor = accessor;
  }

  public String getName() {
    return m_name;
  }

  public JavaType getType() {
    return m_type;
  }

  public Method getAccessor() {
    return m_accessor;
  }

  public Optional<String> getFormatPattern() {
    return m_formatPattern;
  }

  @Override
  public String toString() {
    return DataObjectAttributeDefinition.class.getSimpleName() + " [name=" + m_name + " type=" + m_type + " formatPattern=" + m_formatPattern + " accessor=" + m_accessor + "]";
  }
}
