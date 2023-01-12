/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, DragAndDropHandler, EnumObject, Widget} from '../index';

const SCOUT_TYPES = {
  FILE_TRANSFER: 1 << 0, // IDNDSupport.TYPE_FILE_TRANSFER (NOSONAR)
  JAVA_ELEMENT_TRANSFER: 1 << 1, // IDNDSupport.TYPE_JAVA_ELEMENT_TRANSFER (NOSONAR)
  TEXT_TRANSFER: 1 << 2, // IDNDSupport.TYPE_TEXT_TRANSFER (NOSONAR)
  IMAGE_TRANSFER: 1 << 3 // IDNDSupport.TYPE_IMAGE_TRANSFER (NOSONAR)
} as const;

export const dragAndDrop = {

  SCOUT_TYPES,
  DEFAULT_DROP_MAXIMUM_SIZE: 50 * 1024 * 1024, // 50 MiB

  /**
   * Mapping function from scout drag types to browser drag types.
   *
   * @param scoutTypesArray array of SCOUT_TYPES
   */
  scoutTypeToDragTypeMapping(scoutTypesArray: DropType | DropType[]): Array<string> {
    scoutTypesArray = arrays.ensure(scoutTypesArray);
    let ret = [];
    if (scoutTypesArray.indexOf(dragAndDrop.SCOUT_TYPES.FILE_TRANSFER) >= 0) {
      ret.push('Files');
    }
    return ret;
  },

  /**
   * Check if specific scout type is supported by dataTransfer, if event is not handled by this field (desktop might handle it)
   *
   * @param event including event.originalEvent.dataTransfer
   * @param fieldAllowedTypes allowed types on field (integer, bitwise comparison used)
   * @param scoutTypeArray e.g. FILE_TRANSFER
   */
  verifyDataTransferTypesScoutTypes(event: JQuery.DragEventBase, scoutTypeArray: DropType | DropType[], fieldAllowedTypes: number) {
    scoutTypeArray = arrays.ensure(scoutTypeArray);
    let dragTypeArray = [];

    // check if any scout type is allowed for field allowed types (or no field allowed types defined)
    if (fieldAllowedTypes !== undefined) {
      scoutTypeArray.forEach(scoutType => {
        if ((fieldAllowedTypes & scoutType) === scoutType) { // NOSONAR
          arrays.pushAll(dragTypeArray, dragAndDrop.scoutTypeToDragTypeMapping(scoutTypeArray));
        }
      });
    } else {
      dragTypeArray = dragAndDrop.scoutTypeToDragTypeMapping(scoutTypeArray);
    }

    if (Array.isArray(dragTypeArray) && dragTypeArray.length > 0) {
      dragAndDrop.verifyDataTransferTypes(event, dragTypeArray);
    }
  },

  /**
   * Check if specific type is supported by dataTransfer, if event is not handled by this field (upstream field might handle it, at the latest desktop)
   *
   * @param dataTransfer dataTransfer object (not dataTransfer.types)
   * @param needleArray e.g. 'Files'
   */
  verifyDataTransferTypes(event: JQuery.DragEventBase, needleArray: string | string[]): boolean {
    let dataTransfer = event.originalEvent.dataTransfer;

    if (dragAndDrop.dataTransferTypesContains(dataTransfer, needleArray)) {
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
   * @param scoutTypesArray e.g. FILE_TRANSFER
   */
  dataTransferTypesContainsScoutTypes(dataTransfer: DataTransfer, scoutTypesArray: DropType | DropType[]): boolean {
    scoutTypesArray = arrays.ensure(scoutTypesArray);
    let dragTypesArray = dragAndDrop.scoutTypeToDragTypeMapping(scoutTypesArray);
    return dragAndDrop.dataTransferTypesContains(dataTransfer, dragTypesArray);
  },

  /**
   * dataTransfer.types might be an array (Chrome, IE) or a DOMStringList.
   *
   * Unfortunately there is no intersecting contains method for both types.
   *
   * @param dataTransfer dataTransfer object (not dataTransfer.types)
   * @param needleArray e.g. 'Files'
   */
  dataTransferTypesContains(dataTransfer: DataTransfer, needleArray: string | string[]): boolean {
    needleArray = arrays.ensure(needleArray);
    if (dataTransfer && dataTransfer.types) {
      if (Array.isArray(dataTransfer.types) && arrays.containsAny(dataTransfer.types, needleArray)) {
        // Array: indexOf function
        return true;
      }

      if (dataTransfer.types['contains']) {
        // DOMStringList: contains function
        return needleArray.some(element => {
          return dataTransfer.types['contains'](element);
        });
      }
    }
    return false;
  },

  handler(options: DragAndDropOptions): DragAndDropHandler {
    if (!options || !options.target) {
      return null;
    }
    return new DragAndDropHandler(options);
  },

  /**
   * installs or uninstalls a {@link DragAndDropHandler} on the target.
   */
  installOrUninstallDragAndDropHandler(options: DragAndDropOptions) {
    if (!options.target) {
      return;
    }
    options = $.extend({}, dragAndDrop._createDragAndDropHandlerOptions(options.target), options);
    if (options.doInstall()) {
      dragAndDrop._installDragAndDropHandler(options);
    } else {
      dragAndDrop.uninstallDragAndDropHandler(options.target);
    }
  },

  /** @internal */
  _installDragAndDropHandler(options: DragAndDropOptions) {
    if (options.target.dragAndDropHandler) {
      return;
    }
    options.target.dragAndDropHandler = dragAndDrop.handler(options);
    if (!options.target.dragAndDropHandler) {
      return;
    }
    let $container = options.container();
    if (!$container) {
      return;
    }
    options.target.dragAndDropHandler.install($container, options.selector);
  },

  /** @internal */
  _createDragAndDropHandlerOptions(target: DragAndDropTarget): DragAndDropOptions {
    return {
      target: target,
      supportedScoutTypes: dragAndDrop.SCOUT_TYPES.FILE_TRANSFER,
      validateFiles: (files, defaultValidator) => defaultValidator(files),
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      onDrop: event => {
      },
      dropType: () => dragAndDrop.SCOUT_TYPES.FILE_TRANSFER,
      dropMaximumSize: () => target.dropMaximumSize,
      doInstall: () => target.enabledComputed,
      container: () => target.$container,
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      additionalDropProperties: event => {
      }
    };
  },

  /**
   * uninstalls a {@link DragAndDropHandler} from the target. If no handler is installed, this function does nothing.
   */
  uninstallDragAndDropHandler(target: DragAndDropTarget) {
    if (!target || !target.dragAndDropHandler) {
      return;
    }
    target.dragAndDropHandler.uninstall();
    target.dragAndDropHandler = null;
  }
};

export type DragAndDropTarget = Widget & {
  /**
   * default drop maximum size used in {@link DragAndDropOptions.dropMaximumSize}. If the target object contains a different field or function to retrieve this value override the supplier.
   */
  dropMaximumSize?: number;

  /**
   * default install/uninstall criteria used in {@link DragAndDropOptions.doInstall}. If the target object contains a different field or function to retrieve this value override the supplier.
   */
  enabledComputed?: boolean;

  /**
   * default container used in {@link DragAndDropOptions.container}. If the target object contains a different field or function to retrieve this value override the supplier.
   */
  $container?: JQuery;

  /**
   * installed drag & drop handler. Will be managed through {@link DragAndDropHandler}
   */
  dragAndDropHandler: DragAndDropHandler;
};

export interface DropValidationErrorMessage {
  title: string;
  message: string;
}

export interface FileDropEvent {
  originalEvent: JQuery.DropEvent<HTMLElement, undefined, HTMLElement, HTMLElement>;
  files: File[];
}

export interface DragAndDropOptions {
  /**
   * the target widget where the handler shall be installed.
   */
  target: DragAndDropTarget;
  /**
   * Will be called when a valid element has been dropped.
   */
  onDrop?: (event: FileDropEvent) => void;

  /**
   * Determines if the drag & drop handler should be installed or uninstalled. Default implementation is checking {@link Widget.enabledComputed}
   */
  doInstall?: () => boolean;

  /**
   * Returns the dom container providing the necessary drag & drop events. Default is {@link Widget.$container}
   */
  container?: () => JQuery;

  /**
   * The scout type which will be allowed to drop into the target. Default is {@link dragAndDrop.SCOUT_TYPES.FILE_TRANSFER}
   */
  supportedScoutTypes?: DropType | DropType[];

  /**
   * Allowed mime types.
   * @see https://developer.mozilla.org/en-US/docs/Glossary/MIME_type
   */
  allowedTypes?: () => string[];

  /**
   * CSS selector which will be added to the event source.
   */
  selector?: JQuery.Selector;

  /**
   * Returns the allowed drop type during a drop event. Default is {@link dragAndDrop.SCOUT_TYPES.FILE_TRANSFER}
   */
  dropType?: () => DropType;

  /**
   * Returns the maximum allowed size of a dropped object. Default is {@link Widget.dropMaximumSize}
   */
  dropMaximumSize?: () => number;

  /**
   * An optional function to add a custom file validation logic. Throw a {@link DropValidationErrorMessage} to indicate a failed validation.
   * If no custom validator is installed, the default maximum file size validator is invoked.
   * @throws DropValidationErrorMessage
   */
  validateFiles?: (files: File[], defaultValidator: (f: File[]) => void) => void;

  /**
   * Returns additional drop properties to be used in {@link DragAndDropHandler.uploadFiles} as uploadProperties
   */
  additionalDropProperties?: (event: JQuery.DropEvent<HTMLElement, undefined, HTMLElement, HTMLElement>) => any;
}

export type DropType = EnumObject<typeof SCOUT_TYPES>;
