/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.rest.error;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("ErrorResponse")
public class ErrorResponse extends DoEntity {

  public DoValue<ErrorDo> error() {
    return doValue("error");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public ErrorResponse withError(ErrorDo error) {
    error().set(error);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ErrorDo getError() {
    return error().get();
  }
}
