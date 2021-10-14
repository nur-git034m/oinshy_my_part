package kz.oinshyk.back.security.infra

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.web.servlet.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.ForwardedHeaderFilter
import java.util.*

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfig : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity?) {
        http {
            httpBasic {}
            cors {}
            csrf { disable() }
//            authorizeRequests {
//                authorize("/cat", authenticated)
//            }
        }
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource? {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = Collections.singletonList("*")
            allowedMethods = listOf("GET", "POST", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = Collections.singletonList("*")
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/v1/**", configuration)
        }

    }

    @Bean
    fun forwardedHeaderFilter() = FilterRegistrationBean(ForwardedHeaderFilter()).apply {
        order = HIGHEST_PRECEDENCE
    }

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()
}
