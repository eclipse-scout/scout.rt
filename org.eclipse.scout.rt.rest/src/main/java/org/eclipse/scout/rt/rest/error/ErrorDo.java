/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.error;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

@TypeName("scout.Error")
public class ErrorDo extends DoEntity {

  /**
   * HTTP status code
   */
  public DoValue<Integer> status() {
    return doValue("status");
  }

  /**
   * Application error code
   */
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

  /**
   * @return {@link #getCode()} as int if it contains an integer else zero
   */
  @SuppressWarnings("squid:S1166")
  public int getCodeAsInt() {
    try {
      return TypeCastUtility.castValue(getCode(), int.class);
    }
    catch (RuntimeException e) {
      return 0;
    }
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
