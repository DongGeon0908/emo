package com.goofy.emo.config.database

import com.zaxxer.hikari.HikariDataSource
import jakarta.persistence.EntityManagerFactory
import org.hibernate.cfg.AvailableSettings
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.orm.hibernate5.SpringBeanContainer
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = [
        "com.goofy.emo"
    ],
    entityManagerFactoryRef = "emoEntityManager",
    transactionManagerRef = "emoTransactionManager"
)
class EmoDatabaseConfig {
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "emo.master.datasource")
    fun emoMasterDataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "emo.master.datasource.hikari")
    fun emoMasterHikariDataSource(
        @Qualifier("emoMasterDataSourceProperties") masterProperty: DataSourceProperties,
    ): HikariDataSource {
        return masterProperty
            .initializeDataSourceBuilder()
            .type(HikariDataSource::class.java)
            .build()
    }

    @Bean
    fun emoNamedParameterJdbcTemplate(
        @Qualifier("emoMasterHikariDataSource") dataSource: DataSource,
    ): NamedParameterJdbcTemplate {
        return NamedParameterJdbcTemplate(dataSource)
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "emo.jpa")
    fun emoJpaProperties(): JpaProperties {
        return JpaProperties()
    }

    @Bean
    @Primary
    fun emoEntityManager(
        entityManagerFactoryBuilder: EntityManagerFactoryBuilder,
        configurableListableBeanFactory: ConfigurableListableBeanFactory,
        @Qualifier("emoMasterHikariDataSource") emoDataSource: DataSource,
    ): LocalContainerEntityManagerFactoryBean {
        return entityManagerFactoryBuilder
            .dataSource(emoDataSource)
            .packages("com.goofy.emo")
            .properties(mapOf(AvailableSettings.BEAN_CONTAINER to SpringBeanContainer(configurableListableBeanFactory)))
            .build()
    }

    @Bean
    @Primary
    fun emoTransactionManager(
        @Qualifier("emoEntityManager") emoEntityManager: EntityManagerFactory,
    ): PlatformTransactionManager {
        return JpaTransactionManager(emoEntityManager)
    }

    @Bean
    fun persistenceExceptionTranslationPostProcessor(): PersistenceExceptionTranslationPostProcessor {
        return PersistenceExceptionTranslationPostProcessor()
    }
}
