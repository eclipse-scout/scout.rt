/*******************************************************************************
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.dataobject.id;

import java.util.UUID;

/**
 * Abstract base implementation for all {@link UUID} based {@link IId} classes. The wrapped id is guaranteed to be
 * non-null.
 * <p>
 * For details, see {@link IUuId}.
 */
public abstract class AbstractUuId extends AbstractId<UUID> implements IUuId {
  private static final long serialVersionUID = 1L;

  protected AbstractUuId(UUID id) {
    super(id);
  }
}
