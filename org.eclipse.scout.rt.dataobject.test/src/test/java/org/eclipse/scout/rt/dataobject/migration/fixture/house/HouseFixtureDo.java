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

import java.util.Collection;
import java.util.List;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_2;

/**
 * Change history:
 * <ul>
 * <li>charlieFixture-2: charlieFixture.BuildingFixture -> charlieFixture.HouseFixture, postalAddress and owner
 * attributes were added</li>
 * </ul>
 *
 * @since charlieFixture-1
 */
@TypeName("charlieFixture.HouseFixture")
@TypeVersion(CharlieFixture_2.class)
public class HouseFixtureDo extends DoEntity {

  public DoValue<String> name() {
    return doValue("name");
  }

  public DoValue<PostalAddressFixtureDo> postalAddress() {
    return doValue("postalAddress");
  }

  public DoValue<CustomerFixtureDo> owner() {
    return doValue("owner");
  }

  public DoList<RoomFixtureDo> rooms() {
    return doList("rooms");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public HouseFixtureDo withName(String name) {
    name().set(name);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getName() {
    return name().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public HouseFixtureDo withPostalAddress(PostalAddressFixtureDo postalAddress) {
    postalAddress().set(postalAddress);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public PostalAddressFixtureDo getPostalAddress() {
    return postalAddress().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public HouseFixtureDo withOwner(CustomerFixtureDo owner) {
    owner().set(owner);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CustomerFixtureDo getOwner() {
    return owner().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public HouseFixtureDo withRooms(Collection<? extends RoomFixtureDo> rooms) {
    rooms().updateAll(rooms);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public HouseFixtureDo withRooms(RoomFixtureDo... rooms) {
    rooms().updateAll(rooms);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<RoomFixtureDo> getRooms() {
    return rooms().get();
  }
}
