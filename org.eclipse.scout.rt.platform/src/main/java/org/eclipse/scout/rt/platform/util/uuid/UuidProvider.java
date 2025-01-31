/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.uuid;

import java.util.UUID;

/**
 * Default implementation for {@link IUuidProvider}, returns {@link java.util.UUID#randomUUID()}.
 */
public class UuidProvider implements IUuidProvider {

  @Override
  public UUID createUuid() {
    return UUID.randomUUID();
  }
}
