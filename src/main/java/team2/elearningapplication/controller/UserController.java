package team2.elearningapplication.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team2.elearningapplication.Enum.EnumUserStatus;
import team2.elearningapplication.Enum.ResponseCode;
import team2.elearningapplication.dto.common.ResponseCommon;
import team2.elearningapplication.dto.request.admin.quiz.GetQuizByIdRequest;
import team2.elearningapplication.dto.request.user.*;
import team2.elearningapplication.dto.response.admin.quiz.GetQuizByIdResponse;
import team2.elearningapplication.dto.response.user.*;
import team2.elearningapplication.entity.User;
import team2.elearningapplication.security.UserDetailsImpl;
import team2.elearningapplication.security.jwt.JWTResponse;
import team2.elearningapplication.security.jwt.JWTUtils;
import team2.elearningapplication.service.IUserService;

import javax.validation.Valid;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/user")
@AllArgsConstructor
@Log4j2
public class UserController {

    private IUserService userService;
    private final JWTUtils jwtUtils;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    //    @Operation(
//            security = @SecurityRequirement(name = "bearerAuth")
//    )
    @PostMapping("/register")
    public ResponseEntity<ResponseCommon<CreateUserResponseDTO>> createUser(@Valid @RequestBody CreateUserRequest requestDTO) {
        log.debug("Handle request create user with email{}",requestDTO.getEmail());
        ResponseCommon<CreateUserResponseDTO> responseDTO = userService.createUser(requestDTO);
        if (responseDTO != null) {
            return ResponseEntity.ok(responseDTO);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }


    @PostMapping("/verify-otp")
    public ResponseEntity<ResponseCommon<VerifyOtpResponse>> verifyOtp(@Valid @RequestBody  VerifyOtpRequest request) {
        log.debug("Handle verify otp with id{}", request.getEmail());
        User user = userService.getUserByUsername(userService.genUserFromEmail(request.getEmail()));

        ResponseCommon<VerifyOtpResponse> response = userService.verifyOtp(request);
        // if response code == 0 -> return success
        if(response.getCode()==ResponseCode.SUCCESS.getCode()){
            user.setStatus(EnumUserStatus.ACTIVE);
            userService.updateUser(user);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/getOTP")
    public ResponseEntity<ResponseCommon<GetOTPResponse>> getOTP(@Valid @RequestBody GetOTPRequest request) {
        ResponseCommon<GetOTPResponse> responseDTO = userService.getOtp(request);
        // if responseDTO not null -> return responseDTO
        if (responseDTO != null) return ResponseEntity.ok(responseDTO);
            // else -> return badrequest
        else return ResponseEntity.badRequest().build();
    }
    @PostMapping("/login")
    public ResponseEntity<ResponseCommon<JWTResponse>> login(@RequestBody LoginRequest loginRequest) {
        try {
            ResponseCommon<JWTResponse> response = userService.login(loginRequest);

            if (response.getCode() == 0) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


    @PostMapping("/send-otp-forgot-password")
    public ResponseEntity<ResponseCommon<ForgotPasswordResponse>> sendOTPForgotPass(@Valid @RequestBody SendOTPForgotPasswordRequest sendOTPForgotPasswordRequest) {
        User user = userService.getUserByUsername(userService.genUserFromEmail(sendOTPForgotPasswordRequest.getEmail()));
        // if user is null -> tell error
        if (Objects.isNull(user))
            return new ResponseEntity<>(new ResponseCommon<>(ResponseCode.USER_NOT_FOUND, null), HttpStatus.BAD_REQUEST);
        else {
            GetOTPRequest request = new GetOTPRequest(sendOTPForgotPasswordRequest.getEmail(), false);
            userService.getOtp(request);
            return new ResponseEntity<>(new ResponseCommon<>(ResponseCode.SUCCESS, null), HttpStatus.OK);
        }
    }

    @PostMapping("/verify-otp-forgotPass")
    public ResponseEntity<ResponseCommon<VerifyOtpResponse>> verifyOtpForgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {

        User user = userService.getUserByUsername(userService.genUserFromEmail(forgotPasswordRequest.getEmail()));
        // if user is null -> tell error
        if(Objects.isNull(user)){
            return new ResponseEntity<>(new ResponseCommon<>(ResponseCode.USER_NOT_FOUND,null),HttpStatus.BAD_REQUEST);
        }
        VerifyOtpRequest request = new VerifyOtpRequest(forgotPasswordRequest.getOtp(), user.getEmail());
        ResponseCommon<VerifyOtpResponse> response = userService.verifyOtp(request);
        // if response code == 0 -> return success
        if(response.getCode()==0){
            user.setPassword(forgotPasswordRequest.getPassword());
            userService.updateUser(user);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/changePassword")
    public ResponseEntity<ResponseCommon<ChangePasswordResponse>> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest){
        ResponseCommon<ChangePasswordResponse> response = userService.changePassword(changePasswordRequest);
        // if response equals success -> return response
        if(response.getCode() == 0){
            return ResponseEntity.ok(response);
        } else return ResponseEntity.badRequest().build();
    }

    @PostMapping("/refresh-access-token")
    public ResponseEntity<JWTResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        User user = userService.getUserByUsername(userService.genUserFromEmail(request.getEmail()));

        if (refreshToken.isEmpty() || refreshToken == null) {
            return ResponseEntity.badRequest().body(null);
        } else {
            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            String accessToken = jwtUtils.generateAccessToken(userDetails);
            String newRefreshToken = jwtUtils.generateRefreshToken(userDetails);
            JWTResponse response = new JWTResponse();
            response.setAccessToken(accessToken);
            response.setRefreshToken(newRefreshToken);
            return ResponseEntity.ok(response);
        }
    }


    @PutMapping("/change-profile")
    public ResponseEntity<ResponseCommon<ChangeProfileResponse>> changeProfile(@Valid @RequestBody ChangeProfileRequest changeProfileRequest) {
        try {
            ResponseCommon<ChangeProfileResponse> response = userService.changeProfile(changeProfileRequest);

            if (response.getCode() == ResponseCode.SUCCESS.getCode()) {
                logger.info("Change profile success");
                return ResponseEntity.ok(response);
            } else if (response.getCode() == ResponseCode.USER_NOT_FOUND.getCode()) {
                logger.error("User not found");
                return ResponseEntity.status(404).body(new ResponseCommon<>(response.getCode(), "User not found", null));
            } else if (response.getCode() == ResponseCode.INVALID_DATA.getCode()) {
                logger.error("Invalid data");
                return ResponseEntity.badRequest().body(new ResponseCommon<>(response.getCode(), "Invalid data", null));
            } else {
                logger.error("Change profile failed");
                return ResponseEntity.badRequest().body(new ResponseCommon<>(ResponseCode.FAIL.getCode(), "Change profile failed", null));
            }
        } catch (Exception e) {
            logger.error("An error occurred while changing profile", e);
            return ResponseEntity.badRequest().body(new ResponseCommon<>(ResponseCode.FAIL.getCode(), "An error occurred while changing profile", null));
        }
    }

    @GetMapping("/get-user-by-email")
    public ResponseEntity<ResponseCommon<GetUserByEmailResponse>> getUserByEmail(GetUserByEmailRequest request) {
        ResponseCommon<GetUserByEmailResponse> response = userService.getUserByEmail(request);
        if (response.getCode() == ResponseCode.SUCCESS.getCode()) {
            log.debug("Get user by email successfully.");
            return ResponseEntity.ok(response);
        } else if (response.getCode() == ResponseCode.USER_NOT_FOUND.getCode()) {
            log.debug("User not exist.");
            return ResponseEntity.badRequest().body(new ResponseCommon<>(response.getCode(), "User not exist", null));
        } else {
            log.error("Get user by email failed");
            return ResponseEntity.badRequest().body(new ResponseCommon<>(ResponseCode.FAIL.getCode(), "Get user by email failed", null));
        }
    }
}
