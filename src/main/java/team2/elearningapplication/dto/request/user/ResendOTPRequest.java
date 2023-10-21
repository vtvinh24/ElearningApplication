package team2.elearningapplication.dto.request.user;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResendOTPRequest {
    @NotBlank
    private String email;
}
