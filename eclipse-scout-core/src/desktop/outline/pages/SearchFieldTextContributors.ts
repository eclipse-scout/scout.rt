/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DateField, FormField, ObjectRegistries, ObjectRegistry, SequenceBox, strings, ValueField} from '../../..';

export class SearchFilterTextContributors extends ObjectRegistry<SearchFilterTextContributor> {

  static get(): SearchFilterTextContributors {
    return ObjectRegistries.get(SearchFilterTextContributors);
  }

  static all(): SearchFilterTextContributor[] {
    return SearchFilterTextContributors.get().all();
  }
}

/**
 * Contributes a search text for a field to {@link SearchFilterTextBuilder}.
 *
 * For each field the first non-empty text of a contributor is used.
 * The text should include the label of the field and its display text, typically but not necessarily separated by ': '.
 */
export abstract class SearchFilterTextContributor {

  /**
   * Joins the given label and display text separated by ': ' but only if there is a display text.
   */
  protected _join(label: string, displayText: string): string {
    if (strings.hasText(displayText)) {
      return strings.join(': ', label, displayText);
    }
    return null;
  }

  abstract contribute(field: FormField): Promise<string>;
}

/**
 * Default contributor that handles the common fields used in a search form.
 *
 * It has a low priority so other contributors will come first by default.
 */
export class DefaultSearchFilterTextContributor extends SearchFilterTextContributor {

  async contribute(field: FormField): Promise<string> {
    let label = '';
    let text = '';
    if (field instanceof ValueField) {
      text = field.displayText;
      label = field.label;
    }
    if (field instanceof DateField) {
      text = text.replace('\n', ' ');
    }
    if (field.parent instanceof SequenceBox) {
      label = strings.join(' ', field.parent.label, label);
    }
    return this._join(label, text);
  }
}

SearchFilterTextContributors.get().register(DefaultSearchFilterTextContributor, 6_000);
