/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.testing;

import org.eclipse.scout.rt.jackson.dataobject.JacksonDataObjectMapper;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.testing.shared.TestingUtility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * {@link IDataObjectMapper} implementation based on jackson {@link ObjectMapper} with output indentation enabled (e.g.
 * pretty formatted JSON output).
 */
@Replace
@Order(TestingUtility.TESTING_BEAN_ORDER)
public class TestingJacksonDataObjectMapper extends JacksonDataObjectMapper {

  @Override
  protected ObjectMapper createObjectMapperInstance() {
    ObjectMapper om = super.createObjectMapperInstance();
    om.enable(SerializationFeature.INDENT_OUTPUT);
    return om;
  }

  /**
   * Allow usage of raw Jackson {@link ObjectMapper} instance in (legacy) unit tests
   */
  @SuppressWarnings("deprecation")
  @Override
  public ObjectMapper getObjectMapper() {
    return super.getObjectMapper();
  }
}
