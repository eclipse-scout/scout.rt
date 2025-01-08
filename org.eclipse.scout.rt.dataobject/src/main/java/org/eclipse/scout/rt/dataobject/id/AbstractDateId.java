/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.id;

import java.util.Date;

/**
 * Abstract base implementation for all {@link Date} based {@link IId} classes. The wrapped id is guaranteed to be
 * non-null.
 * <p>
 * For details, see {@link IDateId}.
 */
public abstract class AbstractDateId extends AbstractRootId<Date> implements IDateId {
  private static final long serialVersionUID = 1L;

  protected AbstractDateId(Date id) {
    super(id);
  }
}
