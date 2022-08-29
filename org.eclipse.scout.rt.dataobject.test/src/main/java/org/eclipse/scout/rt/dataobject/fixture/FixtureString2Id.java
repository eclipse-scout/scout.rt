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

import org.eclipse.scout.rt.dataobject.id.AbstractStringId;
import org.eclipse.scout.rt.dataobject.id.IdTypeName;

@IdTypeName("scout.FixtureString2Id")
public final class FixtureString2Id extends AbstractStringId {
  private static final long serialVersionUID = 1L;

  public static FixtureString2Id of(String id) {
    if (id == null) {
      return null;
    }
    return new FixtureString2Id(id);
  }

  private FixtureString2Id(String id) {
    super(id);
  }
}
