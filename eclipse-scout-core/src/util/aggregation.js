/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {icons} from '../index';

export function sumStart() {
  return null;
}

export function sumStep(currentState, newVal) {
  if (typeof newVal === 'number') {
    currentState = (currentState || 0) + newVal;
  }
  return currentState;
}

export function sumFinish(currentState) {
  return currentState;
}

let sumSymbol = icons.SUM;

export function avgStart() {
  return {
    sum: 0,
    count: 0
  };
}

export function avgStep(currentState, newVal) {
  if (typeof newVal === 'number') {
    currentState.sum += newVal;
    currentState.count += 1;
  }
  return currentState;
}

export function avgFinish(currentState) {
  if (currentState.count && currentState.count > 0) {
    return (currentState.sum * 1.0) / currentState.count;
  }
}

let avgSymbol = icons.AVG;

export function minStart() {
  return null;
}

export function minStep(currentState, newVal) {
  if (typeof newVal === 'number') {
    if (typeof currentState === 'number') {
      if (newVal < currentState) {
        currentState = newVal;
      }
    } else {
      currentState = newVal;
    }
  }
  return currentState;
}

export function minFinish(currentState) {
  return currentState;
}

let minSymbol = icons.MIN_BOLD;

export function maxStart() {
  return null;
}

export function maxStep(currentState, newVal) {
  if (typeof newVal === 'number') {
    if (typeof currentState === 'number') {
      if (newVal > currentState) {
        currentState = newVal;
      }
    } else {
      currentState = newVal;
    }
  }
  return currentState;
}

export function maxFinish(currentState) {
  return currentState;
}

let maxSymbol = icons.MAX_BOLD;

export default {
  avgFinish,
  avgStart,
  avgStep,
  avgSymbol,
  maxFinish,
  maxStart,
  maxStep,
  maxSymbol,
  minFinish,
  minStart,
  minStep,
  minSymbol,
  sumFinish,
  sumStart,
  sumStep,
  sumSymbol
};
