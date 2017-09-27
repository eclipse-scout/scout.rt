/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("scout.WidgetTooltipSpec", function() {

  var session, helper;

  beforeEach(function() {
    jasmine.clock().install();

    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);

    // Add class desktop to sandbox, tooltip will be added to closest desktop
    session.$entryPoint.addClass('desktop');
  });

  afterEach(function() {
    jasmine.clock().uninstall();
  });

  it("can create and hold a widget", function() {
    var tooltip = scout.create('WidgetTooltip', {
      parent: session.desktop,
      $anchor: session.desktop.$container,
      widget: {
        objectType: 'StringField',
        value: 'Test1'
      },
      cssClass: 'special-tooltip'
    });
    tooltip.render();

    expect(tooltip.rendered).toBe(true);
    expect(tooltip.widget instanceof scout.StringField).toBe(true);
    expect(tooltip.widget.rendered).toBe(true);
    expect(tooltip.widget.$field.val()).toBe('Test1');
    expect(tooltip.$container).toHaveClass('special-tooltip');

    tooltip.remove();
    expect(tooltip.rendered).toBe(false);
    expect(tooltip.widget.rendered).toBe(false);

    tooltip.render();
    tooltip.widget.$field.val('Test2');

    expect(tooltip.rendered).toBe(true);
    expect(tooltip.widget.rendered).toBe(true);

    tooltip.destroy();
    expect(tooltip.destroyed).toBe(true);
    expect(tooltip.widget.destroyed).toBe(true);
    expect(tooltip.widget.value).toBe('Test2');
  });

});
