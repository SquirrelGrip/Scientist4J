package com.github.squirrelgrip.report.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class WebSecurityConfiguration: WebSecurityConfigurerAdapter() {
    @Value("\${server.cors.enabled}")
    private val corsEnabled: Boolean = true

    override fun configure(http: HttpSecurity) {
        http
                .csrf().disable()
                .cors().configurationSource(configurationSource())
//        http.requiresChannel().anyRequest().requiresSecure()
        http
                .antMatcher("/**").authorizeRequests()
                .antMatchers("/", "/errors**", "/api/**").permitAll()
                .anyRequest().authenticated()
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.inMemoryAuthentication()
    }

    override fun configure(web: WebSecurity?) {
        web?.ignoring()?.antMatchers("/actuator/refresh")
    }

    private fun configurationSource(): CorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()

        if (!corsEnabled) {
            val configuration =  CorsConfiguration()
            configuration.allowedOrigins = listOf("*")
            configuration.allowedMethods = listOf("*")
            configuration.allowedHeaders = listOf("*")
            configuration.allowCredentials = true
            source.registerCorsConfiguration("/**", configuration)
        }
        return source
    }
}