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

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.RestClientTestEcho")
public class RestClientTestEchoDo extends DoEntity {

  public DoValue<String> info() {
    return doValue("info");
  }

  public DoValue<Integer> code() {
    return doValue("code");
  }

  public DoValue<String> httpMethod() {
    return doValue("httpMethod");
  }

  public DoValue<String> data() {
    return doValue("data");
  }

  public DoValue<String> body() {
    return doValue("body");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public RestClientTestEchoDo withInfo(String info) {
    info().set(info);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getInfo() {
    return info().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public RestClientTestEchoDo withCode(Integer code) {
    code().set(code);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getCode() {
    return code().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public RestClientTestEchoDo withHttpMethod(String httpMethod) {
    httpMethod().set(httpMethod);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getHttpMethod() {
    return httpMethod().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public RestClientTestEchoDo withData(String data) {
    data().set(data);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getData() {
    return data().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public RestClientTestEchoDo withBody(String body) {
    body().set(body);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getBody() {
    return body().get();
  }
}
