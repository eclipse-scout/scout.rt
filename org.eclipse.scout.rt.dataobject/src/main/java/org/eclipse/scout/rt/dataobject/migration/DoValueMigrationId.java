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
package org.eclipse.scout.rt.dataobject.migration;

import org.eclipse.scout.rt.dataobject.id.AbstractStringId;
import org.eclipse.scout.rt.dataobject.id.IdTypeName;

/**
 * Unique id for {@link IDoValueMigrationHandler}.
 * <p>
 * A value migration handler usually defines an ID constant using a uuid to guarantee that every migration handler uses
 * a unique ID.
 */
@IdTypeName("scout.DoValueMigrationId")
public final class DoValueMigrationId extends AbstractStringId {
  private static final long serialVersionUID = 1L;

  private DoValueMigrationId(String id) {
    super(id);
  }

  public static DoValueMigrationId of(String id) {
    if (id == null) {
      return null;
    }
    return new DoValueMigrationId(id);
  }
}
