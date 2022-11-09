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


export interface AvgAggregationState {
  sum: number;
  count: number;
}

export const aggregation = {
  sumSymbol: icons.SUM,
  minSymbol: icons.MIN_BOLD,
  avgSymbol: icons.AVG,
  maxSymbol: icons.MAX_BOLD,

  sumStart(): number {
    return null;
  },

  sumStep(currentState?: number, newVal?: number): number {
    if (newVal) {
      return (currentState || 0) + newVal;
    }
    return currentState;
  },

  sumFinish(currentState?: number): number {
    return currentState;
  },

  avgStart(): AvgAggregationState {
    return {
      sum: 0,
      count: 0
    };
  },

  avgStep(currentState: AvgAggregationState, newVal?: number): AvgAggregationState {
    if (!objects.isNullOrUndefined(newVal)) {
      currentState.sum += newVal;
      currentState.count += 1;
    }
    return currentState;
  },

  avgFinish(currentState: AvgAggregationState): number {
    if (currentState.count) {
      return currentState.sum / currentState.count;
    }
    return null;
  },

  minStart(): number {
    return null;
  },

  minStep(currentState?: number, newVal?: number): number {
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
  },

  minFinish(currentState?: number): number {
    return currentState;
  },

  maxStart(): number {
    return null;
  },

  maxStep(currentState?: number, newVal?: number): number {
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
  },

  maxFinish(currentState?: number): number {
    return currentState;
  }
};
