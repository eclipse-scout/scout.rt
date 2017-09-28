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

import org.eclipse.scout.rt.platform.util.IAdaptable;

/**
 * Represents a message received via {@link IMom}.
 *
 * @since 6.1
 */
public interface IMessage<DTO> extends IAdaptable {

  /**
   * The {@link IMessage} which is currently associated with the current thread.
   */
  ThreadLocal<IMessage<?>> CURRENT = new ThreadLocal<>();

  /**
   * Returns the transfer object sent with this message.
   */
  DTO getTransferObject();

  /**
   * Returns the property associated with this message.
   */
  String getProperty(String property);
}
