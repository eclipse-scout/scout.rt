/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.dragAndDrop = {

  SCOUT_TYPES: {
    FILE_TRANSFER: 1 << 0, // IDNDSupport.TYPE_FILE_TRANSFER (NOSONAR)
    JAVA_ELEMENT_TRANSFER: 1 << 1, // IDNDSupport.TYPE_JAVA_ELEMENT_TRANSFER (NOSONAR)
    TEXT_TRANSFER: 1 << 2, // IDNDSupport.TYPE_TEXT_TRANSFER (NOSONAR)
    IMAGE_TRANSFER: 1 << 3 // IDNDSupport.TYPE_IMAGE_TRANSFER (NOSONAR)
  },

  DEFAULT_DROP_MAXIMUM_SIZE: 50 * 1024 * 1024, // 50 MB

  /**
   * Mapping function from scout drag types to browser drag types.
   *
   * @param scoutTypesArray array of scout.dragAndDrop.SCOUT_TYPES
   * @returns {Array} return array
   */
  scoutTypeToDragTypeMapping: function(scoutTypesArray) {
    scoutTypesArray = scout.arrays.ensure(scoutTypesArray);
    var ret = [];
    if (scoutTypesArray.indexOf(this.SCOUT_TYPES.FILE_TRANSFER) >= 0) {
      ret.push('Files');
    }
    return ret;
  },

  /**
   * Check if specific scout type is supported by dataTransfer, if event is not handled by this field (desktop might handle it)
   *
   * @param event including event.originalEvent.dataTransfer
   * @param fieldAllowedTypes allowed types on field (integer, bitwise comparision used)
   * @param scoutTypeArray e.g. scout.dragAndDrop.FILE_TRANSFER
   */
  verifyDataTransferTypesScoutTypes: function(event, scoutTypeArray, fieldAllowedTypes) {
    scoutTypeArray = scout.arrays.ensure(scoutTypeArray);
    var dragTypeArray = [];

    // check if any scout type is allowed for field allowed types (or no field allowed types defined)
    if (fieldAllowedTypes !== undefined) {
      scoutTypeArray.forEach(function fieldAllowedTypesContainsElement(scoutType) {
        if (fieldAllowedTypes & scoutType === scoutType) { // NOSONAR
          scout.arrays.pushAll(dragTypeArray, this.scoutTypeToDragTypeMapping(scoutTypeArray));
        }
      }.bind(this));
    } else {
      dragTypeArray = this.scoutTypeToDragTypeMapping(scoutTypeArray);
    }

    if (Array.isArray(dragTypeArray) && dragTypeArray.length > 0) {
      this.verifyDataTransferTypes(event, dragTypeArray);
    }
  },

  /**
   * Check if specific type is supported by dataTransfer, if event is not handled by this field (upstream field might handle it, at the latest desktop)
   *
   * @param dataTransfer dataTransfer object (not dataTransfer.types)
   * @param needleArray e.g. 'Files'
   */
  verifyDataTransferTypes: function(event, needleArray) {
    var dataTransfer = event.originalEvent.dataTransfer;

    if (this.dataTransferTypesContains(dataTransfer, needleArray)) {
      event.stopPropagation();
      event.preventDefault();
      return true;
    }
    return false;
  },

  /**
   * dataTransfer.types might be an array (Chrome, IE) or a DOMStringList.
   *
   * Unfortunately there is no intersecting contains method for both types.
   *
   * @param dataTransfer dataTransfer object (not dataTransfer.types)
   * @param scoutTypesArray e.g. scout.dragAndDrop.FILE_TRANSFER
   */
  dataTransferTypesContainsScoutTypes: function(dataTransfer, scoutTypesArray) {
    scoutTypesArray = scout.arrays.ensure(scoutTypesArray);
    var dragTypesArray = scout.dragAndDrop.scoutTypeToDragTypeMapping(scoutTypesArray);
    return this.dataTransferTypesContains(dataTransfer, dragTypesArray);
  },

  /**
   * dataTransfer.types might be an array (Chrome, IE) or a DOMStringList.
   *
   * Unfortunately there is no intersecting contains method for both types.
   *
   * @param dataTransfer dataTransfer object (not dataTransfer.types)
   * @param needleArray e.g. 'Files'
   */
  dataTransferTypesContains: function(dataTransfer, needleArray) {
    needleArray = scout.arrays.ensure(needleArray);
    if (dataTransfer && dataTransfer.types) {
      if (Array.isArray(dataTransfer.types) && scout.arrays.containsAny(dataTransfer.types, needleArray)) {
        // Array: indexOf function
        return true;
      } else if (dataTransfer.types.contains) {
        // DOMStringList: contains function
        return needleArray.some(function containsElement(element) {
          return dataTransfer.types.contains(element);
        });
      }
    }
    return false;
  },

  handler: function(target, options) {
    options = options || {};
    options.target = target;
    return new scout.DragAndDropHandler(options);
  }
};
