/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import strings from '../../util/strings';

export default class CompactBean {

  constructor() {
    this.title = '';
    this.titleSuffix = '';
    this.subtitle = '';
    this.content = '';
    this.moreContent = '';
    this.contentLines = [];
  }

  /**
   * @param {string} title
   */
  setTitle(title) {
    this.title = scout.nvl(title, '');
  }

  /**
   * @param {CompactLine} titleLine
   */
  setTitleLine(titleLine) {
    this.titleLine = titleLine;
  }

  /**
   * @param {string} titleSuffix
   */
  setTitleSuffix(titleSuffix) {
    this.titleSuffix = scout.nvl(titleSuffix, '');
  }

  /**
   * @param {CompactLine} titleSuffixLine
   */
  setTitleSuffixLine(titleSuffixLine) {
    this.titleSuffixLine = titleSuffixLine;
  }

  /**
   * @param {string} subtitle
   */
  setSubtitle(subtitle) {
    this.subtitle = scout.nvl(subtitle, '');
  }

  /**
   * @param {CompactLine} subtitleLine
   */
  setSubtitleLine(subtitleLine) {
    this.subtitleLine = subtitleLine;
  }

  /**
   * @param {CompactLine} line
   */
  addContentLine(line) {
    this.contentLines.push(line);
  }

  /**
   * @param {string} content
   */
  setContent(content) {
    this.content = scout.nvl(content, '');
  }

  /**
   * @param {string} moreContent
   */
  setMoreContent(moreContent) {
    this.moreContent = scout.nvl(moreContent, '');
  }

  /**
   * Converts the compact lines into strings and fills the responding properties (title, subtitle, content, more
   * content).
   * @param {object} [options]
   * @param {boolean} [options.removeEmptyContentLines default true
   * @param {number} [options.maxContentLines default 1000
   */
  transform(options) {
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
