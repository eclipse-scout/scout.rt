// TODO AWE: Inheritance Diskussion abwarten: dann ggf. löschen

/**
 * @see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Details_of_the_Object_Model
 */
Function.prototype.inheritsFrom = function(Base) {
  this.prototype = new Base();
  this.prototype.base = Base;
  //May be added when needed
//  this.prototype.constructor = this;
  return this;
};
