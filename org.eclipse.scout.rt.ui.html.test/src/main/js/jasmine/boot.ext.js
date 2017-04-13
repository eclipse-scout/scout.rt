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
/*jshint -W079 */
/*global JasmineMavenReporter*/

// Since we don't use a IIF to wrap the scout code when running tests, we need to define the scout namespace here
var scout = {};

// Patches necessary for phantomJs
// bind does not exist, see https://github.com/ariya/phantomjs/issues/10522
// This piece of code is a simplified version of jQuery#proxy
if (!Function.prototype.bind) {
  Function.prototype.bind = function(context) {
    var fn = this,
      args = Array.prototype.slice.call(arguments, 1);
    return function() {
      return fn.apply(context || this, args.concat(Array.prototype.slice.call(arguments)));
    };
  };
}

// Add reporter for jasmine-maven-plugin
// The plugin actually uses jsApiReporter but requires jasmine 1.x.
// To make it work with jasmine 2.0 we use a custom reporter which fakes jasmine 1.x api
var jasmineMavenReporter = new JasmineMavenReporter({
  timer: new jasmine.Timer()
});
jasmine.getEnv().addReporter(jasmineMavenReporter);
window.reporter = jasmineMavenReporter;


$.fx.off = true;
