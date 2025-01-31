/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.logger;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.util.StringUtility;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.util.CachingDateFormatter;

/**
 * Layouter for logback with a fixed pattern (see {@link #doLayout(ILoggingEvent)}) and custom MDC handling. Allows to
 * define renamings of MDC keys, exclusions, inclusions and the order of MDC entries.
 * <p>
 * Main reason to use this layout instead of e.g. {@link PatternLayout} is that the entire MDC context is logged by
 * default (with default exclusion), thus not requiring to modify logback configs if a new MDC value is added. be
 * missed.
 * <p>
 * Default exclusions are
 * <ul>
 * <li>scout.user.name
 * <li>http.session.id
 * <li>http.request.method
 * <li>http.request.uri
 * </ul>
 * Own exclusions can be defined with the XML tag <code>mdcExclusion</code>. Inclusions can be defined with the XML tag
 * <code>mdcInclusion</code>
 * <p>
 * Default renamings are
 * <ul>
 * <li>scout.job.name &rarr; jobName
 * <li>subject.principal.name &rarr; principal
 * <li>scout.correlation.id &rarr; cid
 * <li>http.request.uri &rarr; httpUri
 * <li>scout.session.id &rarr; scoutSession
 * <li>scout.ui.session.id &rarr; uiSession
 * </ul>
 * Own renamings can be defined with the XML tag <code>mdcRename</code> in the format [mdc key]=[desired name].
 * <p>
 * Default order is
 * <ol>
 * <li>subject.principal.name
 * <li>scout.session.id
 * <li>scout.job.name
 * <li>scout.correlation.id
 * </ol>
 * MDC keys without a defined order are added after ordered entries sorted alphabetically. An own order can be defined
 * with the XML tag <code>mdcOrder</code>. If at least one XML tag is defined, the default order is discarded.
 */
public class FixedPatternLogbackLayout extends LayoutBase<ILoggingEvent> {

  private final CachingDateFormatter m_cachingDateFormatter = new CachingDateFormatter(CoreConstants.ISO8601_PATTERN, null);
  private final ThrowableProxyConverter m_throwableProxyConverter = new ThrowableProxyConverter();

  protected Map<String, String> m_mdcRenames = new HashMap<>();
  protected Set<String> m_mdcExclusions = new HashSet<>();
  protected boolean m_mdcOrderDefault;
  protected Map<String, Integer> m_mdcOrders = new HashMap<>();

  public FixedPatternLogbackLayout() {
    m_throwableProxyConverter.start();

    addMdcRename("scout.job.name", "jobName");
    addMdcRename("subject.principal.name", "principal");
    addMdcRename("scout.correlation.id", "cid");
    addMdcRename("http.request.uri", "httpUri");
    addMdcRename("scout.session.id", "scoutSession");
    addMdcRename("scout.ui.session.id", "uiSession");

    addMdcOrder("scout.user.name");
    addMdcOrder("subject.principal.name");
    addMdcOrder("scout.session.id");
    addMdcOrder("scout.job.name");
    addMdcOrder("scout.correlation.id");
    m_mdcOrderDefault = true; // required so that call to addMdcOrder due to logback.xml config will reset default order

    addMdcExclusion("scout.user.name"); // principal is used instead
    addMdcExclusion("http.session.id");
    addMdcExclusion("http.request.method");
    addMdcExclusion("http.request.uri");
  }

  /**
   * Adds an MDC key rename.
   *
   * @param mdcRename
   *     Format: [mdc key]=[new name]
   */
  public void addMdcRename(String mdcRename) {
    String[] parts = mdcRename.split("=", 2);
    addMdcRename(parts[0], parts[1]);
  }

  /**
   * Adds an MDC key rename.
   */
  protected void addMdcRename(String mdcKey, String newName) {
    m_mdcRenames.put(mdcKey, newName);
  }

  /**
   * Adds an MDC exclusion.
   *
   * @param mdcKey
   *     MDC key to exclude from logging.
   */
  public void addMdcExclusion(String mdcKey) {
    m_mdcExclusions.add(mdcKey);
  }

  /**
   * Adds an MDC inclusion.
   * <p>
   * This is only necessary to override a default exclusion. All non-excluded MDC keys are included by default.
   *
   * @param mdcKey
   *     MDC key to override exclusion for.
   */
  public void addMdcInclusion(String mdcKey) {
    m_mdcExclusions.remove(mdcKey);
  }

  /**
   * Adds an MDC key to be logged in the given order.
   */
  public void addMdcOrder(String mdcKey) {
    if (m_mdcOrderDefault) {
      // remove the default if a custom config is made via logback.xml
      m_mdcOrders.clear();
      m_mdcOrderDefault = false;
    }
    int size = m_mdcOrders.size();
    m_mdcOrders.put(mdcKey, size);
  }

  /**
   * Creates a pattern similar as the one used by {@link PatternLayout} with:
   * <code>%date{ISO8601} %-5level [%thread] %logger.%method\(%file:%line\) - %msg - MDC[${mdc}]%n%ex</code>
   */
  @Override
  public String doLayout(ILoggingEvent event) {
    StackTraceElement[] cda = event.getCallerData();
    String methodName = CoreConstants.NA;
    String file = CoreConstants.NA;
    String lineNumber = CoreConstants.NA;
    if (cda != null && cda.length > 0) {
      methodName = cda[0].getMethodName();
      lineNumber = Integer.toString(cda[0].getLineNumber());
      file = cda[0].getFileName();
    }

    StringBuilder builder = new StringBuilder();
    builder.append(m_cachingDateFormatter.format(event.getTimeStamp()));
    builder.append(" ");
    builder.append(StringUtility.rpad(event.getLevel().toString(), " ", 5));
    builder.append(" [");
    builder.append(event.getThreadName());
    builder.append("] ");
    builder.append(event.getLoggerName());
    builder.append(".");
    builder.append(methodName);
    builder.append("(");
    builder.append(file);
    builder.append(":");
    builder.append(lineNumber);
    builder.append(")");
    builder.append(" - ");
    builder.append(event.getFormattedMessage());

    Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();
    if (!mdcPropertyMap.isEmpty()) {
      List<String> mdcKeys = mdcPropertyMap.keySet().stream()
          .filter(mdc -> !m_mdcExclusions.contains(mdc)) // omit excluded MDC keys
          .sorted(Comparator.comparing(mdcKey -> m_mdcOrders.getOrDefault((String) mdcKey, Integer.MAX_VALUE)).thenComparing(mdcKey -> (String) mdcKey))
          .collect(Collectors.toList());

      StringBuilder mdcBuilder = new StringBuilder();
      for (String mdcKey : mdcKeys) {
        String mdcValue = mdcPropertyMap.get(mdcKey);
        if (StringUtility.isNullOrEmpty(mdcValue)) {
          continue;
        }

        if (mdcBuilder.length() > 0) {
          mdcBuilder.append(", ");
        }

        String name = m_mdcRenames.getOrDefault(mdcKey, mdcKey);
        mdcBuilder.append(name);
        mdcBuilder.append("=");
        mdcBuilder.append(mdcValue);
      }

      if (mdcBuilder.length() > 0) {
        builder.append(" - MDC[");
        builder.append(mdcBuilder);
        builder.append("]");
      }
    }

    builder.append("\n");
    builder.append(m_throwableProxyConverter.convert(event));

    return builder.toString();
  }
}
