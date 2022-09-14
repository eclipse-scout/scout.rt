/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {FormSpecHelper} from '../../src/testing/index';
import {scout, StringField, WidgetTooltip} from '../../src/index';

describe('WidgetTooltipSpec', () => {

  let session, helper;

  beforeEach(() => {
    jasmine.clock().install();

    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);

    // Add class desktop to sandbox, tooltip will be added to closest desktop
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
        objectType: 'StringField',
        value: 'Test1'
      },
      cssClass: 'special-tooltip'
    });
    tooltip.render();

    expect(tooltip.rendered).toBe(true);
    expect(tooltip.content instanceof StringField).toBe(true);
    expect(tooltip.content.rendered).toBe(true);
    expect(tooltip.content.$field.val()).toBe('Test1');
    expect(tooltip.$container).toHaveClass('special-tooltip');

    tooltip.remove();
    expect(tooltip.rendered).toBe(false);
    expect(tooltip.content.rendered).toBe(false);

    tooltip.render();
    tooltip.content.$field.val('Test2');

    expect(tooltip.rendered).toBe(true);
    expect(tooltip.content.rendered).toBe(true);

    tooltip.destroy();
    expect(tooltip.destroyed).toBe(true);
    expect(tooltip.content.destroyed).toBe(true);
    expect(tooltip.content.value).toBe('Test2');
  });

});
