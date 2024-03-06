/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.IValueFormatConstants;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.ConfigUtility;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.StreamWriteConstraints;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@link IDataObjectMapper} implementation based on Jackson databind {@link ObjectMapper}.
 */
@ApplicationScoped
public class JacksonDataObjectMapper implements IDataObjectMapper {

  private final LazyValue<ObjectMapper> m_objectMapper = new LazyValue<>(() -> createObjectMapperInstance(false));
  private final LazyValue<ObjectMapper> m_rawObjectMapper = new LazyValue<>(() -> createObjectMapperInstance(true));

  @Override
  public <T> T readValue(InputStream inputStream, Class<T> valueType) {
    Assertions.assertNotNull(inputStream, "Input stream must not be null");
    try {
      return m_objectMapper.get().readValue(inputStream, valueType);
    }
    catch (IOException e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public <T> T readValue(String value, Class<T> valueType) {
    if (value == null) {
      return null;
    }
    try {
      return m_objectMapper.get().readValue(value, valueType);
    }
    catch (IOException e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public IDataObject readValueRaw(InputStream inputStream) {
    Assertions.assertNotNull(inputStream, "Input stream must not be null");
    try {
      return m_rawObjectMapper.get().readValue(inputStream, IDataObject.class); // use IDataObject as fixed valueType
    }
    catch (IOException e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public IDataObject readValueRaw(String value) {
    if (value == null) {
      return null;
    }
    try {
      return m_rawObjectMapper.get().readValue(value, IDataObject.class); // use IDataObject as fixed valueType
    }
    catch (IOException e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public void writeValue(OutputStream outputStream, Object value) {
    Assertions.assertNotNull(outputStream, "Output stream must not be null");
    if (value == null) {
      return;
    }
    try {
      m_objectMapper.get().writeValue(outputStream, value);
    }
    catch (IOException e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public String writeValue(Object value) {
    if (value == null) {
      return null;
    }
    try {
      return m_objectMapper.get().writeValueAsString(value);
    }
    catch (JsonProcessingException e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e);
    }
  }

  /**
   * <b>Note</b> This method is exposed only for internal framework usage. It is recommended to use the
   * {@link IDataObjectMapper} representation of the object mapper and not to use {@link ObjectMapper} instances
   * directly in code.
   *
   * @deprecated Use Scout data object mapper instead, see BEANS.get(IDataObjectMapper.class)
   */
  @Deprecated
  @SuppressWarnings("DeprecatedIsStillUsed")
  public ObjectMapper getObjectMapper() {
    return m_objectMapper.get();
  }

  /**
   * Creates new {@link ObjectMapper} instance configured to be used with {@link IDoEntity}.
   */
  protected ObjectMapper createObjectMapperInstance(boolean ignoreTypeAttribute) {
    // setup custom-configured JsonFactory used for ObjectMapper
    JsonFactory jsonFactory = JsonFactory.builder()
        .streamReadConstraints(CONFIG.getPropertyValue(StreamReadConstraintsConfigProperty.class))
        .streamWriteConstraints(CONFIG.getPropertyValue(StreamWriteConstraintsConfigProperty.class))
        .build();
    ObjectMapper om = new ObjectMapper(jsonFactory);
    ScoutDataObjectModule scoutDataObjectModule = BEANS.get(ScoutDataObjectModule.class).withIgnoreTypeAttribute(ignoreTypeAttribute);
    prepareScoutDataModuleContext(scoutDataObjectModule.getModuleContext());
    om.registerModule(scoutDataObjectModule);
    om.setDateFormat(new SimpleDateFormat(IValueFormatConstants.DEFAULT_DATE_PATTERN)); // TODO [23.0] pbz: [JSON] check if it can be moved to ScoutDataObjectModule class
    om.deactivateDefaultTyping(); // disabled for security reasons
    //noinspection deprecation
    om.enable(MapperFeature.BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES); // enabled block-unsafe for security reasons
    return om;
  }

  /**
   * Override this method to add custom properties to {@code moduleContext}.
   */
  protected void prepareScoutDataModuleContext(ScoutDataObjectModuleContext moduleContext) {
    // nop
  }

  /**
   * {@link StreamReadConstraints} for {@link JsonFactory}.
   */
  public static class StreamReadConstraintsConfigProperty extends AbstractConfigProperty<StreamReadConstraints, Map<String, String>> {

    static final String MAX_NESTING_DEPTH = "maxNestingDepth";
    static final String MAX_DOCUMENT_LENGTH = "maxDocumentLength";
    static final String MAX_NAME_LENGTH = "maxNameLength";
    static final String MAX_NUMBER_LENGTH = "maxNumberLength";
    static final String MAX_STRING_LENGTH = "maxStringLength";

    /**
     * Default setting for maximum string length is 100 MB.<br>
     * Jackson default value is {@link StreamReadConstraints#DEFAULT_MAX_STRING_LEN}.
     */
    public static final int DEFAULT_MAX_STRING_LEN = 100_000_000;

    @Override
    public String getKey() {
      return "scout.dataobject.jackson.streamReadConstraints";
    }

    @Override
    public String description() {
      return String.format("Jackson constraints to use for JSON reading.\n"
              + "Map property with the keys as follows:\n"
              + "- %s: Sets the maximum nesting depth. The depth is a count of objects and arrays that have not been closed, `{` and `[` respectively. (default: %d)\n"
              + "- %s: Sets the maximum allowed document length (for positive values over 0) or indicate that any length is acceptable (0 or negative number). The length is in input units of the input source, that is, in bytes or chars. (default: %d)\n"
              + "- %s: Sets the maximum name length (in chars or bytes, depending on input context). (default: %d)\n"
              + "- %s: Sets the maximum number length (in chars or bytes, depending on input context). (default: %d)\n"
              + "- %s: Sets the maximum string length for a single attribute value of type text (in chars or bytes, depending on input context). (default: %d)\n",
          MAX_NESTING_DEPTH, StreamReadConstraints.DEFAULT_MAX_DEPTH,
          MAX_DOCUMENT_LENGTH, StreamReadConstraints.DEFAULT_MAX_DOC_LEN,
          MAX_NAME_LENGTH, StreamReadConstraints.DEFAULT_MAX_NAME_LEN,
          MAX_NUMBER_LENGTH, StreamReadConstraints.DEFAULT_MAX_NUM_LEN,
          MAX_STRING_LENGTH, DEFAULT_MAX_STRING_LEN);
    }

    @Override
    public Map<String, String> readFromSource(String namespace) {
      return ConfigUtility.getPropertyMap(getKey(), null, namespace);
    }

    @Override
    public StreamReadConstraints getDefaultValue() {
      return parse(Collections.emptyMap()); // defaults are on a per key base
    }

    @Override
    protected StreamReadConstraints parse(Map<String, String> value) {
      Set<String> invalidMapKeys = new HashSet<>(value.keySet());
      Arrays.asList(MAX_NESTING_DEPTH, MAX_DOCUMENT_LENGTH, MAX_NAME_LENGTH, MAX_NUMBER_LENGTH, MAX_STRING_LENGTH).forEach(invalidMapKeys::remove);
      if (!invalidMapKeys.isEmpty()) {
        throw new PlatformException("Invalid values for map property {}: {}", getKey(), invalidMapKeys);
      }
      return StreamReadConstraints.builder()
          .maxNestingDepth(ObjectUtility.nvl(TypeCastUtility.castValue(value.get(MAX_NESTING_DEPTH), Integer.class), StreamReadConstraints.DEFAULT_MAX_DEPTH))
          .maxDocumentLength(ObjectUtility.nvl(TypeCastUtility.castValue(value.get(MAX_DOCUMENT_LENGTH), Long.class), StreamReadConstraints.DEFAULT_MAX_DOC_LEN))
          .maxNameLength(ObjectUtility.nvl(TypeCastUtility.castValue(value.get(MAX_NAME_LENGTH), Integer.class), StreamReadConstraints.DEFAULT_MAX_NAME_LEN))
          .maxNumberLength(ObjectUtility.nvl(TypeCastUtility.castValue(value.get(MAX_NUMBER_LENGTH), Integer.class), StreamReadConstraints.DEFAULT_MAX_NUM_LEN))
          .maxStringLength(ObjectUtility.nvl(TypeCastUtility.castValue(value.get(MAX_STRING_LENGTH), Integer.class), DEFAULT_MAX_STRING_LEN))
          .build();
    }
  }

  /**
   * {@link StreamReadConstraints} for {@link JsonFactory}.
   */
  public static class StreamWriteConstraintsConfigProperty extends AbstractConfigProperty<StreamWriteConstraints, Map<String, String>> {

    private static final String MAX_NESTING_DEPTH = "maxNestingDepth";

    @Override
    public String getKey() {
      return "scout.dataobject.jackson.streamWriteConstraints";
    }

    @Override
    public String description() {
      return String.format("Jackson constraints to use for JSON writing.\n"
              + "Map property with the keys as follows:\n"
              + "- %s: Sets the maximum nesting depth. The depth is a count of objects and arrays that have not been closed, `{` and `[` respectively. (default: %d)\n",
          MAX_NESTING_DEPTH, StreamWriteConstraints.DEFAULT_MAX_DEPTH);
    }

    @Override
    public Map<String, String> readFromSource(String namespace) {
      return ConfigUtility.getPropertyMap(getKey(), null, namespace);
    }

    @Override
    public StreamWriteConstraints getDefaultValue() {
      return parse(Collections.emptyMap()); // defaults are on a per key base
    }

    @Override
    protected StreamWriteConstraints parse(Map<String, String> value) {
      Set<String> invalidMapKeys = new HashSet<>(value.keySet());
      Arrays.asList(MAX_NESTING_DEPTH).forEach(invalidMapKeys::remove);
      if (!invalidMapKeys.isEmpty()) {
        throw new PlatformException("Invalid values for map property {}: {}", getKey(), invalidMapKeys);
      }
      return StreamWriteConstraints.builder()
          .maxNestingDepth(ObjectUtility.nvl(TypeCastUtility.castValue(value.get(MAX_NESTING_DEPTH), Integer.class), StreamWriteConstraints.DEFAULT_MAX_DEPTH))
          .build();
    }
  }
}
