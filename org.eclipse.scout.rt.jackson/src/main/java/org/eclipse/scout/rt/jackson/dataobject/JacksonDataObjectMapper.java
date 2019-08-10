/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.IValueFormatConstants;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
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
  public ObjectMapper getObjectMapper() {
    return m_objectMapper.get();
  }

  /**
   * Creates new {@link ObjectMapper} instance configured to be used with {@link IDoEntity}.
   */
  protected ObjectMapper createObjectMapperInstance(boolean ignoreTypeAttribute) {
    ObjectMapper om = new ObjectMapper();
    om.registerModule(BEANS.get(ScoutDataObjectModule.class).withIgnoreTypeAttribute(ignoreTypeAttribute));
    om.setDateFormat(new SimpleDateFormat(IValueFormatConstants.DEFAULT_DATE_PATTERN)); // FIXME [9.0] pbz: [JSON] check if it can be moved to ScoutDataObjectModule class
    return om;
  }
}
