package com.faust0z.BookLibraryAPI.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    @Schema(description = "The JWT token", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huMzQ3LmRvZUBleGFtcGxlLmNvbSIsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJpYXQiOjE3NzAxMzA0MDMsImV4cCI6MTc3MDEzNDAwM30.SL1qYD7BRzWlOc1owB0_YpUHgiRSYkvqPHNJW9KiKJJltJDCcyEZCqt44y47TMtObNBvaFYwfiHbfOxh-_hfJA")
    private String accessToken;

    @Schema(description = "The user", example = """
                {
                    "id": "72431e00-b7b9-4a34-800a-0d0d4215a705",
                    "name": "example name",
                    "email": "john943.doe@example.com"
                }\
            """)
    private UserDTO user;
}