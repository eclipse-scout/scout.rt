/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {DateFormat, DateFormatContext, DateFormatParseContext, DateFormatPatternTypes} from '../index';

export interface DateFormatPatternDefinitionOptions {
  /**
   * The "group" where the pattern definition belongs to. E.g. "dd" and "d" belong to the same group "day".
   * This is used during analysis to find other definitions for the same type. Use one of the constants defined in {@link DateFormatPatternType}.
   */
  type?: DateFormatPatternTypes;

  /**
   * An array consisting of all pattern terms that this particular definition can handle.
   * Multiple terms with the same meaning may be accepted (e.g. "yyy", "yy" and "y" can be all used for a 2-digit year formatting, but different parsing rules may apply).
   */
  terms?: string[];

  /**
   * Reference to the corresponding dateFormat object.
   */
  dateFormat?: DateFormat;

  /**
   * An optional function that is used to format this particular term.
   *   @param formatContext
   *            See documentation at _createFormatContext().
   *   @param acceptedTerm
   *            The term that was accepted for this definition. This argument is usually only relevant,
   *            if a definition can accept more than one term.
   *   @returns The function may return a string as result. If a string is returned, it is appended to the formatContext.formattedString automatically. If the formatFunction already did this, it should return "undefined".
   */
  formatFunction?: (formatContext: DateFormatContext, acceptedTerm: string) => string;

  /**
   * A optional JavaScript RegExp object that is applied to the input string to extract this definition's term.
   * The expression _must_ use exactly two capturing groups:
   *   [1] = matched part of the input
   *   [2] = remaining input (will be parsed later by other definitions)
   * Example: /^(\d{4})(.*)$/
   */
  parseRegExp?: RegExp;

  /**
   * If 'parseRegExp' is set and found a match, and this function is defined, it is called to apply the matched part to the parseContext.
   *   @param parseContext
   *            See documentation at _createParseContext().
   *   @param match
   *            The first match from the reg exp.
   *   @param acceptedTerm
   *            The term that was accepted for this definition. This argument is usually only relevant, if a definition can accept more than one term.
   */
  applyMatchFunction?: (parseContext: DateFormatParseContext, match: string, acceptedTerm: string) => void;

  /**
   * If parsing is not possible with a regular expression, this function may be defined to execute more complex parse logic.
   *   @param parseContext
   *            See documentation at _createParseContext().
   *   @param acceptedTerm
   *            The term that was accepted for this definition. This argument is usually only relevant, if a definition can accept more than one term.
   *   @returns A string with the matched part of the input, or null if it did not match.
   */
  parseFunction?: (parseContext: DateFormatParseContext, acceptedTerm: string) => string;
}

/**
 * Definition of a date format pattern.
 */
export class DateFormatPatternDefinition implements DateFormatPatternDefinitionOptions {
  type: DateFormatPatternTypes;
  terms: string[];
  dateFormat: DateFormat;
  formatFunction: (formatContext: DateFormatContext, acceptedTerm: string) => string;
  parseRegExp: RegExp;
  applyMatchFunction: (parseContext: DateFormatParseContext, match: string, acceptedTerm: string) => void;
  parseFunction: (parseContext: DateFormatParseContext, acceptedTerm: string) => string;

  constructor(options: DateFormatPatternDefinitionOptions) {
    options = options || {};
    this.type = options.type;
    this.terms = options.terms;
    this.dateFormat = options.dateFormat;
    this.formatFunction = options.formatFunction && options.formatFunction.bind(this);
    this.parseRegExp = options.parseRegExp;
    this.applyMatchFunction = options.applyMatchFunction && options.applyMatchFunction.bind(this);
    this.parseFunction = options.parseFunction && options.parseFunction.bind(this);
  }

  createFormatFunction(acceptedTerm: string): (formatContext: DateFormatContext) => void {
    return function(formatContext: DateFormatContext) {
      if (this.formatFunction) {
        let result = this.formatFunction(formatContext, acceptedTerm);
        if (result !== undefined) { // convenience
          formatContext.formattedString += result;
        }
      }
    }.bind(this);
  }

  createParseFunction(acceptedTerm: string): (parseContext: DateFormatParseContext) => boolean {
    return function(parseContext: DateFormatParseContext) {
      let m: RegExpExecArray, parsedTerm, match;

      let success = false;
      if (this.parseRegExp) {
        // RegEx handling (default)
        m = this.parseRegExp.exec(parseContext.inputString);
        if (m) { // match found
          if (this.applyMatchFunction) {
            this.applyMatchFunction(parseContext, m[1], acceptedTerm);
          }
          match = m[1];
          // update remaining string
          parseContext.inputString = m[2];
          success = true;
        }
      }
      if (!success && this.parseFunction) {
        // Custom function
        match = this.parseFunction(parseContext, acceptedTerm);
        if (match !== null) {
          success = true;
        }
      }

      if (success) {
        // If patternDefinition accepts more than one term, try to choose
        // the form that matches the length of the match.
        parsedTerm = this.terms[0];
        if (this.terms.length > 1) {
          this.terms.some(term => {
            if (term.length === match.length) {
              parsedTerm = term;
              return true; // found
            }
            return false; // look further
          });
        }
        parseContext.parsedPattern += parsedTerm;
      }
      return success;
    }.bind(this);
  }

  /**
   * @returns the accepted term (if is accepted) or null (if it is not accepted)
   */
  accept(term: string): string {
    if (term) {
      // Check if one of the terms matches
      for (let i = 0; i < this.terms.length; i++) {
        if (term === this.terms[i]) {
          return this.terms[i];
        }
      }
    }
    return null;
  }
}
