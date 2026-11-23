/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import $ from 'jquery';

declare global {
  /**
   * Replaces the content of the fixture container with the given HTML. The container is appended to the body and removed after each spec.
   * @returns the fixture container
   */
  function setFixtures(html: string | JQuery | HTMLElement): JQuery;

  /**
   * Creates a new element &lt;div id="sandbox"> with the given attributes. Use it together with {@link setFixtures}.
   */
  function sandbox(attributes?: Record<string, string>): JQuery;
}

const CONTAINER_ID = 'jasmine-fixtures';

export const specFixtures = {

  set(html: string | JQuery | HTMLElement): JQuery {
    specFixtures.cleanUp();
    return $('<div>')
      .attr('id', CONTAINER_ID)
      .append(html)
      .appendTo(document.body);
  },

  sandbox(attributes?: Record<string, string>): JQuery {
    return $('<div id="sandbox">').attr(attributes || {});
  },

  /**
   * Removes the fixture container.
   */
  cleanUp() {
    $('#' + CONTAINER_ID).remove();
  }
};

window.setFixtures = html => specFixtures.set(html);
window.sandbox = attributes => specFixtures.sandbox(attributes);
