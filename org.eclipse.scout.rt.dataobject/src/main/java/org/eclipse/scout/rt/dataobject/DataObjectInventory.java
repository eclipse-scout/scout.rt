/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import static org.eclipse.scout.rt.platform.util.Assertions.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.inventory.ClassInventory;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.inventory.IClassInventory;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;
import org.eclipse.scout.rt.platform.namespace.Namespaces;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inventory (and cache) for all {@link IDoEntity} with a {@link TypeName} annotation, listing all declared attributes
 * for each {@link IDoEntity}.
 */
@ApplicationScoped
public class DataObjectInventory {

  private static final Logger LOG = LoggerFactory.getLogger(DataObjectInventory.class);

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

  /**
   * Map of {@link IDoEntity} class to its compile-time annotated type version (see {@link TypeVersion} annotation).
   */
  private final Map<Class<? extends IDoEntity>, NamespaceVersion> m_classToTypeVersion = new HashMap<>();

  /** Map of {@link IDoEntity} class to its attributes map */
  private final Map<Class<? extends IDoEntity>, Map<String, DataObjectAttributeDescriptor>> m_classAttributeMap = new ConcurrentHashMap<>();

  /**
   * Map of {@link IDoEntityContribution} class to its compile-time annotated containers (see {@link ContributesTo}
   * annotation).
   */
  private final Map<Class<? extends IDoEntity>, Set<Class<? extends IDoEntity>>> m_contributionClassToContainers = new HashMap<>();

  /**
   * Map of {@link IDoEntity} container class to its {@link IDoEntityContribution contributions} (see
   * {@link ContributesTo} annotation).
   */
  private final Map<Class<? extends IDoEntity>, Set<Class<? extends IDoEntityContribution>>> m_containerClassToContributionClasses = new HashMap<>();

  /**
   * Map of a class to its {@link IDataObjectVisitorExtension} used with data object visitors.
   */
  private final Map<Class<?>, IDataObjectVisitorExtension<?>> m_visitorExtensions = new HashMap<>();

  @PostConstruct
  protected void init() {
    ClassInventory.get()
        .getKnownAnnotatedTypes(TypeName.class)
        .stream()
        .map(IClassInfo::resolveClass)
        .forEach(this::registerClassByTypeName);

    ClassInventory.get()
        .getKnownAnnotatedTypes(TypeVersion.class)
        .stream()
        .map(IClassInfo::resolveClass)
        .forEach(this::registerClassByTypeVersion);

    ClassInventory.get()
        .getKnownAnnotatedTypes(ContributesTo.class)
        .stream()
        .map(IClassInfo::resolveClass)
        .forEach(this::registerClassByContributesTo);

    validateTypeVersionImplementors();
    validateTypeVersionRequired();

    //noinspection unchecked
    BEANS.all(IDataObjectVisitorExtension.class).forEach(this::registerVisitorExtension);

    LOG.info("Registry initialized, found {} {} implementations with @{} annotation and {} implementations with @{} annotation.",
        m_typeNameToClassMap.size(), IDoEntity.class.getSimpleName(), TypeName.class.getSimpleName(),
        m_classToTypeVersion.size(), TypeVersion.class.getSimpleName());
  }

  /**
   * @return type name for specified class {@code clazz}. If the class does not have a type name, the super class
   *         hierarchy is searched for the first available type name. Returns {@code null} if no type name can be found.
   */
  public String toTypeName(Class<?> queryClazz) {
    Class<?> clazz = queryClazz;
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
   * @return Type version for a given {@link IDoEntity} class.
   * @see TypeVersion
   */
  public NamespaceVersion getTypeVersion(Class<? extends IDoEntity> clazz) {
    return m_classToTypeVersion.get(clazz);
  }

  /**
   * A container class might be represented by an interface or abstract class too. Make sure to use
   * {@link Class#isInstance(Object)} or {@link Class#isAssignableFrom(Class)} to check for a matching container.
   *
   * @return Container classes for a given {@link IDoEntityContribution} class.
   * @see ContributesTo
   */
  public Set<Class<? extends IDoEntity>> getContributionContainers(Class<? extends IDoEntityContribution> contributionClass) {
    Set<Class<? extends IDoEntity>> containerClasses = m_contributionClassToContainers.get(contributionClass);
    return containerClasses == null ? Collections.emptySet() : Collections.unmodifiableSet(containerClasses);
  }

  /**
   * Use {@link #getAllContributionClasses(Class)} if all DO entity contributions that maybe contained in the container
   * class are required.
   *
   * @return {@link IDoEntityContribution} classes for a given {@link IDoEntity} container class.
   * @see ContributesTo
   */
  public Set<Class<? extends IDoEntityContribution>> getContributionClasses(Class<? extends IDoEntity> containerClass) {
    Set<Class<? extends IDoEntityContribution>> contributionClasses = m_containerClassToContributionClasses.get(containerClass);
    return contributionClasses == null ? Collections.emptySet() : Collections.unmodifiableSet(contributionClasses);
  }

  /**
   * In contrast to {@link #getContributionClasses(Class)}, this method returns not only the DO entity contributions for
   * exactly the provided container class, but also all DO entity contributions for the containers class super classes
   * and interfaces.
   *
   * @return {@link IDoEntityContribution} classes for a given {@link IDoEntity} container class and it's parent
   *         classes/interfaces.
   * @see ContributesTo
   */
  public Set<Class<? extends IDoEntityContribution>> getAllContributionClasses(Class<? extends IDoEntity> containerClass) {
    Set<Class<? extends IDoEntity>> collectedClasses = new HashSet<>();
    collectDoEntityParents(containerClass, collectedClasses);

    return collectedClasses.stream()
        .map(clazz -> m_containerClassToContributionClasses.get(clazz))
        .filter(Objects::nonNull)
        .flatMap(Set::stream)
        .collect(Collectors.toUnmodifiableSet());
  }

  protected void collectDoEntityParents(Class<? extends IDoEntity> doEntityClass, Set<Class<? extends IDoEntity>> collectedClasses) {
    collectedClasses.add(doEntityClass);

    Class<?> superclass = doEntityClass.getSuperclass();
    if (superclass != null && IDoEntity.class.isAssignableFrom(superclass)) {
      // super class implements IDoEntity
      //noinspection unchecked
      collectDoEntityParents((Class<? extends IDoEntity>) superclass, collectedClasses);
    }

    for (Class<?> interfaceClass : doEntityClass.getInterfaces()) {
      if (IDoEntity.class.isAssignableFrom(interfaceClass)) {
        // interface extends IDoEntity
        //noinspection unchecked
        collectDoEntityParents((Class<? extends IDoEntity>) interfaceClass, collectedClasses);
      }
    }
  }

  /**
   * @return Map with all type name to {@link IDoEntity} class mappings
   */
  public Map<String, Class<? extends IDoEntity>> getTypeNameToClassMap() {
    return Collections.unmodifiableMap(m_typeNameToClassMap);
  }

  /**
   * @return Optional of {@link DataObjectAttributeDescriptor} for specified {@code entityClass} and
   *         {@code attributeName}
   */
  public Optional<DataObjectAttributeDescriptor> getAttributeDescription(Class<? extends IDoEntity> entityClass, String attributeName) {
    ensureEntityDefinitionLoaded(entityClass);
    return Optional.ofNullable(m_classAttributeMap.get(entityClass).get(attributeName));
  }

  /**
   * @return Map with all name/attribute definitions for specified {@code entityClass}
   */
  public Map<String, DataObjectAttributeDescriptor> getAttributesDescription(Class<? extends IDoEntity> entityClass) {
    ensureEntityDefinitionLoaded(entityClass);
    return Collections.unmodifiableMap(m_classAttributeMap.get(entityClass));
  }

  /* **************************************************************************
   * HELPER METHODS
   * *************************************************************************/

  protected void validateTypeVersionImplementors() {
    String typeVersionsWithoutNamespaceVersion = BEANS.all(ITypeVersion.class).stream()
        .filter(typeVersion -> typeVersion.getVersion() == null)
        .map(ITypeVersion::getClass)
        .map(Class::getName)
        .collect(Collectors.joining("\n"));

    assertTrue(StringUtility.isNullOrEmpty(typeVersionsWithoutNamespaceVersion), "Missing namespace version for implementors of {}:\n{}", ITypeVersion.class.getName(), typeVersionsWithoutNamespaceVersion);

    String typeVersionsWithUnknownNamespace = BEANS.all(ITypeVersion.class).stream()
        .filter(typeVersion -> Namespaces.get().byId(typeVersion.getVersion().getNamespace()) == null)
        .map(ITypeVersion::getClass)
        .map(Class::getName)
        .collect(Collectors.joining("\n"));

    assertTrue(StringUtility.isNullOrEmpty(typeVersionsWithUnknownNamespace), "No registered namespaces found for type versions:\n{}", typeVersionsWithUnknownNamespace);

    String duplicateTypeVersions = BEANS.all(ITypeVersion.class).stream()
        .collect(Collectors.groupingBy(ITypeVersion::getVersion))
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue().size() > 1)
        .map(entry -> entry.getKey().unwrap() + ": " + entry.getValue().stream().map(typeVersion -> typeVersion.getClass().getName()).collect(Collectors.joining(", ")))
        .collect(Collectors.joining("\n"));

    assertTrue(StringUtility.isNullOrEmpty(duplicateTypeVersions), "Multiple type version classes for the same namespace version detected:\n{}", duplicateTypeVersions);
  }

  protected void validateTypeVersionRequired() {
    IClassInventory classInventory = ClassInventory.get();
    String classesWithoutRequiredTypeVersion = classInventory
        .getKnownAnnotatedTypes(TypeVersionRequired.class)
        .stream()
        .map(classInventory::getAllKnownSubClasses)
        .flatMap(Set::stream)
        .filter(ci -> (ci.isInstanciable() && !ci.hasAnnotation(TypeVersion.class))) // only data objects (instantiable) are relevant (ignore other interfaces or abstract classes)
        .map(IClassInfo::name)
        .distinct()
        .collect(Collectors.joining("\n"));

    assertTrue(StringUtility.isNullOrEmpty(classesWithoutRequiredTypeVersion), "Missing @{} annotation for data objects due to {} on parent class/implementing interface:\n{}",
        TypeVersion.class.getSimpleName(), TypeVersionRequired.class.getSimpleName(), classesWithoutRequiredTypeVersion);
  }

  /**
   * Adds {@code clazz} to registry based on its {@code TypeName}
   */
  protected void registerClassByTypeName(Class<?> clazz) {
    if (IDoEntity.class.isAssignableFrom(clazz)) {
      Class<? extends IDoEntity> entityClass = clazz.asSubclass(IDoEntity.class);
      String name = resolveTypeName(clazz);
      if (StringUtility.hasText(name)) {
        String registeredName = m_classToTypeName.put(entityClass, name);
        Class<? extends IDoEntity> registeredClass = m_typeNameToClassMap.put(name, entityClass);
        checkDuplicateClassMapping(clazz, name, registeredName, registeredClass);
        LOG.debug("Registered class {} with type name '{}'", entityClass, name);
      }
      else {
        LOG.warn("Class {} is annotated with @{} with an empty type name value, skip registration", clazz.getName(), TypeName.class.getSimpleName());
      }
    }
    else {
      LOG.warn("Class {} is annotated with @{} but is not an instance of {}, skip registration", clazz.getName(), TypeName.class.getSimpleName(), IDoEntity.class);
    }
  }

  /**
   * Adds {@code clazz} to registry based on its {@code TypeVersion}
   */
  protected void registerClassByTypeVersion(Class<?> clazz) {
    if (IDoEntity.class.isAssignableFrom(clazz)) {
      Class<? extends IDoEntity> entityClass = clazz.asSubclass(IDoEntity.class);
      Class<? extends ITypeVersion> typeVersionClass = resolveTypeVersionClass(clazz);
      if (typeVersionClass != null) {
        ITypeVersion typeVersion = assertNotNull(BEANS.opt(typeVersionClass), "No instance found of '{}' for data object '{}'.", typeVersionClass, clazz);
        NamespaceVersion version = typeVersion.getVersion(); // never null, validated in #validateTypeVersionImplementors
        NamespaceVersion registeredVersion = m_classToTypeVersion.put(entityClass, version);
        assertNull(registeredVersion, "{} was already registered with type version {}, register each class only once.", clazz, registeredVersion, TypeVersion.class.getSimpleName());
        LOG.debug("Registered class {} with type version '{}'", entityClass, version);
      }
      else {
        LOG.debug("Registered class {} without type version", entityClass);
      }
    }
    else {
      LOG.warn("Class {} is annotated with @{} but is not an instance of {}, skip registration", clazz.getName(), TypeVersion.class.getSimpleName(), IDoEntity.class);
    }
  }

  /**
   * Adds {@code contributionClass} to registry with it's containers.
   */
  protected void registerClassByContributesTo(Class<?> clazz) {
    if (IDoEntityContribution.class.isAssignableFrom(clazz)) {
      Class<? extends IDoEntityContribution> contributionClass = clazz.asSubclass(IDoEntityContribution.class);
      ContributesTo contributesToAnn = contributionClass.getAnnotation(ContributesTo.class);
      Set<Class<? extends IDoEntity>> containerClasses = contributesToAnn == null ? Collections.emptySet() : CollectionUtility.hashSet(contributesToAnn.value());
      if (!containerClasses.isEmpty()) {
        m_contributionClassToContainers.put(contributionClass, containerClasses);
        containerClasses.forEach(containerClass -> m_containerClassToContributionClasses.computeIfAbsent(containerClass, k -> new HashSet<>()).add(contributionClass));
        LOG.debug("Registered class {} with containers '{}'", contributionClass, containerClasses);
      }
      else {
        LOG.warn("Class {} is annotated with @{} but doesn't contain any containers, skip registration", clazz.getName(), ContributesTo.class.getSimpleName());
      }
    }
    else {
      LOG.warn("Class {} is annotated with @{} but is not an instance of {}, skip registration", clazz.getName(), ContributesTo.class.getSimpleName(), IDoEntityContribution.class);
    }
  }

  /**
   * Adds visitor extension to registry based on its value class.
   */
  protected void registerVisitorExtension(IDataObjectVisitorExtension<?> visitorExtension) {
    // method is called according to bean manager order, do not override existing entries within the visitor extension map (putIfAbsent).
    Class<?> valueClass = visitorExtension.valueClass();
    m_visitorExtensions.putIfAbsent(valueClass, visitorExtension);
    ClassInventory.get().getAllKnownSubClasses(valueClass).forEach(classInfo -> m_visitorExtensions.putIfAbsent(classInfo.resolveClass(), visitorExtension));
  }

  /**
   * Checks for {@link IDoEntity} classes with duplicated {@link TypeName} annotation values.
   */
  protected void checkDuplicateClassMapping(Class<?> clazz, String name, String existingName, Class<? extends IDoEntity> existingClass) {
    assertNull(existingClass, "{} and {} have the same type '{}', use an unique @{} annotation value.", clazz, existingClass, name, TypeName.class.getSimpleName());
    assertNull(existingName, "{} was already registered with type name {}, register each class only once.", clazz, existingName, TypeName.class.getSimpleName());
  }

  /**
   * Ensures that attribute definition for specified {@code entityClass} is loaded and cached.
   */
  protected void ensureEntityDefinitionLoaded(Class<? extends IDoEntity> entityClass) {
    m_classAttributeMap.computeIfAbsent(entityClass, this::createAllAttributes);
  }

  /**
   * Add entity class to attribute definition registry
   */
  protected Map<String, DataObjectAttributeDescriptor> createAllAttributes(Class<? extends IDoEntity> entityClass) {
    LOG.debug("Adding attributes of class {} to registry.", entityClass);
    Map<String, DataObjectAttributeDescriptor> attributes = new HashMap<>();
    for (Method method : entityClass.getMethods()) {
      // consider only attribute accessor methods
      if (method.getParameterCount() == 0 && !Modifier.isStatic(method.getModifiers())) {
        Type returnType = method.getGenericReturnType();
        if (returnType instanceof ParameterizedType) {
          ParameterizedType pt = (ParameterizedType) returnType;
          // return type must be DoList, DoValue, DoSet or DoCollection
          if (ObjectUtility.isOneOf(pt.getRawType(), DoValue.class, DoList.class, DoSet.class, DoCollection.class)) {
            addAttribute(attributes, pt, method);
          }
        }
      }
    }
    return attributes;
  }

  protected void addAttribute(Map<String, DataObjectAttributeDescriptor> attributes, ParameterizedType type, Method accessor) {
    String name = resolveAttributeName(accessor);
    Optional<String> formatPattern = resolveAttributeFormat(accessor);
    attributes.put(name, new DataObjectAttributeDescriptor(name, type, formatPattern, accessor));
    LOG.debug("Adding attribute '{}' with type {} and format pattern '{}' to registry.", name, type, formatPattern.orElse("null"));
  }

  protected String resolveTypeName(Class<?> c) {
    TypeName typeNameAnn = c.getAnnotation(TypeName.class);
    return typeNameAnn == null ? null : typeNameAnn.value();
  }

  protected Class<? extends ITypeVersion> resolveTypeVersionClass(Class<?> c) {
    TypeVersion typeVersionAnn = c.getAnnotation(TypeVersion.class);
    return typeVersionAnn == null ? null : typeVersionAnn.value();
  }

  protected String resolveAttributeName(Method accessor) {
    if (accessor.isAnnotationPresent(AttributeName.class)) {
      return accessor.getAnnotation(AttributeName.class).value();
    }
    return accessor.getName();
  }

  protected Optional<String> resolveAttributeFormat(Method accessor) {
    if (accessor.isAnnotationPresent(ValueFormat.class)) {
      return Optional.ofNullable(accessor.getAnnotation(ValueFormat.class).pattern());
    }
    return Optional.empty();
  }

  /**
   * @return Visitor extension for the given class, or <code>null</code> if none is registered.
   */
  // only used internally by visitor, thus protected
  protected <T> IDataObjectVisitorExtension<T> getVisitorExtension(Class<T> valueClass) {
    //noinspection unchecked
    return (IDataObjectVisitorExtension<T>) m_visitorExtensions.get(valueClass);
  }
}
