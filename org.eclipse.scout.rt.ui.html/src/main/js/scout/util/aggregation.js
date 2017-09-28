/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.aggregation = {

  sumStart: function() {
    return null;
  },

  sumStep: function(currentState, newVal) {
    if (typeof newVal === 'number') {
      currentState = (currentState || 0) + newVal;
    }
    return currentState;
  },

  sumFinish: function(currentState) {
    return currentState;
  },

  sumSymbol: scout.icons.SUM,

  avgStart: function() {
    return {
      sum: 0,
      count: 0
    };
  },

  avgStep: function(currentState, newVal) {
    if (typeof newVal === 'number') {
      currentState.sum += newVal;
      currentState.count += 1;
    }
    return currentState;
  },

  avgFinish: function(currentState) {
    if (currentState.count && currentState.count > 0) {
      return (currentState.sum * 1.0) / currentState.count;
    }
  },

  avgSymbol: scout.icons.AVG,

  minStart: function() {
    return null;
  },

  minStep: function(currentState, newVal) {
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
  },

  minFinish: function(currentState) {
    return currentState;
  },

  minSymbol: scout.icons.MIN,

  maxStart: function() {
    return null;
  },

  maxStep: function(currentState, newVal) {
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
  },

  maxFinish: function(currentState) {
    return currentState;
  },

  maxSymbol: scout.icons.MAX

};
