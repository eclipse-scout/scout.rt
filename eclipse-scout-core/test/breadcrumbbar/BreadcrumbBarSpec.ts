/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {BreadcrumbBar, BreadcrumbItem, scout} from '../../src';

describe('BreadcrumbBar', () => {

  let $sandbox: JQuery, session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = $('#sandbox');
  });

  describe('aria properties', () => {

    it('has items with aria role button', () => {
      let breadcrumbbar = scout.create(BreadcrumbBar, {
        parent: session.desktop,
        breadcrumbItems: [scout.create(BreadcrumbItem, {
          parent: session.desktop,
          text: 'label'
        })]
      });
      breadcrumbbar.render();
      expect(breadcrumbbar.breadcrumbItems[0].$container).toHaveAttr('role', 'button');
    });
  });
});
