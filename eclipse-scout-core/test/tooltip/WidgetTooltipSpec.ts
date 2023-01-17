/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormSpecHelper} from '../../src/testing/index';
import {scout, StringField, WidgetTooltip} from '../../src/index';

describe('WidgetTooltipSpec', () => {

  let session: SandboxSession, helper: FormSpecHelper;

  beforeEach(() => {
    jasmine.clock().install();

    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);

    // Add class desktop to sandbox, tooltip will be added to the closest desktop
    session.$entryPoint.addClass('desktop');
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  it('can create and hold a widget', () => {
    let tooltip = scout.create(WidgetTooltip, {
      parent: session.desktop,
      $anchor: session.desktop.$container,
      content: {
        objectType: StringField,
        value: 'Test1'
      },
      cssClass: 'special-tooltip'
    });
    tooltip.render();

    expect(tooltip.rendered).toBe(true);
    let content = tooltip.content as StringField;
    expect(content instanceof StringField).toBe(true);
    expect(content.rendered).toBe(true);
    expect(content.$field.val()).toBe('Test1');
    expect(tooltip.$container).toHaveClass('special-tooltip');

    tooltip.remove();
    expect(tooltip.rendered).toBe(false);
    expect(content.rendered).toBe(false);

    tooltip.render();
    content.$field.val('Test2');

    expect(tooltip.rendered).toBe(true);
    expect(content.rendered).toBe(true);

    tooltip.destroy();
    expect(tooltip.destroyed).toBe(true);
    expect(content.destroyed).toBe(true);
    expect(content.value).toBe('Test2');
  });

});
