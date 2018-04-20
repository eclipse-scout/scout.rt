/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.platform.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.dataobject.IValueFormatConstants;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.LazyValue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@link IDataObjectMapper} implementation based on Jackson databind {@link ObjectMapper}.
 */
@ApplicationScoped
public class JacksonDataObjectMapper implements IDataObjectMapper {

  private final LazyValue<ObjectMapper> m_objectMapper = new LazyValue<>(() -> createObjectMapperInstance());

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
  public ObjectMapper getObjectMapper() {
    return m_objectMapper.get();
  }

  /**
   * Creates new {@link ObjectMapper} instance configured to be used with {@link IDoEntity}.
   */
  protected ObjectMapper createObjectMapperInstance() {
    ObjectMapper om = new ObjectMapper();
    om.registerModule(BEANS.get(ScoutDataObjectModule.class));
    om.setDateFormat(new SimpleDateFormat(IValueFormatConstants.DEFAULT_DATE_PATTERN)); // FIXME [16.0] pbz: [JSON] check if it can be moved to ScoutDataObjectModule
    return om;
  }
}
