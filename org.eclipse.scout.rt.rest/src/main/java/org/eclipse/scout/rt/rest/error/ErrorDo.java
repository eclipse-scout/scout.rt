/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.error;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

@TypeName("scout.Error")
public class ErrorDo extends DoEntity {

  /**
   * HTTP status code
   */
  public DoValue<Integer> httpStatus() {
    return doValue("httpStatus");
  }

  /**
   * Application error code
   */
  public DoValue<String> errorCode() {
    return doValue("errorCode");
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

  /**
   * @return {@link #getErrorCode()} as int if it contains an integer else zero
   */
  @SuppressWarnings("squid:S1166")
  public int getErrorCodeAsInt() {
    try {
      return TypeCastUtility.castValue(getErrorCode(), int.class);
    }
    catch (RuntimeException e) {
      return 0;
    }
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  /**
   * See {@link #httpStatus()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public ErrorDo withHttpStatus(Integer httpStatus) {
    httpStatus().set(httpStatus);
    return this;
  }

  /**
   * See {@link #httpStatus()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public Integer getHttpStatus() {
    return httpStatus().get();
  }

  /**
   * See {@link #errorCode()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public ErrorDo withErrorCode(String errorCode) {
    errorCode().set(errorCode);
    return this;
  }

  /**
   * See {@link #errorCode()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public String getErrorCode() {
    return errorCode().get();
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
