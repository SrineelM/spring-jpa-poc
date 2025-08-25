package com.example.demo.config;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.Collections;

/**
 * This class provides an advanced, AOP-based configuration for transaction management.
 * This approach allows you to apply transactional behavior to your service layer without
 * scattering @Transactional annotations throughout your codebase. It centralizes the
 * transaction policy, ensuring consistency and adhering to the DRY principle.
 */
@Configuration
@Aspect
public class AopConfig {

    private static final String AOP_POINTCUT_EXPRESSION = "execution(* com.example.demo.service..*.*(..))";

    /**
     * Creates a TransactionInterceptor bean, which is the core of the AOP-based transaction management.
     * This interceptor contains the default transactional attributes (e.g., propagation, rollback rules).
     *
     * @param transactionManager The platform transaction manager.
     * @return A configured TransactionInterceptor.
     */
    @Bean
    public TransactionInterceptor txAdvice(PlatformTransactionManager transactionManager) {
        DefaultTransactionAttribute txAttr_REQUIRED = new DefaultTransactionAttribute();
        txAttr_REQUIRED.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        RuleBasedTransactionAttribute txAttr_REQUIRED_ROLLBACK = new RuleBasedTransactionAttribute();
        txAttr_REQUIRED_ROLLBACK.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        txAttr_REQUIRED_ROLLBACK.setRollbackRules(Collections.singletonList(new RollbackRuleAttribute(Exception.class)));

        TransactionInterceptor txAdvice = new TransactionInterceptor();
        txAdvice.setTransactionManager(transactionManager);
        // You can customize the transactional attributes for different methods here if needed.
        // For simplicity, we are using the same attributes for all methods.
        txAdvice.setTransactionAttributeSource(name -> txAttr_REQUIRED);

        return txAdvice;
    }

    /**
     * Creates an Advisor bean that binds the transactional advice (txAdvice) with a pointcut.
     * The pointcut defines which methods the transactional advice should be applied to.
     *
     * @param txAdvice The transactional advice (interceptor).
     * @return A PointcutAdvisor that applies transactional behavior to the service layer.
     */
    @Bean
    public Advisor txAdviceAdvisor(TransactionInterceptor txAdvice) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        // The pointcut expression targets all public methods in any class within the com.example.demo.service package and its subpackages.
        pointcut.setExpression(AOP_POINTCUT_EXPRESSION);
        return new DefaultPointcutAdvisor(pointcut, txAdvice);
    }
}
