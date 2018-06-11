/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.rest.error;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("Error")
public class ErrorDo extends DoEntity {

  public DoValue<Integer> status() {
    return doValue("status");
  }

  public DoValue<String> code() {
    return doValue("code");
  }

  public DoValue<String> title() {
    return doValue("title");
  }

  public DoValue<String> message() {
    return doValue("message");
  }

  public DoValue<String> correlationId() {
    return doValue("correlationId");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public ErrorDo withStatus(Integer status) {
    status().set(status);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getStatus() {
    return status().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ErrorDo withCode(String code) {
    code().set(code);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getCode() {
    return code().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ErrorDo withTitle(String title) {
    title().set(title);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getTitle() {
    return title().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ErrorDo withMessage(String message) {
    message().set(message);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getMessage() {
    return message().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ErrorDo withCorrelationId(String correlationId) {
    correlationId().set(correlationId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getCorrelationId() {
    return correlationId().get();
  }
}
