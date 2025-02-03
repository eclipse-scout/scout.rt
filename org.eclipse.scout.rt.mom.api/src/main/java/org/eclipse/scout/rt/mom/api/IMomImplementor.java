/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.api;

import java.util.Map;

import javax.naming.InitialContext;

import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * Represents an implementor for {@link IMom}.
 *
 * @see IMom
 * @since 6.1
 */
@Bean
public interface IMomImplementor extends IMom {

  /**
   * Property to specify the JNDI name to lookup the connection factory.
   * <p>
   * <b>Value type:</b> {@link String}
   */
  String CONNECTION_FACTORY = "scout.mom.connectionfactory.name";

  /**
   * Property to specify the symbolic name of a MOM. If the property is missing, the symbolic name is chosen by the
   * implementor.
   * <p>
   * <b>Value type:</b> {@link String}
   */
  String SYMBOLIC_NAME = "scout.mom.name";

  /**
   * Property to specify the default marshaller of a MOM. If the property is not set, the default marshaller is chosen
   * by the implementor.
   * <p>
   * <b>Value type:</b>{@link IMarshaller} or {@link String} (interpreted as the class name of a {@link Bean} of type
   * {@link IMarshaller})
   */
  String MARSHALLER = "scout.mom.marshaller";

  /**
   * Property to enable or disable 'request-reply' messaging in a MOM. The default is <code>true</code>. When the
   * feature is disabled, it is not configured when the MOM is initialized (useful when the implementor does not support
   * automatic creation of the cancellation topic) and all calls to <code>MOM.request()</code> or
   * <code>MOM.reply()</code> will throw an {@link AssertionException}.
   * <p>
   * <b>Value type:</b> {@link Boolean} or {@link String} (<code>"true"</code> or <code>"false"</code>)
   */
  String REQUEST_REPLY_ENABLED = "scout.mom.requestreply.enabled";

  /**
   * Property to specify the topic that is used to cancel a request in 'request-reply' messaging. If the property is not
   * set, the value of {@link RequestReplyCancellationTopicProperty} is used.
   * <p>
   * <b>Value type:</b> {@link IDestination} or {@link String} (parseable by {@link DestinationConfigPropertyParser})
   */
  String REQUEST_REPLY_CANCELLATION_TOPIC = "scout.mom.requestreply.cancellationTopic";

  /**
   * Property to enable or disable connection failover in the scout mom wrapper.
   *
   * @see IMom.ConnectionRetryCountProperty
   * @since 6.1
   */
  String CONNECTION_RETRY_COUNT = "scout.mom.failover.connectionRetryCount";

  /**
   * Property sets the interval in milliseconds between connection attempts.
   *
   * @see IMom.ConnectionRetryIntervalMillisProperty
   * @since 6.1
   */
  String CONNECTION_RETRY_INTERVAL_MILLIS = "scout.mom.failover.connectionRetryIntervalMillis";

  /**
   * Property sets the interval to wait after a failed session call.
   *
   * @see IMom.SessionRetryIntervalMillisProperty
   * @since 6.1
   */
  String SESSION_RETRY_INTERVAL_MILLIS = "scout.mom.failover.sessionRetryIntervalMillis";

  /**
   * Initializes this implementor, e.g. to connect to the environment as specified by the given properties.
   * <p>
   * The initialization may bind resources that have to be released again by calling {@link #destroy()} when the
   * instance is no longer used. If the initialization fails, an exception is thrown and the instance is in an undefined
   * state. In that case, the implementor is responsible on it's own for releasing any resources acquired during
   * initialization.
   *
   * @param properties
   *     used to connect to the network or broker, and is implementor-specific. To connect to a JMS broker, you
   *     typically provide a {@link InitialContext} with the JNDI name of the connection factory specified with the
   *     property {@link #CONNECTION_FACTORY}.
   */
  void init(Map<Object, Object> properties) throws Exception; // NOSONAR
}
