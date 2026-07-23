import { AuthService } from './auth.service';
import { SendOtpDto, VerifyOtpDto, RefreshTokenDto } from './dto/auth.dto';
export declare class AuthController {
    private authService;
    constructor(authService: AuthService);
    sendOtp(dto: SendOtpDto): Promise<{
        code: string;
        message: string;
    }>;
    verifyOtp(dto: VerifyOtpDto): Promise<{
        isNewUser: boolean;
        user: any;
        accessToken: string;
        refreshToken: string;
    }>;
    refresh(dto: RefreshTokenDto, req: any): Promise<{
        accessToken: string;
        refreshToken: string;
    }>;
    logout(req: any): Promise<{
        message: string;
    }>;
    me(req: any): any;
}
