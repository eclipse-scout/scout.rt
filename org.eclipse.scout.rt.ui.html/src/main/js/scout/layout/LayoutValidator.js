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
scout.LayoutValidator = function() {
  this._invalidComponents = [];
};

// FIXME cgu: maybe it is necessary to sort the list so that the top most root is layouted first.

// Testcase: Field in scrollable groupbox gets invisible and also a field outside the groupbox.
// If scrollable is layouted first it may be relayouted again when the form gets layouted
scout.LayoutValidator.prototype.invalidateTree = function(htmlComp) {
  var validateRoot,
    htmlCompParent = htmlComp;

  // Mark every parent as invalid until validate root
  while (htmlCompParent) {
    htmlComp = htmlCompParent;
    htmlComp.invalidateLayout();
    if (htmlComp.isValidateRoot()) {
      validateRoot = htmlComp;
      break;
    }
    htmlCompParent = htmlComp.getParent();
  }

  if (!htmlCompParent) {
    validateRoot = htmlComp;
  }

  this.invalidate(validateRoot);
};

scout.LayoutValidator.prototype.invalidate = function(htmlComp) {
  // Add validate root to list of invalid components. These are the starting point for a subsequent call to validate().
  if (this._invalidComponents.indexOf(htmlComp) < 0) {
    this._invalidComponents.push(htmlComp);
  }
};

/**
 * Layouts all invalid components (as long as they haven't been removed).
 */
scout.LayoutValidator.prototype.validate = function() {
  this._invalidComponents.forEach(function(comp) {
    if (comp.isAttached()) { // don't layout components which don't exist anymore or are detached from the DOM
      comp.validateLayout();
      scout.arrays.remove(this._invalidComponents, comp);
    }
  });
};

scout.LayoutValidator.prototype.cleanupInvalidObjects = function($parentContainer){
  this._invalidComponents.forEach(function(comp){
    if(comp.$comp.closest($parentContainer).length>0){
      scout.arrays.remove(this._invalidComponents, comp);
    }
  }.bind(this));
};
