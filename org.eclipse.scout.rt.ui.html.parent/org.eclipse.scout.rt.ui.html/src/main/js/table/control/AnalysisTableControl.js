scout.AnalysisTableControl = function() {
  scout.AnalysisTableControl.parent.call(this);

  this._addAdapterProperties('dataModel');
};

scout.inherits(scout.AnalysisTableControl, scout.TableControl);

scout.AnalysisTableControl.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('', 'analysis-container'); //FIXME CGU maybe not necessary

  // svg container for venn
  var $vennContainer = this.$container
    .appendSVG('svg', '', 'venn-container')
    .attrSVG('viewBox', '0 0 500 340')
    .attrSVG('preserveAspectRatio', 'xMinYMin')
    .on('click', clickCriteria)
    .on('contextmenu', clickSet);
  var $vennDefs = $vennContainer.appendSVG('defs', '', 'venn-defs');
  appendRect($vennContainer, 'venn-all');

  // commands
  var $commandContainer = this.$container.appendDiv('', 'command-container');

  $commandContainer.appendDiv('', 'command search', 'Daten anzeigen');
  $commandContainer.appendDiv('', 'separator', '');
  $commandContainer.appendDiv('', 'command new', 'Neues Kriterium').click(addCriteria);
  $commandContainer.appendDiv('', 'command delete', 'Kriterium verwerfen').click(removeCriteria);
  $commandContainer.appendDiv('', 'separator', '');
  $commandContainer.appendDiv('', 'command union', 'Ansicht wechseln').click(switchShow);
  $commandContainer.appendDiv('', 'command union', 'Vereinigungsmenge').click(setUnion);
  $commandContainer.appendDiv('', 'command distinct', 'Schnittmenge').click(setIntersect);
  $commandContainer.appendDiv('', 'separator', 'Eine beliebige Menge kann mit Hilfer der rechten Maustaste gesetzt werden');
  $commandContainer.appendDiv('', 'command union', 'Simulator').click(simulateServer);

  // criteria container
  var $criteriaNavigation = this.$container.appendDiv('', 'criteria-navigation').click(closeMap);
  var $criteriaSearch = this.$container.append('<input class="criteria-search"></input>');
  var $criteriaContainer = this.$container.appendDiv('', 'criteria-container');
  var containerWidth = parseFloat($criteriaContainer.css('width')),
    containerHeight = parseFloat($criteriaContainer.css('height'));



  // $criteria = circle per criteria; count calculated by server
  var $criteria = [],
    show = false,
    count = {};
  count.total = undefined;

  // constants for venn diagram
  var MID_X = 250,
    MID_Y = 165,
    DIST_R = 5,
    MIN_R = 20,
    MAX_R = 120;

  // open
  this.addCriteria = addCriteria;
  var that = this;

  if (this.rootEntity) {
    this.addCriteria();
  }

  function addCriteria() {
    // reset count
    count = {};
    count.total = undefined;

    // draw circle
    if ($criteria.length < 3) {
      var $div = appendCircle($vennContainer, 0, 0, 0, 'venn-circle');
      $criteria.push($div);

      updateCriteria();
      selectCriteria($div);
      drawVenn();
      drawCriteria();
    }
  }

  function removeCriteria() {
    // reset count
    count = {};
    count.total = undefined;

    // remove circle
    var $selected = $('.selected', $vennContainer);
    for (var c = 0; c < $criteria.length; c++) {
      if ($criteria[c][0] == $selected[0]) {
        $criteria[c].animateSVG('r', 0, 300, $.removeThis);
        $criteria.splice(c, 1);
        break;
      }
    }
    // select next criteria
    if ($criteria.length) {
      updateCriteria();
      selectCriteria($criteria[$criteria.length - 1]);
      drawVenn();
    }
  }

  function updateCriteria() {
    for (var c = 0; c < $criteria.length; c++) {
      $criteria[c][0].id = c;
    }
  }

  function selectCriteria($criteria) {
    $('.venn-circle', $vennContainer).removeClassSVG('selected');
    $criteria.addClassSVG('selected');
  }

  function drawCriteria() {
    var model = that.rootEntity;
    initIteration($criteriaContainer, that.rootEntity);
  }

  function initIteration ($container, model) {

    // find sizes of elements of tree map and sort elements
    var map = [{size: model.attributes.length * 12, text: '', attributes: model.attributes, entity: null }],
      mapTotal = map[0].size,
      subEntity, s;

    for (var j = 0; j < model.entities.length; j++) {
      subEntity = model.entities[j];
      s = Math.min(60, Math.max(20, subEntity.attributes.length + subEntity.entities.length));
      map.push({size: s, text: subEntity.text, entity: subEntity});
      mapTotal += map[map.length - 1].size;
    }

    // TODO cru: correct for umlaut
    map.sort(function(a, b) { return ((a.text < b.text) ? -1 : ((a.text == b.text) ? 0 : 1)); });

    oneIteration($container, map, 0, 0, 1, 1);
  }
  //

  function oneIteration ($container, list, top, left, height, width) {
    if (list.length === 0) {
      return;
    } else if (list.length === 1) {
      var $div = $container.appendDiv('', 'criteria-entity')
        .css('top', top * 100 + '%')
        .css('left', left * 100 + '%')
        .css('height', height * 100 + '%')
        .css('width', width * 100 + '%')
        .data('entity', list[0].entity)
        .click(openMap);

      var $span = $('<span>' + list[0].text + '</span>').appendTo($div);

      /*if ($span.width() > $div.width() && $div.height() > $div.width())  {
        $span.css('display', 'block');
        $span.css('transform', 'rotate(90deg)');
        $span.css('transform-origin', 'left top ');
        $span.css('width', $div.height());
        $span.css('height', $div.width());
        $span.css('left', $div.width() / 2 + 7);
        $span.css('top', 5);
      }*/
      $span.css('word-wrap', 'break-word');

      if (list[0].attributes) {
        $div.css('border', '0');
        // draw attributes
        var attributes = list[0].attributes,
          x = Math.ceil(Math.sqrt(attributes.length)),
          y = Math.ceil(attributes.length / x);

        attributes.sort(function(a, b) { return ((a.text < b.text) ? -1 : ((a.text == b.text) ? 0 : 1)); });

        for (var b = 0; b < y; b++) {
          for (var a = 0; a < x; a++) {
            if ((a + b * x) < attributes.length - 1) {
              var $a = $div.appendDiv('', 'criteria-attribute', attributes[a + b * x].text)
                .css('top', (top + b * (1 / y)) * 100 + '%')
                .css('left', (left + a * (1/ x)) * 100 + '%')
                .css('height', (1 / y) * 100 + '%')
                .css('width', (1 / x) * 100 + '%');
            }
          }
        }

        $a.css('width', (x * y - attributes.length + 2) * (1 / x) * 100 + '%');
      }

    } else {
      var horizontal = (height * containerHeight <= width * containerWidth),
        //p = Math.floor((list.length - 1 )/ 2),
        p = maxList(list),
        l1 = list.slice(0, p),
        lp = list.slice(p, p + 1),
        sum1 = sumList(l1),
        sump = sumList(lp),
        sum2, sum3,
        cand, best = {ratio: 0};

      // make candidates
      for (var i = list.length + 1; i > p; i--) {
        cand = {};

        cand.l2 = list.slice(p + 1, i);
        cand.l3 = list.slice(i);

        // ignore some l3s?
        if (cand.l3.length != -1) {
          // build sum
          sum2 = sumList(cand.l2);
          sum3 = sumList(cand.l3);

          // calc r1 and rp
          if (horizontal) {
            cand.h1 = height;
            cand.w1 = sum1 / (sum1 + sump + sum2 + sum3) * width;
            cand.hp =  sump / (sump + sum2) * height;
            cand.wp = (sump + sum2) / (sump + sum2 + sum3) * (width - cand.w1);
          } else {
            cand.h1 = sum1 / (sum1 + sump + sum2 + sum3) * height;
            cand.w1 = width;
            cand.hp = (sump + sum2) / (sump + sum2 + sum3) * (height - cand.h1);
            cand.wp =  sump / (sump + sum2) * width;
          }

          cand.ratio =  (cand.hp * containerHeight) / (cand.wp * containerWidth) / 0.5 ;
          if (cand.ratio > 1) {
            cand.ratio = 1 / cand.ratio;
          }

          if (cand.ratio > best.ratio) {
            best = cand;
          }

        }
      }

      // find best ration

      // iterate
      if (horizontal) {
        oneIteration($container, l1, top, left, best.h1, best.w1);
        oneIteration($container, lp, top, left + best.w1, best.hp, best.wp);
        oneIteration($container, best.l2, top + best.hp, left + best.w1, height - best.hp, best.wp);
        oneIteration($container, best.l3, top, left + best.w1 + best.wp, height, width - best.w1 - best.wp);
      } else {
        oneIteration($container, l1, top, left, best.h1, best.w1);
        oneIteration($container, lp, top + best.h1, left, best.hp, best.wp);
        oneIteration($container, best.l2, top + best.h1, left + best.wp, best.hp, width - best.wp);
        oneIteration($container, best.l3, top + best.h1 + best.hp, left, height - best.h1 - best.hp, width);
      }
    }
  }

  function sumList (list) {
    var total = 0;
    for (var i = 0; i < list.length; i++) {
      total += list[i].size;
    }
    return total;
  }

  function maxList (list) {
    var cand,
      max = 0;

    for (var i = 0; i < list.length; i++) {
      if (list[i].size > max) {
        max = list[i];
        cand = i;
      }
    }

    return cand;
  }

  function openMap (list) {
    var $clicked = $(this);

    $clicked
    .data('old-top', $clicked.css('top'))
      .data('old-left', $clicked.css('left'))
      .data('old-height', $clicked.css('height'))
      .data('old-width', $clicked.css('width'));



    $clicked
      .css ('z-index', '1')
      .css('color', 'black')
      .animateAVCSD('top', '0%')
      .animateAVCSD('left', '0%')
      .animateAVCSD('height', '100%')
      .animateAVCSD('width', '100%');

    $criteriaNavigation.text($clicked.text());
    initIteration($clicked, $clicked.data('entity'));
    $criteriaNavigation.data('open-map', $clicked);

    $clicked.children('div').css('opacity', 0).animateAVCSD('opacity', 1);
  }

  function closeMap (list) {
    var $open = $criteriaNavigation.data('open-map');

    $open
      .animateAVCSD('top', $open.data('old-top'))
      .animateAVCSD('left', $open.data('old-left'))
      .animateAVCSD('height', $open.data('old-height'))
      .animateAVCSD('width', $open.data('old-width'), function() {$open.css ('z-index', ''); });

    $open.children('div').animateAVCSD('opacity', 0, $.removeThis);
  }


  function switchShow() {
    show = !show;
    drawVenn();
  }

  function drawVenn() {
    // remove all text
    $('text', $vennContainer)
      .animateSVG('opacity', 0, 100, $.removeThis);

    // show count all data
    if (count.total) {
      $vennContainer.appendSVG('text', '', 'venn-all-text', count.total + ' Datensätze')
        .attr('x', 490).attr('y', 28);
    }

    // remove intersect elements
    $('.venn-set', $vennContainer).remove();

    // init variables
    var x0, x1, x2, y0, y1, y2, r0, r1, r2;
    var ret, d, d01, d02, d12;
    var alpha, beta;

    // move circle, draw text and set
    if ($criteria.length === 1) {
      // that is easy...
      if (count.total && !show) {
        r0 = calcR(count['0'], MAX_R);
      } else {
        r0 = MAX_R * 0.9;
      }

      x0 = y0 = 0;

      moveCircle($criteria[0], r0, x0, y0);
      drawText(count['0'], x0, y0);
      drawSetOuter(r0, x0, y0);
      drawSetMain('0', r0, x0, y0);
    } else if ($criteria.length === 2) {
      if (count.total && !show) {
        // calculate size of circles
        r0 = calcR(count['0'], MAX_R);
        r1 = calcR(count['1'], MAX_R);

        // find distance
        ret = findD(count['0'], count['1'], count['01'], r0, r1);
        r0 = ret.ra;
        r1 = ret.rb;
        d01 = ret.d;

        // find balance
        x1 = d01 / ((r1 * r1) / (r0 * r0) + 1);
        x0 = x1 - d01;
      } else {
        r0 = r1 =  MAX_R * 0.9;
        x0 = -MAX_R * 0.6;
        x1 = MAX_R * 0.6;
      }

      y0 = y1 = 0;

      // move and resize circle
      moveCircle($criteria[0], r0, x0, y0);
      moveCircle($criteria[1], r1, x1, y1);

      // draw text
      if (count.total && !show) {
        if (count['0'] == count['01']) {
          drawText(count['0'], x0, y0);
          drawText(count['1'], x1 - (d01 - r1 - r0) / 2, y1);
        } else if (count['1'] == count['01']) {
          drawText(count['0'], x0 + (d01 - r0 - r1) / 2, y0);
          drawText(count['1'], x1, y1);
        } else if (count['01'] === 0) {
          drawText(count['0'], x0, y0);
          drawText(count['1'], x1, y1);
        } else {
          drawText(count['0'], x0 + (d01 - r0 - r1) / 2, y0);
          drawText(count['1'], x1 - (d01 - r0 - r1) / 2, y1);
          drawText(count['01'], x0 + r0 + (d01 - r0 - r1) / 2, y0);
        }
      } else {
        drawText(count['0'], -MAX_R * 0.8, 0);
        drawText(count['1'], +MAX_R * 0.8, 0);
        drawText(count['01'], 0, 0);
      }

      drawSetOuter(r0, x0, y0, r1, x1, y1);
      drawSetMain('0', r0, x0, y0, r1, x1, y1);
      drawSetMain('1', r1, x1, y1, r0, x0, y0);
      drawSetIntersect('01', r0, x0, y0, r1, x1, y1);
    } else if ($criteria.length === 3) {
      if (count.total && !show) {

        // calculate size of circles
        r0 = calcR(count['0'], MAX_R * 0.9);
        r1 = calcR(count['1'], MAX_R * 0.9);
        r2 = calcR(count['2'], MAX_R * 0.9);

        // find distance
        ret = findD(count['0'], count['1'], count['01'], r0, r1);
        r0 = ret.ra;
        r1 = ret.rb;
        d01 = ret.d;

        ret = findD(count['0'], count['2'], count['02'], r0, r2);
        r0 = ret.ra;
        r2 = ret.rb;
        d02 = ret.d;

        ret = findD(count['1'], count['2'], count['12'], r1, r2);
        r1 = ret.ra;
        r2 = ret.rb;
        d12 = ret.d;

        // find balance, start with 1
        x1 = d01 / ((r1 * r1) / (r0 * r0) + 1);
        y1 = 0;

        // 0 is simple ;)
        x0 = x1 - d01;
        y0 = 0;

        //  fit 2 with 1
        if (d12 > d01 + d02 || count['12'] === 0) {
          x2 = x0 - d02;
          y2 = y0;
        } else if (d12 < d01 - d02 || d12 < d02 - d01) {
          x2 = x0 + d02;
          y2 = y0;
        } else {
          alpha = Math.acos((d02 * d02 + d01 * d01 - d12 * d12) / (2 * d02 * d01));
          beta = Math.acos((d12 * d12 + d01 * d01 - d02 * d02) / (2 * d12 * d01));

          x2 = x0 + d02 * Math.cos(alpha);
          y2 = y0 + d02 * Math.sin(alpha);
        }

        // find center
        var cx = (r0 * x0 + r1 * x1 + r2 * x2) / (r0 + r1 + r2);
        var cy = (r0 * y0 + r1 * y1 + r2 * y2) / (r0 + r1 + r2);

        x0 = x0 - cx;
        x1 = x1 - cx;
        x2 = x2 - cx;

        y0 = y0 - cy;
        y1 = y1 - cy;
        y2 = y2 - cy;
      } else {
        r0 = MAX_R * 0.7;
        r1 = MAX_R * 0.7;
        r2 = MAX_R * 0.7;

        x0 = -MAX_R * 0.47;
        x1 = MAX_R * 0.47;
        x2 = 0;

        y0 = -MAX_R * 0.4;
        y1 = -MAX_R * 0.4;
        y2 = MAX_R * 0.4;
      }

      // move and resize circles
      moveCircle($criteria[0], r0, x0, y0);
      moveCircle($criteria[1], r1, x1, y1);
      moveCircle($criteria[2], r2, x2, y2);

      if (!count.total || show) {
        drawText(count['0'], -MAX_R * 0.7, -MAX_R * 0.5);
        drawText(count['1'], +MAX_R * 0.7, -MAX_R * 0.5);
        drawText(count['2'], 0, MAX_R * 0.6);

        drawText(count['01'], 0, -MAX_R * 0.5);
        drawText(count['02'], -MAX_R * 0.35, MAX_R * 0.1);
        drawText(count['12'], +MAX_R * 0.35, MAX_R * 0.1);

        drawText(count['012'], 0, -MAX_R * 0.1);
      }

      drawSetOuter(r0, x0, y0, r1, x1, y1, r2, x2, y2);
      drawSetMain('0', r0, x0, y0, r1, x1, y1, r2, x2, y2);
      drawSetMain('1', r1, x1, y1, r0, x0, y0, r2, x2, y2);
      drawSetMain('2', r2, x2, y2, r1, x1, y1, r0, x0, y0);
      drawSetIntersect('01', r0, x0, y0, r1, x1, y1, r2, x2, y2);
      drawSetIntersect('02', r0, x0, y0, r2, x2, y2, r1, x1, y1);
      drawSetIntersect('12', r2, x2, y2, r1, x1, y1, r0, x0, y0);
      drawSetTriple('012', r2, x2, y2, r1, x1, y1, r0, x0, y0);
    }
  }

  function calcR(size, limit) {
    return Math.max(limit * Math.sqrt(size / count.total), MIN_R);
  }

  function findD(ca, cb, cab, ra, rb) {
    var ret = {};

    if (ca == cab) {
      ret.ra = ra - DIST_R;
      ret.rb = rb + DIST_R;
      ret.d = rb - ra;
    } else if (cb == cab) {
      ret.ra = ra + DIST_R;
      ret.rb = rb - DIST_R;
      ret.d = ra - rb;
    } else if (cab === 0) {
      ret.ra = ra;
      ret.rb = rb;
      ret.d = ra + rb + DIST_R * 2;
    } else {
      ret.ra = ra;
      ret.rb = rb;
      ret.d = (1 - 2 * cab / (ca + cb)) * (ra + rb - DIST_R * 2);
    }

    return ret;
  }

  function moveCircle($div, r, dx, dy) {
    $div
      .animateSVG('cx', MID_X + dx)
      .animateSVG('cy', MID_Y + dy)
      .animateSVG('r', r);
  }

  function drawText(text, dx, dy) {
    if (count.total) {
      $vennContainer.appendSVG('text', '', 'venn-circle-text', text || '0')
        .attr('x', MID_X + dx)
        .attr('y', MID_Y + dy)
        .attr('opacity', 0)
        .animateSVG('opacity', 1);
    }
  }

  function drawSetOuter(ra, dxa, dya, rb, dxb, dyb, rc, dxc, dyc) {
    var $mask = $vennDefs.empty().appendSVG('mask', 'maskAll', '');
    appendRect($mask, '', 'white');
    if (ra) appendCircle($mask, dxa, dya, ra);
    if (rb) appendCircle($mask, dxb, dyb, rb);
    if (rc) appendCircle($mask, dxc, dyc, rc);

    appendRect($vennContainer, 'venn-set')
      .attr('mask', 'url(#maskAll)')
      .attr('id', 'all');
  }

  function appendRect($def, claz, fill) {
    var $rect = $def.appendSVG('rect', '', '')
      .attr('x', 5).attr('y', 15)
      .attr('width', 490).attr('height', 300)
      .attr('rx', 10).attr('ry', 10);

    if (claz) $rect.attr('class', claz);
    if (fill) $rect.attr('fill', fill);

    return $rect;
  }

  function appendCircle($def, dx, dy, r, claz, fill) {
    var $circle = $def.appendSVG('circle', '', '')
      .attr('cx', MID_X + dx)
      .attr('cy', MID_Y + dy)
      .attr('r', r);

    if (claz) $circle.attr('class', claz);
    if (fill) $circle.attr('fill', fill);

    return $circle;
  }

  function drawSetMain(id, ra, dxa, dya, rb, dxb, dyb, rc, dxc, dyc) {
    var $mask = $vennDefs.appendSVG('mask', 'set' + id, '');
    appendCircle($mask, dxa, dya, ra, '', 'white');
    if (rb) appendCircle($mask, dxb, dyb, rb);
    if (rc) appendCircle($mask, dxc, dyc, rc);

    appendCircle($vennContainer, dxa, dya, ra, 'venn-set')
      .attr('mask', 'url(#set' + id + ')')
      .attr('id', id);
  }

  function drawSetIntersect(id, ra, dxa, dya, rb, dxb, dyb, rc, dxc, dyc) {
    var $clip = $vennDefs.appendSVG('clipPath', 'set-clip' + id, '');
    appendCircle($clip, dxb, dyb, rb);

    if (rc) {
      var $mask = $vennDefs.appendSVG('mask', 'set-mask' + id, '');
      appendCircle($mask, dxa, dya, ra, '', 'white');
      appendCircle($mask, dxc, dyc, rc);
    }

    appendCircle($vennContainer, dxa, dya, ra, 'venn-set')
      .attr('clip-path', 'url(#set-clip' + id + ')')
      .attr('mask', 'url(#set-mask' + id + ')')
      .attr('id', id);
  }

  function drawSetTriple(id, ra, dxa, dya, rb, dxb, dyb, rc, dxc, dyc) {
    var $clip = $vennDefs.appendSVG('clipPath', 'set-clip' + id, '');
    appendCircle($clip, dxb, dyb, rb);

    var $mask = $vennDefs.appendSVG('mask', 'set-mask' + id, '');
    appendCircle($mask, dxa, dya, ra);
    appendCircle($mask, dxc, dyc, rc, '', 'white');

    appendCircle($vennContainer, dxa, dya, ra, 'venn-set')
      .attr('clip-path', 'url(#set-clip' + id + ')')
      .attr('mask', 'url(#set-mask' + id + ')')
      .attr('id', id);
  }

  function clickCriteria(event) {
    var candidate = findSet(event.clientX, event.clientY),
      select = $('.selected', $vennContainer)[0].id,
      next;

    if (candidate.length === 0) return;

    candidate.push(candidate[0]);
    next = candidate[candidate.indexOf(select) + 1];

    selectCriteria($('#' + next, $vennContainer));
  }

  function clickSet(event) {
    var candidate = findSet(event.clientX, event.clientY),
      $clicked;

    if (candidate.length === 0) {
      $clicked = $('#all.venn-set', $vennContainer);
    } else {
      $clicked = $('#' + candidate.join('') + '.venn-set', $vennContainer);
    }

    if ($clicked.hasClassSVG('selected')) {
      $clicked.removeClassSVG('selected');
    } else {
      $clicked.addClassSVG('selected');
    }

    event.preventDefault();
  }

  function findSet(x, y) {
    x = x - $vennContainer.offset().left;
    y = y - $vennContainer.offset().top;

    var ret = [];

    for (var c = 0; c < $criteria.length; c++) {
      var $c = $criteria[c],
        cx = parseFloat($c.attr('cx')),
        cy = parseFloat($c.attr('cy')),
        r = parseFloat($c.attr('r'));

      if ((Math.pow(cx - x, 2) + Math.pow(cy - y, 2)) <= Math.pow(r, 2)) {
        ret.push($c[0].id);
      }
    }

    return ret.sort();
  }

  function setUnion() {
    $('.venn-set', $vennContainer).addClassSVG('selected');
    $('#all', $vennContainer).removeClassSVG('selected');
  }

  function setIntersect() {
    var select = ['', '0', '01', '012'][$criteria.length];

    $('.venn-set', $vennContainer).removeClassSVG('selected');
    $('#' + select + '.venn-set', $vennContainer).addClassSVG('selected');
  }

  function simulateServer() {
    count.total = Math.ceil(Math.random() * 100000);

    var r0 = Math.ceil(count.total * Math.random()),
      r1 = Math.ceil(count.total * Math.random()),
      r2 = Math.ceil(count.total * Math.random());

    count['0'] = r0;
    count['1'] = r1;
    count['2'] = r2;

    if ($criteria.length === 2) {
      if (Math.random() < 0.2 && (r0 + r1 < count.total)) {
        count['01'] = 0;
      } else if (Math.random() < 0.4) {
        count['01'] = Math.ceil(Math.min(r0, r1));
      } else {
        count['01'] = Math.max(Math.ceil(Math.min(r0, r1) * Math.random()), r0 + r1 - count.total);
      }
    } else if ($criteria.length === 3) {
      if (Math.random() < 0.2 && (r0 + r1 + r2 < count.total)) {
        count['01'] = 0;
      } else if (Math.random() < 0.4) {
        count['01'] = Math.ceil(Math.min(r0, r1));
      } else {
        count['01'] = Math.max(Math.ceil(Math.min(r0, r1) * Math.random()), r0 + r1 - count.total);
      }

      if (Math.random() < 0.2 && (r0 + r1 + r2 < count.total)) {
        count['02'] = 0;
      } else if (Math.random() < 0.4) {
        count['02'] = Math.ceil(Math.min(r0, r2));
      } else {
        count['02'] = Math.max(Math.ceil(Math.min(r0, r2) * Math.random()), r0 + r2 - count.total);
      }

      if (Math.random() < 0.2 && (r0 + r1 + r2 < count.total)) {
        count['12'] = 0;
      } else if (Math.random() < 0.4) {
        count['12'] = Math.ceil(Math.min(r1, r2));
      } else {
        count['12'] = Math.max(Math.ceil(Math.min(r1, r2) * Math.random()), r1 + r2 - count.total);
      }

      if (Math.random() < 0.2 && (r0 + r1 + r2 < count.total)) {
        count['012'] = 0;
      } else if (Math.random() < 0.4) {
        count['012'] = Math.ceil(Math.min(count['01'], count['02'], count['12']));
      } else {
        count['012'] = Math.ceil(Math.min(count['01'], count['02'], count['12']) * Math.random());
      }
    }

    drawVenn();
  }

};

scout.AnalysisTableControl.prototype._setDataModel = function(dataModel) {
  this.renderContent();
};

scout.AnalysisTableControl.prototype._setRootEntityRef = function(rootEntityRef) {
  this.rootEntity = this.dataModel[rootEntityRef];
  this.renderContent();
};

scout.AnalysisTableControl.prototype.isContentAvailable = function() {
  return this.dataModel && this.rootEntity;
};
