/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.api;

import org.eclipse.scout.rt.mom.api.IDestination.IDestinationType;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;

/**
 * Config property for {@link IBiDestination}s (<i>request-reply messaging</i>). For {@link IDestination}s, use
 * {@link AbstractDestinationConfigProperty}.
 * <p>
 * The expected format for config values is described here: {@link DestinationConfigPropertyParser}.
 * <p>
 * <h3>Example usage:</h3> <i>Application code:</i>
 *
 * <pre>
 * public static class PersonFinderQueueConfig extends AbstractBiDestinationConfigProperty<Long, Person> {
 *
 *   &#64;Override
 *   public String getKey() {
 *     return "scout.mom.cluster.destination.personFinderQueue";
 *   }
 *
 *   &#64;Override
 *   protected IDestinationType getType() {
 *     return DestinationType.QUEUE;
 *   }
 * }
 *
 * public static final IBiDestination<Long, Person> PERSON_FINDER_QUEUE = CONFIG.getPropertyValue(PersonFinderQueueConfig.class);
 *
 * ...
 * // Usage
 * Person person = MOM.request(ClusterMom.class, PERSON_FINDER_QUEUE, personId);
 * ...
 * </pre>
 *
 * <i>config.properties</i>:
 *
 * <pre>
 * scout.mom.cluster.destination.personFinderQueue=jndi:///person/finder
 * </pre>
 *
 * @see IBiDestination
 * @see IDestination
 * @since 6.1
 */
public abstract class AbstractBiDestinationConfigProperty<REQUEST, REPLY> extends AbstractConfigProperty<IBiDestination<REQUEST, REPLY>, String> {

  // -----------------------------------------------------------------------------------
  // Implementation note: The same code exists in AbstractDestinationConfigProperty.
  // If you change something here, make sure to change it in the other class as well!
  // -----------------------------------------------------------------------------------

  @Override
  protected IBiDestination<REQUEST, REPLY> parse(final String value) {
    final DestinationConfigPropertyParser p = BEANS.get(DestinationConfigPropertyParser.class).parse(value);
    return MOM.newBiDestination(p.getDestinationName(), getType(), p.getResolveMethod(), p.getParameters());
  }

  /**
   * @return The destination type (must not be <code>null</code>)
   */
  protected abstract IDestinationType getType();
}
