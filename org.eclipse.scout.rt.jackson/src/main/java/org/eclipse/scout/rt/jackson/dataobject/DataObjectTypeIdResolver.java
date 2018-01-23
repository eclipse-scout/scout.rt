package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.LazyValue;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.SimpleType;

/**
 * {@link TypeIdResolver} implementation handling type resolution of data objects.
 *
 * @see DataObjectDefinitionRegistry
 */
@Bean
public class DataObjectTypeIdResolver extends TypeIdResolverBase {

  private final LazyValue<DataObjectDefinitionRegistry> m_doEntityDefinitionRegistry = new LazyValue<>(DataObjectDefinitionRegistry.class);

  private JavaType m_baseType;

  @Override
  public void init(JavaType baseType) {
    m_baseType = baseType;
  }

  @Override
  public Id getMechanism() {
    return Id.NAME;
  }

  @Override
  public String idFromValue(Object obj) {
    return idFromClass(obj.getClass());
  }

  @Override
  public String idFromBaseType() {
    return idFromClass(m_baseType.getRawClass());
  }

  @Override
  public String idFromValueAndType(Object value, Class<?> clazz) {
    if (value != null) {
      return idFromClass(value.getClass());
    }
    else {
      return idFromClass(clazz);
    }
  }

  /**
   * @returns type id to use for serialization of specified class.
   */
  protected String idFromClass(Class<?> c) {
    return m_doEntityDefinitionRegistry.get().toTypeName(c);
  }

  @Override
  public JavaType typeFromId(DatabindContext context, String id) throws IOException {
    return SimpleType.constructUnsafe(m_doEntityDefinitionRegistry.get().fromTypeName(id));
  }

  @Override
  public String getDescForKnownTypeIds() {
    return m_doEntityDefinitionRegistry.get().getTypeNameToClassMap()
        .entrySet()
        .stream()
        .map(e -> e.getKey() + " -> " + e.getValue().getName())
        .collect(Collectors.joining("\n"));
  }
}
