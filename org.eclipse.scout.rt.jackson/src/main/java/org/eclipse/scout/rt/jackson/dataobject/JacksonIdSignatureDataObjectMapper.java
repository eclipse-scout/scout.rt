/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject;

import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IIdSignatureDataObjectMapper;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Order;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@link IDataObjectMapper} implementation based on jackson {@link ObjectMapper} with id signed
 * serialization/deserialization.
 */
@Order(IBean.DEFAULT_BEAN_ORDER + 100)
public class JacksonIdSignatureDataObjectMapper extends JacksonDataObjectMapper implements IIdSignatureDataObjectMapper {

  @Override
  protected void prepareScoutDataModuleContext(ScoutDataObjectModuleContext moduleContext) {
    super.prepareScoutDataModuleContext(moduleContext);
    moduleContext.withIdSignature(true);
  }
}
