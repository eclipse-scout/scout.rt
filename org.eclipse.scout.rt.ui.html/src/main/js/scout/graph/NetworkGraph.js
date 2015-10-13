scout.NetworkGraph = function() {
  scout.NetworkGraph.parent.call(this);

  this.cssClass = 'graph';
  this.edgesByNode = {};
  this.edgeLengths = [];
};
scout.inherits(scout.NetworkGraph, scout.Widget);

scout.NetworkGraph.prototype._init = function(options) {
  scout.NetworkGraph.parent.prototype._init.call(this, options);

  $.extend(this, options);
  this.initGraph();
};

scout.NetworkGraph.prototype.initGraph = function() {
  // TODO BSH Check if labels need to be html-encoded (appendSVG)
  var edge, i;

  this.edgeLength = 100;
  this.gravity = 0.0;
  this.repulsion = 150;

  //initialize nodes
  for (i = 0; i < this.nodes.length; i++) {
    this.edgesByNode[this.nodes[i].id] = [];
    this.nodes[i].degree = 0;
  }

  //initialize edges
  for (i = 0; i < this.edges.length; i++) {
    edge = this.edges[i];
    this.edgesByNode[edge.source].push(edge);
    this.edgesByNode[edge.target].push(edge);
    //degree:
    this.nodes[edge.source].degree += 1;
    this.nodes[edge.target].degree += 1;
    this.edgeLengths[i] = edge.length || this.edgeLength;
  }
};

scout.NetworkGraph.prototype._renderInternal = function($parent) {

  var n = this.nodes.length,
    i, node;
  //copied from Chris' prototype...
  this.$container = $parent.appendSVG('svg', 'graph-container');

  //some basics
  this.radius = 10.0;
  this.kelvin = 0.99;
  this.wContainer = this.$container.width();
  this.hContainer = this.$container.height();

  //create all links with label
  for (var l = 0; l < this.edges.length; l++) {
    var link = this.edges[l];

    link.$div = this.$container.appendSVG('line', 'graph-link');
    link.$divText = this.$container.appendSVG('text', 'graph-link-text', link.label)
      .attr('dy', -5);
  }

  //create nodes with text and place them randomly
  for (i = 0; i < this.nodes.length; i++) {

    node = this.nodes[i];

    node.x = Math.random() * (this.wContainer - this.getSize(node));
    node.y = Math.random() * (this.hContainer - this.getSize(node));
    node.dx = 0;
    node.dy = 0;
    node.prevX = node.x;
    node.prevY = node.y;
  }

  var done = false;
  for (i = 0; i < this.nodes.length; i++) {
    done = this.step(false, false);
  }

  this.stopAnimation();

  for (i = 0; i < this.nodes.length; i++) {
    node = this.nodes[i];
    node.$div = this.$container.appendSVG('circle', 'graph-node ' + node.cssClass)
      .attr('r', this.getSize(node))
      .attr('cx', node.x).attr('cy', node.y)
      .on('mousedown', this.moveNode.bind(this))
      .on('dblclick', this.lockNode.bind(this, node));

    node.$divText = this.$container.appendSVG('text', 'graph-node-text', node.label)
      .on('mousedown', this.moveNode.bind(this));
  }

  //start optimization
  this.start();
};

scout.NetworkGraph.prototype.adjustTemperature = function(k) {
  k = +k;
  if (k === 0) {
    this.kelvin = 0; //will stop after current step() call
  } else if (this.kelvin === 0) {
    //start it up:
    this.kelvin = k;
    setTimeout(this.step.bind(this), 0);
  } else if (k > this.kelvin) {
    //already running, increase if necessary.
    this.kelvin = k;
  }
};

scout.NetworkGraph.prototype.start = function() {
  this.adjustTemperature(0.9);
};

scout.NetworkGraph.prototype.resume = function() {
  this.adjustTemperature(0.3);
};

//executes one simulation step
scout.NetworkGraph.prototype.step = function(draw, recurse) {
  draw = scout.helpers.nvl(draw, true);
  recurse = scout.helpers.nvl(recurse, true);

  if ((this.kelvin *= 0.98) < 0.01) {
    //end
    this.kelvin = 0;
    return true;
  }

  //variables:
  var m = this.edges.length,
    n = this.nodes.length,
    i, j = 0,
    dx, dy = 0,
    w,
    f = 0,
    repForce = 0, //force factor
    d = 0,
    cx = this.wContainer / 2,
    cy = this.hContainer / 2, //coordinates of center of drawing area
    rf = [],
    sf = [],
    gf = [],
    n1, n2;

  //First: accumulate rep forces:

  //repulsive force
  for (i = 0; i < n; i++) {
    n1 = this.nodes[i];
    for (j = i; j < n; j++) {
      //repulsion force:
      if (i === j) {
        continue;
      }
      n2 = this.nodes[j];

      //(dx, dy) is the vector from n1 to n2
      dx = n2.x - n1.x; //n1 to n2 in x-coord
      dy = n2.y - n1.y; //n1 to n2 in y-coord

      d = (dx * dx + dy * dy); //squared distance between n1 and n2
      if (d) { //avoid division by zero
        repForce = this.kelvin * (this.repulsion / d);
        dx *= repForce;
        dy *= repForce;
        w = (n1.degree / (n1.degree + n2.degree));
        //        w = 0.5;
        rf.push({
          n1: n1,
          n2: n2,
          x: dx,
          y: dy
        });
        n1.dx -= (2 - w) * dx;
        n1.dy -= (2 - w) * dy;
        n2.dx += (1 + w) * dx;
        n2.dy += (1 + w) * dy;
      }
    }

  }

  //verlet integration:

  var oldx, oldy;
  for (i = 0; i < n; i++) {
    n1 = this.nodes[i];
    if (n1.locked || n1.userLocked) {
      n1.x = n1.prevX;
      n1.y = n1.prevY;
    } else {
      oldx = n1.x;
      oldy = n1.y;
      n1.x = n1.x + (n1.x - n1.prevX + n1.dx) * 0.9;
      n1.y = n1.y + (n1.y - n1.prevY + n1.dy) * 0.9;
      this.ensureInside(n1);
      n1.prevX = oldx;
      n1.prevY = oldy;

    }
    n1.dx = 0;
    n1.dy = 0;
  }

  //constraint satisfaction

  //spring force
  for (i = 0; i < m; i++) {
    n1 = this.nodes[this.edges[i].source];
    n2 = this.nodes[this.edges[i].target];

    //(dx, dy) is the vector from n1 to n2;
    dx = n2.x - n1.x; //n1 to n2 in x-coord
    dy = n2.y - n1.y; //n1 to n2 in y-coord

    d = (dx * dx + dy * dy); //squared distance between n1 and n2
    if (d) { //avoid division by zero
      d = Math.sqrt(d);
      f = this.kelvin * ((d - this.edgeLengths[i]) / (d));
      //      f = this.kelvin * (Math.max((d - this.edgeLengths[i]), 0) / (1.5*d));
      dx *= f;
      dy *= f;
      w = (n1.degree / (n1.degree + n2.degree));
      sf.push({
        n1: n1,
        n2: n2,
        x: dx,
        y: dy
      });
      n1.x += dx * (1 - w);
      n1.y += dy * (1 - w);
      n2.x -= dx * w;
      n2.y -= dy * w;
    }
  }

  for (i = 0; i < n; i++) {
    n1 = this.nodes[i];
    f = this.kelvin * this.gravity;
    if (n1.cssClass.localeCompare('node_center') === 0) {
      f += ((1 - f) / 10); //make center node strive more to middle
    }
    //gravity:
    //(dx, dy) is the vector from n1 to the center;
    dx = cx - n1.x; //n1 to center in x-coord;
    dy = cy - n1.y; //n1 to center in y-coord;
    n1.x += f * dx;
    n1.y += f * dy;
  }

  for (i = 0; i < n; i++) {
    n1 = this.nodes[i];
    if (n1.locked || n1.userLocked) {
      n1.x = n1.prevX;
      n1.y = n1.prevY;
    }
    //    this.ensureInside(n1);
  }

  if (draw) {
    for (i = 0; i < n; i++) {
      n1 = this.nodes[i];
      this.setNodeDirect(n1, n1.x, n1.y);
    }
  }

  if (recurse) {
    setTimeout(this.step.bind(this), 0);
  }

};

//set label of a link
scout.NetworkGraph.prototype.setLabel = function(link) {
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

// dissolve crossing links
scout.NetworkGraph.prototype.disolveLinks = function() {
  for (var l1 = 0; l1 < this.edges.length; l1++) {
    var link1 = this.edges[l1],
      E = {}, F = {};

    E.x = this.getPos(link1, 'x1');
    E.y = this.getPos(link1, 'y1');
    F.x = this.getPos(link1, 'x2');
    F.y = this.getPos(link1, 'y2');

    for (var l2 = 0; l2 < this.edges.length; l2++) {
      if (l1 === l2) {
        continue;
      }

      var link2 = this.edges[l2],
        P = {}, Q = {};

      P.x = this.getPos(link2, 'x1');
      P.y = this.getPos(link2, 'y1');
      Q.x = this.getPos(link2, 'x2');
      Q.y = this.getPos(link2, 'y2');

      // ckeck if crossing exists, if yes: change position
      if ((test(E, P, Q) !== test(F, P, Q)) && (test(E, F, P) !== test(E, F, Q))) {
        var n1 = this.nodes[link1.target],
          n2 = this.nodes[link2.target],
          dx = this.getPos(n1, 'cx') - this.getPos(n2, 'cx'),
          dy = this.getPos(n1, 'cy') - this.getPos(n2, 'cy');

        this.setNode(n1, -dx, -dy);
        this.setNode(n2, dx, dy);
      }
    }
  }

  function test(p1, p2, p3) {
    return (p3.y - p1.y) * (p2.x - p1.x) > (p2.y - p1.y) * (p3.x - p1.x);
  }
};

// move node by mouse
scout.NetworkGraph.prototype.moveNode = function(event) {
  var startX = event.pageX,
    startY = event.pageY,
    clickedNode,
    that = this;

  for (var n = 0; n < this.nodes.length; n++) {
    var node = this.nodes[n];
    if ($(event.target).is(node.$div) || $(event.target).is(node.$divText)) {
      clickedNode = this.nodes[n];
    }
  }

  $(window)
    .on('mousemove.graphtablecontrol', '', nodeMove)
    .one('mouseup', '', nodeEnd.bind(this, clickedNode));
  return false;

  function nodeMove(event) {
    var dx = event.pageX - startX,
      dy = event.pageY - startY;

    clickedNode.x += dx;
    clickedNode.y += dy;
    clickedNode.prevX = clickedNode.x;
    clickedNode.prevY = clickedNode.y;
    clickedNode.locked = true;

    that.setNodeDirect.call(that, clickedNode, clickedNode.x, clickedNode.y);
    startX = event.pageX;
    startY = event.pageY;
    that.resume();
  }

  function nodeEnd(node) {
    $(window).off('mousemove.graphtablecontrol');
    node.locked = false;
    that.resume();
  }
};

scout.NetworkGraph.prototype.lockNode = function(node) {
  if (node.userLocked) {
    node.userLocked = false;
  } else {
    node.userLocked = true;
  }
};

scout.NetworkGraph.prototype.getSize = function(node) {
  return this.radius + Math.ceil(node.degree / 2);
};

scout.NetworkGraph.prototype.getPos = function(e, d) {
  return parseFloat(e.$div.attr(d));
};

//moving nodes and links by dx and dy
scout.NetworkGraph.prototype.setNode = function(node, dx, dy) {
  this.setNodeDirect(node, node.x + dx, node.y + dy);
};

//moves a node and associated links to given position
scout.NetworkGraph.prototype.setNodeDirect = function(node, actualX, actualY) {

  actualX = Math.min(actualX, this.wContainer - this.radius);
  actualX = Math.max(actualX, 0);

  actualY = Math.min(actualY, this.hContainer - this.radius);
  actualY = Math.max(actualY, this.radius);

  node.$div
    .attr('cx', actualX)
    .attr('cy', actualY);

  node.$divText
    .attr('x', actualX)
    .attr('y', actualY - this.getSize(node) * 1.5);

  for (var l = 0; l < this.edgesByNode[node.id].length; l++) {
    var link = (this.edgesByNode[node.id])[l];
    if (link.source === node.id) {
      link.$div
        .attr('x1', actualX)
        .attr('y1', actualY);
      this.setLabel(link);
    } else if (link.target === node.id) {
      link.$div
        .attr('x2', actualX)
        .attr('y2', actualY);
      this.setLabel(link);
    }
  }

  node.x = actualX;
  node.y = actualY;
};

scout.NetworkGraph.prototype._remove = function() {
  this.stopAnimation();
  this._resetGraph();
  scout.NetworkGraph.parent.prototype._remove.call(this);
};

scout.NetworkGraph.prototype._resetGraph = function() {
  var i, n1;
  for (i = 0; i < this.nodes.length; i++) {
    n1 = this.nodes[i];
    n1.locked = false;
    n1.userLocked = false;
  }
};

scout.NetworkGraph.prototype.stopAnimation = function() {
  this.adjustTemperature(0);
};

scout.NetworkGraph.prototype.ensureInside = function(node) {
  var actualX, actualY;
  actualX = Math.min(node.x, this.wContainer - this.radius);
  actualX = Math.max(actualX, 0);

  actualY = Math.min(node.y, this.hContainer - this.radius);
  actualY = Math.max(actualY, this.radius);

  node.x = actualX;
  node.y = actualY;
};

scout.NetworkGraph.prototype.onResize = function() {
  this.wContainer = this.$container.width();
  this.hContainer = this.$container.height();
  this.resume();
};
