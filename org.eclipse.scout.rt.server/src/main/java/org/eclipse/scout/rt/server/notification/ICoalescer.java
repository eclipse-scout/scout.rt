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
package org.eclipse.scout.rt.server.notification;

import java.util.List;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Every implementation of this interface can be applied to coalesce objects of the same super class (generic type of
 * this class).
 */
@FunctionalInterface
@ApplicationScoped
public interface ICoalescer<T> {

  /**
   * @param data
   *          all objects to coalesce. Ordered, such that new data is inserted at the end of the list
   * @return a coalesced result. Never <code>null</code>.
   */
  List<T> coalesce(List<T> data);

}
