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

import static org.eclipse.scout.rt.platform.util.Assertions.*;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.scout.rt.dataobject.AttributeName;
import org.eclipse.scout.rt.dataobject.DataObjectAttributeDescriptor;
import org.eclipse.scout.rt.dataobject.DataObjectInventory;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoNode;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.IPrettyPrintDataObjectMapper;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.dataobject.testing.TestingResourceHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

/**
 * Abstract test implementation for generating data object signatures for each module.
 * <p>
 * The test will output a detailed list of changed data objects. Review the changes and act accordingly (e.g. add
 * migrations) before committing an updated signature file.
 * <p>
 * Due to the importance of a proper review the test is not self-healing, i.e. does not overwrite the existing signature
 * file. Instead, a new file is created which should, after all test output is considered, be the new signature file.
 * <p>
 * Each module containing data objects with {@link TypeVersion} annotation must subclass this test.
 */
@RunWith(PlatformTestRunner.class)
public abstract class AbstractDataObjectSignatureTest {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractDataObjectSignatureTest.class);

  @Rule
  public ErrorCollector m_errorCollector = new ErrorCollector();

  /**
   * @return Filename prefix of signature file.
   */
  protected abstract String getFilenamePrefix();

  /**
   * Only data object with matching package name prefixes are added to signature.
   */
  protected abstract String getPackageNamePrefix();

  /**
   * @return Full filename of signature file.
   */
  protected String getFilename() {
    return getFilenamePrefix() + "-dataobject-signature.json";
  }

  protected Class<?> getResourceBaseClass() {
    return this.getClass();
  }

  /**
   * Allows to filter for certain data object classes, e.g. exclude data objects from test packages for processing
   *
   * @return <code>true</code> if the provided data object class should be processed for signature generation,
   * <code>false</code> otherwise.
   */
  protected boolean acceptDataObject(Class<? extends IDoEntity> dataObjectClass) {
    return true;
  }

  /**
   * Allows to filter for certain data object attributes, e.g. exclude a certain attribute of a specific data object
   * from further processing because verified separately.
   *
   * @return <code>true</code> if the provided data object attribute should be processed for signature generation,
   * <code>false</code> otherwise.
   */
  protected boolean acceptAttribute(Class<? extends IDoEntity> dataObjectClass, String attributeName) {
    return true;
  }

  /**
   * Method may be overridden temporary to add type name, enum names, type id and class name renamings to provide a more
   * detailed comparison.
   *
   * @see DataObjectSignatureComparator#addTypeNameRenaming(String, String)
   * @see DataObjectSignatureComparator#addEnumNameRenaming(String, String)
   * @see DataObjectSignatureComparator#addTypeIdRenaming(String, String)
   * @see DataObjectSignatureComparator#addClassNameRenaming(String, String)
   */
  protected void addRenamings(DataObjectSignatureComparator comparator) {
  }

  @Test
  public void testStructure() {
    DataObjectSignatureDo signature = BEANS.get(DataObjectSignatureGenerator.class).createSignature(CollectionUtility.hashSet(getPackageNamePrefix()), this::acceptDataObject, this::acceptAttribute);
    compareSignatures(signature);
  }

  protected void compareSignatures(DataObjectSignatureDo currentSignature) {
    IDataObjectMapper dataObjectMapper = BEANS.get(IPrettyPrintDataObjectMapper.class);

    File referenceFile = new File(BEANS.get(TestingResourceHelper.class).getTestResourceDirectory(getResourceBaseClass()), getFilename());
    if (!referenceFile.exists()) {
      writeCurrentSignature(currentSignature);
      fail("No previous signature file available. Initial signature file was created");
    }

    // Read previous signature file for comparison
    DataObjectSignatureDo previousSignature = null;
    try (FileInputStream fis = new FileInputStream(referenceFile)) {
      previousSignature = dataObjectMapper.readValue(fis, DataObjectSignatureDo.class);
    }
    catch (IOException | RuntimeException e) { // runtime exception possible due to data object mapper
      writeCurrentSignature(currentSignature);
      LOG.warn("Failed to read previous signature file", e);
      fail("Failed to read previous signature file. Message=" + e.getMessage());
    }

    DataObjectSignatureComparator comparator = BEANS.get(DataObjectSignatureComparator.class);
    addRenamings(comparator);
    comparator.compare(previousSignature, currentSignature);

    // This is more a sanity check. If the data objects are not equal but there are no differences detected by the comparator, the comparator doesn't detect all differences yet.
    boolean differentDataObjects = !ObjectUtility.equals(previousSignature, currentSignature);
    if (differentDataObjects || !comparator.getDifferences().isEmpty()) {
      writeCurrentSignature(currentSignature);
      String details = comparator.getDifferences().isEmpty() ? "Comparator was unable to detect the differences, please review file changes manually." : StringUtility.join("\n", comparator.getDifferences());
      fail("Review all signature differences and create corresponding migrations if necessary before committing any changes in file " + getFilename() + ":\n" + details);
    }
  }

  protected void writeCurrentSignature(DataObjectSignatureDo signature) {
    // Test by default creates a new file to prevent accidental self-healing of test.
    // This behavior can be changed by setting the appropriate system property (not recommended).
    String filename;
    if (System.getProperty("dataObjectSignatureTest.overwriteReferenceFile") != null) {
      filename = getFilename();
    }
    else {
      filename = FileUtility.getFilenameParts(getFilename())[0] + "-to-be-reviewed.json";
    }
    BEANS.get(TestingResourceHelper.class).writeTestResource(getResourceBaseClass(), filename, signature);
  }

  /**
   * Validates all registered {@link DoEntity instances.
   */
  @Test
  public void validateDataObjects() {
    Map<String, Class<? extends IDoEntity>> typeNameToClassMap = BEANS.get(DataObjectInventory.class).getTypeNameToClassMap();
    typeNameToClassMap.values().stream()
        .filter(this::acceptDataObject)
        .forEach(this::validateDataObject);
  }

  protected void validateDataObject(Class<? extends IDoEntity> clazz) {
    LOG.debug("Validate DoEntity class {}", clazz);
    validateTypeName(clazz);
    validateClassName(clazz);

    if (clazz.isAnnotationPresent(IgnoreBean.class)) {
      LOG.debug("Skip validating DoEntity class {} with {} annotation", clazz, IgnoreBean.class.getSimpleName());
      return;
    }

    IDoEntity instance = BEANS.get(clazz);
    Map<String, DataObjectAttributeDescriptor> attributesDescriptions = BEANS.get(DataObjectInventory.class).getAttributesDescription(clazz);
    for (Entry<String, DataObjectAttributeDescriptor> entry : attributesDescriptions.entrySet()) {
      LOG.debug("Validate DoEntity attribute {} of class {}", entry.getKey(), clazz);
      validateAttribute(instance, entry.getKey(), entry.getValue());
    }
  }

  /**
   * Validates type name against class name (type name should be "namespace." + class name without 'Do' suffix
   */
  protected void validateTypeName(Class<? extends IDoEntity> clazz) {
    String typeName = BEANS.get(DataObjectInventory.class).toTypeName(clazz);
    if (typeName == null) {
      addError("Missing type name in data object, check class {}", clazz.getName());
      return;
    }
    else if (typeName.contains(".")) {
      typeName = typeName.substring(typeName.lastIndexOf('.') + 1);
    }
    else {
      addError("Missing namespace in data object type name '{}', check class {}", typeName, clazz.getName());
    }
    String expected = StringUtility.removeSuffixes(clazz.getSimpleName(), "Do");
    if (!expected.equals(typeName)) {
      addError("Wrong data object type name, expected='{}' actual='{}', check class {}", expected, typeName, clazz.getName());
    }
  }

  /**
   * Validates class name against a set of allowed suffixes.
   */
  protected void validateClassName(Class<? extends IDoEntity> clazz) {
    String name = clazz.getSimpleName();
    if (getAllowedDataObjectSuffixes().stream().noneMatch(name::endsWith)) {
      addError("Wrong data object class name, data objects should end with suffix {}, check class {}", getAllowedDataObjectSuffixes(), clazz.getName());
    }
  }

  protected Set<String> getAllowedDataObjectSuffixes() {
    return CollectionUtility.hashSet("Do", "Request", "Response", "Include");
  }

  protected void validateAttribute(IDoEntity instance, String attributeName, DataObjectAttributeDescriptor descriptor) {
    Method attributeAccessor = descriptor.getAccessor();
    try {
      DoNode<?> node = (DoNode<?>) attributeAccessor.invoke(instance);
      if (node == null) {
        addError("accessor method returns null instead of a DoNode. Class={}, attributeName={}, accessorMethodName={}",
            instance.getClass(),
            attributeName,
            attributeAccessor.getName());
        return;
      }
      if (ObjectUtility.notEquals(attributeName, node.getAttributeName())) {
        addError("Name of attribute does not match name of accessor method or @AttributeName annotation value. Class={}, attributeName={}, accessorMethodName={}, annotationValue={}",
            instance.getClass(),
            node.getAttributeName(),
            attributeAccessor.getName(),
            getAttributeNameAnnotationValue(attributeAccessor));
      }

      validateAttributeType(instance, attributeName, descriptor);
    }
    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e);
    }
  }

  protected void validateAttributeType(IDoEntity instance, String attributeName, DataObjectAttributeDescriptor descriptor) {
    // check for Map types using data object as map key
    if (descriptor.getType().getActualTypeArguments()[0] instanceof ParameterizedType parameterizedType) {
      if (parameterizedType.getRawType() == Map.class) {
        Type mapKeyType = parameterizedType.getActualTypeArguments()[0];
        if (DoEntity.class.isAssignableFrom((Class) mapKeyType) && mapKeyType != Object.class) {
          addError("Using DoEntity as key of a map is not supported within data object. Class={}, attributeName={}, type={}", instance.getClass(), attributeName, parameterizedType);
        }
      }
    }
  }

  protected String getAttributeNameAnnotationValue(Method attributeAccessor) {
    AttributeName annotation = attributeAccessor.getAnnotation(AttributeName.class);
    if (annotation != null) {
      return annotation.value();
    }
    return null;
  }

  protected void addError(String message, Object... args) {
    m_errorCollector.addError(new AssertionError(MessageFormatter.arrayFormat(message, args).getMessage()));
  }

  /**
   * Validates that basic DoEntity class was not replaced or changed.
   */
  @Test
  public void validateDoEntityNotReplaced() {
    Class<?> doEntityClass = BEANS.get(DoEntity.class).getClass();
    assertTrue(DoEntity.class == doEntityClass, "DoEntity class should not be replaced by {}", doEntityClass);
    assertNull(BEANS.get(DataObjectInventory.class).toTypeName(DoEntity.class), "TypeName for DoEntity should be null");
  }
}
