/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {CompactLine, scout, strings} from '../../index';

export class CompactBean {
  title: string;
  titleLine: CompactLine;

  titleSuffix: string;
  titleSuffixLine: CompactLine;

  subtitle: string;
  subtitleLine: CompactLine;

  contentLines: CompactLine[];
  content: string;
  moreContent: string;

  constructor() {
    this.title = '';
    this.titleSuffix = '';
    this.subtitle = '';
    this.content = '';
    this.moreContent = '';
    this.contentLines = [];
  }

  setTitle(title: string) {
    this.title = scout.nvl(title, '');
  }

  setTitleLine(titleLine: CompactLine) {
    this.titleLine = titleLine;
  }

  setTitleSuffix(titleSuffix: string) {
    this.titleSuffix = scout.nvl(titleSuffix, '');
  }

  setTitleSuffixLine(titleSuffixLine: CompactLine) {
    this.titleSuffixLine = titleSuffixLine;
  }

  setSubtitle(subtitle: string) {
    this.subtitle = scout.nvl(subtitle, '');
  }

  setSubtitleLine(subtitleLine: CompactLine) {
    this.subtitleLine = subtitleLine;
  }

  addContentLine(line: CompactLine) {
    this.contentLines.push(line);
  }

  setContent(content: string) {
    this.content = scout.nvl(content, '');
  }

  setMoreContent(moreContent: string) {
    this.moreContent = scout.nvl(moreContent, '');
  }

  /**
   * Converts the compact lines into strings and fills the responding properties (title, subtitle, content, moreContent).
   *
   * @param options.removeEmptyContentLines default true
   * @param options.maxContentLines default 1000
   */
  transform(options?: { removeEmptyContentLines?: boolean; maxContentLines?: number }) {
    let removeEmptyContentLines = scout.nvl(options.removeEmptyContentLines, true);
    let maxContentLines = scout.nvl(options.maxContentLines, 1000);
    if (this.titleLine) {
      this.setTitle(this.titleLine.build());
    }
    if (this.titleSuffixLine) {
      this.setTitleSuffix(this.titleSuffixLine.build());
    }
    if (this.subtitleLine) {
      this.setSubtitle(this.subtitleLine.build());
    }

    let contentLines = [];
    if (removeEmptyContentLines) {
      contentLines = this.contentLines.filter(line => !!line.build());
    }
    if (maxContentLines + 1 === contentLines.length) {
      // Don't show more link if it would only reveal one element
      maxContentLines++;
    }
    let contentLineEnd = Math.min(contentLines.length, maxContentLines);
    let content = strings.join('\n', ...contentLines.slice(0, contentLineEnd).map(line => line.build()));
    if (content) {
      this.setContent(content);
    }

    if (contentLineEnd < contentLines.length) {
      let moreContent = strings.join('\n', ...contentLines.slice(contentLineEnd, contentLines.length).map(line => line.build()));
      this.setMoreContent(moreContent);
    }
  }
}
