/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.testing.signature;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.rt.dataobject.DataObjectAttributeDescriptor;
import org.eclipse.scout.rt.dataobject.DataObjectInventory;
import org.eclipse.scout.rt.dataobject.DoCollection;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.dataobject.TypeVersionRequired;
import org.eclipse.scout.rt.dataobject.enumeration.EnumInventory;
import org.eclipse.scout.rt.dataobject.enumeration.EnumName;
import org.eclipse.scout.rt.dataobject.enumeration.IEnum;
import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.IdInventory;
import org.eclipse.scout.rt.dataobject.id.IdTypeName;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.inventory.ClassInventory;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.inventory.IClassInventory;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Creates a data object signature ({@link DataObjectSignatureDo}) based on the available data objects with type
 * version, matching package prefixes and no exclusion by predicate.
 * <p>
 * The signature contains for each relevant data object all attributes with their types. The signature will also contain
 * all values of each referenced {@link IEnum} in an attribute of a relevant data object, independent of the enum's
 * package.
 * <p>
 * Signature generation will fail in case of any inconsistencies, such as:
 * <ul>
 * <li>Unsupported type in attribute</li>
 * <li>An attribute references another data object without type version</li>
 * <li>An attribute references an {@link IEnum} without an {@link EnumName}</li>
 * <li>An attribute references an {@link IId} without an {@link IdTypeName}</li>
 * </ul>
 */
@Bean
public class DataObjectSignatureGenerator {

  /**
   * Used for {@link TypeName} of instanciable {@link IDoEntity}.
   * <p>
   * Require a type name and a type version.
   */
  protected static final String VALUE_TYPE_DO_PREFIX = "DO";

  /**
   * Used for FQN of non-instanciable {@link IDoEntity} (interfaces/abstract classes).
   * <p>
   * Require {@link TypeVersionRequired} annotation.
   */
  protected static final String VALUE_TYPE_DO_INTERFACE_PREFIX = "DOI";

  /**
   * Used for {@link EnumName} of {@link IEnum}.
   * <p>
   * Required an enum name.
   */
  protected static final String VALUE_TYPE_ENUM_PREFIX = "ENUM";

  /**
   * Used for {@link IdTypeName} of {@link IId}.
   * <p>
   * Require a type id.
   */
  protected static final String VALUE_TYPE_ID_PREFIX = "ID";

  /**
   * Used for FQN of non-instanciable {@link IId} (interfaces/abstract classes).
   * <p>
   * NOTE: All subclasses are required to have a @{@link IdTypeName} annotation or a custom implemented
   * {@link IdInventory#getTypeName(Class)} method
   */
  protected static final String VALUE_TYPE_ID_INTERFACE_PREFIX = "IDI";

  /**
   * Used for FQN of supported attribute types ({@link #m_supportedAttributeTypes}).
   */
  protected static final String VALUE_TYPE_CLASS_PREFIX = "CLASS";

  /**
   * Pattern to match for boxed value types.
   * <ul>
   * <li>Group 0: Value type prefix (see VALUE_TYPE_ constants)</li>
   * <li>Group 1: Boxed value type (type name, enum name, id type name, class name)</li>
   * </ul>
   *
   * @see #box(String, String)
   */
  protected static final Pattern VALUE_TYPE_PATTERN = Pattern.compile(""
      + "("
      + StringUtility.join("|",
      VALUE_TYPE_DO_PREFIX,
      VALUE_TYPE_DO_INTERFACE_PREFIX,
      VALUE_TYPE_ENUM_PREFIX,
      VALUE_TYPE_ID_PREFIX,
      VALUE_TYPE_ID_INTERFACE_PREFIX,
      VALUE_TYPE_CLASS_PREFIX)
      + ")"
      + "\\["
      + "([\\w.\\-]+)"
      + "]");

  protected final DataObjectInventory m_dataObjectInventory;
  protected final EnumInventory m_enumInventory;
  protected final List<IDataObjectSignatureTestCustomizer> m_customizers;
  protected final Set<Class<?>> m_supportedAttributeTypes;

  /**
   * Either directly having {@link TypeVersionRequired} annotation or by inheritance (only interfaces and abstract
   * classes)
   */
  protected final Set<Class<?>> m_classesWithTypeVersionRequired = new HashSet<>();

  protected final List<String> m_errors = new ArrayList<>();

  protected final Set<Class<? extends IEnum>> m_referencedEnumClasses = new HashSet<>();

  protected Set<String> m_packageNamePrefixes;
  protected Predicate<Class<? extends IDoEntity>> m_dataObjectPredicate;
  protected BiPredicate<Class<? extends IDoEntity>, String> m_attributePredicate;

  public DataObjectSignatureGenerator() {
    m_dataObjectInventory = BEANS.get(DataObjectInventory.class);
    m_enumInventory = BEANS.get(EnumInventory.class);
    m_customizers = BEANS.all(IDataObjectSignatureTestCustomizer.class);

    m_supportedAttributeTypes = m_customizers.stream()
        .flatMap(collector -> collector.supportedTypes().stream())
        .collect(Collectors.toSet());

    IClassInventory classInventory = ClassInventory.get();
    classInventory
        .getKnownAnnotatedTypes(TypeVersionRequired.class)
        .stream()
        .peek(ci -> m_classesWithTypeVersionRequired.add(ci.resolveClass()))
        .map(classInventory::getAllKnownSubClasses)
        .flatMap(Set::stream)
        .filter(ci -> !ci.isInstanciable())
        .map(IClassInfo::resolveClass)
        .forEach(m_classesWithTypeVersionRequired::add);
  }

  /**
   * Creates the data object signature.
   *
   * @param packageNamePrefixes
   *     Only data object with matching package name prefixes are added to signature. Processing occurs for all
   *     data objects.
   * @param dataObjectPredicate
   *     Allows to filter for certain data object classes, e.g. exclude data objects from test packages for
   *     processing
   * @param attributePredicate
   *     Allows to filter for certain data object attributes, e.g. exclude a certain attribute of a specific data
   *     object from further processing because verified separately.
   */
  public DataObjectSignatureDo createSignature(Set<String> packageNamePrefixes, Predicate<Class<? extends IDoEntity>> dataObjectPredicate, BiPredicate<Class<? extends IDoEntity>, String> attributePredicate) {
    applySanityChecks();

    m_packageNamePrefixes = packageNamePrefixes;
    m_dataObjectPredicate = dataObjectPredicate;
    m_attributePredicate = attributePredicate;

    List<EntityDataObjectSignatureDo> entities = processAndCollectDoEntities();
    List<EnumApiSignatureDo> enums = collectReferencedEnums();

    if (!m_errors.isEmpty()) {
      throw new AssertionError("Building data object signature resulted in errors:\n" + StringUtility.join("\n", m_errors));
    }

    return BEANS.get(DataObjectSignatureDo.class)
        .withEntities(entities)
        .withEnums(enums);
  }

  /**
   * Sanity checks for supported types in case generic objects are added (not allowed) or manually handled types are
   * added (useless).
   */
  protected void applySanityChecks() {
    if (m_supportedAttributeTypes.contains(Object.class) || m_supportedAttributeTypes.contains(IDataObject.class)) {
      throw new PlatformException(
          "Object/IDataObject is never an allowed type for an attribute node. If used as type, use a different one in the data object attribute or add an exclusion via attribute predicate and verify values of this attribute manually.");
    }

    String invalidSupportedTypes = m_supportedAttributeTypes.stream()
        .filter(typeClass -> IEnum.class.isAssignableFrom(typeClass) || IId.class.isAssignableFrom(typeClass) || IDoEntity.class.isAssignableFrom(typeClass))
        .map(Class::getName)
        .collect(Collectors.joining(", "));

    if (!StringUtility.isNullOrEmpty(invalidSupportedTypes)) {
      throw new PlatformException("Subclasses of {}, {} and {} are handled differently and must never be part of the supported attribute types: {}",
          IEnum.class.getSimpleName(), IId.class.getSimpleName(), IDoEntity.class.getSimpleName(), invalidSupportedTypes);
    }
  }

  protected List<EntityDataObjectSignatureDo> processAndCollectDoEntities() {
    List<EntityDataObjectSignatureDo> entities = new LinkedList<>();
    for (Class<? extends IDoEntity> doEntityClass : m_dataObjectInventory.getTypeNameToClassMap().values()) {
      if (m_dataObjectPredicate != null && !m_dataObjectPredicate.test(doEntityClass)) {
        // Explicitly excluded for processing (e.g. test data objects).
        continue;
      }
      if (!isMatchingPackageNamePrefix(doEntityClass)) {
        continue; // wrong package
      }
      if (m_dataObjectInventory.getTypeVersion(doEntityClass) == null) {
        // only data objects with @TypeVersion annotation are relevant
        continue;
      }

      entities.add(processDoEntity(doEntityClass));
    }

    entities.sort(Comparator.comparing(EntityDataObjectSignatureDo::getTypeName));
    return entities;
  }

  /**
   * Collects all {@link IEnum} with their values that were referenced in any processed data object attribute that was
   * part of the signature. If the containing data object was only processed because it was referenced by another data
   * object part of the signature, the {@link IEnum} was not added to m_referencedEnumClasses because no relevant for
   * signature.
   */
  protected List<EnumApiSignatureDo> collectReferencedEnums() {
    List<EnumApiSignatureDo> enums = new ArrayList<>();
    for (Class<? extends IEnum> enumClass : m_referencedEnumClasses) {
      String enumName = m_enumInventory.toEnumName(enumClass);
      if (enumName == null) {
        continue; // skip those without an enum name because they were already reported as errors
      }

      List<String> enumStringValues = Stream.of(enumClass.getEnumConstants())
          .map(IEnum::stringValue)
          .sorted()
          .collect(Collectors.toList());

      enums.add(BEANS.get(EnumApiSignatureDo.class)
          .withEnumName(enumName)
          .withValues(enumStringValues));
    }

    enums.sort(Comparator.comparing(EnumApiSignatureDo::getEnumName));
    return enums;
  }

  protected boolean isMatchingPackageNamePrefix(Class<?> clazz) {
    String className = clazz.getName();
    for (String prefix : m_packageNamePrefixes) {
      if (className.startsWith(prefix)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Only data object with {@link TypeVersion} must be processed.
   *
   * @param doEntityClass
   *     Class of data entity having type name annotation (provided from {@link DataObjectInventory})
   */
  protected EntityDataObjectSignatureDo processDoEntity(Class<? extends IDoEntity> doEntityClass) {
    List<AttributeDataObjectSignatureDo> attributes = new LinkedList<>();
    for (Entry<String, DataObjectAttributeDescriptor> attributeEntry : m_dataObjectInventory.getAttributesDescription(doEntityClass).entrySet()) {
      AttributeDataObjectSignatureDo attribute = processAttribute(doEntityClass, attributeEntry.getValue());
      if (attribute != null) {
        attributes.add(attribute);
      }
    }
    attributes.sort(Comparator.comparing(AttributeDataObjectSignatureDo::getName));

    EntityDataObjectSignatureDo entity = BEANS.get(EntityDataObjectSignatureDo.class)
        .withTypeName(m_dataObjectInventory.toTypeName(doEntityClass))
        .withTypeVersion(m_dataObjectInventory.getTypeVersion(doEntityClass).unwrap())
        .withAttributes(attributes);

    if (doEntityClass.isAnnotationPresent(Replace.class)) {
      @SuppressWarnings("unchecked")
      Class<? extends IDoEntity> replacedClass = (Class<? extends IDoEntity>) doEntityClass.getSuperclass();
      NamespaceVersion parentTypeVersion = m_dataObjectInventory.getTypeVersion(replacedClass);
      if (parentTypeVersion != null) {
        // Only set when available to omit node if data object doesn't replace another data object
        entity.withParentTypeVersion(parentTypeVersion.unwrap());
      }
    }

    return entity;
  }

  protected AttributeDataObjectSignatureDo processAttribute(Class<? extends IDoEntity> doEntityClass, DataObjectAttributeDescriptor attributeDescriptor) {
    ParameterizedType attributeType = attributeDescriptor.getType();

    if (!(attributeType.getRawType() instanceof Class)) {
      m_errors.add(String.format("Raw type must be a class (referenced in '%s')", getContextText(attributeDescriptor.getName(), doEntityClass)));
      return null;
    }

    Class<?> rawType = (Class<?>) attributeType.getRawType();

    Type type;
    boolean list;
    if (DoValue.class.equals(rawType)) {
      type = attributeType.getActualTypeArguments()[0];
      list = false;
    }
    else if (ObjectUtility.isOneOf(rawType, DoList.class, DoSet.class, DoCollection.class)) {
      // handling DoList, DoSet and DoCollection the same (no distinction) because serialized output is compatible (semantic changes only)
      type = attributeType.getActualTypeArguments()[0];
      list = true;
    }
    else {
      m_errors.add(String.format("Unknown attribute type '%s' referenced in '%s'", rawType.getName(), getContextText(attributeDescriptor.getName(), doEntityClass)));
      return null;
    }

    String valueType = type.getTypeName();
    if (m_attributePredicate == null || m_attributePredicate.test(doEntityClass, attributeDescriptor.getName())) {
      // Only process type if not excluded
      valueType = processType(attributeDescriptor.getName(), type, doEntityClass);
    }

    AttributeDataObjectSignatureDo attribute = BEANS.get(AttributeDataObjectSignatureDo.class)
        .withName(attributeDescriptor.getName())
        .withValueType(valueType);

    if (list) {
      attribute.withList(true);
    }

    if (attributeDescriptor.getFormatPattern().isPresent()) {
      attribute.withFormatPattern(attributeDescriptor.getFormatPattern().get());
    }

    return attribute;
  }

  protected String processType(String attributeName, Type type, Class<? extends IDoEntity> containingEntityClass) {
    if (type instanceof ParameterizedType) {
      return processParametrizedType(attributeName, (ParameterizedType) type, containingEntityClass);
    }
    else if (type instanceof WildcardType) {
      return processWildcardType(attributeName, (WildcardType) type, containingEntityClass);
    }
    else if (type instanceof Class) {
      return processClass(attributeName, (Class<?>) type, containingEntityClass);
    }
    else {
      m_errors.add(String.format("Unknown type '%s' referenced in '%s'", type, getContextText(attributeName, containingEntityClass)));
      return type.getTypeName();
    }
  }

  protected String processParametrizedType(String attributeName, ParameterizedType parameterizedType, Class<? extends IDoEntity> containingEntityClass) {
    String rawType = processType(attributeName, parameterizedType.getRawType(), containingEntityClass);
    List<String> genericParameters = new LinkedList<>();
    for (Type genericType : parameterizedType.getActualTypeArguments()) {
      genericParameters.add(processType(attributeName, genericType, containingEntityClass));
    }

    return box(VALUE_TYPE_CLASS_PREFIX, rawType) + "<" + StringUtility.join(", ", genericParameters) + ">";
  }

  /**
   * Example: DoValue&lt;TypedId&lt;? extends IUuId&gt;&gt;
   */
  protected String processWildcardType(String attributeName, WildcardType wildcardType, Class<? extends IDoEntity> containingEntityClass) {
    List<String> lowerBounds = new ArrayList<>();
    for (Type lowerBound : wildcardType.getLowerBounds()) {
      lowerBounds.add(processType(attributeName, lowerBound, containingEntityClass));
    }
    List<String> upperBunds = new ArrayList<>();
    for (Type upperBund : wildcardType.getUpperBounds()) {
      lowerBounds.add(processType(attributeName, upperBund, containingEntityClass));
    }

    String lowerBoundText = StringUtility.join(" & ", lowerBounds);
    String upperBoundText = StringUtility.join(" & ", upperBunds);
    return StringUtility.join(";", StringUtility.isNullOrEmpty(lowerBoundText) ? null : "? extends " + lowerBoundText, StringUtility.isNullOrEmpty(upperBoundText) ? null : "? super " + upperBoundText);
  }

  protected String processClass(String attributeName, Class<?> clazz, Class<? extends IDoEntity> containingEntityClass) {
    for (IDataObjectSignatureTestCustomizer c : m_customizers) {
      String errorMsg = c.validateAttributeType(attributeName, clazz, containingEntityClass);
      if (errorMsg != null) {
        m_errors.add(errorMsg);
      }
    }

    if (IEnum.class.isAssignableFrom(clazz)) {
      @SuppressWarnings("unchecked")
      Class<? extends IEnum> enumClass = (Class<? extends IEnum>) clazz;
      return processAttributeTypeEnum(attributeName, enumClass, containingEntityClass);
    }

    if (IId.class.isAssignableFrom(clazz)) {
      @SuppressWarnings("unchecked")
      Class<? extends IId> idClass = (Class<? extends IId>) clazz;
      return processAttributeTypeId(attributeName, idClass, containingEntityClass);
    }

    if (IDoEntity.class.isAssignableFrom(clazz)) {
      // It's a DoEntity, resolve type name as reference
      @SuppressWarnings("unchecked")
      Class<? extends IDoEntity> doEntityClass = (Class<? extends IDoEntity>) clazz;
      return processAttributeTypeDoEntity(attributeName, doEntityClass, containingEntityClass);
    }

    if (m_supportedAttributeTypes.contains(clazz)) {
      return box(VALUE_TYPE_CLASS_PREFIX, clazz.getName());
    }

    m_errors.add(String.format("Unsupported class type '%s' referenced in '%s' (check for missing dependencies on %s implementors)",
        clazz,
        getContextText(attributeName, containingEntityClass),
        IDataObjectSignatureTestCustomizer.class.getName()));

    return clazz.getName();
  }

  protected String processAttributeTypeEnum(String attributeName, Class<? extends IEnum> enumClass, Class<? extends IDoEntity> containingEntityClass) {
    if (isMatchingPackageNamePrefix(containingEntityClass)) {
      // only add as reference enum class if enum is referenced in an accepted class, i.e. from own module
      // We don't want to add signature for all enums, only for those that are referenced in one of the own data objects.
      m_referencedEnumClasses.add(enumClass);
    }

    String enumName = m_enumInventory.toEnumName(enumClass);
    if (enumName == null) {
      m_errors.add(String.format("Enum class '%s' is missing enum name (referenced in '%s')", enumClass.getName(), getContextText(attributeName, containingEntityClass)));
      enumName = enumClass.getName();
    }

    return box(VALUE_TYPE_ENUM_PREFIX, enumName);
  }

  protected String processAttributeTypeId(String attributeName, Class<? extends IId> idClass, Class<? extends IDoEntity> containingEntityClass) {
    if (Modifier.isAbstract(idClass.getModifiers()) || idClass.isInterface()) {
      return box(VALUE_TYPE_ID_INTERFACE_PREFIX, idClass.getName());
    }

    String idTypeName = resolveIdTypeName(idClass);
    if (idTypeName == null) {
      m_errors.add(String.format("IId class '%s' is missing id type name (referenced in '%s')", idClass.getName(), getContextText(attributeName, containingEntityClass)));
      idTypeName = idClass.getName();
    }

    return box(VALUE_TYPE_ID_PREFIX, idTypeName);
  }

  protected String resolveIdTypeName(Class<? extends IId> idClass) {
    return BEANS.get(IdInventory.class).getTypeName(idClass);
  }

  protected String processAttributeTypeDoEntity(String attributeName, Class<? extends IDoEntity> doEntityClass, Class<? extends IDoEntity> containingEntityClass) {
    if (Modifier.isAbstract(doEntityClass.getModifiers()) || doEntityClass.isInterface()) {
      // An interface or abstract class that is referenced within a data object node must have a TypeVersionRequired annotation.
      if (!m_classesWithTypeVersionRequired.contains(doEntityClass)) {
        m_errors.add(String.format("Interface/abstract class '%s' is missing @TypeVersionRequired (referenced in '%s')", doEntityClass, getContextText(attributeName, containingEntityClass)));
      }

      // Use FQN for interface/abstract class because there is not other identifier available.
      return box(VALUE_TYPE_DO_INTERFACE_PREFIX, doEntityClass.getName());
    }
    else {
      String typeName = m_dataObjectInventory.toTypeName(doEntityClass); // works for replaced classes too
      NamespaceVersion typeVersion = m_dataObjectInventory.getTypeVersion(doEntityClass); // if replaced, replaced data object must have it's own type version
      if (typeName == null) {
        // Check for type name is required to because there might be data objects only having a type version but not type name annotation
        m_errors.add(String.format("Data object '%s' is missing @TypeName (referenced in '%s')", doEntityClass, getContextText(attributeName, containingEntityClass)));
      }
      if (typeVersion == null) {
        // Check for type name is required to because there might be data objects only having a type version but not type name annotation
        m_errors.add(String.format("Data object '%s' is missing @TypeVersion (referenced in '%s')", doEntityClass, getContextText(attributeName, containingEntityClass)));
      }
      return box(VALUE_TYPE_DO_PREFIX, typeName);
    }
  }

  /**
   * A value type of an attribute ({@link AttributeDataObjectSignatureDo#getValueType()}) consists of one or more boxed
   * expressions. A boxed expression has a prefix (one of the VALUE_TYPE_ constants) and a value (depending on the
   * VALUE_TYPE_ constant, e.g. for {@link #VALUE_TYPE_DO_PREFIX} it's a type name, 'DO[example.Lorem]').
   * <p>
   * The value type is built that way so that when comparing signatures a value type can be normalized, meaning that
   * renamings can be applied to allow a more detailed comparison.
   *
   * @see #VALUE_TYPE_PATTERN
   */
  protected static String box(String valueTypePrefix, String name) {
    return valueTypePrefix + "[" + name + "]";
  }

  protected String getContextText(String attributeName, Class<? extends IDoEntity> containingEntityClass) {
    return m_dataObjectInventory.toTypeName(containingEntityClass) + "#" + attributeName;
  }
}
