/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.prefs.migration;

import java.util.Set;

import org.eclipse.scout.rt.api.data.prefs.UiPreferencesDo;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.dataobject.ScoutTypeVersions.Scout_25_2_002__uiPreferences;
import org.eclipse.scout.rt.dataobject.migration.AbstractDoStructureMigrationHandler;
import org.eclipse.scout.rt.dataobject.migration.DataObjectMigrationContext;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * No-op migration handler that adds a type version to the {@link UiPreferencesDo} which previously didn't have one.
 */
public class UiPreferencesAddTypeVersionMigrationHandler_25_2_002 extends AbstractDoStructureMigrationHandler {

  @Override
  public Class<? extends ITypeVersion> toTypeVersionClass() {
    return Scout_25_2_002__uiPreferences.class;
  }

  @Override
  public Set<String> getTypeNames() {
    return CollectionUtility.hashSet("scout.UiPreferences");
  }

  @Override
  protected boolean migrate(DataObjectMigrationContext ctx, IDoEntity doEntity) {
    return false;
  }
}
