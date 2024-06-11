/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop.hybrid;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.BrowserCallbackError")
public class BrowserCallbackErrorDo extends DoEntity {

  public DoValue<String> message() {
    return doValue("message");
  }

  public DoValue<String> code() {
    return doValue("code");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public BrowserCallbackErrorDo withMessage(String message) {
    message().set(message);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getMessage() {
    return message().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BrowserCallbackErrorDo withCode(String code) {
    code().set(code);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getCode() {
    return code().get();
  }
}
