/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Outline, OutlineAdapter, OutlineModel, PageModel, Session} from '../../../index';
import {FormSpecHelper, TableSpecHelper} from '../../index';
import $ from 'jquery';
import {Optional, RefModel, SomeRequired} from '../../../types';
import ModelAdapterModel from '../../../session/ModelAdapterModel';
import {ObjectType} from '../../../ObjectFactory';

export default class OutlineSpecHelper {
  session: Session;

  constructor(session: Session) {
    this.session = session;
  }

  createModelFixture(nodeCount?: number, depth?: number, expanded?: boolean): OutlineModel & { objectType: ObjectType<Outline> } {
    return this.createModel(this.createModelNodes(nodeCount, depth, expanded));
  }

  createModel(nodes: RefModel<PageModel>[]): OutlineModel & { objectType: ObjectType<Outline> } {
    let model = createSimpleModel('Outline', this.session) as OutlineModel & { objectType: ObjectType<Outline> };
    if (nodes) {
      model.nodes = nodes;
    }
    return model;
  }

  createModelNode(id: string, text: string): Optional<PageModel, 'parent'> {
    return {
      id: id,
      text: text
    };
  }

  createModelNodes(nodeCount: number, depth?: number, expanded?: boolean): RefModel<PageModel>[] {
    return this.createModelNodesInternal(nodeCount, depth, expanded);
  }

  createModelNodesInternal(nodeCount: number, depth?: number, expanded?: boolean, parentNode?: PageModel): RefModel<PageModel>[] {
    if (!nodeCount) {
      return;
    }

    let nodes = [],
      nodeId;
    if (!depth) {
      depth = 0;
    }
    for (let i = 0; i < nodeCount; i++) {
      nodeId = i;
      if (parentNode) {
        nodeId = parentNode.id + '_' + nodeId;
      }
      nodes[i] = this.createModelNode(nodeId, 'node ' + i);
      nodes[i].expanded = expanded;
      if (depth > 0) {
        nodes[i].childNodes = this.createModelNodesInternal(nodeCount, depth - 1, expanded, nodes[i]);
      }
    }
    return nodes;
  }

  createOutline(model: OutlineModel): Outline {
    let defaults = {
      parent: this.session.desktop
    };
    model = $.extend({}, defaults, model);
    let tree = new Outline();
    tree.init(model);
    return tree;
  }

  createOutlineAdapter(model: ModelAdapterModel | SomeRequired<OutlineModel, 'session' | 'id'>): OutlineAdapter {
    let outlineAdapter = new OutlineAdapter();
    outlineAdapter.init(model);
    return outlineAdapter;
  }

  /**
   * Creates an outline with 3 nodes, the first node has a visible detail form
   */
  createOutlineWithOneDetailForm(): Outline {
    let model = this.createModelFixture(3, 2, true);
    let outline = this.createOutline(model);
    let node = outline.nodes[0];
    node.detailForm = new FormSpecHelper(this.session).createFormWithOneField({
      modal: false
    });
    node.detailFormVisible = true;
    return outline;
  }

  /**
   * Creates an outline with 3 nodes, the first node has a visible detail table
   */
  createOutlineWithOneDetailTable(): Outline {
    let model = this.createModelFixture(3, 2, true);
    let outline = this.createOutline(model);
    let node = outline.nodes[0];
    node.detailTable = new TableSpecHelper(this.session).createTableWithOneColumn();
    node.detailTableVisible = true;
    return outline;
  }

  setMobileFlags(outline: Outline) {
    outline.setBreadcrumbStyleActive(true);
    outline.setToggleBreadcrumbStyleEnabled(false);
    outline.setCompact(true);
    outline.setEmbedDetailContent(true);
  }
}
