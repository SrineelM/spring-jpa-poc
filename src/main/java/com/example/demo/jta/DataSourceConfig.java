package com.example.demo.jta;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Consolidated configuration for setting up multiple data sources to demonstrate JTA (Java Transaction API)
 * distributed transactions. This class defines the beans for two separate databases, a primary and a secondary one,
 * and configures their respective JPA Entity Manager Factories.
 */
@Configuration
public class DataSourceConfig {

    // =====================================================================================
    // Data Source Bean Definitions
    // =====================================================================================

    /**
     * Defines the primary data source bean. This bean is configured using properties prefixed with
     * "spring.jta.atomikos.datasource.primary". It uses AtomikosDataSourceBean to ensure it is
     * an XA-compliant data source, which is a requirement for participating in JTA transactions.
     *
     * @return A configured XA DataSource for the primary database.
     */
    @Primary
    @Bean(name = "primaryDataSource")
    @ConfigurationProperties(prefix = "spring.jta.atomikos.datasource.primary")
    public DataSource primaryDataSource() {
        return new AtomikosDataSourceBean();
    }

    /**
     * Defines the secondary data source bean, configured with properties prefixed with
     * "spring.jta.atomikos.datasource.secondary". Like the primary, it is an XA-compliant data source
     * managed by Atomikos.
     *
     * @return A configured XA DataSource for the secondary database.
     */
    @Bean(name = "secondaryDataSource")
    @ConfigurationProperties(prefix = "spring.jta.atomikos.datasource.secondary")
    public DataSource secondaryDataSource() {
        return new AtomikosDataSourceBean();
    }

    // =====================================================================================
    // JPA Properties
    // =====================================================================================

    /**
     * Helper method to define common JPA properties. These properties configure Hibernate for use with JTA.
     * - `hibernate.hbm2ddl.auto`: "create-drop" is used for demo purposes to create the schema on startup.
     * - `hibernate.dialect`: Specifies the SQL dialect for H2.
     * - `hibernate.transaction.jta.platform`: This is the crucial property that integrates Hibernate with the
     *   Atomikos JTA transaction manager.
     *
     * @return A Properties object containing essential Hibernate JTA settings.
     */
    private Properties jpaProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.transaction.jta.platform", "com.atomikos.icatch.jta.hibernate4.AtomikosPlatform");
        return properties;
    }

    // =====================================================================================
    // Primary Persistence Unit Configuration
    // =====================================================================================

    @Configuration
    @EnableJpaRepositories(
            basePackages = "com.example.demo.jta.primary",
            entityManagerFactoryRef = "primaryEntityManagerFactory",
            transactionManagerRef = "transactionManager" // Shared JTA transaction manager
    )
    public static class PrimaryDbConfig {
        /**
         * Configures the JPA EntityManagerFactory for the primary data source.
         *
         * @param builder  The factory builder provided by Spring Boot.
         * @param dataSource The primary data source bean.
         * @return The configured LocalContainerEntityManagerFactoryBean for the primary database.
         */
        @Primary
        @Bean(name = "primaryEntityManagerFactory")
        public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
                EntityManagerFactoryBuilder builder,
                @Qualifier("primaryDataSource") DataSource dataSource) {
            return builder
                    .dataSource(dataSource)
                    .packages("com.example.demo.jta.Account") // Package where entities are located
                    .persistenceUnit("primary") // A unique name for the persistence unit
                    .properties(jpaProperties()) // Apply common JTA properties
                    .build();
        }
    }

    // =====================================================================================
    // Secondary Persistence Unit Configuration
    // =====================================================================================

    @Configuration
    @EnableJpaRepositories(
            basePackages = "com.example.demo.jta.secondary",
            entityManagerFactoryRef = "secondaryEntityManagerFactory",
            transactionManagerRef = "transactionManager" // Shared JTA transaction manager
    )
    public static class SecondaryDbConfig {
        /**
         * Configures the JPA EntityManagerFactory for the secondary data source.
         *
         * @param builder  The factory builder provided by Spring Boot.
         * @param dataSource The secondary data source bean.
         * @return The configured LocalContainerEntityManagerFactoryBean for the secondary database.
         */
        @Bean(name = "secondaryEntityManagerFactory")
        public LocalContainerEntityManagerFactoryBean secondaryEntityManagerFactory(
                EntityManagerFactoryBuilder builder,
                @Qualifier("secondaryDataSource") DataSource dataSource) {
            return builder
                    .dataSource(dataSource)
                    .packages("com.example.demo.jta.Account") // Can reuse the same entity
                    .persistenceUnit("secondary") // A unique name for the persistence unit
                    .properties(jpaProperties()) // Apply common JTA properties
                    .build();
        }
    }
}
