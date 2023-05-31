/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BusyIndicator, BusySupport, Event, EventHandler, scout} from '../../src/index';

describe('BusySupport', () => {
  let session: SandboxSession;

  class SpecBusySupport extends BusySupport {
    declare _busyCounter: number;
    declare _cancellationCallbacks: EventHandler<Event<BusyIndicator>>[];

    override _renderBusy() {
      // NOP
    }
  }

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('indicator', () => {
    it('model can be customized', () => {
      let support = scout.create(SpecBusySupport, {
        parent: session.desktop,
        busyIndicatorModel: {
          cancellable: true,
          label: 'test'
        },
        renderDelay: 10
      });
      expect(support.defaultBusyIndicatorModel.label).toEqual('test');
      expect(support.defaultBusyIndicatorModel.cancellable).toBe(true);
      expect(support.defaultBusyIndicatorModel.parent).toBe(session.desktop);
      expect(support.defaultRenderDelay).toBe(10);
      expect(support._cancellationCallbacks.length).toBe(0);
      expect(support.isBusy()).toBe(false);

      support.setBusy({
        busy: true,
        busyIndicatorModel: {
          cancellable: false,
          label: 'test2'
        },
        onCancel: () => {
        }
      });
      expect(support.isBusy()).toBe(true);
      expect(support.busyIndicator.cancellable).toBe(false); // overwritten by setBusy
      expect(support.busyIndicator.label).toBe('test2'); // overwritten by setBusy
      expect(support._cancellationCallbacks.length).toBe(1);

      support.setBusy({
        busy: true,
        busyIndicatorModel: {
          cancellable: true,
          label: 'test3'
        },
        onCancel: () => {
        }
      });
      expect(support.isBusy()).toBe(true); // still true
      expect(support.busyIndicator.cancellable).toBe(false); // overwritten by first setBusy, no effect in second
      expect(support.busyIndicator.label).toBe('test2'); // overwritten by first setBusy, no effect in second
      expect(support._cancellationCallbacks.length).toBe(2);

      support.setBusy(false);
      support.setBusy(false);
      expect(support.isBusy()).toBe(false);
      expect(support._cancellationCallbacks.length).toBe(0); // has been reset
    });

    it('is not cancellable by default', () => {
      let support = scout.create(SpecBusySupport, {
        parent: session.desktop
      });
      expect(support.defaultBusyIndicatorModel.cancellable).toBe(false);
    });
  });

  describe('callback', () => {
    it('is executed on cancel', () => {
      let support = scout.create(SpecBusySupport, {
        parent: session.desktop
      });
      let isCancelled = false;
      support.setBusy({
        busy: true,
        onCancel: e => {
          isCancelled = true;
        }
      });
      support.busyIndicator.trigger('cancel');
      expect(isCancelled).toBe(true);
    });

    it('has no duplicates', () => {
      let support = scout.create(SpecBusySupport, {
        parent: session.desktop
      });
      let cancelCounter = 0;
      support.setBusy({
        busy: true,
        onCancel: e => {
          cancelCounter++;
        }
      });
      expect(support._cancellationCallbacks.length).toBe(1);
      let handler = e => {
        cancelCounter++;
      };
      support.setBusy({
        busy: true,
        onCancel: handler
      });
      support.setBusy({
        busy: true,
        onCancel: handler
      });
      expect(support._cancellationCallbacks.length).toBe(2);
      support.busyIndicator.trigger('cancel');
      expect(cancelCounter).toBe(2);
    });
  });

  describe('counter', () => {
    it('can be reset with "force"', () => {
      let support = scout.create(SpecBusySupport, {
        parent: session.desktop
      });
      support.setBusy(true);
      support.setBusy(true);
      expect(support._busyCounter).toBe(2);
      support.setBusy({
        busy: false,
        force: true
      });
      expect(support._busyCounter).toBe(0);
    });

    it('cannot fall below zero', () => {
      let support = scout.create(SpecBusySupport, {
        parent: session.desktop
      });
      expect(support._busyCounter).toBe(0);
      support.setBusy(false);
      expect(support._busyCounter).toBe(0);
      expect(support.busyIndicator).toBeFalsy();
      support.setBusy(true);
      expect(support._busyCounter).toBe(1);
      let busyIndicator = support.busyIndicator;
      expect(busyIndicator).toBeTruthy();
      support.setBusy(true);
      expect(support._busyCounter).toBe(2);
      expect(support.busyIndicator).toBe(busyIndicator); // still the same
      support.setBusy(false);
      expect(support._busyCounter).toBe(1);
      expect(support.busyIndicator).toBe(busyIndicator); // still the same
      support.setBusy(false);
      support.setBusy(false);
      support.setBusy(false);
      expect(support._busyCounter).toBe(0);
      expect(support.busyIndicator).toBeFalsy();
    });
  });
});
