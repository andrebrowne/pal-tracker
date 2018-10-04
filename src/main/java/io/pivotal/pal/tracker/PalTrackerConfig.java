package io.pivotal.pal.tracker;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.cloud.config.java.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import javax.sql.DataSource;
import java.util.Properties;

@ServiceScan
public class PalTrackerConfig {

    @Configuration
    @EnableWebSecurity
    static class SecurityConfiguration extends WebSecurityConfigurerAdapter {

        @Autowired
        private PalTrackerProperties palTrackerProperties;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            if (!palTrackerProperties.isDisabled()) {
                http.requiresChannel().anyRequest().requiresSecure();
            }

            http
                    .authorizeRequests().antMatchers("/**").hasRole("USER")
                    .and()
                    .httpBasic()
                    .and()
                    .csrf().disable();
        }

        @Override
        public void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth
                    .inMemoryAuthentication()
                    .withUser("user").password("password").roles("USER");
        }
    }

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