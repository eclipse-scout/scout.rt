/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * AI Disclosure: This file was partially AI-generated.
 * The AI-generated portions are made available under CC0-1.0
 * and not subject to the project's licence.
 *
 * SPDX-License-Identifier: EPL-2.0 and CC0-1.0
 */

import {Abortable, abortableContext, AbortError} from '../../src';

describe('abortableContext', () => {

  describe('runInContext', () => {

    it('asserts that callback is not null', () => {
      expect(() =>
        abortableContext.runInContext(
          null,
          new AbortController()
        )
      ).toThrowError('Missing required parameter \'callback\'');
    });

    it('asserts that abortController is not null', () => {
      expect(() =>
        abortableContext.runInContext(
          () => {
          },
          null
        )
      ).toThrowError('Missing required parameter \'abortController\'');
    });

    it('runs the callback and returns its result', () => {
      const controller = new AbortController();
      const result = abortableContext.runInContext(() => 42, controller);
      expect(result).toBe(42);
    });

    it('aborts registered abortables when the controller is aborted', () => {
      const controller = new AbortController();
      const abortable = new SimpleAbortable();

      abortableContext.runInContext(
        () => abortableContext.registerAbortableInCurrentContext(abortable),
        controller
      );

      expect(abortable.aborted).toBeFalse();
      controller.abort();
      expect(abortable.aborted).toBeTrue();
    });

    it('runs the callback synchronously so asynchronous registered abortables are not aborted', async () => {
      const controller = new AbortController();
      const synchronousAbortable = new SimpleAbortable();
      const asynchronousAbortable = new SimpleAbortable();
      await abortableContext.runInContext(
        async () => {
          abortableContext.registerAbortableInCurrentContext(synchronousAbortable);
          await Promise.resolve();
          abortableContext.registerAbortableInCurrentContext(asynchronousAbortable);
        },
        controller
      );

      expect(synchronousAbortable.aborted).toBeFalse();
      expect(asynchronousAbortable.aborted).toBeFalse();
      controller.abort();
      expect(synchronousAbortable.aborted).toBeTrue();
      expect(asynchronousAbortable.aborted).toBeFalse();
    });

    it('restores the previous context after the callback', () => {
      const outerController = new AbortController();
      const innerController = new AbortController();
      const outerAbortable = new SimpleAbortable();

      abortableContext.runInContext(
        () => {
          abortableContext.runInContext(
            () => {
            },
            innerController
          );
          // outer context must be active again here
          abortableContext.registerAbortableInCurrentContext(outerAbortable);
        },
        outerController
      );

      innerController.abort();
      expect(outerAbortable.aborted).toBeFalse();
    });

    it('restores the previous context even if the callback throws', () => {
      const outerController = new AbortController();
      const innerController = new AbortController();
      const outerAbortable = new SimpleAbortable();

      abortableContext.runInContext(
        () => {
          try {
            abortableContext.runInContext(
              () => {
                throw new Error('test error');
              },
              innerController
            );
          } catch (e) {
            // swallow inner error
          }
          // outer context must be active again here
          abortableContext.registerAbortableInCurrentContext(outerAbortable);
        },
        outerController
      );

      outerController.abort();
      expect(outerAbortable.aborted).toBeTrue();
    });

    it('throws an AbortError if the controller is already aborted', () => {
      const controller = new AbortController();
      controller.abort();
      expect(() =>
        abortableContext.runInContext(
          () => {
          },
          controller
        )
      ).toThrow(new AbortError());
    });

    it('does not execute the callback if the controller is already aborted', () => {
      const controller = new AbortController();
      controller.abort();
      let called = false;
      try {
        abortableContext.runInContext(() => {
          called = true;
        }, controller);
      } catch (e) {
        // expected AbortError
      }
      expect(called).toBeFalse();
    });

    it('registers the controller in the outer context so that aborting the outer controller also aborts the inner one', () => {
      const outerController = new AbortController();
      const innerController = new AbortController();
      const innerAbortable = new SimpleAbortable();

      abortableContext.runInContext(
        () => {
          abortableContext.runInContext(
            () => abortableContext.registerAbortableInCurrentContext(innerAbortable),
            innerController
          );
        },
        outerController
      );

      expect(innerAbortable.aborted).toBeFalse();
      outerController.abort();
      expect(innerAbortable.aborted).toBeTrue();
    });
  });

  describe('registerAbortableInCurrentContext', () => {

    it('does nothing when there is no active context', () => {
      const controller = new AbortController();
      const abortable = new SimpleAbortable();

      abortableContext.registerAbortableInCurrentContext(abortable);

      controller.abort();
      expect(abortable.aborted).toBeFalse();
    });

    it('aborts the abortable immediately when the current context is already aborted', () => {
      const controller = new AbortController();
      const abortable = new SimpleAbortable();

      abortableContext.runInContext(
        () => {
          controller.abort(); // abort while inside the context, otherwise abortableContext.runInContext throws an AbortError
          abortableContext.registerAbortableInCurrentContext(abortable);
        },
        controller
      );

      expect(abortable.aborted).toBeTrue();
    });

    it('registers the abortable to be aborted when the current context is later aborted', () => {
      const controller = new AbortController();
      const abortable = new SimpleAbortable();

      abortableContext.runInContext(
        () => abortableContext.registerAbortableInCurrentContext(abortable),
        controller
      );

      expect(abortable.aborted).toBeFalse();
      controller.abort();
      expect(abortable.aborted).toBeTrue();
    });

    it('registers multiple abortables in the same context', () => {
      const controller = new AbortController();
      const abortable1 = new SimpleAbortable();
      const abortable2 = new SimpleAbortable();

      abortableContext.runInContext(
        () => {
          abortableContext.registerAbortableInCurrentContext(abortable1);
          abortableContext.registerAbortableInCurrentContext(abortable2);
        },
        controller
      );

      expect(abortable1.aborted).toBeFalse();
      expect(abortable2.aborted).toBeFalse();
      controller.abort();
      expect(abortable1.aborted).toBeTrue();
      expect(abortable2.aborted).toBeTrue();
    });
  });

  class SimpleAbortable implements Abortable {

    aborted = false;

    abort() {
      this.aborted = true;
    }
  }
});
