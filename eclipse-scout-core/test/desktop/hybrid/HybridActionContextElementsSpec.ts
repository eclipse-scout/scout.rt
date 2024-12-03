/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Form, HybridActionContextElement, HybridActionContextElements, scout, Tree, TreeNode} from '../../../src';

describe('HybridActionContextElements', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  it('can create HybridActionContextElements', () => {
    let form0 = new Form();
    let form1 = new Form();
    let form2 = new Form();
    let form3 = new Form();
    let tree = new Tree();
    let treeNode = new TreeNode();

    let baseMap = {
      zero: [],
      one: [HybridActionContextElement.of(form1)],
      two: [HybridActionContextElement.of(form2), HybridActionContextElement.of(form3)]
    };
    let contextElements = scout.create(HybridActionContextElements);
    expect(contextElements.isEmpty()).toBe(true);

    contextElements
      .withElement('existing', HybridActionContextElement.of(form0))
      .withElement('one', HybridActionContextElement.of(form0))
      .withMap(baseMap)
      .withElement('three', tree)
      .withElement('four', tree, treeNode)
      .withElements('five', [])
      .withElements('six', [HybridActionContextElement.of(form1), HybridActionContextElement.of(form2), HybridActionContextElement.of(form3)]);

    expect(contextElements.isEmpty()).toBe(false);
    expect(contextElements.map.size).toBe(8);

    let list = contextElements.getList('existing');
    expect(list.length).toBe(1);
    expect(list[0].getWidget()).toBe(form0);

    list = contextElements.getList('zero');
    expect(list.length).toBe(0);

    list = contextElements.getList('one');
    expect(list.length).toBe(1);
    expect(list[0].getWidget()).toBe(form1);

    list = contextElements.getList('two');
    expect(list.length).toBe(2);
    expect(list[0].getWidget()).toBe(form2);
    expect(list[1].getWidget()).toBe(form3);

    list = contextElements.getList('three');
    expect(list.length).toBe(1);
    expect(list[0].getWidget()).toBe(tree);

    list = contextElements.getList('four');
    expect(list.length).toBe(1);
    expect(list[0].getWidget()).toBe(tree);
    expect(list[0].getElement()).toBe(treeNode);

    list = contextElements.getList('five');
    expect(list.length).toBe(0);

    list = contextElements.getList('six');
    expect(list.length).toBe(3);
    expect(list[0].getWidget()).toBe(form1);
    expect(list[1].getWidget()).toBe(form2);
    expect(list[2].getWidget()).toBe(form3);
  });

  it('provides access to context elements', () => {
    let form1 = new Form();
    let form2 = new Form();
    let form3 = new Form();
    let tree = new Tree();
    let treeNode = new TreeNode();

    let contextElements = scout.create(HybridActionContextElements)
      .withElement('myTree', tree)
      .withElement('myNode', tree, treeNode)
      .withElements('myForms', [HybridActionContextElement.of(form1), HybridActionContextElement.of(form2), HybridActionContextElement.of(form3)]);

    expect(contextElements.getSingle('myTree').getWidget()).toBe(tree);
    expect(contextElements.getList('myTree').length).toBe(1);
    expect(contextElements.getList('myTree')[0]).toBe(contextElements.getSingle('myTree'));

    expect(contextElements.getSingle('myNode').getWidget()).toBe(tree);
    expect(contextElements.getSingle('myNode').getElement()).toBe(treeNode);
    expect(contextElements.getList('myNode').length).toBe(1);
    expect(contextElements.getList('myNode')[0]).toBe(contextElements.getSingle('myNode'));

    expect(contextElements.getSingle('myForms').getWidget()).toBe(form1);
    expect(contextElements.getList('myForms').length).toBe(3);
    expect(contextElements.getList('myForms')[0]).toBe(contextElements.getSingle('myForms'));
    expect(contextElements.getList('myForms')[1].getWidget()).toBe(form2);
    expect(contextElements.getList('myForms')[2].getWidget()).toBe(form3);

    expect(contextElements.optSingle('doesNotExist')).toBe(undefined);
    expect(contextElements.optList('doesNotExist')).toBe(undefined);
    expect(() => contextElements.getSingle('doesNotExist')).toThrow();
    expect(() => contextElements.getList('doesNotExist')).toThrow();
  });
});
