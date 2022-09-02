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
import {icons, objects} from '../index';

export function sumStart(): number {
  return null;
}

export function sumStep(currentState?: number, newVal?: number): number {
  if (newVal) {
    return (currentState || 0) + newVal;
  }
  return currentState;
}

export function sumFinish(currentState?: number): number {
  return currentState;
}

let sumSymbol = icons.SUM;

export interface AvgAggregationState {
  sum: number;
  count: number;
}

export function avgStart(): AvgAggregationState {
  return {
    sum: 0,
    count: 0
  };
}

export function avgStep(currentState: AvgAggregationState, newVal?: number): AvgAggregationState {
  if (!objects.isNullOrUndefined(newVal)) {
    currentState.sum += newVal;
    currentState.count += 1;
  }
  return currentState;
}

export function avgFinish(currentState: AvgAggregationState): number {
  if (currentState.count) {
    return currentState.sum / currentState.count;
  }
  return null;
}

let avgSymbol = icons.AVG;

export function minStart(): number {
  return null;
}

export function minStep(currentState?: number, newVal?: number): number {
  if (!objects.isNullOrUndefined(newVal)) {
    if (!objects.isNullOrUndefined(currentState)) {
      if (newVal < currentState) {
        currentState = newVal;
      }
    } else {
      currentState = newVal;
    }
  }
  return currentState;
}

export function minFinish(currentState?: number): number {
  return currentState;
}

let minSymbol = icons.MIN_BOLD;

export function maxStart(): number {
  return null;
}

export function maxStep(currentState?: number, newVal?: number): number {
  if (!objects.isNullOrUndefined(newVal)) {
    if (!objects.isNullOrUndefined(currentState)) {
      if (newVal > currentState) {
        currentState = newVal;
      }
    } else {
      currentState = newVal;
    }
  }
  return currentState;
}

export function maxFinish(currentState?: number): number {
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
