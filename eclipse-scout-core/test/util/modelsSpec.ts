/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import * as models from '../../src/util/models';

describe('scout.models', () => {

  describe('get', () => {

    let model = {
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

    beforeEach(() => {
      models.init(model);
    });

    it('load object without type possible', () => {
      expect(models._get('object', 'model')['value']).toBe('objectValue');
    });

    it('ensure the object is a copy', () => {
      models._get('model', 'model')['value'] = 'changed';
      expect(models._get('model', 'model')['value']).toBe('modelValue');
    });

  });

  describe('extend', () => {

    let parentObj;

    let originalParent = {
      id: 'parent',
      color: 'red',
      text: 'test',
      rootContainer: {
        id: 'root',
        text: 'rootContainer',
        children: [{
          id: 'child1',
          value: 1
        }, {
          id: 'child2',
          value: 2,
          children: [{
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

    let newPropertyInRoot = {
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

    let overridePropertyInRoot = {
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

    let overridePropertyInTree = {
      extensions: [{
        operation: 'appendTo',
        target: {
          id: 'child2'
        },
        extension: {
          value: 'property in tree overridden'
        }
      }]
    };

    let newObjectNoArrayInRoot = {
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

    let newObjectInTree = {
      extensions: [{
        operation: 'insert',
        target: {
          id: 'child2',
          property: 'children'
        },
        extension: {
          id: 'newObj',
          value: 'new object in tree'
        }
      }]
    };

    let newObjectTreeInTree = {
      extensions: [{
        operation: 'insert',
        target: {
          id: 'child2',
          property: 'children'
        },
        extension: {
          id: 'newObjTree',
          value: 15,
          children: [{
            id: 'newObj',
            value: 'new object tree in tree'
          }]
        }
      }]
    };

    let newObjectInTreeIndexed = {
      extensions: [{
        operation: 'insert',
        target: {
          id: 'child2',
          property: 'children',
          index: 0
        },
        extension: {
          id: 'newObj',
          value: 'fixed index insert'
        }
      }]
    };

    let newObjectInTreeRelativeIndex = {
      type: 'extension',
      extensions: [{
        operation: 'insert',
        target: {
          id: 'child2',
          property: 'children',
          before: 'child3'
        },
        extension: {
          id: 'newObj',
          value: 'relative index insert'
        }
      }]
    };

    let newObjectInTreeRelativeIndexWithArray = {
      type: 'extension',
      extensions: [{
        operation: 'insert',
        target: {
          id: 'child2',
          property: 'children',
          before: 'child3'
        },
        extension: [{
          id: 'newObj',
          value: 'relative index insert'
        }, {
          id: 'newObj2',
          value: 'relative index insert2'
        }]
      }]
    };

    let newObjectGroupWithTarget = {
      type: 'extension',
      extensions: [{
        operation: 'insert',
        target: {
          id: 'child2',
          property: 'children',
          before: 'child3',
          groupWithTarget: true
        },
        extension: {
          id: 'newObjBound'
        }
      }]
    };

    let newObjectArrayGroupWithTarget = {
      type: 'extension',
      extensions: [{
        operation: 'insert',
        target: {
          id: 'child2',
          property: 'children',
          before: 'child3',
          groupWithTarget: true
        },
        extension: [{
          id: 'newObjBound'
        }, {
          id: 'newObjBound2'
        }]
      }]
    };

    beforeEach(() => {
      parentObj = $.extend(true, {}, originalParent);
    });

    it('insert new property into root object', () => {
      models.extend(newPropertyInRoot, parentObj);
      expect(parentObj.newColor).toBe('green');
    });
    it('override property in root object', () => {
      models.extend(overridePropertyInRoot, parentObj);
      expect(parentObj.color).toBe('yellow');
    });
    it('insert new property into a non existing array on root object', () => {
      models.extend(newObjectNoArrayInRoot, parentObj);
      expect(parentObj.array[0].value).toBe('inserted into non existing Array');
    });
    it('override property in tree object', () => {
      models.extend(overridePropertyInTree, parentObj);
      expect(parentObj.rootContainer.children[1].value).toBe('property in tree overridden');
    });
    it('insert new object into tree object', () => {
      models.extend(newObjectInTree, parentObj);
      expect(parentObj.rootContainer.children[1].children[1].value).toBe('new object in tree');
    });
    it('insert new object tree into tree object', () => {
      models.extend(newObjectTreeInTree, parentObj);
      expect(parentObj.rootContainer.children[1].children[1].children[0].value).toBe('new object tree in tree');
    });
    it('insert new object into tree object with fixed index', () => {
      models.extend(newObjectInTreeIndexed, parentObj);
      expect(parentObj.rootContainer.children[1].children[0].value).toBe('fixed index insert');
    });
    it('insert new object into tree object with relative index', () => {
      models.extend(newObjectInTreeRelativeIndex, parentObj);
      expect(parentObj.rootContainer.children[1].children[0].value).toBe('relative index insert');
    });
    it('insert new object into tree object with relative index and two extension elements', () => {
      models.extend(newObjectInTreeRelativeIndexWithArray, parentObj);
      expect(parentObj.rootContainer.children[1].children[0].value).toBe('relative index insert');
      expect(parentObj.rootContainer.children[1].children[1].value).toBe('relative index insert2');
    });
    it('insert object referenced by String', () => {
      let model = {};
      model['newObjectInTreeRelativeIndex'] = newObjectInTreeRelativeIndex;
      models.init(model);
      models.extend('newObjectInTreeRelativeIndex', parentObj);
      expect(parentObj.rootContainer.children[1].children[0].value).toBe('relative index insert');
    });
    it('insert object bound to field', () => {
      models.extend(newObjectGroupWithTarget, parentObj);
      expect(parentObj.rootContainer.children[1].children[0].groupedWith).toBe('child3');

      models.extend(newObjectInTreeRelativeIndexWithArray, parentObj);

      expect(parentObj.rootContainer.children[1].children[0].id).toBe('newObj');
      expect(parentObj.rootContainer.children[1].children[1].id).toBe('newObj2');
      expect(parentObj.rootContainer.children[1].children[2].id).toBe('newObjBound');
      expect(parentObj.rootContainer.children[1].children[3].id).toBe('child3');

    });

    it('insert objects array bound to field', () => {
      models.extend(newObjectArrayGroupWithTarget, parentObj);
      expect(parentObj.rootContainer.children[1].children[0].groupedWith).toBe('child3');
      expect(parentObj.rootContainer.children[1].children[1].groupedWith).toBe('child3');

      models.extend(newObjectInTreeRelativeIndexWithArray, parentObj);

      expect(parentObj.rootContainer.children[1].children[0].id).toBe('newObj');
      expect(parentObj.rootContainer.children[1].children[1].id).toBe('newObj2');
      expect(parentObj.rootContainer.children[1].children[2].id).toBe('newObjBound');
      expect(parentObj.rootContainer.children[1].children[3].id).toBe('newObjBound2');
      expect(parentObj.rootContainer.children[1].children[4].id).toBe('child3');

    });

  });

});
