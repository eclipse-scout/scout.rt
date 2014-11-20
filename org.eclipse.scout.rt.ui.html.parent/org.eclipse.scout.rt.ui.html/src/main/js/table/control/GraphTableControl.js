// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.GraphTableControl = function() {
  scout.GraphTableControl.parent.call(this);
};
scout.inherits(scout.GraphTableControl, scout.TableControl);

scout.GraphTableControl.prototype._renderContent = function($parent) {
  this.$container = $parent.appendSVG('svg', '', 'graph-container');

  // some basics
  this.wBox = 120;
  this.hBox = 60;
  this.kelvin = 1000;
  this.wContainer = this.$container.width();
  this.hContainer = this.$container.height();

  // create all links with label
  for (var l = 0; l < this.graph.links.length; l++) {
    var link = this.graph.links[l];

    link.$div = this.$container.appendSVG('line', null, 'graph-link');
    link.$divText = this.$container.appendSVG('text', null, 'graph-link-text', link.label)
      .attr('dy', -5);
  }

  // create nodes with text and place them randomly
  for (var n = 0; n < this.graph.nodes.length; n++) {
    var node = this.graph.nodes[n];

    node.$div = this.$container.appendSVG('rect', null, 'graph-node ' + node.type)
      .attr('width', this.wBox).attr('height', this.hBox)
      .attr('x', 0).attr('y', 0)
      .on('mousedown', this.moveNode.bind(this));

    node.$divText = this.$container.appendSVG('text', null, 'graph-node-text', node.name)
      .on('mousedown', this.moveNode.bind(this));

    this.setNode(node, Math.random() * (this.wContainer - this.wBox), Math.random() * (this.hContainer - this.hBox));
  }

  // start optimization
  this.disolveLinks();
  this.doPhysics();
};

// moving nodes and links by dx and dy
scout.TableControl.prototype.setNode = function (node, dx, dy) {
  var x = this.getPos(node, 'x'),
    y = this.getPos(node, 'y');

  node.$div
    .attr('x', x + dx)
    .attr('y', y + dy);

  node.$divText
    .attr('x', x + dx + this.wBox / 2)
    .attr('y', y + dy + this.hBox / 2);

  for (var l = 0; l < this.graph.links.length; l++) {
    var link = this.graph.links[l];

    if (link.source === node.id) {
      link.$div
        .attr('x1', x + dx + this.wBox / 2)
        .attr('y1', y + dy + this.hBox / 2);
      this.setLabel(link);
    } else if (link.target === node.id) {
      link.$div
        .attr('x2', x + dx + this.wBox / 2)
        .attr('y2', y + dy + this.hBox / 2);
      this.setLabel(link);
    }
  }
};

// set label of a link
scout.TableControl.prototype.setLabel = function (link) {
  var x1 = this.getPos(link, 'x1'),
    y1 = this.getPos(link, 'y1'),
    x2 = this.getPos(link, 'x2'),
    y2 = this.getPos(link, 'y2');

  link.$divText
    .attr('x', (x1 + x2) / 2)
    .attr('y', (y1 + y2) / 2)
    .attr('transform', 'rotate( ' + (Math.atan((y2 - y1) / (x2 - x1)) / Math.PI * 180) +
      ', ' + ((x1 + x2) / 2) + ', ' + ((y1 + y2) / 2) + ')');
};

// disolve crossing links
scout.TableControl.prototype.disolveLinks = function () {
  for (var l1 = 0; l1 < this.graph.links.length; l1++) {
    var link1 = this.graph.links[l1],
      E = {}, F = {};

    E.x = this.getPos(link1, 'x1'),
    E.y = this.getPos(link1, 'y1'),
    F.x = this.getPos(link1, 'x2'),
    F.y = this.getPos(link1, 'y2');

    for (var l2 = 0; l2 < this.graph.links.length; l2++) {
      if (l1 === l2) continue;

      var link2 = this.graph.links[l2],
        P = {}, Q = {};

      P.x = this.getPos(link2, 'x1'),
      P.y = this.getPos(link2, 'y1'),
      Q.x = this.getPos(link2, 'x2'),
      Q.y = this.getPos(link2, 'y2');

      // ckeck if crossing exists, if yes: change position
      if ((_test(E, P, Q) !== _test(F, P, Q)) && (_test(E, F, P) !== _test(E, F, Q))) {
        var n1 = this.graph.nodes[link1.target],
          n2 = this.graph.nodes[link2.target],
          dx = this.getPos(n1, 'x') - this.getPos(n2, 'x'),
          dy = this.getPos(n1, 'y') - this.getPos(n2, 'y');

        this.setNode(n1, -dx, -dy);
        this.setNode(n2, dx, dy);
      }
    }
  }

  function _test(p1, p2, p3) {
    return (p3.y - p1.y) * (p2.x - p1.x) > (p2.y - p1.y) * (p3.x - p1.x);
  }
};

  // force the nodes to their place
scout.TableControl.prototype.doPhysics = function () {
  var totalDiff = 0;

  for (var n = 0; n < this.graph.nodes.length; n++) {
    var node = this.graph.nodes[n],
      x = this.getPos(node, 'x'),
      y = this.getPos(node, 'y'),
      dx,dy;
    dx = 0, dy = 0;

    // move center to the middle
    if (node.type === 'center') {
      dx += ((this.wContainer - this.wBox) / 2 - x) / 40;
      dy += ((this.hContainer - this.hBox) / 2 - y) / 40;
    }

    // move from outside
    dx -= (Math.min(x, 5) - 5) / 2;
    dy -= (Math.min(y, 5) - 5) / 2;
    dx += (Math.min(this.wContainer - this.wBox - x, 10) - 10) / 4;
    dy += (Math.min(this.hContainer - this.hBox - y, 10) - 10) / 4;

    // gravity
    dx += ((this.wContainer - this.wBox) / 2 - x) / 500;
    dy += ((this.hContainer - this.hBox) / 2 - y) / 500;

    // repulsion force
    for (var o = 0; o < this.graph.nodes.length; o++) {
      var otherNode = this.graph.nodes[o];
      if (o !== n) {
        var oX = this.getPos(otherNode, 'x'),
          oY = this.getPos(otherNode, 'y'),
          repForce = 100 / (Math.pow(x - oX, 2) + Math.pow(y - oY, 2));

        dx += (x - oX) * repForce;
        dy += (y - oY) * repForce;
      }
    }

    // spring force
    for (var l = 0; l < this.graph.links.length; l++) {
      var link = this.graph.links[l],
        oppositeNode = null;

      if (link.source === node.id) {
        oppositeNode = this.graph.nodes[link.target];
      } else if (link.target === node.id) {
        oppositeNode = this.graph.nodes[link.source];
      }

      if (oppositeNode) {
        var otherX = this.getPos(oppositeNode, 'x');
        var otherY = this.getPos(oppositeNode, 'y');

        var dist = Math.sqrt(Math.pow(x - otherX, 2) + Math.pow(y - otherY, 2)),
          springForce = Math.log(dist / 260) / 10;

        dx -= (x - otherX) * springForce;
        dy -= (y - otherY) * springForce;
      }
    }

    // adjust position
    this.setNode(node, dx, dy);
    totalDiff += Math.abs(dx) + Math.abs(dy);
  }

  // cool down, heat up
  if (this.kelvin-- > 0) setTimeout(this.doPhysics.bind(this), 0);
};

// move node by mouse
scout.GraphTableControl.prototype.moveNode = function (event) {
  var startX = event.pageX,
    startY = event.pageY,
    clickedNode,
    that = this;

  for (var n = 0; n < this.graph.nodes.length; n++) {
    var node = this.graph.nodes[n];
    if ($(event.target).is(node.$div) || $(event.target).is(node.$divText)) {
      clickedNode = this.graph.nodes[n];
    }
  }

  $('body').on('mousemove', '', nodeMove)
    .one('mouseup', '', nodeEnd);
  return false;

  function nodeMove(event) {
    that.setNode.call(that, clickedNode, event.pageX - startX, event.pageY - startY);
    startX = event.pageX;
    startY = event.pageY;
    that.kelvin = 0;
  }

  function nodeEnd() {
    $('body').off('mousemove');
    that.kelvin = 200;
    that.doPhysics.call(that);
  }
};

scout.GraphTableControl.prototype.getPos = function(e, d) {
  return parseFloat(e.$div.attr(d));
};

scout.GraphTableControl.prototype._removeContent = function() {
  this.$container.remove();
};

scout.GraphTableControl.prototype._renderGraph = function(graph) {
  this.renderContent();
};

scout.GraphTableControl.prototype.isContentAvailable = function() {
  return this.graph;
};

scout.GraphTableControl.prototype.onResize = function() {
  if (this.contentRendered) {
    this.wContainer = this.$container.width();
    this.hContainer = this.$container.height();
    this.kelvin = 600;
    this.doPhysics();
  }
};
