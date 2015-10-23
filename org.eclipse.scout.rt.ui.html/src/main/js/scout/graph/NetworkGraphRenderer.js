scout.NetworkGraphRenderer = function(graph) {
  this.graph = graph;
  this.session = graph.session;

  this.rendered = false;
  this.width = 0;
  this.height = 0;

  // Constants
  this.defaultEdgeLength = 100;
  this.gravity = 0.0;
  this.repulsion = 150;
  this.radius = 10.0;
  this.minEnergy = 0.01; // epsilon

  // Data structures
  this.nodes = [];
  this.edges = [];
  this.edgesByNodeId = {};
};

scout.NetworkGraphRenderer.prototype.render = function() {
  this.$svg = this.graph.$container.appendSVG('svg', 'graph-svg');
  this.width = this.$svg.width();
  this.height = this.$svg.height();

  if (this.graph && this.graph.graphModel) {
    // Initial
    this._prepareData();
    this._prepareSVG();
    this._distributeNodes();
    this._dissolveEdges();
    // Start optimization
    this.start();
  }

  this.rendered = true;
};

scout.NetworkGraphRenderer.prototype.remove = function() {
  // Remove SVG
  if (this.rendered) {
    this.stop();

    this.$svg.remove();
    this.$svg = null;
  }

  // Clear data structures
  this.nodes = [];
  this.edges = [];
  this.edgesByNodeId = {};

  this.rendered = false;
};

scout.NetworkGraphRenderer.prototype.touch = function() {
  if (this.rendered && this.graph.visible) {
    var oldWidth = this.width;
    var oldHeight = this.height;
    this.width = this.$svg.width();
    this.height = this.$svg.height();
    if (oldWidth !== this.width || oldHeight !== this.height) {
      this.resume();
    }
  }
};

scout.NetworkGraphRenderer.prototype._prepareData = function() {
  // Create copies of nodes and edges to be able to modify them without modifying the original graphModel
  this.nodes = scout.arrays.ensure(scout.objects.valueCopy(this.graph.graphModel.nodes));
  this.edges = scout.arrays.ensure(scout.objects.valueCopy(this.graph.graphModel.edges));

  // Initialize nodes
  this.nodes.forEach(function(node) {
    this.edgesByNodeId[node.id] = [];
    node.degree = 0;
    node.radius = this.nodeRadius(node);
  }.bind(this));

  // Initialize edges
  this.edges.forEach(function(edge, i) {
    this.edgesByNodeId[edge.source].push(edge);
    this.edgesByNodeId[edge.target].push(edge);
    // degree
    this.nodes[edge.source].degree += 1;
    this.nodes[edge.target].degree += 1;
    edge.length = scout.helpers.nvl(edge.length, this.defaultEdgeLength);
  }.bind(this));

  // Calculate node radius
  this.nodes.forEach(function(node) {
    node.radius = this.nodeRadius(node);
  }.bind(this));
};

scout.NetworkGraphRenderer.prototype._prepareSVG = function() {
  // Create all links with label
  this.edges.forEach(function(edge) {
    edge.$edge = this.$svg.appendSVG('line', 'graph-link')
      .data('edge', edge);
    if (edge.label) {
      edge.$label = this.$svg.appendSVG('text', 'graph-link-text')
        .text(edge.label)
        .attr('dy', -5);
    }
  }.bind(this));

  // Create all nodes with label
  this.nodes.forEach(function(node) {
    node.$node = this.$svg.appendSVG('circle', scout.strings.join(' ', 'graph-node', node.cssClass))
      .data('node', node)
      .attr('r', node.radius)
      .on('mousedown', this._onNodeMousedown.bind(this))
      .on('dblclick', this._onNodeDblclick.bind(this));
    if (node.label) {
      node.$label = this.$svg.appendSVG('text', scout.strings.join(' ', 'graph-node-text', node.cssClass))
        .text(node.label)
        .on('mousedown', this._onNodeMousedown.bind(this));
    }
  }.bind(this));
};

scout.NetworkGraphRenderer.prototype._distributeNodes = function() {
  // Place nodes randomly on drawing area
  this.nodes.forEach(function(node) {
    node.x = Math.random() * (this.width - node.radius);
    node.y = Math.random() * (this.height - node.radius);
    node.dx = 0;
    node.dy = 0;
    node.prevX = node.x;
    node.prevY = node.y;

    this.updateNodePosition(node);
  }.bind(this));

  // Manually perform a few simulation steps (number of nodes seems to be a reasonable value)
  var n = this.nodes.length;
  this.energy = 0.99;
  while (n--) {
    this.step();
  }
  this.energy = 0;
};

//dissolve crossing links
scout.NetworkGraphRenderer.prototype._dissolveEdges = function() {
  for (var i = 0; i < this.edges.length; i++) {
    var edge1 = this.edges[i],
      E = {},
      F = {};

    E.x = edge1.x1;
    E.y = edge1.y1;
    F.x = edge1.x2;
    F.y = edge1.y2;

    for (var j = 0; j < this.edges.length; j++) {
      if (i === j) {
        continue;
      }

      var edge2 = this.edges[j],
        P = {},
        Q = {};

      P.x = edge2.x1;
      P.y = edge2.y1;
      Q.x = edge2.x2;
      Q.y = edge2.y2;

      // Check if crossing exists, if yes: change position
      if ((test(E, P, Q) !== test(F, P, Q)) && (test(E, F, P) !== test(E, F, Q))) {
        var node1 = this.nodes[edge1.target],
          node2 = this.nodes[edge2.target],
          dx = node1.x - node2.x,
          dy = node1.y - node2.y;
        this.updateNodePosition(node1, -dx, -dy);
        this.updateNodePosition(node2, dx, dy);
      }
    }
  }

  // ----- Helper functions -----

  function test(p1, p2, p3) {
    return (p3.y - p1.y) * (p2.x - p1.x) > (p2.y - p1.y) * (p3.x - p1.x);
  }
};

//move node by mouse
scout.NetworkGraphRenderer.prototype._onNodeMousedown = function(event) {
  var startX = Math.floor(event.pageX),
    startY = Math.floor(event.pageY),
    $target = $(event.target),
    node = $target.data('node'),
    moved = false;

  $(window)
    .on('mousemove.graph', nodeMove.bind(this))
    .one('mouseup', nodeMoveEnd.bind(this));

  return false;

  // ----- Helper functions -----

  function nodeMove(event) {
    var x = Math.floor(event.pageX),
      y = Math.floor(event.pageY),
      dx = x - startX,
      dy = y - startY;

    node.x += dx;
    node.y += dy;
    node.prevX = node.x;
    node.prevY = node.y;
    node.locked = true;
    this.updateNodePosition(node);

    startX = x;
    startY = y;
    if (dx !== 0 || dy !== 0) {
      moved = true;
      this.resume();
    }
  }

  function nodeMoveEnd(event) {
    $(window).off('mousemove.graph');
    node.locked = false;
    if (moved) {
      this.resume();
    }
  }
};

scout.NetworkGraphRenderer.prototype._onNodeDblclick = function(event) {
  var $node = $(event.target),
    node = $node.data('node');
  this.lockNode(node);
};

scout.NetworkGraphRenderer.prototype.lockNode = function(node) {
  node.userLocked = !node.userLocked;
};

scout.NetworkGraphRenderer.prototype.nodeRadius = function(node) {
  return this.radius + Math.ceil(node.degree / 2);
};

//executes one simulation step
scout.NetworkGraphRenderer.prototype.step = function() {
  // Remove a little bit of energy
  this.energy *= 0.98;

  // Stop immediately when a minimal energy level is reached
  if (this.energy < this.minEnergy) {
    this.energy = 0;
    return;
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
    cx = this.width / 2,
    cy = this.height / 2, //coordinates of center of drawing area
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
        repForce = this.energy * (this.repulsion / d);
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
      f = this.energy * ((d - this.edges[i].length) / (d));
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
    f = this.energy * this.gravity;
    if (n1.cssClass === 'center') { // FIXME FKO Replace by flag
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
  }
};

scout.NetworkGraphRenderer.prototype.stepAndDraw = function() {
  this.step();
  this.nodes.forEach(function(node) {
    this.updateNodePosition(node);
  }.bind(this));
};

scout.NetworkGraphRenderer.prototype.stepAndDrawRecursive = function() {
  this.stepAndDraw();
  if (this.energy) {
    setTimeout(this.stepAndDrawRecursive.bind(this));
  }
};

scout.NetworkGraphRenderer.prototype.adjustEnergy = function(energy) {
  energy = Math.abs(energy);
  if (energy === 0) {
    this.energy = 0; // will cause the next call of stepAndDrawRecursive() to stop the recursion
  } else if (this.energy === 0) {
    // Start the animation
    this.energy = energy;
    this.stepAndDrawRecursive();
  } else {
    // Already running
    this.energy = Math.max(energy, this.energy); // increase only, no sudden energy drops
  }
};

scout.NetworkGraphRenderer.prototype.start = function() {
  this.adjustEnergy(0.9);
};

scout.NetworkGraphRenderer.prototype.resume = function() {
  this.adjustEnergy(0.3);
};

scout.NetworkGraphRenderer.prototype.stop = function() {
  this.adjustEnergy(0);
};

//moving nodes and links by dx and dy
//moves a node and associated links to given position
scout.NetworkGraphRenderer.prototype.updateNodePosition = function(node, dx, dy) {
  if (dx !== undefined) {
    node.x += dx;
  }
  if (dy !== undefined) {
    node.y += dy;
  }

  // Validate coordinates
  this.ensureInside(node);
  var x = node.x;
  var y = node.y;

  // Update node
  node.$node
    .attr('cx', x)
    .attr('cy', y);

  // Update node label
  if (node.$label) {
    node.$label
      .attr('x', x)
      .attr('y', y - node.radius * 1.5);
  }

  // Update edges
  this.edgesByNodeId[node.id].forEach(function(edge) {
    if (edge.source === node.id) {
      edge.x1 = x;
      edge.y1 = y;
      edge.$edge
        .attr('x1', x)
        .attr('y1', y);
    } else {
      edge.x2 = x;
      edge.y2 = y;
      edge.$edge
        .attr('x2', x)
        .attr('y2', y);
    }
  }.bind(this));

  // Update edge labels
  this.edgesByNodeId[node.id].forEach(function(edge) {
    var x1 = edge.x1,
      y1 = edge.y1,
      x2 = edge.x2,
      y2 = edge.y2;
    if (!edge.$label || isNaN(edge.x1) || isNaN(edge.y1) || isNaN(edge.x2) || isNaN(edge.y2)) {
      return;
    }
    var angle = -90;
    if (x2 !== x1) {
      angle = (Math.atan((y2 - y1) / (x2 - x1)) / Math.PI * 180);
    }
    edge.$label
      .attr('x', (x1 + x2) / 2)
      .attr('y', (y1 + y2) / 2)
      .attr('transform', 'rotate(' + angle + ', ' + ((x1 + x2) / 2) + ', ' + ((y1 + y2) / 2) + ')');
  }.bind(this));
};

scout.NetworkGraphRenderer.prototype.ensureInside = function(node) {
  var x = node.x;
  var y = node.y;

  x = Math.min(x, this.width - node.radius);
  x = Math.max(x, node.radius);

  y = Math.min(y, this.height - node.radius);
  y = Math.max(y, node.radius);

  node.x = x;
  node.y = y;
};
