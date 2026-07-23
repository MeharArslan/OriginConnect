import { IsString, Length, IsNotEmpty } from 'class-validator';
export class SendOtpDto { @IsNotEmpty() @IsString() phone: string; }
export class VerifyOtpDto { @IsNotEmpty() @IsString() phone: string; @IsNotEmpty() @IsString() @Length(6,6) code: string; }
export class RefreshTokenDto { @IsNotEmpty() @IsString() refreshToken: string; }
