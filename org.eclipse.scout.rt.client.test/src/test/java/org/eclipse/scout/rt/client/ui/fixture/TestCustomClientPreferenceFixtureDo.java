/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.fixture;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.client.ui.fixture.ScoutTestFixtureTypeVersions.ScoutTestFixture_1_0_0;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.shared.prefs.ICustomClientPreferenceDo;

@TypeName("scoutTestFixture.TestCustomClientPreferenceFixture")
@TypeVersion(ScoutTestFixture_1_0_0.class)
public class TestCustomClientPreferenceFixtureDo extends DoEntity implements ICustomClientPreferenceDo {

  public DoValue<String> name() {
    return doValue("name");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestCustomClientPreferenceFixtureDo withName(String name) {
    name().set(name);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getName() {
    return name().get();
  }
}
