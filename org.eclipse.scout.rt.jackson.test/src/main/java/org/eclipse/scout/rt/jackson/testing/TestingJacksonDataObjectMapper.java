/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
  protected ObjectMapper createObjectMapperInstance(boolean ignoreTypeAttribute) {
    ObjectMapper om = super.createObjectMapperInstance(ignoreTypeAttribute);
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
