/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.fixture;

import org.eclipse.scout.rt.dataobject.id.AbstractRootId;

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
