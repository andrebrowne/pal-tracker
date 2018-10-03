package io.pivotal.pal.tracker;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.cloud.config.java.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.Properties;

@ServiceScan
public class PalTrackerConfig {
    @Configuration
    @Profile("cloud")
    static class CloudConfiguration extends AbstractCloudConfig {

        @Bean
        public DataSource dataSource() {
            return connectionFactory().dataSource();
        }

        @Bean
        public Properties cloudProperties() {
            return properties();
        }
    }

    @Configuration
    @Profile("default")
    static class LocalConfiguration {
        @Bean
        public DataSource dataSource() {
            MysqlDataSource dataSource = new MysqlDataSource();
            dataSource.setUrl(System.getenv("SPRING_DATASOURCE_URL"));
            return dataSource;
        }
        @Bean
        public Properties cloudProperties() {
            return new Properties();
        }

    }
}