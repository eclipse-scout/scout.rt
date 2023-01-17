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

import org.eclipse.scout.rt.mom.api.IDestination.IDestinationType;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;

/**
 * Config property for {@link IDestination}s. For {@link IBiDestination}s (<i>request-reply messaging</i>), use
 * {@link AbstractBiDestinationConfigProperty}.
 * <p>
 * The expected format for config values is described here: {@link DestinationConfigPropertyParser}.
 * <p>
 * <h3>Example usage:</h3> <i>Application code:</i>
 *
 * <pre>
 * public static class PersonChangesTopicConfig extends AbstractDestinationConfigProperty<Person> {
 *
 *   &#64;Override
 *   public String getKey() {
 *     return "scout.mom.cluster.destination.personChangesTopic";
 *   }
 *
 *   &#64;Override
 *   protected IDestinationType getType() {
 *     return DestinationType.TOPIC;
 *   }
 * }
 *
 * public static final IDestination<Person> PERSON_CHANGES_TOPIC = CONFIG.getPropertyValue(PersonChangesTopicConfig.class);
 *
 * ....
 * // Usage
 * MOM.publish(ClusterMom.class, PERSON_CHANGES_TOPIC, person);
 * ...
 * </pre>
 *
 * <i>config.properties</i>:
 *
 * <pre>
 * scout.mom.cluster.destination.personChangesTopic=jndi:///person/changes
 * </pre>
 *
 * @see IBiDestination
 * @see IDestination
 * @since 6.1
 */
public abstract class AbstractDestinationConfigProperty<DTO> extends AbstractConfigProperty<IDestination<DTO>, String> {

  // -----------------------------------------------------------------------------------
  // Implementation note: The same code exists in AbstractBiDestinationConfigProperty.
  // If you change something here, make sure to change it in the other class as well!
  // -----------------------------------------------------------------------------------

  @Override
  protected IDestination<DTO> parse(final String value) {
    final DestinationConfigPropertyParser p = BEANS.get(DestinationConfigPropertyParser.class).parse(value);
    return MOM.newDestination(p.getDestinationName(), getType(), p.getResolveMethod(), p.getParameters());
  }

  /**
   * @return The destination type (must not be <code>null</code>)
   */
  protected abstract IDestinationType getType();
}
