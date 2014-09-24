// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.GraphTableControl = function() {
  scout.GraphTableControl.parent.call(this);
};
scout.inherits(scout.GraphTableControl, scout.TableControl);

scout.GraphTableControl.prototype._renderContent = function($parent) {
  this.$container = $parent.appendSVG('svg', 'GraphContainer');

  // some basics
  var wBox = 120,
    hBox = 60,
    kelvin = 1000,
    wContainer = this.$container.width(),
    hContainer = this.$container.height(),
    graph = this.graph;


  // create all links with label
  for (var l = 0; l < graph.links.length; l++) {
    var link = graph.links[l];

    link.$div = this.$container.appendSVG('line', null, 'graph-link');
    link.$divText = this.$container.appendSVG('text', null, 'graph-link-text', link.label)
      .attr('dy', -5);
  }

  // create nodes with text and place them randomly
  for (var n = 0; n < graph.nodes.length; n++) {
    var node = graph.nodes[n];

    node.$div = this.$container.appendSVG('rect', null, 'graph-node ' + node.type)
      .attr('width', wBox).attr('height', hBox)
      .attr('x', 0).attr('y', 0)
      .on('mousedown', moveNode);

    node.$divText = this.$container.appendSVG('text', null, 'graph-node-text', node.name)
      .on('mousedown', moveNode);

    setNode(node, Math.random() * (wContainer - wBox), Math.random() * (hContainer - hBox));
  }

  // start optimization
  disolveLinks();
  doPhysics();

  // moving nodes and links by dx and dy
  function setNode(node, dx, dy) {
    var x = getPos(node, 'x'),
      y = getPos(node, 'y');

    node.$div
      .attr('x', x + dx)
      .attr('y', y + dy);

    node.$divText
      .attr('x', x + dx + wBox / 2)
      .attr('y', y + dy + hBox / 2);

    for (var l = 0; l < graph.links.length; l++) {
      var link = graph.links[l];

      if (link.source == node.id) {
        link.$div
          .attr('x1', x + dx + wBox / 2)
          .attr('y1', y + dy + hBox / 2);
        setLabel(link);
      } else if (link.target == node.id) {
        link.$div
          .attr('x2', x + dx + wBox / 2)
          .attr('y2', y + dy + hBox / 2);
        setLabel(link);
      }
    }
  }

  // set label of a link
  function setLabel(link) {
    var x1 = getPos(link, 'x1'),
      y1 = getPos(link, 'y1'),
      x2 = getPos(link, 'x2'),
      y2 = getPos(link, 'y2');

    link.$divText
      .attr('x', (x1 + x2) / 2)
      .attr('y', (y1 + y2) / 2)
      .attr('transform', 'rotate( ' + (Math.atan((y2 - y1) / (x2 - x1)) / Math.PI * 180) +
        ', ' + ((x1 + x2) / 2) + ', ' + ((y1 + y2) / 2) + ')');
  }

  // disolve crossing links
  function disolveLinks() {
    for (var l1 = 0; l1 < graph.links.length; l1++) {
      var link1 = graph.links[l1],
        E = {}, F = {};

      E.x = getPos(link1, 'x1'),
      E.y = getPos(link1, 'y1'),
      F.x = getPos(link1, 'x2'),
      F.y = getPos(link1, 'y2');

      for (var l2 = 0; l2 < graph.links.length; l2++) {
        if (l1 == l2) continue;

        var link2 = graph.links[l2],
          P = {}, Q = {};

        P.x = getPos(link2, 'x1'),
        P.y = getPos(link2, 'y1'),
        Q.x = getPos(link2, 'x2'),
        Q.y = getPos(link2, 'y2');

        // ckeck if crossing exists, if yes: change position
        if ((_test(E, P, Q) != _test(F, P, Q)) && (_test(E, F, P) != _test(E, F, Q))) {
          var n1 = graph.nodes[link1.target],
            n2 = graph.nodes[link2.target],
            dx = getPos(n1, 'x') - getPos(n2, 'x'),
            dy = getPos(n1, 'y') - getPos(n2, 'y');

          setNode(n1, -dx, -dy);
          setNode(n2, dx, dy);
        }
      }
    }

    function _test(p1, p2, p3) {
      return (p3.y - p1.y) * (p2.x - p1.x) > (p2.y - p1.y) * (p3.x - p1.x);
    }
  }

  // force the nodes to their place
  function doPhysics() {
    var totalDiff = 0;

    for (var n = 0; n < graph.nodes.length; n++) {
      var node = graph.nodes[n],
        x = getPos(node, 'x'),
        y = getPos(node, 'y'),
        dx,dy;
      dx = 0, dy = 0;

      // move center to the middle
      if (node.type == 'center') {
        dx += ((wContainer - wBox) / 2 - x) / 40;
        dy += ((hContainer - hBox) / 2 - y) / 40;
      }

      // move from outside
      dx -= (Math.min(x, 5) - 5) / 2;
      dy -= (Math.min(y, 5) - 5) / 2;
      dx += (Math.min(wContainer - wBox - x, 10) - 10) / 4;
      dy += (Math.min(hContainer - hBox - y, 10) - 10) / 4;

      // gravity
      dx += ((wContainer - wBox) / 2 - x) / 500;
      dy += ((hContainer - hBox) / 2 - y) / 500;

      // repulsion force
      for (var o = 0; o < graph.nodes.length; o++) {
        var otherNode = graph.nodes[o];
        if (o != n) {
          var oX = getPos(otherNode, 'x'),
            oY = getPos(otherNode, 'y'),
            repForce = 100 / (Math.pow(x - oX, 2) + Math.pow(y - oY, 2));

          dx += (x - oX) * repForce;
          dy += (y - oY) * repForce;
        }
      }

      // spring force
      for (var l = 0; l < graph.links.length; l++) {
        var link = graph.links[l],
          oppositeNode = null;

        if (link.source === node.id) {
          oppositeNode = graph.nodes[link.target];
        } else if (link.target === node.id) {
          oppositeNode = graph.nodes[link.source];
        }

        if (oppositeNode) {
          var otherX = getPos(oppositeNode, 'x');
          var otherY = getPos(oppositeNode, 'y');

          var dist = Math.sqrt(Math.pow(x - otherX, 2) + Math.pow(y - otherY, 2)),
            springForce = Math.log(dist / 260) / 10;

          dx -= (x - otherX) * springForce;
          dy -= (y - otherY) * springForce;
        }
      }

      // adjust position
      setNode(node, dx, dy);
      totalDiff += Math.abs(dx) + Math.abs(dy);
    }

    // cool down, heat up
    if (kelvin-- > 0) setTimeout(doPhysics, 0);
  }

  // move node by mouse
  function moveNode(event) {
    var startX = event.pageX,
      startY = event.pageY,
      clickedNode;

    for (var n = 0; n < graph.nodes.length; n++) {
      var node = graph.nodes[n];
      if ($(this).is(node.$div) || $(this).is(node.$divText)) clickedNode = graph.nodes[n];
    }

    $('body').on('mousemove', '', nodeMove)
      .one('mouseup', '', nodeEnd);
    return false;

    function nodeMove(event) {
      setNode(clickedNode, event.pageX - startX, event.pageY - startY);
      startX = event.pageX;
      startY = event.pageY;
      kelvin = 0;
    }

    function nodeEnd() {
      $('body').off('mousemove');
      kelvin = 200;
      doPhysics();
    }
  }

  function getPos(e, d) {
    return parseFloat(e.$div.attr(d));
  }

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
