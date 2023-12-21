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

/**
 * Abstract base implementation for all {@link String} based {@link IId} classes. The wrapped id is guaranteed to be
 * non-null.
 * <p>
 * Usually not used for transactional data ids which should be saved from being probed. Therefore, {@link IdSignature}
 * is disabled.
 * <p>
 * For details, see {@link IStringId}.
 */
@IdSignature(false)
public abstract class AbstractStringId extends AbstractRootId<String> implements IStringId {
  private static final long serialVersionUID = 1L;

  protected AbstractStringId(String id) {
    super(id);
  }
}
