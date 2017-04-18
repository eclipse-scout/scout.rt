package org.eclipse.scout.rt.mom.api.marshaller;

import java.io.IOException;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
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
      while (bean != null && bean.hasAnnotation(Replace.class)) {
        bean = m_beanManagerUtility.lookupRegisteredBean(bean.getBeanClazz().getSuperclass());
        if (bean.hasAnnotation(JsonTypeName.class)) {
          return bean.getBeanAnnotation(JsonTypeName.class).value();
        }
      }
      LOG.warn("Class {} misses annotation {}, cannot add class id information to serialized JSON.", clazz, JsonTypeName.class);
      return null;
    }
  }

  /**
   * This method tries to find the {@link JavaType} matching the specified {@code id} type identifier.
   * <p>
   * The algorithm works using the following steps:
   * <ul>
   * <li>Check if {@link #m_baseType} has a {@link JsonTypeName} matching the specified identifier.
   * <li>Check if {@link #m_baseType} is replacing a super class having a {@link JsonTypeName} matching the specified
   * identifier.
   * <li>Check if {@link #m_baseType} has a sub class with a {@link JsonTypeName} matching the specified identifier.
   * </ul>
   * <p>
   * Using the following example, and assuming that {@code ImplClass} acts as the {@link #m_baseType} in the example,
   * the algorithm would start by checking the {@code ImplClass} for a matching type identifier. If the identifier does
   * not match, since {@code ImplClass} is annotated with {@link Replace}, the {@code CoreClass} is checked for a
   * matching type identifier. If the identifier does not match, then all available subclasses are checked, in this
   * example the classes {@code ClassA} and {@code ClassA} (including possibly replaced super classes as well).
   *
   * <pre>
   *               +-----------+------------+
   *               |         Object         |
   *               +-----------+------------+
   *                           |
   *               +-----------+------------+
   *               |       CoreClass        |
   *               +-----------+------------+
   *                           |
   *               +-----------+------------+
   *               |   ImplClass (@Replace) |
   *               +-----------^------------+
   *                           |
   *              +------------+------------+
   *              |                         |
   *    +--------------------+   +--------------------+
   *    |      ClassA        |   |       ClassB       |
   *    +--------------------+   +--------------------+
   * </pre>
   */
  @Override
  public JavaType typeFromId(DatabindContext context, String id) throws IOException {
    // STEP 1: Check if base type class has a matching type identifier or is replacing another class having the a matching type identifier
    JavaType javaType = typeFromClassOrSuperClass(id, m_baseType.getRawClass());
    if (javaType != null) {
      return javaType;
    }

    // STEP 2: Check if a subclass of the base type class has a matching type identifier
    javaType = typeFromSubClasses(id, m_baseType.getRawClass());
    if (javaType != null) {
      return javaType;
    }

    LOG.warn("Could not find suitable class for id {}, base type {}", id, m_baseType);
    return null;
  }

  /**
   * Method called to resolve the type from a given type identifier {@code id} and class {@code lookupClazz} checking
   * for a matching type identifier on the class itself and replaced super classes
   * <p>
   * This custom implementation finally resolves the matching class against the bean manager to get the most specific
   * bean.
   */
  protected JavaType typeFromClassOrSuperClass(String id, Class<?> lookupClazz) {
    // STEP 1: Check if lookup class itself has a matching type identifier
    if (hasMatchingJsonTypeAnnotation(lookupClazz, id)) {
      if (m_beanManagerUtility.isBeanClass(lookupClazz)) {
        // CASE 1a: lookupClass has a matching JSON type annotation and is a Scout bean, lookup most specific bean using bean manager
        IBean<?> uniqueBean = BEANS.getBeanManager().uniqueBean(lookupClazz);
        if (uniqueBean != null) {
          return TypeFactory.defaultInstance().constructSpecializedType(m_baseType, uniqueBean.getBeanClazz());
        }
      }
      else {
        // CASE 1b: lookupClass has a matching JSON type annotation and is not a Scout bean, use lookupClazz as resolved type
        return TypeFactory.defaultInstance().constructSpecializedType(m_baseType, lookupClazz);
      }
    }

    // STEP 2: Check if base type class is replacing other bean(s) and check if their type identifier matches
    IBean<?> bean = m_beanManagerUtility.lookupRegisteredBean(lookupClazz);
    if (bean != null) {
      JavaType javaType = typeFromReplacedSuperClass(id, bean);
      if (javaType != null) {
        return javaType;
      }
    }
    return null;
  }

  /**
   * Method called to resolve type from given type identifier {@code id} starting from {@code bean} class and checking
   * type annotation of replaced super classes.
   */
  protected JavaType typeFromReplacedSuperClass(String id, IBean<?> bean) {
    Class<?> baseLookupClazz = bean.getBeanClazz();
    while (bean.hasAnnotation(Replace.class)) {
      bean = m_beanManagerUtility.lookupRegisteredBean(bean.getBeanClazz().getSuperclass());
      if (hasMatchingJsonTypeAnnotation(bean.getBeanClazz(), id)) {
        return TypeFactory.defaultInstance().constructSpecializedType(m_baseType, baseLookupClazz);
      }
    }
    return null;
  }

  /**
   * Method called to resolve type from given type identifier {@code id} and all subclasses of class
   * {@code lookupClazz}. If the subclasses are replacing other classes, the super type hierarchy is scanned.
   * <p>
   * This custom implementation resolves the matching sub class against the bean manager to get the most specific bean.
   */
  protected JavaType typeFromSubClasses(String id, Class<?> lookupClazz) {
    Set<IClassInfo> subClasses = ClassInventory.get().getAllKnownSubClasses(lookupClazz);
    for (IClassInfo subClassInfo : subClasses) {
      // Check if subClazz has matching type identifier (Note: subclass could be a Scout bean or a POJO java class)
      JavaType javaType = typeFromClassOrSuperClass(id, subClassInfo.resolveClass());
      if (javaType != null) {
        return javaType;
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
