/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.migration.fixture.house;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_5;

/**
 * Change history:
 * <ul>
 * <li>charlieFixture-2: roomName -> name</li>
 * <li>charlieFixture-3: added areaInSquareFoot</li> (would not need an update of type version if no real migration is
 * required)
 * <li>charlieFixture-4: changed areaInSquareFoot to areaInSquareMeter</li>
 * <li>charlieFixture-5: added displayText</li>
 * </ul>
 *
 * @since charlieFixture-1
 */
@TypeName("charlieFixture.RoomFixture")
@TypeVersion(CharlieFixture_5.class)
public class RoomFixtureDo extends DoEntity {

  public DoValue<String> name() {
    return doValue("name");
  }

  public DoValue<String> displayText() {
    return doValue("displayText");
  }

  public DoValue<Integer> areaInSquareMeter() {
    return doValue("areaInSquareMeter");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public RoomFixtureDo withName(String name) {
    name().set(name);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getName() {
    return name().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public RoomFixtureDo withDisplayText(String displayText) {
    displayText().set(displayText);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getDisplayText() {
    return displayText().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public RoomFixtureDo withAreaInSquareMeter(Integer areaInSquareMeter) {
    areaInSquareMeter().set(areaInSquareMeter);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getAreaInSquareMeter() {
    return areaInSquareMeter().get();
  }
}
