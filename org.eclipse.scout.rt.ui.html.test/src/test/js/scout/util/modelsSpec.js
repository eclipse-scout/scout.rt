/**
 *
 */
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
describe("scout.models", function() {

  describe('get', function() {

    var models = {
      model: {
        type: 'model',
        value: 'modelValue'
      },
      extension: {
        type: 'model',
        value: 'extensionValue'
      },
      object: {
        value: 'objectValue'
      }
    };

    beforeEach(function() {
      scout.models.init(models);
    });

    it('load object without type', function() {
      expect(function() {
        scout.models.get('object', 'model');
      }).toThrow(new Error('object is not of type \'model\''));
    });

    it('ensure the object is a copy', function() {
      scout.models.get('model', 'model').value = 'changed';
      expect(scout.models.get('model', 'model').value).toBe('modelValue');
    });

  });

  describe('extend', function() {

    var parentObj;

    var originalparent = {
      id: 'parent',
      color: 'red',
      text: 'test',
      rootContainer: {
        id: 'root',
        text: 'rootContainer',
        childs: [{
            id: 'child1',
            value: 1
          }, {
            id: 'child2',
            value: 2,
            childs: [{
                id: 'child3',
                value: 3
              }

            ]
          }, {
            id: 'child4',
            value: 4
          }

        ]
      }
    };

    var newPropertyInRoot = {
      extensions: [{
        operation: 'appendTo',
        target: {
          root: true
        },
        extension: {
          newColor: 'green'
        }
      }]
    };

    var overridePropertyInRoot = {
      extensions: [{
        operation: 'appendTo',
        target: {
          root: true
        },
        extension: {
          color: 'yellow'
        }
      }]
    };

    var overridePropertyInTree = {
      extensions: [{
        operation: 'appendTo',
        target: {
          id: 'child2'
        },
        extension: {
          value: 'property in tree overriden'
        }
      }]
    };

    var newObjectNoArrayInRoot = {
      extensions: [{
        operation: 'insert',
        target: {
          root: true,
          property: 'array'
        },
        extension: {
          id: 'newObj',
          value: 'inserted into non existing Array'
        }
      }]
    };

    var newObjectInTree = {
      extensions: [{
        operation: 'insert',
        target: {
          id: 'child2',
          property: 'childs'
        },
        extension: {
          id: 'newObj',
          value: 'new object in tree'
        }
      }]
    };

    var newObjectTreeInTree = {
      extensions: [{
        operation: 'insert',
        target: {
          id: 'child2',
          property: 'childs'
        },
        extension: {
          id: 'newObjTree',
          value: 15,
          childs: [{
            id: 'newObj',
            value: 'new object tree in tree'
          }]
        }
      }]
    };

    var newObjectInTreeIndexed = {
      extensions: [{
        operation: 'insert',
        target: {
          id: 'child2',
          property: 'childs',
          index: 0
        },
        extension: {
          id: 'newObj',
          value: 'fixed index insert'
        }
      }]
    };

    var newObjectInTreeRelativeindex = {
      type: 'extension',
      extensions: [{
        operation: 'insert',
        target: {
          id: 'child2',
          property: 'childs',
          before: 'child3'
        },
        extension: {
          id: 'newObj',
          value: 'relative index insert'
        }
      }]
    };

    beforeEach(function() {
      parentObj = $.extend(true, {}, originalparent);
    });

    it('insert new property into root object', function() {
      scout.models.extend(newPropertyInRoot, parentObj);
      expect(parentObj.newColor).toBe('green');
    });
    it('override property in root object', function() {
      scout.models.extend(overridePropertyInRoot, parentObj);
      expect(parentObj.color).toBe('yellow');
    });
    it('insert new property into a non existing array on root object', function() {
      scout.models.extend(newObjectNoArrayInRoot, parentObj);
      expect(parentObj.array[0].value).toBe('inserted into non existing Array');
    });
    it('override property in tree object', function() {
      scout.models.extend(overridePropertyInTree, parentObj);
      expect(parentObj.rootContainer.childs[1].value).toBe('property in tree overriden');
    });
    it('insert new object into tree object', function() {
      scout.models.extend(newObjectInTree, parentObj);
      expect(parentObj.rootContainer.childs[1].childs[1].value).toBe('new object in tree');
    });
    it('insert new object tree into tree object', function() {
      scout.models.extend(newObjectTreeInTree, parentObj);
      expect(parentObj.rootContainer.childs[1].childs[1].childs[0].value).toBe('new object tree in tree');
    });
    it('insert new object into tree object with fixed index', function() {
      scout.models.extend(newObjectInTreeIndexed, parentObj);
      expect(parentObj.rootContainer.childs[1].childs[0].value).toBe('fixed index insert');
    });
    it('insert new object into tree object with fixed index', function() {
      scout.models.extend(newObjectInTreeRelativeindex, parentObj);
      expect(parentObj.rootContainer.childs[1].childs[0].value).toBe('relative index insert');
    });
    it('insert object referenced by String', function() {
      var models ={};
      models.newObjectInTreeRelativeindex = newObjectInTreeRelativeindex;
      scout.models.init(models);
      scout.models.extend('newObjectInTreeRelativeindex', parentObj);
      expect(parentObj.rootContainer.childs[1].childs[0].value).toBe('relative index insert');
    });

  });

});
