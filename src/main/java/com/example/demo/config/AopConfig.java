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
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;

import java.util.Collections;
import java.util.Properties;

/**
 * This class provides an advanced, AOP-based configuration for transaction management.
 * This approach allows applying transactional behavior to the service layer without
 * scattering @Transactional annotations throughout the codebase. It centralizes the
 * transaction policy, ensuring consistency and adhering to the DRY principle.
 * By using AOP, we separate the cross-cutting concern of transaction management
 * from the business logic.
 */
@Configuration
@Aspect // Marks this class as an Aspect for AOP
public class AopConfig {

    // Pointcut expression to target all public methods in the service layer.
    private static final String AOP_POINTCUT_EXPRESSION = "execution(* com.example.demo.service..*.*(..))";

    /**
     * Creates a TransactionInterceptor bean, which is the core of the AOP-based transaction management.
     * This interceptor defines the default transactional attributes (e.g., propagation, rollback rules)
     * and can be configured to apply different rules for different methods.
     *
     * @param transactionManager The platform transaction manager provided by Spring.
     * @return A configured TransactionInterceptor.
     */
    @Bean
    public TransactionInterceptor txAdvice(PlatformTransactionManager transactionManager) {
        // Define transaction attributes for read-only methods
        DefaultTransactionAttribute txAttr_REQUIRED_READONLY = new DefaultTransactionAttribute();
        txAttr_REQUIRED_READONLY.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        txAttr_REQUIRED_READONLY.setReadOnly(true);

        // Define transaction attributes for methods that require a new transaction and should roll back on any exception
        RuleBasedTransactionAttribute txAttr_REQUIRED = new RuleBasedTransactionAttribute();
        txAttr_REQUIRED.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        txAttr_REQUIRED.setRollbackRules(Collections.singletonList(new RollbackRuleAttribute(Exception.class)));

        // Use NameMatchTransactionAttributeSource to apply different transaction attributes based on method names
        NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();
        Properties txAttributes = new Properties();
        
        // Read-only methods: find*, get*, search*, count*
        txAttributes.setProperty("find*", txAttr_REQUIRED_READONLY.toString());
        txAttributes.setProperty("get*", txAttr_REQUIRED_READONLY.toString());
        txAttributes.setProperty("search*", txAttr_REQUIRED_READONLY.toString());
        txAttributes.setProperty("count*", txAttr_REQUIRED_READONLY.toString());
        
        // Writable methods (default): create*, save*, update*, delete*, etc.
        txAttributes.setProperty("*", txAttr_REQUIRED.toString());
        
        source.setProperties(txAttributes);

        // Create the TransactionInterceptor with the configured source and transaction manager
        TransactionInterceptor txAdvice = new TransactionInterceptor();
        txAdvice.setTransactionAttributeSource(source);
        txAdvice.setTransactionManager(transactionManager);

        return txAdvice;
    }

    /**
     * Creates an Advisor bean that binds the transactional advice (txAdvice) with a pointcut.
     * The pointcut defines which methods the transactional advice should be applied to.
     * An Advisor is a combination of an Advice (the "what", e.g., transaction management) and a Pointcut (the "where", e.g., service layer methods).
     *
     * @param txAdvice The transactional advice (interceptor).
     * @return A PointcutAdvisor that applies transactional behavior to the service layer.
     */
    @Bean
    public Advisor txAdviceAdvisor(TransactionInterceptor txAdvice) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        // The pointcut expression targets all public methods in any class within the com.example.demo.service package and its subpackages.
        pointcut.setExpression(AOP_POINTCUT_EXPRESSION);
        // The DefaultPointcutAdvisor links the pointcut with the advice.
        return new DefaultPointcutAdvisor(pointcut, txAdvice);
    }
}
