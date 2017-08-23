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
describe('Splitter', function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    $('<style>.splitter {position: absolute;}</style>').appendTo($('#sandbox'));
  });

  it('renders the splitter at the given position', function() {
    var splitter = scout.create('Splitter', {
      parent: session.desktop,
      position: 100
    });
    splitter.render();
    expect(splitter.position).toBe(100);
    expect(splitter.$container.cssLeft()).toBe(100);
  });

});
