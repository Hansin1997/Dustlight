package cn.dustlight.oauth2.uim.configurations;

import cn.dustlight.oauth2.uim.handlers.UimHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.util.Arrays;
import java.util.logging.Logger;

@EnableWebSecurity
public class UimWebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    public UimProperties uimProperties;

    @Autowired
    public UimHandler uimHandler;

    @Bean
    @ConditionalOnMissingBean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        String[] publicPaths = uimProperties.getPublicPaths();
        Logger.getLogger(getClass().getName()).info("Public paths: " + Arrays.toString(publicPaths));

        http.authorizeRequests()
                .antMatchers(publicPaths).permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(((httpServletRequest, httpServletResponse, e) -> uimHandler.handleAuthenticationEntryPoint(httpServletRequest, httpServletResponse, e)))
                .accessDeniedHandler(((httpServletRequest, httpServletResponse, e) -> uimHandler.handleAccessDenied(httpServletRequest, httpServletResponse, e)))
                .and()
                .formLogin()
                .loginPage(uimProperties.getFormLogin().getLoginPage())
                .loginProcessingUrl(uimProperties.getFormLogin().getLoginProcessingUrl())
                .successHandler(((httpServletRequest, httpServletResponse, authentication) -> uimHandler.handleSignInSuccess(httpServletRequest, httpServletResponse, authentication)))
                .failureHandler(((httpServletRequest, httpServletResponse, e) -> uimHandler.handleSignInFail(httpServletRequest, httpServletResponse, e)))
                .usernameParameter(uimProperties.getFormLogin().getUsernameParameter())
                .passwordParameter(uimProperties.getFormLogin().getPasswordParameter())
                .permitAll()
                .and()
                .logout()
                .logoutUrl(uimProperties.getLogout().getLogoutUrl())
                .deleteCookies(uimProperties.getLogout().getDeleteCookies())
                .logoutSuccessHandler(((httpServletRequest, httpServletResponse, authentication) -> uimHandler.handleLogout(httpServletRequest, httpServletResponse, authentication)))
                .permitAll()
                .and()
        ;

        if (uimProperties.isCsrfEnabled())
            http.csrf();
        else
            http.csrf().disable();
    }

}