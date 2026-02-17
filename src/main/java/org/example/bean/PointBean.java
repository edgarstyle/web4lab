package org.example.bean;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.example.entity.Result;
import org.example.exception.ValidationException;
import org.example.util.AreaChecker;
import org.example.util.ErrorHandler;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("pointBean")
@ViewScoped
public class PointBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(PointBean.class.getName());

    @Inject
    private SessionBean sessionBean;

    private BigDecimal x;
    private BigDecimal y;
    private BigDecimal r = new BigDecimal("1.0");
    
    private static final BigDecimal MIN_X = new BigDecimal("-5");
    private static final BigDecimal MAX_X = new BigDecimal("3");
    private static final BigDecimal MIN_Y = new BigDecimal("-5");
    private static final BigDecimal MAX_Y = new BigDecimal("3");
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    public BigDecimal getX() {
        return x;
    }

    public void setX(BigDecimal x) {
        if (x != null && (x.compareTo(MIN_X) < 0 || x.compareTo(MAX_X) > 0)) {
            ErrorHandler.handleValidationError("X должно быть в диапазоне от -5 до 3");
            return;
        }
        this.x = x;
    }
    
    public void setX(String xStr) {
        if (xStr == null || xStr.trim().isEmpty()) {
            this.x = null;
            return;
        }
        try {
            BigDecimal xValue = new BigDecimal(xStr.trim());
            if (xValue.compareTo(MIN_X) < 0 || xValue.compareTo(MAX_X) > 0) {
                ErrorHandler.handleValidationError("X должно быть в диапазоне от -5 до 3");
                this.x = null;
                return;
            }
            this.x = xValue;
        } catch (NumberFormatException e) {
            ErrorHandler.handleValidationError("X должно быть числом");
            this.x = null;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Ошибка при установке X из строки", e);
            ErrorHandler.handleValidationError("Неверный формат значения X");
            this.x = null;
        }
    }
    
    public String setXValue(Double value) {
        try {
            if (value != null) {
                BigDecimal xValue = BigDecimal.valueOf(value);
                if (xValue.compareTo(MIN_X) < 0 || xValue.compareTo(MAX_X) > 0) {
                    ErrorHandler.handleValidationError("X должно быть в диапазоне от -5 до 3");
                    return null;
                }
                this.x = xValue;
            } else {
                this.x = null;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Ошибка при установке X через commandLink", e);
            ErrorHandler.handleValidationError("Ошибка при выборе значения X");
        }
        return null;
    }

    public BigDecimal getY() {
        return y;
    }

    public void setY(BigDecimal y) {
        this.y = y;
    }
    
    public void setY(String yStr) {
        if (yStr == null || yStr.trim().isEmpty()) {
            this.y = null;
            return;
        }
        try {
            this.y = new BigDecimal(yStr.trim());
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Ошибка парсинга Y из строки: " + yStr, e);
            this.y = null;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Неожиданная ошибка при установке Y из строки", e);
            this.y = null;
        }
    }

    public BigDecimal getR() {
        return r;
    }

    public void setR(BigDecimal r) {
        this.r = r;
    }
    
    public void setR(String rStr) {
        if (rStr == null || rStr.trim().isEmpty()) {
            this.r = new BigDecimal("1.0");
            return;
        }
        try {
            BigDecimal rValue = new BigDecimal(rStr.trim());
            if (rValue.compareTo(ZERO) <= 0) {
                logger.warning("Попытка установить неположительное значение R: " + rStr);
                this.r = new BigDecimal("1.0");
                return;
            }
            this.r = rValue;
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Ошибка парсинга R из строки: " + rStr, e);
            this.r = new BigDecimal("1.0"); // Значение по умолчанию
        } catch (Exception e) {
            logger.log(Level.WARNING, "Неожиданная ошибка при установке R из строки", e);
            this.r = new BigDecimal("1.0");
        }
    }

    public String checkPoint() {
        try {
            if (x == null) {
                ErrorHandler.handleValidationError("Необходимо выбрать значение X (кликните на графике или выберите из списка)");
                return null;
            }
            
            if (y == null) {
                ErrorHandler.handleValidationError("Необходимо ввести значение Y");
                return null;
            }
            
            if (r == null || r.compareTo(ZERO) <= 0) {
                ErrorHandler.handleValidationError("Необходимо выбрать положительное значение R");
                return null;
            }

            if (sessionBean == null) {
                logger.severe("SessionBean не инициализирован");
                ErrorHandler.handleError(new IllegalStateException("Ошибка инициализации сессии"), 
                        "Ошибка инициализации приложения. Перезагрузите страницу.");
                return null;
            }

            long startTime = System.nanoTime();
            boolean hit;
            try {
                hit = AreaChecker.checkHit(x, y, r);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Ошибка при проверке попадания точки", e);
                ErrorHandler.handleError(e, "Ошибка при проверке попадания точки в область");
                return null;
            }
            
            long executionTime = (System.nanoTime() - startTime) / 1000;

            Result result = new Result(x, y, r, hit);
            result.setExecutionTime(executionTime);
            
            try {
                sessionBean.addResult(result);
                logger.fine("Результат успешно добавлен: " + result);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Ошибка при сохранении результата", e);
                ErrorHandler.handleError(e, "Не удалось сохранить результат. Попробуйте еще раз.");
                return null;
            }

            return "main";
        } catch (ValidationException e) {
            ErrorHandler.handleValidationError(e.getMessage());
            return null;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Неожиданная ошибка при проверке точки", e);
            ErrorHandler.handleError(e, "Произошла непредвиденная ошибка при проверке точки");
            return null;
        }
    }


    public void validateY(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value == null) {
            return;
        }

        BigDecimal yValue;
        try {
            if (value instanceof BigDecimal) {
                yValue = (BigDecimal) value;
            } else if (value instanceof Double) {
                yValue = BigDecimal.valueOf((Double) value);
            } else if (value instanceof String) {
                String strValue = ((String) value).trim();
                if (strValue.isEmpty()) {
                    return;
                }
                try {
                    yValue = new BigDecimal(strValue);
                } catch (NumberFormatException e) {
                    logger.log(Level.WARNING, "Ошибка парсинга Y: " + strValue, e);
                    throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Ошибка валидации", "Y должно быть числом"));
                }
            } else {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Ошибка валидации", "Y должно быть числом"));
            }

            if (yValue.compareTo(MIN_Y) < 0 || yValue.compareTo(MAX_Y) > 0) {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Ошибка валидации", "Y должно быть в диапазоне от -5 до 3"));
            }
        } catch (ValidatorException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Неожиданная ошибка при валидации Y", e);
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Ошибка валидации", "Ошибка при проверке значения Y"));
        }
    }

    public void validateR(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value == null) {
            return;
        }

        BigDecimal rValue;
        try {
            if (value instanceof BigDecimal) {
                rValue = (BigDecimal) value;
            } else if (value instanceof Double) {
                rValue = BigDecimal.valueOf((Double) value);
            } else if (value instanceof String) {
                String strValue = ((String) value).trim();
                if (strValue.isEmpty()) {
                    throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Ошибка валидации", "R обязательно для заполнения"));
                }
                try {
                    rValue = new BigDecimal(strValue);
                } catch (NumberFormatException e) {
                    logger.log(Level.WARNING, "Ошибка парсинга R: " + strValue, e);
                    throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Ошибка валидации", "R должно быть числом"));
                }
            } else {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Ошибка валидации", "R должно быть числом"));
            }

            if (rValue.compareTo(ZERO) <= 0) {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Ошибка валидации", "R должно быть положительным числом"));
            }
        } catch (ValidatorException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Неожиданная ошибка при валидации R", e);
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Ошибка валидации", "Ошибка при проверке значения R"));
        }
    }
}

