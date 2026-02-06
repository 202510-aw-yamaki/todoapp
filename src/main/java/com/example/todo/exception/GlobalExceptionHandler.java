package com.example.todo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({TodoNotFoundException.class, AttachmentNotFoundException.class})
    public ModelAndView handleNotFound(RuntimeException ex) {
        logger.warn("Resource not found", ex);
        ModelAndView mv = new ModelAndView("error/404");
        mv.addObject("message", ex.getMessage());
        return mv;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleStaticNotFound(NoResourceFoundException ex) {
        logger.warn("Static resource not found", ex);
        return new ModelAndView("error/404");
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception ex) {
        logger.error("Unexpected error", ex);
        return new ModelAndView("error/500");
    }
}
