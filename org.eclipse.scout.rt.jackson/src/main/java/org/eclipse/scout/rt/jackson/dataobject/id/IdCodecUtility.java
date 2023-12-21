/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.id;

import java.util.Set;

import org.eclipse.scout.rt.dataobject.id.IdCodec.IIdCodecFlag;
import org.eclipse.scout.rt.dataobject.id.IdCodec.IdCodecFlag;
import org.eclipse.scout.rt.jackson.dataobject.ScoutDataObjectModuleContext;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

public final class IdCodecUtility {

  private IdCodecUtility() {
  }

  /**
   * Computes the {@link IIdCodecFlag}s for the given {@link ScoutDataObjectModuleContext}. E.g.:
   * {@link IdCodecFlag#SIGNATURE} if {@link ScoutDataObjectModuleContext#isIdSignature()} returns {@code true}. The
   * resulting set is mutable.
   */
  public static Set<IIdCodecFlag> getIdCodecFlags(ScoutDataObjectModuleContext moduleContext) {
    var flags = CollectionUtility.<IIdCodecFlag> emptyHashSet();
    if (moduleContext == null) {
      return flags;
    }
    if (moduleContext.isLenientMode()) {
      flags.add(IdCodecFlag.LENIENT);
    }
    if (moduleContext.isIdSignature()) {
      flags.add(IdCodecFlag.SIGNATURE);
    }
    return flags;
  }
}
