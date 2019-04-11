/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.jms;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.Context;

import org.eclipse.scout.rt.mom.api.IMomImplementor;
import org.eclipse.scout.rt.testing.platform.runner.parameterized.IScoutTestParameter;

public final class FixtureParameters {
  private static final AtomicInteger MOM_COUNTER = new AtomicInteger(0);

  public static List<IScoutTestParameter> createParameters() {
    List<IScoutTestParameter> parametersList = new LinkedList<IScoutTestParameter>();

    // We do not need jmx for unit testing. Also we must disable watchTopicAdvisories else some concurrent issues with broker recreation will happen
    final String activeMQUrlOptions = "?broker.persistent=false&broker.useJmx=false&jms.watchTopicAdvisories=false";
    final Map<String, String> activeMQEnvironment = new HashMap<>();
    activeMQEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, org.apache.activemq.jndi.ActiveMQInitialContextFactory.class.getName());
    activeMQEnvironment.put("connectionFactoryNames", "JUnitConnectionFactory"); // Active MQ specific
    activeMQEnvironment.put(IMomImplementor.CONNECTION_FACTORY, "JUnitConnectionFactory");

    parametersList.add(new AbstractJmsMomTestParameter("activemq") {

      @Override
      public Class<? extends IMomImplementor> getImplementor() {
        return JmsMomImplementor.class;
      }

      @Override
      public Map<String, String> getEnvironment() {
        Map<String, String> env = new HashMap<>(activeMQEnvironment);
        env.put(Context.PROVIDER_URL, "vm://mom" + MOM_COUNTER.incrementAndGet() + "/junit" + activeMQUrlOptions);
        env.put(IMomImplementor.SYMBOLIC_NAME, "Scout JUnit MOM #" + MOM_COUNTER.get());
        return env;
      }
    });
    parametersList.add(new AbstractJmsMomTestParameter("activemq-j2ee") {

      @Override
      public Class<? extends IMomImplementor> getImplementor() {
        return J2eeJmsMomImplementor.class;
      }

      @Override
      public Map<String, String> getEnvironment() {
        Map<String, String> env = new HashMap<>(activeMQEnvironment);
        env.put(Context.PROVIDER_URL, "vm://mom" + MOM_COUNTER.incrementAndGet() + "/junit" + activeMQUrlOptions);
        env.put(IMomImplementor.SYMBOLIC_NAME, "Scout JUnit MOM #" + MOM_COUNTER.get());
        return env;
      }
    });

    final Map<String, String> artemisEnvironment = new HashMap<>();
    artemisEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory.class.getName());
    artemisEnvironment.put(IMomImplementor.CONNECTION_FACTORY, "invmConnectionFactory");
    artemisEnvironment.put("connectionFactory.invmConnectionFactory", "vm://0");
    artemisEnvironment.put(IMomImplementor.SYMBOLIC_NAME, "Scout JUnit MOM [artemis]");

    parametersList.add(new AbstractJmsMomTestParameter("artemis") {

      @Override
      public Class<? extends IMomImplementor> getImplementor() {
        return JmsMomImplementor.class;
      }

      @Override
      public Map<String, String> getEnvironment() {
        return new HashMap<>(artemisEnvironment);
      }
    });
    parametersList.add(new AbstractJmsMomTestParameter("artemis-j2ee") {

      @Override
      public Class<? extends IMomImplementor> getImplementor() {
        return J2eeJmsMomImplementor.class;
      }

      @Override
      public Map<String, String> getEnvironment() {
        return new HashMap<>(artemisEnvironment);
      }
    });
    return parametersList;
  }

  private FixtureParameters() {
  }
}
