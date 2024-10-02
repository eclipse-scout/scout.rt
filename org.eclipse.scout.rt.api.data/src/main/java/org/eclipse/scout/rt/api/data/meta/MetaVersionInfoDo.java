/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.meta;

import java.util.Date;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.MetaVersionInfo")
public class MetaVersionInfoDo extends DoEntity {

  public DoValue<String> applicationName() {
    return doValue("applicationName");
  }

  public DoValue<String> applicationVersion() {
    return doValue("applicationVersion");
  }

  public DoValue<Date> buildDate() {
    return doValue("buildDate");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public MetaVersionInfoDo withApplicationName(String applicationName) {
    applicationName().set(applicationName);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getApplicationName() {
    return applicationName().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public MetaVersionInfoDo withApplicationVersion(String applicationVersion) {
    applicationVersion().set(applicationVersion);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getApplicationVersion() {
    return applicationVersion().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public MetaVersionInfoDo withBuildDate(Date buildDate) {
    buildDate().set(buildDate);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Date getBuildDate() {
    return buildDate().get();
  }
}
