/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.table;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.ContributesTo;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.IDoEntityContribution;
import org.eclipse.scout.rt.dataobject.TypeName;

@ContributesTo(IDoEntity.class)
@TypeName("scout.MaxRowCountContribution")
public class MaxRowCountContributionDo extends DoEntity implements IDoEntityContribution {

  public MaxRowCountContributionDo() {
    withOverride(0);
    withHint(0);
  }

  /**
   * @return Row count override. May be used to overrule any other limit. The server decides if this value is respected
   * or not. A value <= 0 means no override.
   */
  public DoValue<Integer> override() {
    return doValue("override");
  }

  /**
   * @return Maximum number of rows the client would like to load. The server decides if this value is respected or not.
   * A value <= 0 means no limit.
   */
  public DoValue<Integer> hint() {
    return doValue("hint");
  }

  /**
   * Set to false if the server should not try to estimate the actual row count (if the result is limited). If the value
   * is true or not set, then the server <em>tries</em> to estimate the value.
   */
  public DoValue<Boolean> estimateRowCountHint() {
    return doValue("estimateRowCountHint");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  /**
   * See {@link #override()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public MaxRowCountContributionDo withOverride(Integer override) {
    override().set(override);
    return this;
  }

  /**
   * See {@link #override()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public Integer getOverride() {
    return override().get();
  }

  /**
   * See {@link #hint()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public MaxRowCountContributionDo withHint(Integer hint) {
    hint().set(hint);
    return this;
  }

  /**
   * See {@link #hint()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public Integer getHint() {
    return hint().get();
  }

  /**
   * See {@link #estimateRowCountHint()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public MaxRowCountContributionDo withEstimateRowCountHint(Boolean estimateRowCountHint) {
    estimateRowCountHint().set(estimateRowCountHint);
    return this;
  }

  /**
   * See {@link #estimateRowCountHint()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getEstimateRowCountHint() {
    return estimateRowCountHint().get();
  }

  /**
   * See {@link #estimateRowCountHint()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public boolean isEstimateRowCountHint() {
    return nvl(getEstimateRowCountHint());
  }
}
