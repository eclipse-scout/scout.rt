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

@IdTypeName("scout.FixtureString2Id")
public final class FixtureString2Id extends AbstractStringId {
  private static final long serialVersionUID = 1L;

  private FixtureString2Id(String id) {
    super(id);
  }

  public static FixtureString2Id of(String id) {
    if (StringUtility.isNullOrEmpty(id)) {
      return null;
    }
    return new FixtureString2Id(id);
  }
}
