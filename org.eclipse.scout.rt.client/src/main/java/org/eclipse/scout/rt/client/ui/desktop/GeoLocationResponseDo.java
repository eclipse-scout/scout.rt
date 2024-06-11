/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.GeoLocationResponse")
public class GeoLocationResponseDo extends DoEntity {

  public DoValue<String> latitude() {
    return doValue("latitude");
  }

  public DoValue<String> longitude() {
    return doValue("longitude");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public GeoLocationResponseDo withLatitude(String latitude) {
    latitude().set(latitude);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getLatitude() {
    return latitude().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public GeoLocationResponseDo withLongitude(String longitude) {
    longitude().set(longitude);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getLongitude() {
    return longitude().get();
  }
}
