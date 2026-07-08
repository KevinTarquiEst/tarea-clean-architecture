package org.cat.usercleanarchitecture.infraestructure.adapters.input.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
}
