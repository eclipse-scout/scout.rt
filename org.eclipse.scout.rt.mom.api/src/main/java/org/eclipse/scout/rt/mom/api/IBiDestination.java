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

/**
 * Represents the target for 'request-reply' messaging, which allows synchronous communication between a publisher and a
 * subscriber, and which is based on P2P or pub/sub messaging
 * <p>
 * Typically, request-reply is used with a queue destination. If using a topic, it is the first reply which is returned.
 * <p>
 * This is a lightweight object with no physical messaging resources allocated, and which can be constructed even if not
 * connected to the network or broker, e.g. in static initialization sections.
 * <p>
 * Two destinations with the same <i>name</i> are considered 'equals'.
 * <p>
 * See {@link IMom} documentation for more information about the difference between topic and queue based messaging.
 *
 * @see IDestination
 * @since 6.1
 */
public interface IBiDestination<REQUEST, REPLY> extends IDestination<REQUEST> {
}
