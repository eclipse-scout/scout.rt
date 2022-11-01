/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.id;

/**
 * Abstract base implementation for all {@link Long} based {@link IId} classes. The wrapped id is guaranteed to be
 * non-null.
 * <p>
 * For details, see {@link ILongId}.
 */
public abstract class AbstractLongId extends AbstractId<Long> implements ILongId {
  private static final long serialVersionUID = 1L;

  protected AbstractLongId(Long id) {
    super(id);
  }
}
