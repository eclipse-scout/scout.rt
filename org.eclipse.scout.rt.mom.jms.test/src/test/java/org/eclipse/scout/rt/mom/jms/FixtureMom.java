/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.mom.jms;

import java.util.Map;

import org.eclipse.scout.rt.mom.api.AbstractMomTransport;
import org.eclipse.scout.rt.mom.api.IMomImplementor;
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
}
