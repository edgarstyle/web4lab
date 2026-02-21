package org.example.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Класс для проверки попадания точки в область
 * Вариант 88744
 * 
 * Область состоит из:
 * 1. Квадрат в четвертой четверти: x от 0 до R, y от -R до 0
 * 2. Треугольник во второй четверти: вершины (0, 0), (-R/2, 0), (0, R/2)
 *    Линия от (-R/2, 0) до (0, R/2): y = x + R/2
 *    Условие: y ≤ x + R/2, где x от -R/2 до 0, y от 0 до R/2
 * 3. Четверть круга в первой четверти: центр (0,0), радиус R, от x=0 до R, от y=0 до R
 *    Условие: x ≥ 0, y ≥ 0, x² + y² ≤ R²
 */
public class AreaChecker {
    private static final MathContext MATH_CONTEXT = new MathContext(50, RoundingMode.HALF_UP);
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal TWO = new BigDecimal("2");

    public static boolean checkHit(BigDecimal x, BigDecimal y, BigDecimal r) {
        if (r == null || r.compareTo(ZERO) <= 0) {
            return false;
        }
        
        if (x == null || y == null) {
            return false;
        }

        BigDecimal rHalf = r.divide(TWO, MATH_CONTEXT);
        
        // Квадрат в четвертой четверти: x от 0 до R, y от -R до 0
        if (x.compareTo(ZERO) >= 0 && x.compareTo(r) <= 0 
                && y.compareTo(r.negate()) >= 0 && y.compareTo(ZERO) <= 0) {
            return true;
        }

        // Треугольник во второй четверти: вершины (0, 0), (-R/2, 0), (0, R/2)
        // Линия от (-R/2, 0) до (0, R/2): y = x + R/2
        // Условие: y ≤ x + R/2, где x от -R/2 до 0, y от 0 до R/2
        if (x.compareTo(rHalf.negate()) >= 0 && x.compareTo(ZERO) <= 0 
                && y.compareTo(ZERO) >= 0 && y.compareTo(rHalf) <= 0) {
            BigDecimal boundary = x.add(rHalf, MATH_CONTEXT);
            if (y.compareTo(boundary) <= 0) {
                return true;
            }
        }

        // Четверть круга в первой четверти: центр (0,0), радиус R, от x=0 до R, от y=0 до R
        // Условие: x ≥ 0, y ≥ 0, x² + y² ≤ R²
        if (x.compareTo(ZERO) >= 0 && y.compareTo(ZERO) >= 0) {
            BigDecimal xSquared = x.multiply(x, MATH_CONTEXT);
            BigDecimal ySquared = y.multiply(y, MATH_CONTEXT);
            BigDecimal distanceSquared = xSquared.add(ySquared, MATH_CONTEXT);
            BigDecimal rSquared = r.multiply(r, MATH_CONTEXT);
            if (distanceSquared.compareTo(rSquared) <= 0) {
                return true;
            }
        }

        return false;
    }
}



