/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.migration;

import java.util.Random;
import java.util.UUID;

import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.util.uuid.IUuidProvider;

/**
 * A UUID provider for testing that always returns the same order of UUIDs. This provider needs to be registered
 * <strong>manually</strong> with the {@link IBeanManager}.
 */
@IgnoreBean
public class ConstantUuidProvider implements IUuidProvider {

  private final Random m_random = new Random(42); // initialize with a constant seed so that output is predictable over different runs

  @Override
  public UUID createUuid() {
    byte[] bytes = new byte[16];
    m_random.nextBytes(bytes);
    return UUID.nameUUIDFromBytes(bytes);
  }
}
