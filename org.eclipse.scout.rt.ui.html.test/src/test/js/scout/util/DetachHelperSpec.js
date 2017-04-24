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
describe("DetachHelper", function() {
  var $sandbox, session;

  beforeEach(function() {
    setFixtures(sandbox());
    $sandbox = $('#sandbox');
    jasmine.Ajax.install();
    session = sandboxSession();
  });

  afterEach(function() {
    jasmine.Ajax.uninstall();
  });

  it("restores sticky tooltips", function() {
    var $tooltip,
      detachHelper = new scout.DetachHelper(session),
      $div = $('<div>').appendTo($sandbox),
      $anchor = $('<div>').appendTo($div)
      .cssLeft(50)
      .cssTop(50)
      .width(20)
      .height(20);

    var tooltip = scout.create('Tooltip', {
      parent: session.desktop,
      session: session,
      text: 'hello',
      $anchor: $anchor
    });
    tooltip.render($sandbox);

    $tooltip = $('.tooltip');
    expect($tooltip.length).toBe(1);

    detachHelper.beforeDetach($div);
    $div.detach();
    $tooltip = $('.tooltip');
    expect($tooltip.length).toBe(0);

    $div.appendTo($sandbox);
    detachHelper.afterAttach($div);
    $tooltip = $('.tooltip');
    expect($tooltip.length).toBe(1);
  });

  it("removes tooltip when achor is detached", function() {
    var $tooltip,
      detachHelper = new scout.DetachHelper(session),
      $div = $('<div>').appendTo($sandbox),
      $anchor = $('<div>').appendTo($div)
      .cssLeft(50)
      .cssTop(50)
      .width(20)
      .height(20);

    detachHelper.beforeDetach($div);
    $div.detach();
    $tooltip = $('.tooltip');
    expect($tooltip.length).toBe(0);

    var tooltip = scout.create('Tooltip', {
      parent: session.desktop,
      session: session,
      text: 'hello',
      $anchor: $anchor
    });
    tooltip.render($sandbox);
    $tooltip = $('.tooltip');
    expect(tooltip.rendered).toBe(false); // because $div and $anchor are not attached
    expect($tooltip.length).toBe(0);

    $div.appendTo($sandbox);
    tooltip._afterAttach(); // this would be typically called when scout.Form is attached again

    $tooltip = $('.tooltip');
    expect(tooltip.rendered).toBe(true); // because $div is now attached
    expect($tooltip.length).toBe(1);
  });

  it("considers the context of $anchor -> only removes tooltips in that context", function() {
    var $tooltip,
      detachHelper = new scout.DetachHelper(session),
      $div = $('<div>').appendTo($sandbox),
      $topLevelAnchor = $('<div>').appendTo($sandbox),
      $anchor = $('<div>').appendTo($div)
      .cssLeft(50)
      .cssTop(50)
      .width(20)
      .height(20);

    var topLevelTooltip = scout.create('Tooltip', {
      parent: session.desktop,
      session: session,
      text: 'top level',
      $anchor: $topLevelAnchor
    });
    topLevelTooltip.render($sandbox);

    var tooltip = scout.create('Tooltip', {
      parent: session.desktop,
      session: session,
      text: 'hello',
      $anchor: $anchor
    });
    tooltip.render($sandbox);

    $tooltip = $('.tooltip');
    expect($tooltip.length).toBe(2);

    detachHelper.beforeDetach($div);
    $div.detach();
    $tooltip = $('.tooltip');
    expect($tooltip.length).toBe(1);

    $div.appendTo($sandbox);
    detachHelper.afterAttach($div);
    $tooltip = $('.tooltip');
    expect($tooltip.length).toBe(2);
  });
});
