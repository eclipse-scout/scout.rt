/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
export class CachedElement {
  tagName: string;
  cachedElement: HTMLElement;

  constructor(tagName: string) {
    this.tagName = tagName;
    this.cachedElement = null;
  }

  get(): HTMLElement {
    if (!this.cachedElement) {
      this.cachedElement = document.createElement(this.tagName);
    }
    return this.cachedElement;
  }
}
