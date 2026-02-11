package com.example.mangxahoi.Config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DataSourcesConfig {

    // ===== MySQL (PRIMARY) =====
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties mysqlProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "dataSource")
    @Primary
    public DataSource mysqlDataSource() {
        return mysqlProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    // ===== PostgreSQL (SEARCH) =====
    @Bean
    @ConfigurationProperties("search.datasource")
    public DataSourceProperties searchProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "searchDataSource")
    public DataSource searchDataSource() {
        return searchProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean(name = "searchJdbcTemplate")
    public JdbcTemplate searchJdbcTemplate(@Qualifier("searchDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }
}
