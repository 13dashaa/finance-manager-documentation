package com.example.fmanager;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.example.fmanager.service.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        if (logger.isInfoEnabled()) {
            logger.info("Executing method: {} with args: {}",
                    joinPoint.getSignature().toShortString(), joinPoint.getArgs());
        }
    }

    @AfterReturning(
            pointcut = "execution(* com.example.fmanager.service.*.*(..))",
            returning = "result"
    )
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        if (logger.isInfoEnabled()) {
            logger.info("Method {} executed successfully. Result: {}",
                    joinPoint.getSignature().toShortString(), (result != null ? result : "void"));
        }
    }

    @AfterThrowing(
            pointcut = "execution(* com.example.fmanager.service.*.*(..))",
            throwing = "exception"
    )
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        String errorMessage = String.format(
                "Exception in method: %s. Reason: %s",
                joinPoint.getSignature().toShortString(),
                exception.getMessage()
        );
        logger.error(errorMessage);
    }
}

