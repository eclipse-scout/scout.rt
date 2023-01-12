/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.id;

import java.util.List;

/**
 * An interface to represent an arbitrary id based on the composition of multiple {@link IId}'s as components. The
 * type(s) of the raw (wrapped) ids is required to be an instance of {@link IId}.
 * <p>
 * Subclasses must provide the following static factory methods:
 * <ul>
 * <li><b>of(wrapped-type(s))</b>: used by {@link IdFactory} to construct new instances. The method is expected to
 * return <code>null</code> if the given {@code value(s)} are <code>null</code>, otherwise the wrapped value.
 * </ul>
 * <p>
 *
 * @see AbstractCompositeId
 */
public interface ICompositeId extends IId {

  @Override
  List<? extends IId> unwrap();
}
