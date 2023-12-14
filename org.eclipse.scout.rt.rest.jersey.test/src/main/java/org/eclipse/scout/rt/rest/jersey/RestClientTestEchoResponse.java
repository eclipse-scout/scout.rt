/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey;

import java.util.Map;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.RestClientTestEchoResponse")
public class RestClientTestEchoResponse extends DoEntity {

  public DoValue<RestClientTestEchoDo> echo() {
    return doValue("echo");
  }

  public DoValue<Map<String, String>> receivedHeaders() {
    return doValue("receivedHeaders");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public RestClientTestEchoResponse withEcho(RestClientTestEchoDo echo) {
    echo().set(echo);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public RestClientTestEchoDo getEcho() {
    return echo().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public RestClientTestEchoResponse withReceivedHeaders(Map<String, String> receivedHeaders) {
    receivedHeaders().set(receivedHeaders);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<String, String> getReceivedHeaders() {
    return receivedHeaders().get();
  }
}
