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

import java.util.UUID;

/**
 * Abstract base implementation for all {@link UUID} based {@link IId} classes. The wrapped id is guaranteed to be
 * non-null.
 * <p>
 * For details, see {@link IUuId}.
 */
public abstract class AbstractUuId extends AbstractRootId<UUID> implements IUuId {
  private static final long serialVersionUID = 1L;

  protected AbstractUuId(UUID id) {
    super(id);
  }
}
