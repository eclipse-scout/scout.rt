/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Abortable, scout} from '../index';

let currentAbortController: AbortController;

export const abortableContext = {

  /**
   * Runs the given callback in an abortable context.
   * All {@link Abortable}s registered during the callback using {@link registerAbortableInCurrentContext} are aborted when the provided {@link AbortController} is aborted.
   * If the code is already running in an abortable context, the given {@link AbortController} is registered as {@link Abortable} in the current context.
   * As a result, nested abortable contexts are aborted when the outer {@link AbortController} is aborted.
   */
  runInContext<T>(callback: () => T, abortController: AbortController): T {
    // assert parameters
    scout.assertParameter('callback', callback);
    scout.assertParameter('abortController', abortController);

    // register new AbortController in current context -> nested contexts will be aborted when the outer context is aborted
    abortableContext.registerAbortableInCurrentContext(abortController);

    // throw AbortError if the AbortController has already been aborted
    if (abortController.signal.aborted) {
      throw new AbortError();
    }

    // run callback using the given AbortController and reset to current one afterward
    const oldAbortController = currentAbortController;
    try {
      currentAbortController = abortController;
      return callback();
    } finally {
      currentAbortController = oldAbortController;
    }
  },

  /**
   * Registers an {@link Abortable} in the current abortable context. The abortable is linked to the current {@link AbortController} (see {@link runInContext}) and is aborted when the controller is aborted.
   */
  registerAbortableInCurrentContext(abortable: Abortable) {
    // no AbortController present -> nothing to register
    if (!currentAbortController) {
      return;
    }

    // abort Abortable directly if the current AbortController has already been aborted
    if (currentAbortController.signal.aborted) {
      abortable.abort();
      return;
    }

    // add listener to abort Abortable when AbortController is aborted
    currentAbortController.signal.addEventListener('abort', () => abortable.abort());
  }
};

/**
 * Marker class for aborted computations.
 */
export class AbortError {
  objectType: string;

  constructor() {
    this.objectType = 'AbortError';
  }
}
