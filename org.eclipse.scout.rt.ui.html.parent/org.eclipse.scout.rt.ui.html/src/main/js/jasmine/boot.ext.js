/*jshint -W079 */
/*global JasmineMavenReporter */

//Since we don't use a IIF to wrap the scout code when running tests, we need to define the scout namespace here
var scout = {};

// Patches necessary for phantomJs
// bind does not exist, see https://github.com/ariya/phantomjs/issues/10522
// replace with jQuery's bind alternative
if (!Function.prototype.bind) {
  Function.prototype.bind = function(thisArg) {
    return jQuery.proxy(this, thisArg);
  };
}

//Add reporter for jasmine-maven-plugin
//The plugin actually uses jsApiReporter but requires jasmine 1.x.
//To make it work with jasmine 2.0 we use a custom reporter which fakes jasmine 1.x api
var jasmineMavenReporter = new JasmineMavenReporter({
  timer: new jasmine.Timer()
});
jasmine.getEnv().addReporter(jasmineMavenReporter);
window.reporter = jasmineMavenReporter;
