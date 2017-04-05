package org.eclipse.scout.rt.mom.api.marshaller;

import java.io.IOException;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.Replace;
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
//TODO [7.0] pbz: Move class to own scout json/jackson module
public class JandexTypeNameIdResolver implements TypeIdResolver {

  private static final Logger LOG = LoggerFactory.getLogger(JandexTypeNameIdResolver.class);

  private JavaType m_baseType;
  private BeanManagerUtility m_beanManagerUtility;

  @Override
  public void init(JavaType baseType) {
    m_baseType = baseType;
    m_beanManagerUtility = BEANS.get(BeanManagerUtility.class);
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
      IBean<?> bean = m_beanManagerUtility.lookupRegisteredBean(clazz);
      while (m_beanManagerUtility.hasAnnotation(bean, Replace.class)) {
        bean = m_beanManagerUtility.lookupRegisteredBean(bean.getBeanClazz().getSuperclass());
        if (m_beanManagerUtility.hasAnnotation(bean, JsonTypeName.class)) {
          return bean.getBeanAnnotation(JsonTypeName.class).value();
        }
      }
      LOG.warn("Class {} misses annotation {}, cannot add class id information to serialized JSON.", clazz, JsonTypeName.class);
      return null;
    }
  }

  @Override
  public JavaType typeFromId(DatabindContext context, String id) throws IOException {
    // STEP 1: Check if base type class has a matching type identifier
    JavaType javaType = typeFromId(id, m_baseType.getRawClass());
    if (javaType != null) {
      return javaType;
    }

    // STEP 2: Check if base type is a Scout bean and for each available bean, check if the bean is replacing other bean(s) and check if their type identifier matches.
    for (IBean<?> bean : Platform.get().getBeanManager().getBeans(m_baseType.getRawClass())) {
      javaType = typeFromReplacedSuperClassId(id, bean);
      if (javaType != null) {
        return javaType;
      }
    }

    // STEP 3: Check if a subclass of the base type class has a matching type identifier
    javaType = typeFromSubclassId(id, m_baseType.getRawClass());
    if (javaType != null) {
      return javaType;
    }

    LOG.warn("Could not find suitable class for id {}, base type {}", id, m_baseType);
    return null;
  }

  /**
   * Method called to resolve type from given type identifier {@code id} and class {@code lookupClazz}
   * <p>
   * This custom implementation resolves the matching class against the bean manager to get the most specific bean.
   */
  protected JavaType typeFromId(String id, Class<?> lookupClazz) {
    if (hasMatchingJsonTypeAnnotation(lookupClazz, id)) {
      if (m_beanManagerUtility.isBeanClass(lookupClazz)) {
        // CASE 1: lookupClass has matching JSON type annotation and is a Scout bean, lookup most specific bean using bean manager
        Class<?> beanClazz = m_beanManagerUtility.lookupClass(lookupClazz);
        return TypeFactory.defaultInstance().constructSpecializedType(m_baseType, beanClazz);
      }
      else {
        // CASE 2: lookupClass has matching JSON type annotation and is not a Scout bean, use lookupClass as resolved type
        return TypeFactory.defaultInstance().constructSpecializedType(m_baseType, lookupClazz);

      }
    }
    return null;
  }

  /**
   * Method called to resolve type from given type identifier {@code id} starting from {@code bean} class and checking
   * type annotation of replaced super classes.
   */
  protected JavaType typeFromReplacedSuperClassId(String id, IBean<?> bean) {
    Class<?> baseLookupClazz = bean.getBeanClazz();
    while (m_beanManagerUtility.hasAnnotation(bean, Replace.class)) {
      bean = m_beanManagerUtility.lookupRegisteredBean(bean.getBeanClazz().getSuperclass());
      if (hasMatchingJsonTypeAnnotation(bean.getBeanClazz(), id)) {
        return TypeFactory.defaultInstance().constructSpecializedType(m_baseType, baseLookupClazz);
      }
    }
    return null;
  }

  /**
   * Method called to resolve type from given type identifier {@code id} and all subclasses of class {@code lookupClazz}
   * <p>
   * This custom implementation resolves the matching sub class against the bean manager to get the most specific bean.
   */
  protected JavaType typeFromSubclassId(String id, Class<?> lookupClazz) {
    Set<IClassInfo> subClasses = ClassInventory.get().getAllKnownSubClasses(lookupClazz);
    for (IClassInfo subClassInfo : subClasses) {
      if (subClassInfo.hasAnnotation(JsonTypeName.class)) {
        Class<?> subClazz = subClassInfo.resolveClass();

        // Check if subClazz has matching type identifier (Note: subclass could be a Scout bean or a POJO java class)
        JavaType subClassJavaType = typeFromId(id, subClazz);
        if (subClassJavaType != null) {
          return subClassJavaType;
        }
      }
    }
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
