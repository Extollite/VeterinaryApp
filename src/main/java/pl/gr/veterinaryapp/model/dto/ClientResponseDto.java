package pl.gr.veterinaryapp.model.dto;

import lombok.Data;

@Data
public class ClientResponseDto {

    private long id;
    private String name;
    private String surname;
    private String username;
}
