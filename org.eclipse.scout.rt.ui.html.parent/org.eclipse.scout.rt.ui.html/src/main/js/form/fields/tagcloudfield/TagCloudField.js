scout.TagCloudField = function() {
  scout.TagCloudField.parent.call(this);
};
scout.inherits(scout.TagCloudField, scout.FormField);

scout.TagCloudField.prototype._renderProperties = function() {
  scout.TagCloudField.parent.prototype._renderProperties.call(this);
};

// test data
scout.TagCloudField.prototype.demo = [[368, 'Geschäftsvorfälle'], [117726, 'Kommunikationen'],
                                      [1404, 'Aufgaben'], [94639, 'Anliegen'], [5051, 'Aufträge'],
                                      [5312, 'Firmen'], [16330, 'Personen'], [176, 'Kampagnen'],
                                      [833, 'Aktionen'], [54008, 'Dokumente']];

scout.TagCloudField.prototype._render = function($parent) {
  this.addContainer($parent, 'tag-cloud-field');
  this.$cloud = this.$container.appendSVG('svg', '', 'tag-cloud');

  this.addLabel();
  this.addStatus();


  // hack? wait until svg is complete renderd
  setTimeout(this._init.bind(this), 0);
};

scout.TagCloudField.prototype._init = function() {
  // init vars
  var data = this.demo,
    wCloud = this.$container.width(),
    hCloud = this.$container.height(),
    i, minCount, maxCount, $t, size, color, driftX, driftY;

  // find min and max
  for (i = 0; i < data.length; i++) {
    if (data[i][0] < minCount || minCount === undefined) {
      minCount = data[i][0];
    }
    if (data[i][0] > maxCount || maxCount === undefined) {
      maxCount = data[i][0];
    }
  }

  // initial draw with size and color and position
  for (i = 0; i < data.length; i++) {
    $t = this.$cloud.appendSVG('text', '', '', this.session.locale.decimalFormat.format(data[i][0]) + ' ' + data[i][1]);
    color = i % 5;
    size = 25 + (data[i][0] - minCount) / (maxCount - minCount) * 25;
    driftX = (Math.random() * 2) - 1;
    driftY = (Math.random() * 2) - 1;

    $t.addClassSVG('tag-text-' + color)
      .attr('font-size', size)
      .attr('x', Math.random() * wCloud).attr('y', Math.random() * hCloud)
      .attr('drift-x', driftX).attr('drift-y', driftY)
      .attr('count', data[i][0]).attr('entity', data[i][1]);
  }

  // start show
  setTimeout(this._change.bind(this), 1000);
  setTimeout(this._iterate.bind(this), 10);

};

scout.TagCloudField.prototype._change = function() {
  if (this.$container === null) {
    return false;
  }

  var that = this,
    $t, count, entity;

  // simulate new data

  this.$cloud.children().each( function () {
    $t = $(this);

    if (Math.random() > 0.2) {
      count = parseFloat($t.attr('count')) + Math.ceil(Math.random() * 10);
      $t.attr('count', count);
      entity = $t.attr('entity');

      $t[0].textContent = that.session.locale.decimalFormat.format(count) + ' ' + entity;
    }
  });

  // again!
  setTimeout(this._change.bind(this), 1000);
};

scout.TagCloudField.prototype._iterate = function() {
  if (this.$container === null) {
    return false;
  }

  var $t, x, y, w, h, driftX, driftY,
    wCloud = this.$container.width(),
    hCloud = this.$container.height();

  this.$cloud.children().each( function () {
    // init
    $t = $(this);
    x = parseFloat($t.attr('x'));
    y = parseFloat($t.attr('y'));
    w = $(this).width();
    h = $(this).height();
    driftX = parseFloat($t.attr('drift-x'));
    driftY = parseFloat($t.attr('drift-y'));

    // bounce at walls
    if (x < 0) {
      driftX = -driftX;
      x = 0;
    }

    if (x > wCloud - w) {
      driftX = -driftX;
      x = wCloud - w;
    }

    if (y < h) {
      driftY = -driftY;
      y = h;
    }

    if (y > hCloud - 10) {
      driftY = -driftY;
      y = hCloud - 10;
    }

    // drift
    $t.attr('x', x + driftX).attr('y', y + driftY);
    $t.attr('drift-x', driftX).attr('drift-y', driftY);

  });

  // again!
  setTimeout(this._iterate.bind(this), 10);
};

