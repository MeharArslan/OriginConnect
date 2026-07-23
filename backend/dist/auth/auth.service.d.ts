import { JwtService } from '@nestjs/jwt';
import { Repository } from 'typeorm';
import { ConfigService } from '@nestjs/config';
import { User } from '../database/entities/user.entity';
import { OtpService } from './otp.service';
import { SendOtpDto, VerifyOtpDto } from './dto/auth.dto';
export declare class AuthService {
    private userRepo;
    private jwtService;
    private otpService;
    private config;
    constructor(userRepo: Repository<User>, jwtService: JwtService, otpService: OtpService, config: ConfigService);
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
    refreshTokens(userId: string, rt: string): Promise<{
        accessToken: string;
        refreshToken: string;
    }>;
    logout(userId: string): Promise<{
        message: string;
    }>;
    private generateTokens;
    sanitize(user: User): any;
}
