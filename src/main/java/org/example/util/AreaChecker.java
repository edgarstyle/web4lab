package org.example.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Класс для проверки попадания точки в область
 * Вариант 18881 (anime)
 * 
 * Область состоит из:
 * 1. Прямоугольник во второй четверти: x от -R/2 до 0, y от 0 до R
 * 2. Треугольник в первой четверти: x от 0 до R/2, y от 0 до R, с наклонной границей от (0, R) до (R/2, 0)
 *    Уравнение наклонной границы: y = R - 2x
 * 3. Четверть круга в третьей четверти: центр (0,0), радиус R, от x=-R до 0, от y=-R до 0
 */
public class AreaChecker {
    
    // MathContext для вычислений с высокой точностью
    private static final MathContext MATH_CONTEXT = new MathContext(50, RoundingMode.HALF_UP);
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal TWO = new BigDecimal("2");

    public static boolean checkHit(BigDecimal x, BigDecimal y, BigDecimal r) {
        // Проверка на валидность радиуса
        if (r == null || r.compareTo(ZERO) <= 0) {
            return false;
        }
        
        if (x == null || y == null) {
            return false;
        }

        // 1. Прямоугольник во второй четверти: -R/2 <= x <= 0, 0 <= y <= R
        BigDecimal rHalf = r.divide(TWO, MATH_CONTEXT);
        if (x.compareTo(rHalf.negate()) >= 0 && x.compareTo(ZERO) <= 0 
                && y.compareTo(ZERO) >= 0 && y.compareTo(r) <= 0) {
            return true;
        }

        // 2. Треугольник в первой четверти: 0 <= x <= R/2, 0 <= y <= R
        // Наклонная граница: линия от (0, R) до (R/2, 0)
        // Уравнение линии: y = R - 2x (проходит через (0, R) и (R/2, 0))
        // Точка попадает, если y <= R - 2x
        if (x.compareTo(ZERO) >= 0 && x.compareTo(rHalf) <= 0 
                && y.compareTo(ZERO) >= 0 && y.compareTo(r) <= 0) {
            BigDecimal boundary = r.subtract(x.multiply(TWO, MATH_CONTEXT), MATH_CONTEXT);
            if (y.compareTo(boundary) <= 0) {
                return true;
            }
        }

        // 3. Четверть круга в третьей четверти: центр (0,0), радиус R
        // x^2 + y^2 <= R^2, где x <= 0 и y <= 0
        if (x.compareTo(ZERO) <= 0 && y.compareTo(ZERO) <= 0) {
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



