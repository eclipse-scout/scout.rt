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
import {arrays, dragAndDrop, DragAndDropHandler} from '../index';

const SCOUT_TYPES = {
  FILE_TRANSFER: 1 << 0, // IDNDSupport.TYPE_FILE_TRANSFER (NOSONAR)
  JAVA_ELEMENT_TRANSFER: 1 << 1, // IDNDSupport.TYPE_JAVA_ELEMENT_TRANSFER (NOSONAR)
  TEXT_TRANSFER: 1 << 2, // IDNDSupport.TYPE_TEXT_TRANSFER (NOSONAR)
  IMAGE_TRANSFER: 1 << 3 // IDNDSupport.TYPE_IMAGE_TRANSFER (NOSONAR)
};

const DEFAULT_DROP_MAXIMUM_SIZE = 50 * 1024 * 1024; // 50 MB

/**
 * Mapping function from scout drag types to browser drag types.
 *
 * @param scoutTypesArray array of SCOUT_TYPES
 * @returns {Array} return array
 */
export function scoutTypeToDragTypeMapping(scoutTypesArray) {
  scoutTypesArray = arrays.ensure(scoutTypesArray);
  let ret = [];
  if (scoutTypesArray.indexOf(SCOUT_TYPES.FILE_TRANSFER) >= 0) {
    ret.push('Files');
  }
  return ret;
}

/**
 * Check if specific scout type is supported by dataTransfer, if event is not handled by this field (desktop might handle it)
 *
 * @param event including event.originalEvent.dataTransfer
 * @param fieldAllowedTypes allowed types on field (integer, bitwise comparison used)
 * @param scoutTypeArray e.g. FILE_TRANSFER
 */
export function verifyDataTransferTypesScoutTypes(event, scoutTypeArray, fieldAllowedTypes) {
  scoutTypeArray = arrays.ensure(scoutTypeArray);
  let dragTypeArray = [];

  // check if any scout type is allowed for field allowed types (or no field allowed types defined)
  if (fieldAllowedTypes !== undefined) {
    scoutTypeArray.forEach(scoutType => {
      if ((fieldAllowedTypes & scoutType) === scoutType) { // NOSONAR
        arrays.pushAll(dragTypeArray, scoutTypeToDragTypeMapping(scoutTypeArray));
      }
    });
  } else {
    dragTypeArray = scoutTypeToDragTypeMapping(scoutTypeArray);
  }

  if (Array.isArray(dragTypeArray) && dragTypeArray.length > 0) {
    verifyDataTransferTypes(event, dragTypeArray);
  }
}

/**
 * Check if specific type is supported by dataTransfer, if event is not handled by this field (upstream field might handle it, at the latest desktop)
 *
 * @param dataTransfer dataTransfer object (not dataTransfer.types)
 * @param needleArray e.g. 'Files'
 */
export function verifyDataTransferTypes(event, needleArray) {
  let dataTransfer = event.originalEvent.dataTransfer;

  if (dataTransferTypesContains(dataTransfer, needleArray)) {
    event.stopPropagation();
    event.preventDefault();
    return true;
  }
  return false;
}

/**
 * dataTransfer.types might be an array (Chrome, IE) or a DOMStringList.
 *
 * Unfortunately there is no intersecting contains method for both types.
 *
 * @param dataTransfer dataTransfer object (not dataTransfer.types)
 * @param scoutTypesArray e.g. FILE_TRANSFER
 */
export function dataTransferTypesContainsScoutTypes(dataTransfer, scoutTypesArray) {
  scoutTypesArray = arrays.ensure(scoutTypesArray);
  let dragTypesArray = scoutTypeToDragTypeMapping(scoutTypesArray);
  return dataTransferTypesContains(dataTransfer, dragTypesArray);
}

/**
 * dataTransfer.types might be an array (Chrome, IE) or a DOMStringList.
 *
 * Unfortunately there is no intersecting contains method for both types.
 *
 * @param dataTransfer dataTransfer object (not dataTransfer.types)
 * @param needleArray e.g. 'Files'
 */
export function dataTransferTypesContains(dataTransfer, needleArray) {
  needleArray = arrays.ensure(needleArray);
  if (dataTransfer && dataTransfer.types) {
    if (Array.isArray(dataTransfer.types) && arrays.containsAny(dataTransfer.types, needleArray)) {
      // Array: indexOf function
      return true;
    } else if (dataTransfer.types.contains) {
      // DOMStringList: contains function
      return needleArray.some(element => {
        return dataTransfer.types.contains(element);
      });
    }
  }
  return false;
}

/**
 *
 * @param {DragAndDropOptions} options
 * @return {null|DragAndDropHandler}
 */
export function handler(options) {
  if (!options || !options.target) {
    return null;
  }
  return new DragAndDropHandler(options);
}

/**
 * installs or uninstalls a {@link DragAndDropHandler} on the target.
 *
 * @param {DragAndDropOptions} options
 */
export function installOrUninstallDragAndDropHandler(options) {
  if (!options.target) {
    return;
  }
  options = $.extend({}, _createDragAndDropHandlerOptions(options.target), options);
  if (options.doInstall()) {
    _installDragAndDropHandler(options);
  } else {
    uninstallDragAndDropHandler(options.target);
  }
}

/**
 *
 * @param {DragAndDropOptions} options
 * @private
 */
export function _installDragAndDropHandler(options) {
  if (options.target.dragAndDropHandler) {
    return;
  }
  options.target.dragAndDropHandler = handler(options);
  if (!options.target.dragAndDropHandler) {
    return;
  }
  let $container = options.container();
  if (!$container) {
    return;
  }
  options.target.dragAndDropHandler.install($container, options.selector);
}

/**
 *
 * @param {DragAndDropTarget} target
 * @private
 */
export function _createDragAndDropHandlerOptions(target) {
  return {
    target: target,
    supportedScoutTypes: dragAndDrop.SCOUT_TYPES.FILE_TRANSFER,
    validateFiles: (files, defaultValidator) => defaultValidator(files),
    onDrop: files => {
    },
    dropType: () => dragAndDrop.SCOUT_TYPES.FILE_TRANSFER,
    dropMaximumSize: () => target.dropMaximumSize,
    doInstall: () => target.enabledComputed,
    container: () => target.$container,
    additionalDropProperties: event => {
    }
  };
}

/**
 * uninstalls a {@link DragAndDropHandler} from the target. If no handler is installed, this function does nothing.
 * @param {DragAndDropTarget} target the target widget.
 */
export function uninstallDragAndDropHandler(target) {
  if (!target || !target.dragAndDropHandler) {
    return;
  }
  target.dragAndDropHandler.uninstall();
  target.dragAndDropHandler = null;
}

export default {
  DEFAULT_DROP_MAXIMUM_SIZE,
  SCOUT_TYPES,
  dataTransferTypesContains,
  dataTransferTypesContainsScoutTypes,
  handler,
  installOrUninstallDragAndDropHandler,
  scoutTypeToDragTypeMapping,
  uninstallDragAndDropHandler,
  verifyDataTransferTypes,
  verifyDataTransferTypesScoutTypes
};

// ----------------- TYPEDEF -----------------

/**
 * @typedef {Widget} DragAndDropTarget
 * @property {number} [dropMaximumSize] default drop maximum size used in {@link DragAndDropOptions.dropMaximumSize}. If the target object contains a different field or function to retrieve this value override the supplier.
 * @property {boolean} [enabledComputed]  default install/uninstall criteria used in {@link DragAndDropOptions.doInstall}. If the target object contains a different field or function to retrieve this value override the supplier.
 * @property {$} [$container] default container used in {@link DragAndDropOptions.container}. If the target object contains a different field or function to retrieve this value override the supplier.
 * @property {DragAndDropHandler} [dragAndDropHandler] installed drag & drop handler. Will be managed through {@link DragAndDropHandler}
 */

/**
 * @callback validateFiles
 * @param {File[]} files
 * @param {DragAndDropHandler._validateFiles} defaultValidator
 * @throws {dropValidationErrorMessage} validationErrorMessage
 */

/**
 * @callback onDrop
 * @param {File[]} files
 */

/**
 * @callback additionalDropProperties
 * @param {Event} event
 * @returns {Object}
 */

/**
 * @callback doInstall
 * @returns {boolean}
 */

/**
 * @callback container
 * @returns {$}
 */

/**
 * @callback dropType
 * @returns {dragAndDrop.SCOUT_TYPES.FILE_TRANSFER | number}
 */

/**
 * @callback dropMaximumSize
 * @returns {number}
 */

/**
 * @typedef {Object} DragAndDropOptions
 * @property {DragAndDropTarget} target the target object where the handler shall be installed.
 * @property {onDrop} onDrop Will be called when a valid element has been dropped.
 * @property {doInstall} [doInstall] Determines if the drag & drop handler should be installed or uninstalled. Default implementation is checking {@link DragAndDropTarget.enabledComputed}
 * @property {container} [container] Returns the dom container providing the necessary drag & drop events. Default is {@link DragAndDropTarget.$container}
 * @property {SCOUT_TYPES} [supportedScoutTypes] The scout type which will be allowed to drop into the target. Default is {@link dragAndDrop.SCOUT_TYPES.FILE_TRANSFER}
 * @property {String} [selector] CSS selector which will be added to the event source.
 * @property {dropType} [dropType] Returns the allowed drop type during a drop event. Default is {@link dragAndDrop.SCOUT_TYPES.FILE_TRANSFER}
 * @property {dropMaximumSize} [dropMaximumSize] Returns the maximum allowed size of a dropped object. Default is {@link DragAndDropTarget.dropMaximumSize}
 * @property {validateFiles} [validateFiles] An optional function to add a custom file validation logic. Throw a {@link dropValidationErrorMessage} to indicate a failed validation.
 *           If no custom validator is installed, the default maximum file size validator is invoked.
 * @property {additionalDropProperties} [additionalDropProperties] Returns additional drop properties to be used in {@link DragAndDropHandler.uploadFiles} as uploadProperties
 */
