package com.example.em_card_service.config;

import com.example.em_card_service.data.properties.EncryptionProperties;
import com.example.em_card_service.util.DelegatingTextEncryptor;
import com.example.em_card_service.util.FixedIVGenerator;
import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.salt.StringFixedSaltGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.HexFormat;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final EncryptionProperties encryptionProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
        return httpSecurity.authorizeHttpRequests(matcherRegistry -> matcherRegistry
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .csrf(CsrfConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthoritiesClaimName("roles");
        converter.setAuthorityPrefix("");
        return converter;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(JwtGrantedAuthoritiesConverter authoritiesConverter) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    @Bean
    public TextEncryptor textEncryptor() {
        StringEncryptor encryptor = jasyptEncryptor();
        return DelegatingTextEncryptor.of(encryptor::encrypt, encryptor::decrypt);
        //        return Encryptors.text(encryptionProperties.getPassword(), encryptionProperties.getSalt());
    }

    private StandardPBEStringEncryptor jasyptEncryptor() {
        byte[] saltBytes = HexFormat.of().parseHex(encryptionProperties.getSalt());

        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(encryptionProperties.getPassword());
        encryptor.setAlgorithm("PBEWithHMACSHA512AndAES_256");
        encryptor.setSaltGenerator(new StringFixedSaltGenerator(encryptionProperties.getSalt()));
        encryptor.setIvGenerator(new FixedIVGenerator(saltBytes));
        return encryptor;
    }
}
