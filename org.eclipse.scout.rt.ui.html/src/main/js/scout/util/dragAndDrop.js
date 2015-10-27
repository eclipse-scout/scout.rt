scout.dragAndDrop = {

  SCOUT_TYPES: {
    FILE_TRANSFER: 1 << 0 /* IDNDSupport.TYPE_FILE_TRANSFER */ ,
    JAVA_ELEMENT_TRANSFER: 1 << 1 /* IDNDSupport.TYPE_JAVA_ELEMENT_TRANSFER */ ,
    TEXT_TRANSFER: 1 << 2 /* IDNDSupport.TYPE_TEXT_TRANSFER */ ,
    IMAGE_TRANSFER: 1 << 3 /* IDNDSupport.TYPE_IMAGE_TRANSFER */
  },

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
    var dataTransfer = event.originalEvent.dataTransfer;

    // check if any scout type is allowed for field allowed types (or no field allowed types defined)
    if (fieldAllowedTypes !== undefined) {
      var allowed = false;

      scoutTypeArray.forEach(function fieldAllowedTypesContainsElement(scoutType) {
        if (fieldAllowedTypes & scoutType === scoutType) {
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

  handler: function(that, supportedScoutTypesArray, dropTypeCallback, dropMaximumSizeCallback, additionalDropPropertiesCallback, allowedTypesCallback) {
    supportedScoutTypesArray = scout.arrays.ensure(supportedScoutTypesArray);

    // create handler
    var handlerInternal = {
      install: function(element) {
        element.on('dragenter', handlerInternal.onDragEnter)
          .on('dragover', handlerInternal.onDragOver)
          .on('drop', handlerInternal.onDrop);
      },

      onDragEnter: function(event) {
        handlerInternal.onDragEnterOrOver(event);
      },

      onDragOver: function(event) {
        handlerInternal.onDragEnterOrOver(event);
      },

      onDragEnterOrOver: function(event) {
        scout.dragAndDrop.verifyDataTransferTypesScoutTypes(event, handlerInternal.supportedScoutTypesArray, handlerInternal.dropType());
      },

      onDrop: function(event) {
        if (handlerInternal.supportedScoutTypesArray.indexOf(scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER) >= 0 &&
          handlerInternal.dropType() & scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER === scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER &&
          scout.dragAndDrop.dataTransferTypesContainsScoutTypes(event.originalEvent.dataTransfer, scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER)) {
          event.stopPropagation();
          event.preventDefault();

          var files = event.originalEvent.dataTransfer.files;
          if (files.length >= 1) {
            that.session.uploadFiles(that, files,
              handlerInternal.additionalDropProperties ? handlerInternal.additionalDropProperties(event) : undefined,
              handlerInternal.dropSize ? handlerInternal.dropSize() : undefined,
              handlerInternal.allowedTypes ? handlerInternal.allowedTypes() : undefined);
          }
        }
      }
    };

    // set handler properties (some are functions)
    handlerInternal.that = that;
    handlerInternal.supportedScoutTypesArray = supportedScoutTypesArray;
    handlerInternal.dropType = dropTypeCallback;
    handlerInternal.dropSize = dropMaximumSizeCallback;
    handlerInternal.additionalDropProperties = additionalDropPropertiesCallback;
    handlerInternal.allowedTypes = allowedTypesCallback;

    // return handler
    return handlerInternal;
  }
};
