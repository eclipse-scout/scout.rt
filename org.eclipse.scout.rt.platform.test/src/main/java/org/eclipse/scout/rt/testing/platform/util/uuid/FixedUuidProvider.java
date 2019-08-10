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
