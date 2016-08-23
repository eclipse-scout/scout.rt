/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.mom.api;

import java.util.Map;

import javax.naming.InitialContext;

/**
 * Allows the initialization of a MOM implementor.
 *
 * @see IMom
 * @since 6.1
 */
public interface IMomInitializer {

  /**
   * Use this property to specify the JNDI name to lookup the connection factory.
   */
  String CONNECTION_FACTORY = "scout.naming.mom.factory.connection";

  /**
   * Initializes this {@link IMom} to connect to the environment as specified by the given properties.
   *
   * @param properties
   *          used to connect to the network or broker, and is implementor-specific. To connect to a JMS broker, you
   *          typically provide a {@link InitialContext} with the JNDI name of the connection factory specified with the
   *          property {@link #CONNECTION_FACTORY}.
   */
  void init(Map<Object, Object> properties) throws Exception;
}
