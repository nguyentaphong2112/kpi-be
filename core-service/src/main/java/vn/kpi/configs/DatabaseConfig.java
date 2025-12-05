package vn.kpi.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean(name = "primaryJdbc")
    @Autowired
    @Qualifier("primaryDataSource")
    public NamedParameterJdbcTemplate primaryJdbcTemplate(){
        return new NamedParameterJdbcTemplate(primaryDataSource());
    }

    @Bean(name = "secondJdbc")
    @Autowired
    @Qualifier("secondDataSource")
    public NamedParameterJdbcTemplate secondJdbcTemplate() {
        return new NamedParameterJdbcTemplate(secondaryDataSource());
    }

    @Bean (name = "primaryDataSource")
    @Primary
    @ConfigurationProperties(prefix="spring.datasource.hikari")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "secondDataSource")
    @ConfigurationProperties(prefix="spring.datasource2.hikari")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create().build();
    }

}
