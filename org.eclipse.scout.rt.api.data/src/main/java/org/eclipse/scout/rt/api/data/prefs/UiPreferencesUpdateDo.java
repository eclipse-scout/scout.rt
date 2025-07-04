/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.prefs;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.UiPreferencesUpdate")
public class UiPreferencesUpdateDo extends DoEntity {

  public DoValue<UiPreferencesDo> preferences() {
    return doValue("preferences");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public UiPreferencesUpdateDo withPreferences(UiPreferencesDo preferences) {
    preferences().set(preferences);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public UiPreferencesDo getPreferences() {
    return preferences().get();
  }
}
