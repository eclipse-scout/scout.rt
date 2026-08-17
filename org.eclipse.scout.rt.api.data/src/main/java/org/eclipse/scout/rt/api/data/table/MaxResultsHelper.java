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

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.BooleanUtility;
import org.eclipse.scout.rt.platform.util.NumberUtility;

/**
 * Helper for computing and applying max results on selected data.
 */
@ApplicationScoped
public class MaxResultsHelper {

  /**
   * Default maximum number of rows returned if no other limit is given.
   */
  public static final int DEFAULT_MAX_RESULTS = 2000;

  /**
   * Creates a {@link ResultLimiter} which uses the value in a {@link MaxRowCountContributionDo} attached to the given
   * {@link IDoEntity}.
   *
   * @param dataObject
   *     The {@link IDoEntity} in which the {@link MaxRowCountContributionDo} is searched.
   * @return A {@link ResultLimiter} which respects the hints given in a {@link MaxRowCountContributionDo} attached to
   * the given {@link IDoEntity}. If no limit is given, the {@link #DEFAULT_MAX_RESULTS} is used.
   */
  public ResultLimiter limiter(IDoEntity dataObject) {
    return limiter(getMaxResultsHintFromContribution(dataObject), getEstimateRowCountHintFromContribution(dataObject));
  }

  /**
   * Extracts and returns the {@link MaxRowCountContributionDo#getHint()} from the given {@link IDoEntity}.
   * If {@link MaxRowCountContributionDo#getOverride()} is set and greater than 0, it will be returned instead.
   *
   * @param dataObject
   *     The {@link IDoEntity} in which the {@link MaxRowCountContributionDo} should be searched.
   * @return The value of {@link MaxRowCountContributionDo#getHint()} attached to ghe given {@link IDoEntity} as
   * contribution or -1 if no such contribution is present.
   */
  public int getMaxResultsHintFromContribution(IDoEntity dataObject) {
    if (dataObject == null) {
      return -1;
    }

    MaxRowCountContributionDo maxRowCountContributionDo = dataObject.getContribution(MaxRowCountContributionDo.class);
    if (maxRowCountContributionDo != null) {
      Integer maxRowCountOverride = maxRowCountContributionDo.getOverride();
      if (maxRowCountOverride != null && maxRowCountOverride > 0) {
        return maxRowCountOverride;
      }

      Integer maxRowCount = maxRowCountContributionDo.getHint();
      if (maxRowCount != null && maxRowCount > 0) {
        return maxRowCount;
      }
    }

    return -1;
  }

  /**
   * Extracts the {@link MaxRowCountContributionDo#getEstimateRowCountHint()} from the given {@link IDoEntity}.
   *
   * @param dataObject
   *     The {@link IDoEntity} in which the {@link MaxRowCountContributionDo} should be searched.
   * @return The value of {@link MaxRowCountContributionDo#getEstimateRowCountHint()} attached to ghe given
   * {@link IDoEntity} as contribution.
   */
  public Boolean getEstimateRowCountHintFromContribution(IDoEntity dataObject) {
    if (dataObject != null) {
      MaxRowCountContributionDo maxRowCountContributionDo = dataObject.getContribution(MaxRowCountContributionDo.class);
      if (maxRowCountContributionDo != null) {
        return maxRowCountContributionDo.getEstimateRowCountHint();
      }
    }
    return null;
  }

  /**
   * Creates a {@link ResultLimiter} that limits to the requested limit or to {@link #getMaxResultLimit()} if it is
   * smaller that the requested value.
   *
   * @param requestedMaxResults
   *     The maximum number of rows as requested by the client. {@code null} or <= 0 means no limit. In that case
   *     the limit is given by {@link #getMaxResultLimit()}.
   * @return The created {@link ResultLimiter}
   */
  public ResultLimiter limiter(Integer requestedMaxResults) {
    return createResultLimiter(getMaxResultLimit(), NumberUtility.nvl(requestedMaxResults, -1), isEstimateRowCountEnabled());
  }

  /**
   * Creates a {@link ResultLimiter} that limits to the requested limit or to {@link #getMaxResultLimit()} if it is
   * smaller that the requested value.
   *
   * @param requestedMaxResults
   *     The maximum number of rows as requested by the client. {@code null} or <= 0 means no limit. In that case
   *     the limit is given by {@link #getMaxResultLimit()}.
   * @param estimateRowCountHint
   *     Set to false if the server should not try to estimate the actual row count (if the result is limited). If the
   *     value is true or not set, then the server <em>tries</em> to estimate the value.
   * @return The created {@link ResultLimiter}
   */
  public ResultLimiter limiter(Integer requestedMaxResults, Boolean estimateRowCountHint) {
    return createResultLimiter(getMaxResultLimit(), NumberUtility.nvl(requestedMaxResults, -1), BooleanUtility.nvl(estimateRowCountHint, isEstimateRowCountEnabled()));
  }

  protected ResultLimiter createResultLimiter(int maxResultsLimit, int requestedMaxResults, boolean estimateRowCountEnabled) {
    return new ResultLimiter(maxResultsLimit, requestedMaxResults, estimateRowCountEnabled);
  }

  /**
   * @return The default upper limit for the number of rows. Used if no row limit is requested or the requested limit
   * value is too high.
   */
  public int getMaxResultLimit() {
    return DEFAULT_MAX_RESULTS;
  }

  protected boolean isEstimateRowCountEnabled() {
    return true; // method hook
  }

  public static class ResultLimiter {

    private int m_maxResultsLimit; // always > 0
    private int m_requestedMaxResults; // <= 0 if not specified
    private boolean m_estimateRowCountEnabled;
    private int m_estimatedRowCount; // <= 0 if not specified
    private LimitedResultInfoContributionDo m_limitedResultInfoContributionDo;

    /**
     * @param maxResultsLimit
     *     The maximum number of results (hard limit). Must be > 0.
     */
    public ResultLimiter(int maxResultsLimit) {
      this(maxResultsLimit, -1);
    }

    /**
     * @param maxResultsLimit
     *     The maximum number of results (hard limit). Must be > 0.
     * @param requestedMaxResults
     *     The maximum number of results as requested by the client (client limit). May be <= 0 if the client has
     *     no preference.
     */
    public ResultLimiter(int maxResultsLimit, int requestedMaxResults) {
      this(maxResultsLimit, requestedMaxResults, true, -1);
    }

    /**
     * @param maxResultsLimit
     *     The maximum number of results (hard limit). Must be > 0.
     * @param requestedMaxResults
     *     The maximum number of results as requested by the client (client limit). May be <= 0 if the client has
     *     no preference.
     * @param estimateRowCountEnabled
     *     true if the server should try to estimate row count
     */
    public ResultLimiter(int maxResultsLimit, int requestedMaxResults, boolean estimateRowCountEnabled) {
      this(maxResultsLimit, requestedMaxResults, estimateRowCountEnabled, -1);
    }

    /**
     * @param maxResultsLimit
     *     The maximum number of results (hard limit). Must be > 0.
     * @param requestedMaxResults
     *     The maximum number of results as requested by the client (client limit). May be <= 0 if the client has
     *     no preference.
     * @param estimateRowCountEnabled
     *     true if the server should try to estimate row count
     * @param estimatedRowCount
     *     The estimated number of results that would be available in total. May be <= 0 in case it is unknown.
     */
    public ResultLimiter(int maxResultsLimit, int requestedMaxResults, boolean estimateRowCountEnabled, int estimatedRowCount) {
      setMaxResultsLimit(maxResultsLimit);
      setRequestedMaxResults(requestedMaxResults);
      setEstimateRowCountEnabled(estimateRowCountEnabled);
      setEstimatedRowCount(estimatedRowCount);
    }

    /**
     * @return the number of records to be selected from the <em>repository</em>. The value is max results + 1.
     */
    public int getQueryLimit() {
      return getMaxResults() + 1;
    }

    /**
     * @return the maximum number of rows that are allowed in this limiter. Either defined by a requested limit
     * ({@link #getRequestedMaxResults()}), or the hard limit {@link #getMaxResultLimit()} in case there is no
     * requested limit or the requested limit is too high.
     */
    public int getMaxResults() {
      int requestedMaxResults = getRequestedMaxResults();
      int maxResultsLimit = getMaxResultsLimit();
      if (requestedMaxResults <= 0) {
        // no requested max result: use max results limit
        return maxResultsLimit;
      }
      return Math.min(requestedMaxResults, maxResultsLimit);
    }

    /**
     * Removes surplus items from the data {@link List} if the maximum number of results is exceeded and attaches a
     * {@link LimitedResultInfoContributionDo} as contribution to the given {@link IDoEntity}. The contribution contains
     * metadata about the limit operation:
     * <ol>
     * <li>If surplus items have been removed from the list
     * ({@link LimitedResultInfoContributionDo#isLimitedResult()})</li>
     * <li>The maximum number of entries that are allowed
     * ({@link LimitedResultInfoContributionDo#getMaxRowCount()})</li>
     * <li>({@link LimitedResultInfoContributionDo#getEstimatedRowCount()}) An estimation how may rows would be
     * available. Must be set using {@link #setEstimatedRowCount(int)}.</li>
     * </ol>
     * <p>
     * <b>Note</b>: The given data {@link List} is directly modified! No new list is created! The returned list is the
     * (limited) input list.
     *
     * @param data
     *     The list in which the surplus items should be removed. Therefore, the list must be modifiable!
     * @param response
     *     The {@link IDoEntity} to which the {@link LimitedResultInfoContributionDo} should be attached.
     * @return The input data {@link List} with all surplus items removed.
     */
    public <E, L extends List<E>> L limit(L data, IDoEntity response) {
      Consumer<Boolean> limitedResultsConsumer = null;
      LimitedResultInfoContributionDo contribution = null;
      if (response != null) {
        contribution = response.contribution(LimitedResultInfoContributionDo.class)
            .withMaxRowCount(getMaxResultsLimit())
            .withEstimatedRowCount(-1);
        limitedResultsConsumer = contribution::withLimitedResult;
      }
      L result = limit(data, limitedResultsConsumer);
      if (contribution != null && contribution.isLimitedResult()) {
        // only set estimation if limited
        contribution.withEstimatedRowCount(getEstimatedRowCount());
      }
      m_limitedResultInfoContributionDo = contribution;
      return result;
    }

    /**
     * Removes surplus items from the data {@link List} if the maximum number of results is exceeded and reports to the
     * optional consumer whether the values were limited.
     * <p>
     * <b>Note</b>: The given data {@link List} is directly modified! No new list is created! The returned list is the
     * (limited) input list.
     *
     * @param data
     *     The list in which the surplus items should be removed. Therefore, the list must be modifiable!
     * @param limitedResultsConsumer
     *     An optional consumer to report if the list has been limited (modified) or not. May be {@code null}.
     * @return The input data {@link List} with all surplus items removed.
     */
    public <E, L extends List<E>> L limit(L data, Consumer<Boolean> limitedResultsConsumer) {
      boolean limited = false;
      int maxResults = getMaxResults();
      if (data != null && data.size() > maxResults) {
        limited = true;
        data.subList(maxResults, data.size()).clear();
      }
      if (limitedResultsConsumer != null) {
        limitedResultsConsumer.accept(limited);
      }
      return data;
    }

    /**
     * @return The hard limit of results. Is always > 0 and defines the maximum upper bound this limiter may return.
     */
    public int getMaxResultsLimit() {
      return m_maxResultsLimit;
    }

    /**
     * Sets the hard limit. This limiter will never return more results than this upper bound.
     *
     * @param maxResultsLimit
     *     The new hard limit. Must be > 0.
     */
    public void setMaxResultsLimit(int maxResultsLimit) {
      Assertions.assertTrue(maxResultsLimit > 0, "maxResultsLimit must be greater than 0, but was {}.", maxResultsLimit);
      m_maxResultsLimit = maxResultsLimit;
    }

    /**
     * @return The maximum number of results as requested by the client. <= 0 if the client has no preference.
     */
    public int getRequestedMaxResults() {
      return m_requestedMaxResults;
    }

    /**
     * @param requestedMaxResults
     *     The maximum number of results as requested by the client.
     */
    public void setRequestedMaxResults(int requestedMaxResults) {
      m_requestedMaxResults = requestedMaxResults;
    }

    /**
     * @return true if the server should try to estimate row count
     */
    public boolean isEstimateRowCountEnabled() {
      return m_estimateRowCountEnabled;
    }

    /**
     * @param estimateRowCountEnabled
     *     If the server should try to estimate row count.
     */
    public void setEstimateRowCountEnabled(boolean estimateRowCountEnabled) {
      m_estimateRowCountEnabled = estimateRowCountEnabled;
    }

    /**
     * @return The estimated row count if set or <= 0 if no estimation is available.
     */
    public int getEstimatedRowCount() {
      return m_estimatedRowCount;
    }

    /**
     * Sets a row count estimation which will be added to the {@link LimitedResultInfoContributionDo} when limiting. May
     * be <= 0 if the estimation is unknown.
     */
    public void setEstimatedRowCount(int estimatedRowCount) {
      m_estimatedRowCount = estimatedRowCount;
    }

    /**
     * @return The {@link LimitedResultInfoContributionDo} or {@code null}. A value can be set by calling {@link #limit(List, IDoEntity)} or {@link #setLimitedResultInfoContributionDo(LimitedResultInfoContributionDo)}.
     */
    public LimitedResultInfoContributionDo getLimitedResultInfoContributionDo() {
      return m_limitedResultInfoContributionDo;
    }

    /**
     * Sets the created {@link LimitedResultInfoContributionDo}.
     *
     * @param limitedResultInfoContributionDo
     *     the new {@link LimitedResultInfoContributionDo} or {@code null}.
     */
    public void setLimitedResultInfoContributionDo(LimitedResultInfoContributionDo limitedResultInfoContributionDo) {
      m_limitedResultInfoContributionDo = limitedResultInfoContributionDo;
    }

    /**
     * Writes the current {@link #getLimitedResultInfoContributionDo()} to the given {@link IDoEntity}.
     *
     * @param response
     *     The {@link IDoEntity} that should receive the {@link LimitedResultInfoContributionDo}. May be {@code null}, then this method does nothing.
     * @return The input {@link IDoEntity}.
     */
    public <T extends IDoEntity> T applyResultInfoTo(T response) {
      if (response != null) {
        LimitedResultInfoContributionDo resultInfo = getLimitedResultInfoContributionDo();
        if (resultInfo != null) {
          response.putContribution(resultInfo);
        }
      }
      return response;
    }
  }
}
