/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Form, HybridActionContextElement, HybridActionContextElements, Tree, TreeNode} from '../../../src';

describe('HybridActionContextElements', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  it('throws with null', () => {
    expect(() => HybridActionContextElement.of(null)).toThrow();
    expect(() => HybridActionContextElement.of(null, null)).toThrow();
  });

  it('works with widget', () => {
    let form = new Form();
    let contextElement = HybridActionContextElement.of(form);

    expect(contextElement.widget).toBe(form);
    expect(contextElement.getWidget()).toBe(form);
    expect(contextElement.getWidget(Form)).toBe(form);

    expect(contextElement.element).toBe(null);
    expect(contextElement.optElement()).toBe(null);
    expect(() => contextElement.getElement()).toThrow();
  });

  it('works with widget and element', () => {
    let tree = new Tree();
    let treeNode = new TreeNode();
    let contextElement = HybridActionContextElement.of(tree, treeNode);

    expect(contextElement.widget).toBe(tree);
    expect(contextElement.getWidget()).toBe(tree);
    expect(contextElement.getWidget(Tree)).toBe(tree);
    expect(() => contextElement.getWidget(Form)).toThrow();

    expect(contextElement.element).toBe(treeNode);
    expect(contextElement.optElement()).toBe(treeNode);
    expect(contextElement.optElement(TreeNode)).toBe(treeNode);
    expect(() => contextElement.optElement(Form)).toThrow();
    expect(contextElement.getElement()).toBe(treeNode);
    expect(contextElement.getElement(TreeNode)).toBe(treeNode);
    expect(() => contextElement.getElement(Form)).toThrow();

    // Unrelated elements are ok in model code (but not in JSON layer)
    let form = new Form();
    HybridActionContextElement.of(form, {});
    HybridActionContextElement.of(form, treeNode);
  });
});
