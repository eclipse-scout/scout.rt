/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.util.StringUtility;

public class CompactBean {
  private String m_title;
  private CompactLine m_titleLine;
  private String m_titleSuffix;
  private CompactLine m_titleSuffixLine;
  private String m_subtitle;
  private CompactLine m_subtitleLine;
  private String m_content;
  private String m_moreContent;
  private List<CompactLine> m_contentLines;
  private Map<Object, String> m_customData;

  public CompactBean() {
    m_title = "";
    m_titleSuffix = "";
    m_subtitle = "";
    m_content = "";
    m_moreContent = "";
    m_customData = new HashMap<>();
    m_contentLines = new ArrayList<>();
  }

  public String getTitle() {
    return m_title;
  }

  public void setTitle(String title) {
    m_title = title;
  }

  public CompactLine getTitleLine() {
    return m_titleLine;
  }

  public void setTitleLine(CompactLine titleLine) {
    m_titleLine = titleLine;
  }

  public void setTitleSuffix(String titleSuffix) {
    m_titleSuffix = titleSuffix;
  }

  public String getTitleSuffix() {
    return m_titleSuffix;
  }

  public CompactLine getTitleSuffixLine() {
    return m_titleSuffixLine;
  }

  public void setTitleSuffixLine(CompactLine titleSuffixLine) {
    m_titleSuffixLine = titleSuffixLine;
  }

  public String getSubtitle() {
    return m_subtitle;
  }

  public void setSubtitle(String subtitle) {
    m_subtitle = subtitle;
  }

  public CompactLine getSubtitleLine() {
    return m_subtitleLine;
  }

  public void setSubtitleLine(CompactLine subtitleLine) {
    m_subtitleLine = subtitleLine;
  }

  public void addContentLine(CompactLine line) {
    m_contentLines.add(line);
  }

  public List<CompactLine> getContentLines() {
    return m_contentLines;
  }

  public String getContent() {
    return m_content;
  }

  public void setContent(String content) {
    m_content = content;
  }

  public String getMoreContent() {
    return m_moreContent;
  }

  public void setMoreContent(String moreContent) {
    m_moreContent = moreContent;
  }

  public Map<Object, String> getCustomData() {
    return m_customData;
  }

  public void setCustomData(Map<Object, String> customData) {
    m_customData = customData;
  }

  /**
   * Converts the compact lines into strings and fills the responding properties (title, subtitle, content, more
   * content).
   *
   * @param removeEmptyContentLines
   *     true, to remove empty content lines.
   * @param maxContentLines
   *     the number of content lines to consider. Empty lines are not counted.
   * @param moreLinkAvailable
   *     if true, maxContentLines may be increased by 1 if the more link would reveal only one line. Does not have
   *     any effect if it is false.
   */
  public void transform(boolean removeEmptyContentLines, int maxContentLines, boolean moreLinkAvailable) {
    CompactLine titleLine = getTitleLine();
    if (titleLine != null) {
      setTitle(titleLine.build());
    }

    CompactLine titleSuffixLine = getTitleSuffixLine();
    if (titleSuffixLine != null) {
      setTitleSuffix(titleSuffixLine.build());
    }

    CompactLine subtitleLine = getSubtitleLine();
    if (subtitleLine != null) {
      setSubtitle(subtitleLine.build());
    }

    List<CompactLine> contentLines = getContentLines();
    if (removeEmptyContentLines) {
      contentLines = contentLines.stream().filter(line -> !StringUtility.isNullOrEmpty(line.build())).collect(Collectors.toList());
    }
    if (moreLinkAvailable && maxContentLines + 1 == contentLines.size()) {
      // Don't show more link if it would only reveal one element
      maxContentLines++;
    }
    int contentLineEnd = Math.min(contentLines.size(), maxContentLines);
    String content = StringUtility.join("\n", contentLines.subList(0, contentLineEnd).stream().map(CompactLine::build).collect(Collectors.toList()));
    if (!StringUtility.isNullOrEmpty(content)) {
      setContent(content);
    }

    if (contentLineEnd < contentLines.size()) {
      String moreContent = StringUtility.join("\n", contentLines.subList(contentLineEnd, contentLines.size()).stream().map(CompactLine::build).collect(Collectors.toList()));
      setMoreContent(moreContent);
    }
  }
}
