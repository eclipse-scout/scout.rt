/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {scout, Splitter} from '../../src/index';

describe('Splitter', () => {
  let session;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    $('<style>.splitter {position: absolute;} .with-padding {padding: 10px;}</style>').appendTo($('#sandbox'));
  });

  it('renders the splitter at the given position', () => {
    let splitter = scout.create(Splitter, {
      parent: session.desktop,
      position: 100
    });
    splitter.render();
    expect(splitter.position).toBe(100);
    expect(splitter.$container.cssLeft()).toBe(100);
  });

  it('renders can handle position changes while not visible', () => {
    let splitter = scout.create(Splitter, {
      parent: session.desktop,
      position: 123,
      cssClass: 'with-padding',
      visible: false
    });
    splitter.render();
    expect(splitter.position).toBe(123);
    expect(splitter.$container.css('left')).toBe('auto');

    splitter.setPosition(234);
    expect(splitter.position).toBe(234);
    expect(splitter.$container.css('left')).toBe('auto');

    splitter.setVisible(true);
    expect(splitter.$container.cssLeft()).toBe(224); // 10px less because of padding

    splitter.setPosition(345);
    expect(splitter.position).toBe(345);
    expect(splitter.$container.cssLeft()).toBe(335); // 10px less because of padding
  });

});
