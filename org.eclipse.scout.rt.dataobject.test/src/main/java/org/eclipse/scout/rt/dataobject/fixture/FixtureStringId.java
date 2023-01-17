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

import org.eclipse.scout.rt.dataobject.id.AbstractStringId;
import org.eclipse.scout.rt.dataobject.id.IdTypeName;
import org.eclipse.scout.rt.platform.util.StringUtility;

@IdTypeName("scout.FixtureStringId")
public final class FixtureStringId extends AbstractStringId {
  private static final long serialVersionUID = 1L;

  private FixtureStringId(String id) {
    super(id);
  }

  public static FixtureStringId of(String id) {
    if (StringUtility.isNullOrEmpty(id)) {
      return null;
    }
    return new FixtureStringId(id);
  }
}
