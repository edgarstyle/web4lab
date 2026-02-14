/**
 * JavaScript для работы с canvas и отрисовки области
 */

var canvas;
var ctx;
var currentR = 1.0;
// Хранилище точек для каждого радиуса: { "1.0": [{x, y, hit}, ...], "1.5": [...], ... }
var pointsByRadius = {};

function initCanvas() {
    canvas = document.getElementById('areaCanvas');
    if (!canvas) {
        return false;
    }
    ctx = canvas.getContext('2d');
    
    // Получаем текущее значение R из формы
    var rSelect = document.getElementById('pointForm:rValue');
    if (rSelect) {
        currentR = parseFloat(rSelect.value) || 1.0;
    }
    return true;
}

function drawArea() {
    // Убеждаемся, что canvas инициализирован
    if (!canvas || !ctx) {
        if (!initCanvas()) {
            return;
        }
    }
    
    // Обновляем значение R
    var rSelect = document.getElementById('pointForm:rValue');
    if (rSelect) {
        currentR = parseFloat(rSelect.value) || 1.0;
    }
    
    // Очищаем canvas
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    var centerX = 250;
    var centerY = 250;
    // Масштаб: canvas 500x500, область от -R до R по обеим осям = 2R единиц
    // Уменьшенный масштаб: 1 единица = 100 / R пикселей (чтобы область занимала меньше места на canvas)
    var scale = 100 / currentR; // Масштаб зависит от R
    
    // Рисуем оси
    ctx.strokeStyle = '#000';
    ctx.lineWidth = 2;
    
    // Ось X
    ctx.beginPath();
    ctx.moveTo(0, centerY);
    ctx.lineTo(canvas.width, centerY);
    ctx.stroke();
    
    // Ось Y
    ctx.beginPath();
    ctx.moveTo(centerX, 0);
    ctx.lineTo(centerX, canvas.height);
    ctx.stroke();
    
    // Подписи осей
    ctx.fillStyle = '#000';
    ctx.font = '12px Arial';
    ctx.fillText('x', canvas.width - 10, centerY - 5);
    ctx.fillText('y', centerX + 5, 15);
    
    // Рисуем засечки и подписи
    drawTicks(centerX, centerY, scale);
    
    // Рисуем область
    ctx.fillStyle = 'rgba(0, 100, 255, 0.5)';
    ctx.strokeStyle = 'rgba(0, 100, 255, 0.8)';
    ctx.lineWidth = 2;
    
    // 1. Прямоугольник во второй четверти: -R/2 <= x <= 0, 0 <= y <= R
    ctx.beginPath();
    ctx.rect(centerX - (currentR / 2) * scale, centerY - currentR * scale, 
             (currentR / 2) * scale, currentR * scale);
    ctx.fill();
    ctx.stroke();
    
    // 2. Треугольник в первой четверти: 0 <= x <= R/2, 0 <= y <= R
    // Наклонная граница: линия от (0, R) до (R/2, 0)
    // Уравнение: y = R - 2x
    ctx.beginPath();
    ctx.moveTo(centerX, centerY - currentR * scale); // (0, R)
    ctx.lineTo(centerX + (currentR / 2) * scale, centerY); // (R/2, 0)
    ctx.lineTo(centerX, centerY); // (0, 0)
    ctx.closePath();
    ctx.fill();
    ctx.stroke();
    
    // 3. Четверть круга в третьей четверти
    ctx.beginPath();
    ctx.arc(centerX, centerY, currentR * scale, Math.PI / 2, Math.PI, false);
    ctx.lineTo(centerX, centerY);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();
    
    // Рисуем точки из результатов (передаем scale для правильного позиционирования)
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
    
    // Используем переданный scale (который уже учитывает текущий R)
    // scale = 150 / r, поэтому tick * scale = tick * (150 / r)
    
    // Засечки на оси X
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
    
    // Засечки на оси Y
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
    // Рисуем точки для текущего радиуса из памяти
    try {
        var rKey = currentR.toString();
        var points = pointsByRadius[rKey];
        
        if (points && points.length > 0) {
            points.forEach(function(point) {
                var x = point.x;
                var y = point.y;
                var hit = point.hit;
                
                // Преобразуем координаты в координаты canvas
                var canvasX = centerX + x * scale;
                var canvasY = centerY - y * scale; // Инвертируем Y
                
                // Рисуем точку
                ctx.beginPath();
                ctx.arc(canvasX, canvasY, 4, 0, 2 * Math.PI);
                ctx.fillStyle = hit ? '#4caf50' : '#f44336'; // Зеленый для попадания, красный для непопадания
                ctx.fill();
                ctx.strokeStyle = '#000';
                ctx.lineWidth = 1;
                ctx.stroke();
            });
        }
    } catch (e) {
        // Ошибка при отрисовке точек
    }
}

// Загружает точки из таблицы результатов в память для текущего радиуса
function loadPointsFromTable() {
    try {
        var rKey = currentR.toString();
        pointsByRadius[rKey] = []; // Очищаем текущие точки
        
        // Пробуем найти таблицу несколькими способами
        var resultsTable = document.querySelector('.results-table');
        if (!resultsTable) {
            var resultsPanel = document.getElementById('resultsForm:resultsPanel');
            if (resultsPanel) {
                resultsTable = resultsPanel.querySelector('table.results-table');
            }
        }
        
        if (resultsTable && resultsTable.rows && resultsTable.rows.length > 1) {
            // Пропускаем заголовок (первая строка)
            for (var i = 1; i < resultsTable.rows.length; i++) {
                var row = resultsTable.rows[i];
                var cells = row.cells;
                if (cells && cells.length >= 4) {
                    var x = parseFloat(cells[0].textContent.trim());
                    var y = parseFloat(cells[1].textContent.trim());
                    var r = parseFloat(cells[2].textContent.trim());
                    var hit = cells[3].textContent.trim().indexOf('Попадание') !== -1;
                    
                    // Проверяем, что координаты валидны
                    if (isNaN(x) || isNaN(y) || isNaN(r)) {
                        continue;
                    }
                    
                    // Сохраняем только точки для текущего радиуса
                    if (Math.abs(r - currentR) < 0.001) {
                        pointsByRadius[rKey].push({x: x, y: y, hit: hit});
                    }
                }
            }
        }
    } catch (e) {
        // Ошибка при загрузке точек из таблицы
    }
}

function handleCanvasClick(event) {
    // Переинициализируем canvas на случай, если он был пересоздан
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
    
    // Преобразуем координаты canvas в реальные координаты
    var centerX = 250;
    var centerY = 250;
    
    // Получаем текущее значение R
    var rSelect = document.getElementById('pointForm:rValue');
    if (rSelect) {
        currentR = parseFloat(rSelect.value) || 1.0;
    }
    
    // Масштаб должен совпадать с масштабом в drawArea
    var scale = 100 / currentR;
    
    var realX = (x - centerX) / scale;
    var realY = (centerY - y) / scale; // Инвертируем Y
    
    // Устанавливаем значения в форму
    setYValue(realY);
    
    // Отправляем форму
    submitPoint(realX, realY);
}

function setYValue(value) {
    var yInput = document.getElementById('pointForm:yValue');
    if (yInput) {
        // Передаем как строку для сохранения точности (BigDecimal на сервере)
        yInput.value = value.toString();
    }
}

function submitPoint(x, y) {
    // Форматируем числа как строки для сохранения точности (BigDecimal на сервере)
    // Используем toFixed с достаточным количеством знаков для длинных чисел
    var xStr = x.toString();
    var yStr = y.toString();
    
    // Устанавливаем X в скрытое поле (может быть любым значением)
    var xInput = document.getElementById('pointForm:xValue');
    if (xInput) {
        xInput.value = xStr;
        
        // Обновляем текст "Выбрано: X" даже если X не из списка
        var selectedText = document.getElementById('pointForm:xSelectedValue');
        if (selectedText) {
            // Показываем с разумным количеством знаков для отображения
            var displayX = parseFloat(xStr).toFixed(10).replace(/\.?0+$/, '');
            selectedText.textContent = 'Выбрано: ' + displayX;
        }
        
        // Убираем подсветку со всех кнопок, так как X не из списка
        var xLinks = document.querySelectorAll('#xSelector a');
        xLinks.forEach(function(link) {
            link.classList.remove('selected');
        });
    }
    
    // Устанавливаем Y
    var yInput = document.getElementById('pointForm:yValue');
    if (yInput) {
        yInput.value = yStr;
    }
    
    // Небольшая задержка и отправка формы (один AJAX-запрос)
    setTimeout(function() {
        var checkButton = document.getElementById('pointForm:checkButton');
        if (checkButton) {
            checkButton.click();
        }
    }, 50);
}

// Функция для установки обработчика клика на canvas
function setupCanvasClickHandler() {
    var canvasEl = document.getElementById('areaCanvas');
    if (canvasEl) {
        // Удаляем старый обработчик, если есть
        canvasEl.removeEventListener('click', handleCanvasClick);
        // Добавляем новый обработчик
        canvasEl.addEventListener('click', handleCanvasClick);
    }
}

// Функция для принудительной перерисовки (для вызова после AJAX)
function forceRedraw() {
    // Загружаем точки для текущего радиуса перед перерисовкой
    loadPointsFromTable();
    // Сбрасываем canvas и ctx
    canvas = null;
    ctx = null;
    // Переинициализируем
    if (initCanvas()) {
        drawArea();
        setupCanvasClickHandler();
        return true;
    }
    return false;
}

// Обработка смены радиуса
function handleRadiusChange() {
    // Получаем новое значение радиуса
    var rSelect = document.getElementById('pointForm:rValue');
    if (rSelect) {
        var newR = parseFloat(rSelect.value) || 1.0;
        
        // Обновляем currentR
        currentR = newR;
        
        // Загружаем точки для нового радиуса из таблицы
        loadPointsFromTable();
        
        // Перерисовываем canvas с новыми точками
        setTimeout(function() {
            if (initCanvas()) {
                drawArea();
            }
        }, 100);
    }
}

// Функция выбора X из быстрых кнопок (для клика по графику)
function setX(value) {
    var xInput = document.getElementById('pointForm:xValue');
    if (xInput) {
        xInput.value = value;
    }
    
    // Обновляем визуальное выделение (для commandLink)
    updateXSelection(value);
    
    // Обновляем текст "Выбрано: X"
    var selectedText = document.getElementById('pointForm:xSelectedValue');
    if (selectedText) {
        selectedText.textContent = 'Выбрано: ' + value;
    }
}

// Функция для обновления визуального выделения выбранной ссылки X
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

// Экспортируем функции для глобального доступа
window.drawArea = drawArea;
window.handleCanvasClick = handleCanvasClick;
window.initCanvas = initCanvas;
window.setupCanvasClickHandler = setupCanvasClickHandler;
window.setX = setX;
window.updateXSelection = updateXSelection;
window.forceRedraw = forceRedraw;
window.handleRadiusChange = handleRadiusChange;
window.loadPointsFromTable = loadPointsFromTable;

// Устанавливаем обработчик клика при загрузке
document.addEventListener('DOMContentLoaded', function() {
    setTimeout(setupCanvasClickHandler, 100);
});

// Также устанавливаем при загрузке страницы
window.addEventListener('load', function() {
    setTimeout(setupCanvasClickHandler, 200);
});

