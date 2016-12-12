/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.mom.api;

import java.util.Map;

import javax.naming.InitialContext;

import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.JsonMarshaller;

/**
 * Represents a implementor for {@link IMom}.
 *
 * @see IMom
 * @since 6.1
 */
public interface IMomImplementor extends IMom {

  /**
   * Use this property to specify the JNDI name to lookup the connection factory.
   */
  String CONNECTION_FACTORY = "scout.mom.connectionfactory.name";

  /**
   * Use this property to optionally specify the symbolic name of a MOM.
   */
  String SYMBOLIC_NAME = "scout.mom.name";

  /**
   * Initializes this implementor, e.g. to connect to the environment as specified by the given properties.
   *
   * @param properties
   *          used to connect to the network or broker, and is implementor-specific. To connect to a JMS broker, you
   *          typically provide a {@link InitialContext} with the JNDI name of the connection factory specified with the
   *          property {@link #CONNECTION_FACTORY}.
   */
  void init(Map<Object, Object> properties) throws Exception; // NOSONAR

  /**
   * Sets the given {@link IMarshaller} to be used if no specific marshaller is registered for a {@link IDestination}.
   * If not set, the marshaller as configured in property {@link MarshallerProperty} is used. By default,
   * {@link JsonMarshaller} is configured.
   */
  void setDefaultMarshaller(IMarshaller marshaller);

}
