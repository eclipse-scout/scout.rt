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

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.enumeration.EnumName;
import org.eclipse.scout.rt.dataobject.id.IdTypeName;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

/**
 * Stateful bean that compares two {@link DataObjectSignatureDo} with each other.
 * <p>
 * For a more detailed comparison a developer might add known renamings (e.g. data object with type name 'lorem.A' was
 * renamed to 'lorem.B'), allowing to compare attributes within a renamed entity.
 * <p>
 * It doesn't implement the {@link Comparator} interface.
 */
@Bean
public class DataObjectSignatureComparator {

  protected final List<String> m_differences = new ArrayList<>();

  protected Map<String, String> m_typeNameRenamings = new HashMap<>();
  protected Map<String, String> m_enumNameRenamings = new HashMap<>();
  protected Map<String, String> m_typeIdRenamings = new HashMap<>();
  protected Map<String, String> m_classNameRenamings = new HashMap<>();

  protected DataObjectSignatureDo m_previousSignature;
  protected DataObjectSignatureDo m_currentSignature;

  /**
   * @param oldTypeName
   *     Old {@link TypeName}
   * @param newTypeName
   *     New {@link TypeName}
   */
  public void addTypeNameRenaming(String oldTypeName, String newTypeName) {
    m_typeNameRenamings.put(oldTypeName, newTypeName);
  }

  /**
   * @param oldEnumName
   *     Old {@link EnumName}
   * @param newEnumName
   *     New {@link EnumName}
   */
  public void addEnumNameRenaming(String oldEnumName, String newEnumName) {
    m_enumNameRenamings.put(oldEnumName, newEnumName);
  }

  /**
   * @param oldTypeId
   *     Old {@link IdTypeName}
   * @param newTypeId
   *     New {@link IdTypeName}
   */
  public void addTypeIdRenaming(String oldTypeId, String newTypeId) {
    m_typeIdRenamings.put(oldTypeId, newTypeId);
  }

  /**
   * @param oldClassName
   *     Old fully qualified class name
   * @param newClassName
   *     New fully qualified class name
   */
  public void addClassNameRenaming(String oldClassName, String newClassName) {
    m_classNameRenamings.put(oldClassName, newClassName);
  }

  public List<String> getDifferences() {
    return m_differences;
  }

  public void compare(DataObjectSignatureDo previousSignature, DataObjectSignatureDo currentSignature) {
    m_previousSignature = assertNotNull(previousSignature, "previousSignature is required");
    m_currentSignature = assertNotNull(currentSignature, "currentSignature is required");

    m_differences.clear();
    compareEntities();
    compareEnums();
  }

  protected void compareEntities() {
    Map<String, EntityDataObjectSignatureDo> currentEntities = m_currentSignature.getEntities().stream()
        .collect(Collectors.toMap(EntityDataObjectSignatureDo::getTypeName, Function.identity()));

    Set<String> remainingCurrentEntities = new HashSet<>(currentEntities.keySet());

    for (EntityDataObjectSignatureDo previousEntity : m_previousSignature.getEntities()) {
      NamespaceVersion previousTypeVersionValue = NamespaceVersion.of(previousEntity.getTypeVersion());
      String previousTypeName = previousEntity.getTypeName();
      String currentTypeName = m_typeNameRenamings.getOrDefault(previousTypeName, previousTypeName);

      if (!currentEntities.containsKey(currentTypeName)) {
        m_differences.add(String.format("[ACTION] DO Entity '%s' is missing in new version", currentTypeName));
        continue;
      }

      if (ObjectUtility.notEquals(previousTypeName, currentTypeName)) {
        // Add difference due to type name renaming
        m_differences.add(String.format("[ACTION] DO Entity '%s' has a new type name '%s'", previousTypeName, currentTypeName));
      }

      remainingCurrentEntities.remove(currentTypeName);

      EntityDataObjectSignatureDo currentEntity = currentEntities.get(currentTypeName);

      compareAttributes(previousEntity, currentEntity);

      if (ObjectUtility.notEquals(previousEntity.getTypeVersion(), currentEntity.getTypeVersion())) {
        NamespaceVersion currentTypeVersionValue = NamespaceVersion.of(currentEntity.getTypeVersion());
        if (ObjectUtility.equals(previousTypeVersionValue.getNamespace(), currentTypeVersionValue.getNamespace())) {
          // Same names
          if (NamespaceVersion.compareVersion(previousTypeVersionValue, currentTypeVersionValue) < 0) {
            // Common case
            m_differences.add(String.format("[ACTION] DO Entity '%s' has a higher type version than before. Old: '%s', new: '%s'", currentTypeName, previousEntity.getTypeVersion(), currentEntity.getTypeVersion()));
          }
          else {
            // Invalid, new type version must always be higher than old type version.
            m_differences.add(String.format("[INVALID] DO Entity '%s' has a lower type version than before. Old: '%s', new: '%s'", currentTypeName, previousEntity.getTypeVersion(), currentEntity.getTypeVersion()));
          }
        }
        else {
          // Different names, versions are not comparable (rare case that type version name changes)
          m_differences.add(String.format("[ACTION] DO Entity '%s' has a changed type version namespace. Old: '%s', new: '%s'", currentTypeName, previousEntity.getTypeVersion(), currentEntity.getTypeVersion()));
        }
      }

      if (ObjectUtility.notEquals(previousEntity.getParentTypeVersion(), currentEntity.getParentTypeVersion())) {
        // Requires to apply migration from replaced data object to own data object
        m_differences.add(String.format("[ACTION] DO Entity '%s' has a changed parent type version. Old: '%s', new: '%s'", currentTypeName, previousEntity.getParentTypeVersion(), currentEntity.getParentTypeVersion()));
      }
    }

    remainingCurrentEntities.forEach(typeName -> m_differences.add(String.format("[VERIFY] New DO entity '%s' available", typeName)));
  }

  protected void compareAttributes(EntityDataObjectSignatureDo previousEntity, EntityDataObjectSignatureDo currentEntity) {
    String currentTypeName = currentEntity.getTypeName();

    Map<String, AttributeDataObjectSignatureDo> currentAttributes = currentEntity.getAttributes().stream()
        .collect(Collectors.toMap(AttributeDataObjectSignatureDo::getName, Function.identity()));

    Set<String> processedAttributeNames = new HashSet<>();
    for (AttributeDataObjectSignatureDo previousAttribute : previousEntity.getAttributes()) {
      String previousAttributeName = previousAttribute.getName();
      if (!currentAttributes.containsKey(previousAttributeName)) {
        m_differences.add(String.format("[ACTION] DO Entity '%s' is missing attribute '%s' in new version", currentTypeName, previousAttributeName));
        continue;
      }

      processedAttributeNames.add(previousAttributeName);

      AttributeDataObjectSignatureDo currentAttribute = currentAttributes.get(previousAttributeName);
      compareAttribute(previousAttribute, currentAttribute, currentTypeName);
    }

    for (AttributeDataObjectSignatureDo currentAttribute : currentEntity.getAttributes()) {
      String currentAttributeName = currentAttribute.getName();
      if (!processedAttributeNames.contains(currentAttributeName)) {
        m_differences.add(String.format("[VERIFY] DO Entity '%s' has new attribute '%s'", currentTypeName, currentAttributeName));
      }
    }
  }

  protected void compareAttribute(AttributeDataObjectSignatureDo previousAttribute, AttributeDataObjectSignatureDo currentAttribute, String currentTypeName) {
    String currentAttributeName = currentAttribute.getName();

    boolean previousList = previousAttribute.isList();
    boolean currentList = currentAttribute.isList();
    if (previousList != currentList) {
      m_differences.add(String.format("[ACTION] DO Entity '%s' attribute '%s' has change list/non-list. Old: '%s', new: '%s'", currentTypeName, currentAttributeName, previousList, currentList));
    }

    String previousFormatPattern = previousAttribute.getFormatPattern();
    String currentFormatPattern = currentAttribute.getFormatPattern();
    if (ObjectUtility.notEquals(previousFormatPattern, currentFormatPattern)) {
      m_differences.add(String.format("[ACTION] DO Entity '%s' attribute '%s' has a different format pattern. Old: '%s', new: '%s'", currentTypeName, currentAttributeName, previousFormatPattern, currentFormatPattern));
    }

    String previousValueType = previousAttribute.getValueType();
    String normalizedPreviousValueType = normalizeValueType(previousAttribute.getValueType());
    String currentValueType = currentAttribute.getValueType();
    if (ObjectUtility.notEquals(normalizedPreviousValueType, currentValueType)) {
      // Comparison with normalized value type failed
      m_differences.add(String.format("[ACTION] DO Entity '%s' attribute '%s' has a different type. Old: '%s', new: '%s'", currentTypeName, currentAttributeName, normalizedPreviousValueType, currentValueType));
    }
    else if (ObjectUtility.notEquals(previousValueType, currentValueType)) {
      // Comparison with value type failed but comparison with normalized value type was a success
      // -> renamings were added, no need to do something -> INFO
      m_differences.add(String.format("[VERIFY] DO Entity '%s' attribute '%s' has changed type due to applied renamings. Old: '%s', new: '%s'", currentTypeName, currentAttributeName, previousValueType, currentValueType));
    }
  }

  protected void compareEnums() {
    Map<String, EnumApiSignatureDo> currentEnums = m_currentSignature.getEnums().stream()
        .collect(Collectors.toMap(EnumApiSignatureDo::getEnumName, Function.identity()));

    Set<String> remainingCurrentEnums = new HashSet<>(currentEnums.keySet());

    for (EnumApiSignatureDo previousEnum : m_previousSignature.getEnums()) {
      String previousEnumName = previousEnum.getEnumName();

      String currentEnumName = m_enumNameRenamings.getOrDefault(previousEnumName, previousEnumName);
      if (!currentEnums.containsKey(currentEnumName)) {
        m_differences.add(String.format("[ACTION] Enum '%s' is missing in new version", currentEnumName));
        continue;
      }

      // As compared to type name renaming, there is no need to add a difference for a renamed enum name (based on m_enumNameRenamings)
      // because the enum was referenced in at least one attribute and a difference was already inserted.

      remainingCurrentEnums.remove(currentEnumName);

      EnumApiSignatureDo currentEnum = currentEnums.get(currentEnumName);

      Set<String> newEnumValues = new HashSet<>(currentEnum.getValues());
      previousEnum.getValues().forEach(newEnumValues::remove);
      newEnumValues.forEach(enumValue -> m_differences.add(String.format("[VERIFY] Enum '%s' has new enum value '%s'", currentEnumName, enumValue)));

      Set<String> previousEnumValues = new HashSet<>(previousEnum.getValues());
      currentEnum.getValues().forEach(previousEnumValues::remove);

      previousEnumValues.forEach(enumValue -> m_differences.add(String.format("[ACTION] Enum '%s' is missing value '%s' in new version", currentEnumName, enumValue)));
    }

    remainingCurrentEnums.forEach(enumName -> m_differences.add(String.format("[VERIFY] New enum '%s' available", enumName)));
  }

  /**
   * Apply renamings to {@link AttributeDataObjectSignatureDo#getValueType()} because it references type names, enum
   * names, id type names and class names that may have been renamed.
   */
  protected String normalizeValueType(String valueType) {
    Matcher matcher = DataObjectSignatureGenerator.VALUE_TYPE_PATTERN.matcher(valueType);

    StringBuilder buffer = new StringBuilder();
    while (matcher.find()) {
      String type = matcher.group(1);
      String name = matcher.group(2);

      switch (type) {
        case DataObjectSignatureGenerator.VALUE_TYPE_DO_PREFIX:
          name = m_typeNameRenamings.getOrDefault(name, name);
          break;
        case DataObjectSignatureGenerator.VALUE_TYPE_ENUM_PREFIX:
          name = m_enumNameRenamings.getOrDefault(name, name);
          break;
        case DataObjectSignatureGenerator.VALUE_TYPE_ID_PREFIX:
          name = m_typeIdRenamings.getOrDefault(name, name);
          break;
        case DataObjectSignatureGenerator.VALUE_TYPE_DO_INTERFACE_PREFIX:
        case DataObjectSignatureGenerator.VALUE_TYPE_CLASS_PREFIX:
        case DataObjectSignatureGenerator.VALUE_TYPE_ID_INTERFACE_PREFIX:
          name = m_classNameRenamings.getOrDefault(name, name);
          break;
      }

      String replacement = DataObjectSignatureGenerator.box(type, name);
      matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(buffer);

    return buffer.toString();
  }
}
