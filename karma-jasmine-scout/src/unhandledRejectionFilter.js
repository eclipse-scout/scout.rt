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

// Loaded BEFORE jasmine-core (see framework 'jasmine-scout-preload' in index.js), so this listener is registered before
// Jasmine's own 'unhandledrejection' listener. Listeners of the same phase run in registration order, which allows
// stopping the propagation to Jasmine for rejections that should not fail a spec (e.g. expected cancellations).
// The actual decision is delegated to a filter function that is installed later by the application (e.g. TestingApp),
// because the classes needed to decide (e.g. AbortError) are not available yet when this script runs.
window.addEventListener('unhandledrejection', event => {
  const filter = window.jasmineScoutUnhandledRejectionFilter;
  if (typeof filter === 'function' && filter(event)) {
    event.preventDefault();
    event.stopImmediatePropagation();
  }
});
