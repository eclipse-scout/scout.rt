package org.eclipse.scout.rt.jackson.dataobject;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.dataobject.AttributeName;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.dataobject.TypeName;
import org.eclipse.scout.rt.platform.dataobject.ValueFormat;
import org.eclipse.scout.rt.platform.inventory.ClassInventory;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Registry and cache for all {@link IDoEntity} and their declared attributes.
 */
@ApplicationScoped
public class DataObjectDefinitionRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(DataObjectDefinitionRegistry.class);

  /**
   * <b>NOTE:</b> This from/to class maps contains the static, compile-time "class" to "type name" mapping as defined by
   * the Jandex index.
   * <p>
   * Replaced classes are not removed or replaced in those two maps. A replaced class can still be found in this type
   * mapping by its declared type name, but is then on-the-fly replaced with the correct replacing class when a lookup
   * using {@link #fromTypeName(String)} method is performed.
   */
  private final Map<String, Class<? extends IDoEntity>> m_typeNameToClassMap = new HashMap<>();
  private final Map<Class<? extends IDoEntity>, String> m_classToTypeName = new HashMap<>();

  /** Map of {@link IDoEntity} class to its attributes map */
  private final Map<Class<? extends IDoEntity>, Map<String, DataObjectAttributeDefinition>> m_classAttributeMap = new ConcurrentHashMap<>();

  @PostConstruct
  public void init() {
    ClassInventory.get()
        .getKnownAnnotatedTypes(TypeName.class)
        .stream()
        .map(ci -> ci.resolveClass())
        .forEach(c -> registerClass(c));
    LOG.info("Registry initialized, found {} {} implementations with @{} annotation.", m_typeNameToClassMap.size(), IDoEntity.class.getSimpleName(), TypeName.class.getSimpleName());
  }

  /**
   * Adds one class to registry.
   */
  protected void registerClass(Class<?> clazz) {
    if (IDoEntity.class.isAssignableFrom(clazz)) {
      Class<? extends IDoEntity> entityClass = clazz.asSubclass(IDoEntity.class);
      String name = jsonAnnotationName(clazz);
      String registeredName = m_classToTypeName.put(entityClass, name);
      Class<? extends IDoEntity> registeredClass = m_typeNameToClassMap.put(name, entityClass);
      checkDuplicateClassMapping(clazz, name, registeredName, registeredClass);
      LOG.debug("Registered class {} with typename '{}'", entityClass, name);
    }
    else {
      LOG.warn("Class {} is annotated with @{} but is not an instance of {}, skip registration", clazz.getName(), TypeName.class.getSimpleName(), IDoEntity.class);
    }
  }

  /**
   * Checks for {@link IDoEntity} classes with duplicated {@link TypeName} annotation values.
   */
  protected void checkDuplicateClassMapping(Class<?> clazz, String name, String existingName, Class<? extends IDoEntity> existingClass) {
    Assertions.assertNull(existingClass, "{} and {} have the same type '{}', use an unique @{} annotation value.", clazz, existingClass, name, TypeName.class.getSimpleName());
    Assertions.assertNull(existingName, "{} was already registered with type name {}, register each class only once.", clazz, existingName, TypeName.class.getSimpleName());
  }

  protected String jsonAnnotationName(Class<?> c) {
    TypeName typeNameAnn = c.getAnnotation(TypeName.class);
    if (typeNameAnn != null && StringUtility.hasText(typeNameAnn.value())) {
      return typeNameAnn.value();
    }
    return jsonDefaultName(c);
  }

  protected String jsonDefaultName(Class<?> c) {
    return c.getSimpleName();
  }

  /**
   * @return type name for specified class {@code clazz}. If the class does not have a type name, the super class
   *         hierarchy is searched for the first available type name.
   */
  public String toTypeName(Class<?> clazz) {
    // FIXME [16.0] pbz: [JSON] check if we should assert/warn if name == null or use default json name instead?
    while (true) {
      String name = m_classToTypeName.get(clazz);
      if (name != null) {
        return name;
      }
      if (clazz == null || clazz == Object.class) {
        return null;
      }
      clazz = clazz.getSuperclass();
    }
  }

  /**
   * Returns the correct class for specified {@code typeName}, if the originally type name annotated class was replaced
   * by another bean, the replacing bean class is returned, as long as the replacing class can uniquely be resolved.
   *
   * @return class for specified {@code typeName}, if class is uniquely resolvable as scout bean, else {@code null}
   * @see IBeanManager#uniqueBean(Class)
   */
  public Class<? extends IDoEntity> fromTypeName(String typeName) {
    Class<? extends IDoEntity> rawClass = m_typeNameToClassMap.get(typeName);
    if (rawClass != null) {
      // check if requested class is a valid registered bean, and the lookup to a concrete bean class is unique
      if (BEANS.getBeanManager().isBean(rawClass)) {
        IBean<? extends IDoEntity> bean = BEANS.getBeanManager().uniqueBean(rawClass);
        if (bean != null) {
          return bean.getBeanClazz();
        }
        else {
          LOG.warn("Class lookup for raw class {} with type name {} is not unique, cannot lookup matching bean class!", rawClass, typeName);
        }
      }
      else {
        return rawClass;
      }
    }
    return null;
  }

  /**
   * @return Map with all type name to {@link IDoEntity} class mappings
   */
  public Map<String, Class<? extends IDoEntity>> getTypeNameToClassMap() {
    return Collections.unmodifiableMap(m_typeNameToClassMap);
  }

  /**
   * @return Optional of {@link DataObjectAttributeDefinition} for specified {@code enttiyClass} and
   *         {@code attributeName}
   */
  public Optional<DataObjectAttributeDefinition> getAttributeDescription(Class<? extends IDoEntity> entityClass, String attributeName) {
    ensureEntityDefinitionLoaded(entityClass);
    return Optional.ofNullable(m_classAttributeMap.get(entityClass).get(attributeName));
  }

  /**
   * @return Map with all name/attribute definitions for specified {@code entityClass}
   */
  public Map<String, DataObjectAttributeDefinition> getAttributesDescription(Class<? extends IDoEntity> entityClass) {
    ensureEntityDefinitionLoaded(entityClass);
    return Collections.unmodifiableMap(m_classAttributeMap.get(entityClass));
  }

  /**
   * Ensures that attribute definition for specified {@code entityClass} is loaded and cached.
   */
  protected void ensureEntityDefinitionLoaded(Class<? extends IDoEntity> entityClass) {
    if (!m_classAttributeMap.containsKey(entityClass)) {
      registerAllAttributes(entityClass);
    }
  }

  /**
   * Add entity class to attribute definition registry
   */
  protected void registerAllAttributes(Class<? extends IDoEntity> entityClass) {
    LOG.debug("Adding attributes of class {} to registry.", entityClass);
    Map<String, DataObjectAttributeDefinition> attributes = new HashMap<>();
    for (Method method : entityClass.getMethods()) {
      // consider only attribute accessor methods
      if (method.getParameterCount() == 0) {
        Type returnType = method.getGenericReturnType();
        if (returnType instanceof ParameterizedType) {
          ParameterizedType pt = (ParameterizedType) returnType;
          // return type must be DoList or DoValue
          if (DoList.class == pt.getRawType()) {
            JavaType listItemsType = TypeFactory.defaultInstance().constructType(pt.getActualTypeArguments()[0]);
            addAttribute(attributes, TypeFactory.defaultInstance().constructParametricType((Class<?>) pt.getRawType(), listItemsType), method);
          }
          else if (DoValue.class == pt.getRawType()) {
            addAttribute(attributes, TypeFactory.defaultInstance().constructType(pt.getActualTypeArguments()[0]), method);
          }
        }
      }
    }
    m_classAttributeMap.put(entityClass, attributes);
  }

  protected void addAttribute(Map<String, DataObjectAttributeDefinition> attributes, JavaType type, Method accessor) {
    String name = getAttributeName(accessor);
    Optional<String> formatPattern = getAttributeFormat(accessor);
    attributes.put(name, new DataObjectAttributeDefinition(name, type, formatPattern, accessor));
    LOG.debug("Adding attribute '{}' with type {} and format pattern '{}' to registry.", name, type, formatPattern.orElse("null"));
  }

  protected String getAttributeName(Method accessor) {
    if (accessor.isAnnotationPresent(AttributeName.class)) {
      return accessor.getAnnotation(AttributeName.class).value();
    }
    return accessor.getName();
  }

  protected Optional<String> getAttributeFormat(Method accessor) {
    if (accessor.isAnnotationPresent(ValueFormat.class)) {
      return Optional.ofNullable(accessor.getAnnotation(ValueFormat.class).pattern());
    }
    return Optional.empty();
  }
}
