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

import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IPrettyPrintDataObjectMapper;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * {@link IDataObjectMapper} implementation based on jackson {@link ObjectMapper} with output indentation enabled (e.g.
 * pretty formatted JSON output).
 */
@Order(IBean.DEFAULT_BEAN_ORDER + 100)
public class JacksonPrettyPrintDataObjectMapper extends JacksonDataObjectMapper implements IPrettyPrintDataObjectMapper {

  @Override
  protected ObjectMapper createObjectMapperInstance(boolean ignoreTypeAttribute) {
    ObjectMapper om = super.createObjectMapperInstance(ignoreTypeAttribute);
    om.enable(SerializationFeature.INDENT_OUTPUT);
    return om;
  }
}
