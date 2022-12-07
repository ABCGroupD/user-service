package com.profatar.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ProfileDTO {
    private String fullName;
    private String emailAddress;
    private String phoneNumber;
    private String imageURL;
}
