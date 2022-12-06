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
import {scout, UuidPool} from '../../src/index';

describe('UuidPool', () => {
  let session, uuidPool;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();

    uuidPool = scout.create(UuidPool, {
      parent: session.desktop
    });
  });

  it('take', () => {
    uuidPool.uuids.push('115b5718-5bb7-46a7-ab41-2a843395dd5f');
    uuidPool.uuids.push('af01d7a5-bf91-485d-b4bc-6df9c2c8d0af');
    let result = uuidPool.take();
    expect(result).toBe('115b5718-5bb7-46a7-ab41-2a843395dd5f');
  });

  it('take failOnStarvation --> UUID pool exhausted', () => {
    uuidPool.failOnStarvation = true;

    // must fail
    expect(() => {
      uuidPool.take();
    }).toThrow();
  });

  it('take generateUuid', () => {
    let result = uuidPool.take();
    expect(result).not.toBeNull();
    expect(result).not.toBe('115b5718-5bb7-46a7-ab41-2a843395dd5f');
  });

  it('generateUuid', () => {
    let result = uuidPool.generateUuid();
    expect(result).not.toBeNull();
    expect(result.length).toBe(36);
  });

  it('refill & fill', () => {
    expect(uuidPool.uuids.length).toBe(0);

    let spy = jasmine.createSpy('event');
    uuidPool.on('refill', spy);
    expect(spy).not.toHaveBeenCalled();
    uuidPool.refill();
    expect(spy).toHaveBeenCalled();
  });

  it('can provide an instance', () => {
    let pool1 = UuidPool.get(session);
    let pool2 = UuidPool.get(session);
    expect(pool1).toBeDefined();
    expect(pool2).toBeDefined();
    expect(pool1).not.toBe(pool2);

    // Install as desktop add-on
    session.desktop.addOns.push(uuidPool);
    let pool3 = UuidPool.get(session);
    let pool4 = UuidPool.get(session);
    expect(pool3).toBeDefined();
    expect(pool4).toBeDefined();
    expect(pool3).toBe(uuidPool);
    expect(pool4).toBe(uuidPool);
  });
});
