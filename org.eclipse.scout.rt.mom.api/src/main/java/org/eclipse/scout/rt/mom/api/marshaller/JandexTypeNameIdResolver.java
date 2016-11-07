package org.eclipse.scout.rt.mom.api.marshaller;

import java.io.IOException;
import java.util.Set;

import org.eclipse.scout.rt.platform.inventory.ClassInventory;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * {@link TypeIdResolver} implementation used for JSON serialization and deserialization. The type information is based
 * on {@link JsonTypeName} annotation on classes to serialize. In order to support polymorphism the correct subclass of
 * an abstract class is found using the Jandex class inventory using a {@link JsonTypeName} annotation lookup.
 *
 * @see https://github.com/FasterXML/jackson-docs/wiki/JacksonPolymorphicDeserialization
 */
public class JandexTypeNameIdResolver implements TypeIdResolver {

  private static final Logger LOG = LoggerFactory.getLogger(JandexTypeNameIdResolver.class);

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
   * Returns type id to use for serialization of specified class.
   */
  protected String idFromClass(Class<?> clazz) {
    if (clazz.isAnnotationPresent(JsonTypeName.class)) {
      return clazz.getAnnotation(JsonTypeName.class).value();
    }
    else {
      LOG.warn("Class {} misses annotation {}, cannot add class id information to serialized JSON.", clazz, JsonTypeName.class);
      return null;
    }
  }

  @Override
  public JavaType typeFromId(DatabindContext context, String id) throws IOException {
    if (hasMatchingJsonTypeAnnotation(m_baseType.getRawClass(), id)) {
      return m_baseType;
    }
    else {
      Set<IClassInfo> subClasses = ClassInventory.get().getAllKnownSubClasses(m_baseType.getRawClass());
      for (IClassInfo subClass : subClasses) {
        if (subClass.hasAnnotation(JsonTypeName.class)) {
          Class<?> clazz = subClass.resolveClass();
          if (hasMatchingJsonTypeAnnotation(clazz, id)) {
            return TypeFactory.defaultInstance().constructSpecializedType(m_baseType, clazz);
          }
        }
      }
    }
    LOG.warn("Could not find suitable class for id {}, base type {}", id, m_baseType);
    return null;
  }

  /**
   * Checks for matching {@link JsonTypeName} annotation.
   */
  protected boolean hasMatchingJsonTypeAnnotation(Class<?> clazz, String id) {
    return clazz.isAnnotationPresent(JsonTypeName.class) && ObjectUtility.equals(clazz.getAnnotation(JsonTypeName.class).value(), id);
  }

  @Override
  public String getDescForKnownTypeIds() {
    Set<IClassInfo> subClasses = ClassInventory.get().getAllKnownSubClasses(m_baseType.getRawClass());
    StringBuilder sb = new StringBuilder(m_baseType.getRawClass().getName()).append(",");
    for (IClassInfo subClass : subClasses) {
      sb.append(",").append(subClass.name());
    }
    return sb.toString();
  }
}
