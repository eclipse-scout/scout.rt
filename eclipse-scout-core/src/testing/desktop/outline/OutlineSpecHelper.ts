/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FullModelOf, InitModelOf, ModelAdapter, ObjectOrModel, Outline, OutlineAdapter, OutlineModel, Page, PageModel, Session} from '../../../index';
import {FormSpecHelper, TableSpecHelper} from '../../index';
import $ from 'jquery';

export class OutlineSpecHelper {
  session: Session;

  constructor(session: Session) {
    this.session = session;
  }

  createModelFixture(nodeCount?: number, depth?: number, expanded?: boolean): FullModelOf<Outline> & { id: string; session: Session } {
    return this.createModel(this.createModelNodes(nodeCount, depth, expanded));
  }

  createModel(nodes: ObjectOrModel<Page>[]): FullModelOf<Outline> & { id: string; session: Session } {
    let model = createSimpleModel('Outline', this.session) as FullModelOf<Outline> & { id: string; session: Session };
    if (nodes) {
      model.nodes = nodes;
    }
    return model;
  }

  createModelNode(id: string, text: string): PageModel {
    return {
      id: id,
      text: text
    };
  }

  createModelNodes(nodeCount: number, depth?: number, expanded?: boolean): PageModel[] {
    return this.createModelNodesInternal(nodeCount, depth, expanded);
  }

  createModelNodesInternal(nodeCount: number, depth?: number, expanded?: boolean, parentNode?: PageModel): PageModel[] {
    if (!nodeCount) {
      return;
    }

    let nodes: PageModel[] = [], nodeId;
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

  createOutline(model?: OutlineModel): Outline {
    let defaults = {
      parent: this.session.desktop
    };
    let m = $.extend({}, defaults, model) as InitModelOf<Outline>;
    let outline = new Outline();
    outline.init(m);
    return outline;
  }

  createOutlineAdapter(model: InitModelOf<ModelAdapter> | OutlineModel & { id: string; session: Session }): OutlineAdapter {
    let outlineAdapter = new OutlineAdapter();
    outlineAdapter.init(model as InitModelOf<OutlineAdapter>);
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
