/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.testing;

import org.eclipse.scout.rt.jackson.dataobject.JacksonDataObjectMapper;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.dataobject.IDataObjectMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * {@link IDataObjectMapper} implementation based on jackson {@link ObjectMapper} with output indentation enabled (e.g.
 * pretty formatted JSON output).
 */
@Replace
public class TestingJacksonDataObjectMapper extends JacksonDataObjectMapper {

  @Override
  protected ObjectMapper createObjectMapperInstance() {
    ObjectMapper om = super.createObjectMapperInstance();
    om.enable(SerializationFeature.INDENT_OUTPUT);
    return om;
  }
}
