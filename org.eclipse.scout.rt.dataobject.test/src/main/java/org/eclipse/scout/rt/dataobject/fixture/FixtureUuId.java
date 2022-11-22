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
package org.eclipse.scout.rt.dataobject.fixture;

import java.util.UUID;

import org.eclipse.scout.rt.dataobject.id.AbstractUuId;
import org.eclipse.scout.rt.dataobject.id.IdTypeName;
import org.eclipse.scout.rt.platform.util.StringUtility;

@IdTypeName("scout.FixtureUuId")
public final class FixtureUuId extends AbstractUuId {
  private static final long serialVersionUID = 1L;

  private FixtureUuId(UUID id) {
    super(id);
  }

  public static FixtureUuId create() {
    return new FixtureUuId(UUID.randomUUID());
  }

  public static FixtureUuId of(UUID id) {
    if (id == null) {
      return null;
    }
    return new FixtureUuId(id);
  }

  public static FixtureUuId of(String id) {
    if (StringUtility.isNullOrEmpty(id)) {
      return null;
    }
    return new FixtureUuId(UUID.fromString(id));
  }
}
