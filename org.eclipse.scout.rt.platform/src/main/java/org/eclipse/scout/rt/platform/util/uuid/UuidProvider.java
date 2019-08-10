/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
