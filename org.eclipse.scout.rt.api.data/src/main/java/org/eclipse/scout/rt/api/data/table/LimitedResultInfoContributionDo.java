/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.table;

import java.util.Optional;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.ContributesTo;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.IDoEntityContribution;
import org.eclipse.scout.rt.dataobject.TypeName;

@ContributesTo(IDoEntity.class)
@TypeName("scout.LimitedResultInfoContribution")
public class LimitedResultInfoContributionDo extends DoEntity implements IDoEntityContribution {

  /**
   * Specifies, that the data returned by a {@link IDoEntity} does not contain all data but has been limited.
   *
   * @return {@code true} if the result is limited, {@code false} otherwise.
   */
  public DoValue<Boolean> limitedResult() {
    return doValue("limitedResult");
  }

  /**
   * @return the maximum rows the server allowed to load. A value <= 0 means no limit.
   */
  public DoValue<Integer> maxRowCount() {
    return doValue("maxRowCount");
  }

  /**
   * @return an estimation of the total available row count. A value <= 0 means the total number of rows is unknown.
   */
  public DoValue<Integer> estimatedRowCount() {
    return doValue("estimatedRowCount");
  }

  /* **************************************************************************
   * HELPER METHODS
   * *************************************************************************/

  /**
   * Retrieves the {@link LimitedResultInfoContributionDo} from the contributions of the given {@link IDoEntity} if
   * present.
   *
   * @param entity
   *     The {@link IDoEntity} to get the {@link LimitedResultInfoContributionDo} from. May be {@code null}.
   * @return An {@link Optional} holding the {@link LimitedResultInfoContributionDo} or an empty {@link Optional} if the
   * contribution could not be found.
   */
  public static Optional<LimitedResultInfoContributionDo> of(IDoEntity entity) {
    return Optional.ofNullable(entity)
        .map(e -> e.getContribution(LimitedResultInfoContributionDo.class));
  }

  /**
   * Checks if the given {@link IDoEntity} contains a limited result. This means a
   * {@link LimitedResultInfoContributionDo} is attached having {@link #isLimitedResult()} set to {@code true}.
   *
   * @param entity
   *          The {@link IDoEntity} to check or {@code null}.
   * @return {@code true} if the entity contains a {@link LimitedResultInfoContributionDo} having
   *         {@link LimitedResultInfoContributionDo#isLimitedResult()} set to true.
   */
  public static boolean isLimited(IDoEntity entity) {
    return of(entity)
        .filter(LimitedResultInfoContributionDo::isLimitedResult)
        .isPresent();
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  /**
   * See {@link #limitedResult()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public LimitedResultInfoContributionDo withLimitedResult(Boolean limitedResult) {
    limitedResult().set(limitedResult);
    return this;
  }

  /**
   * See {@link #limitedResult()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getLimitedResult() {
    return limitedResult().get();
  }

  /**
   * See {@link #limitedResult()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public boolean isLimitedResult() {
    return nvl(getLimitedResult());
  }

  /**
   * See {@link #maxRowCount()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public LimitedResultInfoContributionDo withMaxRowCount(Integer maxRowCount) {
    maxRowCount().set(maxRowCount);
    return this;
  }

  /**
   * See {@link #maxRowCount()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public Integer getMaxRowCount() {
    return maxRowCount().get();
  }

  /**
   * See {@link #estimatedRowCount()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public LimitedResultInfoContributionDo withEstimatedRowCount(Integer estimatedRowCount) {
    estimatedRowCount().set(estimatedRowCount);
    return this;
  }

  /**
   * See {@link #estimatedRowCount()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public Integer getEstimatedRowCount() {
    return estimatedRowCount().get();
  }
}
