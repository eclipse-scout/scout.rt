/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.jms;

import javax.jms.Message;

/**
 * Properties used in {@link JmsMomImplementor} implementation.
 * <p>
 * <strong>JMS property names must obey the rules for a message selector identifier. See the section <em>Message
 * Selectors</em> of {@link Message} for more information.</strong>
 */
public interface IJmsMomProperties {

  /**
   * The property id to query the keys of a marshaller context.
   */
  String JMS_PROP_MARSHALLER_CONTEXT = "x_scout_mom_marshaller_properties"; // name must comply with the rules for a message selector identifier

  /**
   * The property id for the unique ID of a 'request-reply' communication.
   */
  String JMS_PROP_REPLY_ID = "x_scout_mom_requestreply_id"; // name must comply with the rules for a message selector identifier

  /**
   * The property id to check whether 'request-reply' communication failed, meaning that request processing failed.
   */
  String CTX_PROP_REQUEST_REPLY_SUCCESS = "x-scout.mom.requestreply.code";

  /**
   * The property ID to indicate whether this is a <code>null</code> object.
   */
  String CTX_PROP_NULL_OBJECT = "x-scout.mom.transferobject.null";
}
