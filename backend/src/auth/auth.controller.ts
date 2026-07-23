import { Controller, Post, Body, UseGuards, Get, Req, HttpCode } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';
import { AuthService } from './auth.service';
import { SendOtpDto, VerifyOtpDto, RefreshTokenDto } from './dto/auth.dto';
@Controller('auth')
export class AuthController {
  constructor(private authService: AuthService) {}
  @Post('send-otp') @HttpCode(200) sendOtp(@Body() dto: SendOtpDto) { return this.authService.sendOtp(dto); }
  @Post('verify-otp') @HttpCode(200) verifyOtp(@Body() dto: VerifyOtpDto) { return this.authService.verifyOtp(dto); }
  @Post('refresh') @HttpCode(200) refresh(@Body() dto: RefreshTokenDto, @Req() req) {
    const p=JSON.parse(Buffer.from((req.headers.authorization?.split(' ')[1]||'..').split('.')[1],'base64').toString());
    return this.authService.refreshTokens(p.sub,dto.refreshToken);
  }
  @Post('logout') @UseGuards(AuthGuard('jwt')) @HttpCode(200) logout(@Req() req) { return this.authService.logout(req.user.id); }
  @Get('me') @UseGuards(AuthGuard('jwt')) me(@Req() req) { return this.authService.sanitize(req.user); }
}
