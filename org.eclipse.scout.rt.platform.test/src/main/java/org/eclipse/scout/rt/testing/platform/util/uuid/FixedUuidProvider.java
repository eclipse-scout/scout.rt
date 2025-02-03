/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.util.uuid;

import java.util.UUID;

import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.util.uuid.IUuidProvider;

/**
 * A UUID provider for testing that returns the same UUID when called repeatedly. This provider needs to be registered
 * <strong>manually</strong> with the {@link IBeanManager}.
 */
@IgnoreBean
public class FixedUuidProvider implements IUuidProvider {

  private final UUID m_uuid;

  public FixedUuidProvider(UUID uuid) {
    m_uuid = uuid;
  }

  @Override
  public UUID createUuid() {
    return m_uuid;
  }
}
