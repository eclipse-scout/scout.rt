/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.jms;

import java.util.Map;

import org.eclipse.scout.rt.mom.api.AbstractMomTransport;
import org.eclipse.scout.rt.mom.api.IMomImplementor;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.JsonMarshaller;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IgnoreBean;

/**
 * Encapsulates {@link JmsMomImplementor} for testing purpose.
 */
@IgnoreBean
public class FixtureMom extends AbstractMomTransport {
  private final AbstractJmsMomTestParameter m_parameter;

  public FixtureMom(AbstractJmsMomTestParameter parameter) {
    m_parameter = parameter;
  }

  @Override
  protected Class<? extends IMomImplementor> getConfiguredImplementor() {
    return m_parameter.getImplementor();
  }

  @Override
  protected Map<String, String> getConfiguredEnvironment() {
    final Map<String, String> env = m_parameter.getEnvironment();
    env.put(JmsMomImplementor.JMS_MESSAGE_HANDLER, IJmsMessageHandler.class.getName());
    return env;
  }

  @Override
  protected IMarshaller getConfiguredDefaultMarshaller() {
    return BEANS.get(JsonMarshaller.class);
  }
}
