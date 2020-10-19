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
import {Outline, OutlineAdapter} from '../../../index';
import {FormSpecHelper, TableSpecHelper} from '../../index';
import $ from 'jquery';

export default class OutlineSpecHelper {
  constructor(session) {
    this.session = session;
  }

  createModelFixture(nodeCount, depth, expanded) {
    return this.createModel(this.createModelNodes(nodeCount, depth, expanded));
  }

  createModel(nodes) {
    let model = createSimpleModel('Outline', this.session);
    if (nodes) {
      model.nodes = nodes;
    }
    return model;
  }

  createModelNode(id, text) {
    return {
      'id': id,
      'text': text
    };
  }

  createModelNodes(nodeCount, depth, expanded) {
    return this.createModelNodesInternal(nodeCount, depth, expanded);
  }

  createModelNodesInternal(nodeCount, depth, expanded, parentNode) {
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

  createOutline(model) {
    let defaults = {
      parent: this.session.desktop
    };
    model = $.extend({}, defaults, model);
    let tree = new Outline();
    tree.init(model);
    return tree;
  }

  createOutlineAdapter(model) {
    let outlineAdapter = new OutlineAdapter();
    outlineAdapter.init(model);
    return outlineAdapter;
  }

  /**
   * Creates an outline with 3 nodes, the first node has a visible detail form
   */
  createOutlineWithOneDetailForm() {
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
  createOutlineWithOneDetailTable() {
    let model = this.createModelFixture(3, 2, true);
    let outline = this.createOutline(model);
    let node = outline.nodes[0];
    node.detailTable = new TableSpecHelper(this.session).createTableWithOneColumn();
    node.detailTableVisible = true;
    return outline;
  }

  setMobileFlags(outline) {
    outline.setBreadcrumbStyleActive(true);
    outline.setToggleBreadcrumbStyleEnabled(false);
    outline.setCompact(true);
    outline.setEmbedDetailContent(true);
  }
}
