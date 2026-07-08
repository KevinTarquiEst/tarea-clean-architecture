package org.cat.usercleanarchitecture.infraestructure.adapters.input.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private String firstName;
    private String lastName;
}
