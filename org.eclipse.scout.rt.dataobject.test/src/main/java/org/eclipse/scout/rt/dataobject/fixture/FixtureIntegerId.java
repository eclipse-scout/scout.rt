/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.fixture;

import org.eclipse.scout.rt.dataobject.id.AbstractRootId;
import org.eclipse.scout.rt.dataobject.id.IdTypeName;

@IdTypeName("scout.FixtureIntegerId")
public final class FixtureIntegerId extends AbstractRootId<Integer> {
  private static final long serialVersionUID = 1L;

  private FixtureIntegerId(Integer id) {
    super(id);
  }

  public static FixtureIntegerId of(Integer id) {
    if (id == null || id.intValue() == 0) {
      return null;
    }
    return new FixtureIntegerId(id);
  }

  public static FixtureIntegerId of(String string) {
    if (string == null) {
      return null;
    }
    return new FixtureIntegerId(Integer.parseInt(string));
  }
}
