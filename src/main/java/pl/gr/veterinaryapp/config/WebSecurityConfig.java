package pl.gr.veterinaryapp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pl.gr.veterinaryapp.jwt.JwtAuthenticationEntryPoint;
import pl.gr.veterinaryapp.jwt.JwtAuthenticationFilter;
import pl.gr.veterinaryapp.security.encoder.BCryptPepperPasswordEncoder;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;

    private final JwtAuthenticationEntryPoint unauthorizedHandler;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Autowired
    public void globalUserDetails(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(encoder());
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf()
                .disable();

        httpSecurity
                .authorizeRequests()
                .antMatchers("/login").permitAll()
                .antMatchers(HttpMethod.GET, "/api/visits/**", "/api/pets/**").authenticated()
                .antMatchers(HttpMethod.POST, "/api/visits/**", "/api/pets/**", "/log-out")
                .authenticated()
                .antMatchers(HttpMethod.POST, "/api/users/**", "/api/clients/**", "/api/vet/**")
                .hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/users/**", "/api/clients/**", "/api/vet/**")
                .hasRole("ADMIN")
                .antMatchers(HttpMethod.PATCH, "/api/users/**", "/api/clients/**", "/api/vet/**")
                .hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/users/**", "/api/clients/**", "/api/vet/**")
                .hasRole("ADMIN")
                .anyRequest().hasAnyRole("ADMIN", "VET")
                .and()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPepperPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity webSecurity) {
        webSecurity.ignoring().antMatchers("/login");
    }
}
