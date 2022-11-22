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
