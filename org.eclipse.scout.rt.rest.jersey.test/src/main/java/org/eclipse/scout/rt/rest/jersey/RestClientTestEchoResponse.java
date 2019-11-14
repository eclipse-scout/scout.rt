/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.jersey;

import java.util.Map;

import javax.annotation.Generated;

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
