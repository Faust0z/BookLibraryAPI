package com.faust0z.BookLibraryAPI.config;

import com.faust0z.BookLibraryAPI.dto.LoanDTO;
import com.faust0z.BookLibraryAPI.entity.LoanEntity;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setMatchingStrategy(MatchingStrategies.STANDARD);

        modelMapper.createTypeMap(LoanEntity.class, LoanDTO.class)
                .addMappings(mapper -> {
                    mapper.map(src -> src.getUser().getId(), LoanDTO::setUserId);
                    mapper.map(src -> src.getBook().getId(), LoanDTO::setBookId);
                    mapper.map(src -> src.getBook().getName(), LoanDTO::setBookName);
                }); //TODO: this crashes when returning Loans but actually returns them

        return modelMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}