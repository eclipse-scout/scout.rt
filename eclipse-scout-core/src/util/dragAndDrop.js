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
 * @param fieldAllowedTypes allowed types on field (integer, bitwise comparision used)
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

export function handler(options) {
  options = options || {};
  if (!options.target) {
    return null;
  }
  return new DragAndDropHandler(options);
}

/**
 * installs or uninstalls a drag & drop handler on the target.
 *
 * @param target: the target object where the handler shall be installed.
 * @param options:
 *   [doInstall]
 *   function which determines if the drag & drop handler should be installed or uninstalled
 *   Default is checking {@code target.enabledComputed}
 *   [container]
 *   a function which returns the dom container providing the necessary drag & drop events
 *   Default is using {@code target.$container}
 *   [supportedScoutTypes]
 *   The scout type which will be allowed to drop into the target
 *   Default is {@code dragAndDrop.SCOUT_TYPES.FILE_TRANSFER}
 *   [dropType]
 *   function which returns the allowed drop type during a drop event
 *   Default is supplying {@code dragAndDrop.SCOUT_TYPES.FILE_TRANSFER}
 *   [dropMaximumSize]
 *   function which returns the maximum allowed size of a dropped object
 *   Default is {@code target.dropMaximumSize}
 *   [customValidateFiles(files)]
 *   an optional function to add a custom file validation logic.
 *   use {@code throw {title: title, message: message}; } to indicate a failed validation.
 *   [onDrop(files)]
 *   function which will be called when a valid element has been dropped
 *   [additionalDropProperties]
 *   Optional argument which will be used in {@link DragAndDropHandler.uploadFiles} as uploadProperties.
 */
export function installOrUninstallDragAndDropHandler(target, options) {
  if (!target) {
    return;
  }
  options = $.extend({}, _createDragAndDropHandlerOptions(target), options);
  if (options.doInstall()) {
    _installDragAndDropHandler(options);
  } else {
    uninstallDragAndDropHandler(target);
  }
}

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

export function _createDragAndDropHandlerOptions(target) {
  return {
    target: target,
    supportedScoutTypes: dragAndDrop.SCOUT_TYPES.FILE_TRANSFER,
    customValidateFiles: () => {
    },
    onDrop: () => {
    },
    dropType: () => dragAndDrop.SCOUT_TYPES.FILE_TRANSFER,
    dropMaximumSize: () => target.dropMaximumSize,
    doInstall: () => target.enabledComputed,
    container: () => target.$container
  };
}

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
