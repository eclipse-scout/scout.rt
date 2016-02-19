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
  var position = 0;
  // Don't insert if already inserted
  if (this._invalidComponents.indexOf(htmlComp) >= 0) {
    return;
  }

  // Make sure it will be inserted before any descendant
  // This prevents multiple layouting of the descendant
  this._invalidComponents.forEach(function(invalidComponent, i) {
    if (invalidComponent.isDescendantOf(htmlComp)) {
      return;
    }
    position++;
  }, this);

  // Add validate root to list of invalid components. These are the starting point for a subsequent call to validate().
  scout.arrays.insert(this._invalidComponents, htmlComp, position);
};

/**
 * Layouts all invalid components (as long as they haven't been removed).
 */
scout.LayoutValidator.prototype.validate = function() {
  this._invalidComponents.slice().forEach(function(comp) {
    if (comp.isAttached()) { // don't layout components which don't exist anymore or are detached from the DOM
      comp.validateLayout();
      scout.arrays.remove(this._invalidComponents, comp);
    }
  }, this);
};

scout.LayoutValidator.prototype.cleanupInvalidObjects = function($parentContainer){
  this._invalidComponents.slice().forEach(function(comp){
    if (comp.$comp.closest($parentContainer).length > 0){
      scout.arrays.remove(this._invalidComponents, comp);
    }
  }, this);
};
