/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Form, FormField, SearchFilterTextContributors, TreeVisitResult} from '../../..';

/**
 * Builds a text for the given search form containing the display texts of the fields having a value separated by newlines.
 * The text can be used to show what search conditions are used for a specific search.
 *
 * The search text for each field is built by a {@link SearchFilterTextContributor}.
 */
export class SearchFilterTextBuilder {

  async build(searchForm: Form): Promise<string> {
    if (!searchForm) {
      return null;
    }

    let fields = this._collectFields(searchForm);
    let texts = await this._buildTexts(fields);
    return texts.join('\n');
  }

  protected async _buildTexts(fields: any[]): Promise<string[]> {
    let result = [];
    for (const field of fields) {
      for (const contributor of SearchFilterTextContributors.all()) {
        let text = await contributor.contribute(field);
        if (text) {
          result.push(text);
          break;
        }
      }
    }
    return result;
  }

  protected _collectFields(searchForm: Form): string[] {
    let fields = [];
    searchForm.visitFields((field: FormField) => {
      fields.push(field);

      if (field.lifecycleBoundary) {
        // Fields having this flag set don't want to expose inner fields for these kind of operations
        return TreeVisitResult.SKIP_SUBTREE;
      }
    });
    return fields;
  }
}
