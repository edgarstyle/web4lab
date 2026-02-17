/**
 * JavaScript для работы с canvas и отрисовки области
 */

var canvas;
var ctx;
var currentR = 1.0;
var pointsByRadius = {};

function initCanvas() {
    canvas = document.getElementById('areaCanvas');
    if (!canvas) {
        return false;
    }
    ctx = canvas.getContext('2d');
    
    var rSelect = document.getElementById('pointForm:rValue');
    if (rSelect) {
        currentR = parseFloat(rSelect.value) || 1.0;
    }
    return true;
}

function drawArea() {
    if (!canvas || !ctx) {
        if (!initCanvas()) {
            return;
        }
    }
    
    var rSelect = document.getElementById('pointForm:rValue');
    if (rSelect) {
        currentR = parseFloat(rSelect.value) || 1.0;
    }
    
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    var centerX = 250;
    var centerY = 250;
    var scale = 100 / currentR;
    
    ctx.strokeStyle = '#000';
    ctx.lineWidth = 2;
    
    ctx.beginPath();
    ctx.moveTo(0, centerY);
    ctx.lineTo(canvas.width, centerY);
    ctx.stroke();
    
    ctx.beginPath();
    ctx.moveTo(centerX, 0);
    ctx.lineTo(centerX, canvas.height);
    ctx.stroke();
    
    ctx.fillStyle = '#000';
    ctx.font = '12px Arial';
    ctx.fillText('x', canvas.width - 10, centerY - 5);
    ctx.fillText('y', centerX + 5, 15);
    
    drawTicks(centerX, centerY, scale);
    
    ctx.fillStyle = 'rgba(0, 100, 255, 0.5)';
    ctx.strokeStyle = 'rgba(0, 100, 255, 0.8)';
    ctx.lineWidth = 2;
    
    ctx.beginPath();
    ctx.rect(centerX - (currentR / 2) * scale, centerY - currentR * scale, 
             (currentR / 2) * scale, currentR * scale);
    ctx.fill();
    ctx.stroke();
    
    ctx.beginPath();
    ctx.moveTo(centerX, centerY - currentR * scale);
    ctx.lineTo(centerX + (currentR / 2) * scale, centerY);
    ctx.lineTo(centerX, centerY);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();
    
    ctx.beginPath();
    ctx.arc(centerX, centerY, currentR * scale, Math.PI / 2, Math.PI, false);
    ctx.lineTo(centerX, centerY);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();
    
    drawPoints(centerX, centerY, scale);
}

function drawTicks(centerX, centerY, scale) {
    ctx.strokeStyle = '#000';
    ctx.fillStyle = '#000';
    ctx.font = '10px Arial';
    
    var rSelect = document.getElementById('pointForm:rValue');
    var r = currentR;
    if (rSelect) {
        r = parseFloat(rSelect.value) || 1.0;
    }
    
    var xTicks = [-r, -r/2, 0, r/2, r];
    xTicks.forEach(function(tick) {
        var x = centerX + tick * scale;
        ctx.beginPath();
        ctx.moveTo(x, centerY - 5);
        ctx.lineTo(x, centerY + 5);
        ctx.stroke();
        var label = tick === 0 ? '0' : (tick === r ? 'R' : (tick === -r ? '-R' : (tick === r/2 ? 'R/2' : '-R/2')));
        ctx.fillText(label, x - 8, centerY + 20);
    });
    
    var yTicks = [-r, -r/2, 0, r/2, r];
    yTicks.forEach(function(tick) {
        var y = centerY - tick * scale;
        ctx.beginPath();
        ctx.moveTo(centerX - 5, y);
        ctx.lineTo(centerX + 5, y);
        ctx.stroke();
        var label = tick === 0 ? '0' : (tick === r ? 'R' : (tick === -r ? '-R' : (tick === r/2 ? 'R/2' : '-R/2')));
        ctx.fillText(label, centerX + 10, y + 5);
    });
}

function drawPoints(centerX, centerY, scale) {
    try {
        var rKey = currentR.toString();
        var points = pointsByRadius[rKey];
        
        if (points && points.length > 0) {
            points.forEach(function(point) {
                var x = point.x;
                var y = point.y;
                var hit = point.hit;
                
                var canvasX = centerX + x * scale;
                var canvasY = centerY - y * scale;
                
                ctx.beginPath();
                ctx.arc(canvasX, canvasY, 4, 0, 2 * Math.PI);
                ctx.fillStyle = hit ? '#4caf50' : '#f44336';
                ctx.fill();
                ctx.strokeStyle = '#000';
                ctx.lineWidth = 1;
                ctx.stroke();
            });
        }
    } catch (e) {
    }
}

function loadPointsFromTable() {
    try {
        var rKey = currentR.toString();
        pointsByRadius[rKey] = [];
        
        var resultsTable = document.querySelector('.results-table');
        if (!resultsTable) {
            var resultsPanel = document.getElementById('resultsForm:resultsPanel');
            if (resultsPanel) {
                resultsTable = resultsPanel.querySelector('table.results-table');
            }
        }
        
        if (resultsTable && resultsTable.rows && resultsTable.rows.length > 1) {
            for (var i = 1; i < resultsTable.rows.length; i++) {
                var row = resultsTable.rows[i];
                var cells = row.cells;
                if (cells && cells.length >= 4) {
                    var x = parseFloat(cells[0].textContent.trim());
                    var y = parseFloat(cells[1].textContent.trim());
                    var r = parseFloat(cells[2].textContent.trim());
                    var hit = cells[3].textContent.trim().indexOf('Попадание') !== -1;
                    
                    if (isNaN(x) || isNaN(y) || isNaN(r)) {
                        continue;
                    }
                    
                    if (Math.abs(r - currentR) < 0.001) {
                        pointsByRadius[rKey].push({x: x, y: y, hit: hit});
                    }
                }
            }
        }
    } catch (e) {
    }
}

function handleCanvasClick(event) {
    canvas = document.getElementById('areaCanvas');
    if (!canvas) {
        return;
    }
    
    if (!ctx) {
        ctx = canvas.getContext('2d');
    }
    
    var rect = canvas.getBoundingClientRect();
    var x = event.clientX - rect.left;
    var y = event.clientY - rect.top;
    
    var centerX = 250;
    var centerY = 250;
    
    var rSelect = document.getElementById('pointForm:rValue');
    if (rSelect) {
        currentR = parseFloat(rSelect.value) || 1.0;
    }
    
    var scale = 100 / currentR;
    
    var realX = (x - centerX) / scale;
    var realY = (centerY - y) / scale;
    
    setYValue(realY);
    submitPoint(realX, realY);
}

function setYValue(value) {
    var yInput = document.getElementById('pointForm:yValue');
    if (yInput) {
        yInput.value = value.toString();
    }
}

function submitPoint(x, y) {
    var xStr = x.toString();
    var yStr = y.toString();
    
    var xInput = document.getElementById('pointForm:xValue');
    if (xInput) {
        xInput.value = xStr;
        
        var selectedText = document.getElementById('pointForm:xSelectedValue');
        if (selectedText) {
            var displayX = parseFloat(xStr).toFixed(10).replace(/\.?0+$/, '');
            selectedText.textContent = 'Выбрано: ' + displayX;
        }
        
        var xLinks = document.querySelectorAll('#xSelector a');
        xLinks.forEach(function(link) {
            link.classList.remove('selected');
        });
    }
    
    var yInput = document.getElementById('pointForm:yValue');
    if (yInput) {
        yInput.value = yStr;
    }
    
    setTimeout(function() {
        var checkButton = document.getElementById('pointForm:checkButton');
        if (checkButton) {
            checkButton.click();
        }
    }, 50);
}

function setupCanvasClickHandler() {
    var canvasEl = document.getElementById('areaCanvas');
    if (canvasEl) {
        canvasEl.removeEventListener('click', handleCanvasClick);
        canvasEl.addEventListener('click', handleCanvasClick);
    }
}

function forceRedraw() {
    loadPointsFromTable();
    canvas = null;
    ctx = null;
    if (initCanvas()) {
        drawArea();
        setupCanvasClickHandler();
        return true;
    }
    return false;
}

function handleRadiusChange() {
    var rSelect = document.getElementById('pointForm:rValue');
    if (rSelect) {
        var newR = parseFloat(rSelect.value) || 1.0;
        
        currentR = newR;
        loadPointsFromTable();
        
        setTimeout(function() {
            if (initCanvas()) {
                drawArea();
            }
        }, 100);
    }
}

function setX(value) {
    var xInput = document.getElementById('pointForm:xValue');
    if (xInput) {
        xInput.value = value;
    }
    
    updateXSelection(value);
    
    var selectedText = document.getElementById('pointForm:xSelectedValue');
    if (selectedText) {
        selectedText.textContent = 'Выбрано: ' + value;
    }
}

function updateXSelection(value) {
    var xLinks = document.querySelectorAll('#xSelector a, #xSelector .x-link');
    xLinks.forEach(function(link) {
        var linkText = link.textContent.trim();
        if (linkText == value.toString() || linkText == '-' + value.toString()) {
            link.classList.add('selected');
        } else {
            link.classList.remove('selected');
        }
    });
}

window.drawArea = drawArea;
window.handleCanvasClick = handleCanvasClick;
window.initCanvas = initCanvas;
window.setupCanvasClickHandler = setupCanvasClickHandler;
window.setX = setX;
window.updateXSelection = updateXSelection;
window.forceRedraw = forceRedraw;
window.handleRadiusChange = handleRadiusChange;
window.loadPointsFromTable = loadPointsFromTable;

document.addEventListener('DOMContentLoaded', function() {
    setTimeout(setupCanvasClickHandler, 100);
});

window.addEventListener('load', function() {
    setTimeout(setupCanvasClickHandler, 200);
});

