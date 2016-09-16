/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
var JasmineMavenReporter = function(options) { // NOSONAR
  var noopTimer = {
    start: function() {},
    elapsed: function() {
      return 0;
    }
  };
  var timer = options.timer || noopTimer,
    status = 'loaded';

  this.started = false;
  this.finished = false;

  this.jasmineStarted = function() {
    this.started = true;
    status = 'started';
    timer.start();
  };

  var executionTime;

  this.jasmineDone = function() {
    this.finished = true;
    executionTime = timer.elapsed();
    status = 'done';
  };

  this.status = function() {
    return status;
  };

  var suites = [];

  this.suiteStarted = function(result) {
    suites.push(result);

    //api for maven plugin
    result.children = [];
    result.name = '';
  };

  this.suiteDone = function(result) {};

  //api for maven plugin
  this.suites = function() {
    return suites;
  };

  var specs = {};
  this.specStarted = function(result) {
    //api for maven plugin
    suites[suites.length - 1].children.push(result);
    result.children = [];
  };

  this.specDone = function(result) {
    specs[result.id] = result;

    //api for maven plugin
    result.name = result.fullName;
    result.type = 'spec';
    result.result = result.status;
    result.messages = result.failedExpectations;
    for (var i = 0; i < result.messages.length; i++) {
      result.messages[i].type = '';
    }
  };

  //api for maven plugin
  this.results = function() {
    return specs;
  };

  this.specs = function() {
    return specs;
  };

  this.executionTime = function() {
    return executionTime;
  };
};
