/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jms.clustersync;

import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;

/**
 * Config properties for JMS
 */
public final class JmsPublishSubscribeMessageProperties {
  private JmsPublishSubscribeMessageProperties() {
  }

  public static class JndiInitialContextFactory extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "jms.jndiInitialContextFactory";
    }
  }

  public static class JndiProviderUrl extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "jms.jndiProviderUrl";
    }
  }

  public static class JndiConnectionFactory extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "jms.jndiConnectionFactory";
    }
  }

  public static class PublishSubscribeTopic extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "jms.publishSubscribeMessageTopic";
    }
  }

  public static class JndiUsername extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "jms.jndiUsername";
    }
  }

  public static class JndiPassword extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "jms.jndiPassword";
    }
  }
}
