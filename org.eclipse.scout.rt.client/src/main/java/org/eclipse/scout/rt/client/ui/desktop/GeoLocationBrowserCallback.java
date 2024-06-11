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

import org.eclipse.scout.rt.api.data.ObjectType;
import org.eclipse.scout.rt.client.ui.Coordinates;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.IBrowserCallback;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.platform.util.StringUtility;

@ObjectType("GeoLocationBrowserHandler")
public class GeoLocationBrowserCallback implements IBrowserCallback<GeoLocationResponseDo, Coordinates> {

  @Override
  public Pair<Coordinates, ProcessingException> onCallbackDone(GeoLocationResponseDo response) {
    String latitude = response.getLatitude();
    String longitude = response.getLongitude();

    // some browsers do not provide an errorCode but still deliver no location data
    if (!StringUtility.hasText(latitude) || !StringUtility.hasText(longitude)) {
      return geolocationFailed(null, null);
    }
    return ImmutablePair.of(new Coordinates(latitude, longitude), null);
  }

  @Override
  public Pair<Coordinates, ? extends Throwable> onCallbackFailed(Throwable exception, String message, String code) {
    return geolocationFailed(message, code);
  }

  protected Pair<Coordinates, ProcessingException> geolocationFailed(String errorMessage, String errorCode) {
    IDesktop desktop = IDesktop.CURRENT.get();
    if (desktop != null) {
      desktop.setGeolocationServiceAvailable(false);
    }
    String locationFailed = TEXTS.get("GeolocationFailed");
    String msg = StringUtility.hasText(errorMessage) ? locationFailed + ": " + errorMessage : locationFailed + ".";
    ProcessingException pe = new ProcessingException(msg);
    return ImmutablePair.of(null, pe);
  }
}
