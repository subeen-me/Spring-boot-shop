package com.shop.config;

import com.shop.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    MemberService memberService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin()
                .loginPage("/members/login")
                .defaultSuccessUrl("/")
                .usernameParameter("email")
                .failureUrl("/members/login/error")
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))
                .logoutSuccessUrl("/");

        http.authorizeRequests() //시큐리티 처리에 HttpServletRequest를 이용한다
                .mvcMatchers("/", "/members/**",
                        "/item/**", "/images/**").permitAll() //모든 사용자가 인증(로그인)없이 경로에 접근할 수 있는 곳들 설정
                .mvcMatchers("/admin/**").hasRole("ADMIN") //해당 계정이 ADMIN Role일때만 접근가능하도록 설정
                .anyRequest().authenticated(); //위에서 설정한 경로 이외의 나머지 경로들은 모두 인증 요구하도록 설정

        http.exceptionHandling()
                .authenticationEntryPoint
                        (new CustomAuthenticationEntryPoint()); //인증되지 않은 사용자가 리소스에 접근하였을 때 수행되는 핸들러 등록
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(memberService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    public void configure(WebSecurity web) throws Exception { //static 디렉터리의 하위 파일들은 인증을 무시하도록 설정
        web.ignoring().antMatchers("/css/**", "/js/**", "/img/**");
    }
}
