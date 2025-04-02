/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.bookmark;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.ActivateBookmarkOptions")
public class ActivateBookmarkOptionsDo extends DoEntity {

  public DoValue<Boolean> activateOutline() {
    return doValue("activateOutline");
  }

  public DoValue<Boolean> resetViewAndWarnOnFail() {
    return doValue("resetViewAndWarnOnFail");
  }

  public DoValue<Boolean> handleErrors() {
    return doValue("handleErrors");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public ActivateBookmarkOptionsDo withActivateOutline(Boolean activateOutline) {
    activateOutline().set(activateOutline);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getActivateOutline() {
    return activateOutline().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isActivateOutline() {
    return nvl(getActivateOutline());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ActivateBookmarkOptionsDo withResetViewAndWarnOnFail(Boolean resetViewAndWarnOnFail) {
    resetViewAndWarnOnFail().set(resetViewAndWarnOnFail);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getResetViewAndWarnOnFail() {
    return resetViewAndWarnOnFail().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isResetViewAndWarnOnFail() {
    return nvl(getResetViewAndWarnOnFail());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ActivateBookmarkOptionsDo withHandleErrors(Boolean handleErrors) {
    handleErrors().set(handleErrors);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getHandleErrors() {
    return handleErrors().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isHandleErrors() {
    return nvl(getHandleErrors());
  }
}
