import { Injectable, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { ConfigService } from '@nestjs/config';
import * as bcrypt from 'bcryptjs';
import { User } from '../database/entities/user.entity';
import { OtpService } from './otp.service';
import { SendOtpDto, VerifyOtpDto } from './dto/auth.dto';
@Injectable()
export class AuthService {
  constructor(@InjectRepository(User) private userRepo: Repository<User>, private jwtService: JwtService, private otpService: OtpService, private config: ConfigService) {}
  async sendOtp(dto: SendOtpDto) { const code=await this.otpService.generate(dto.phone); const mock=this.config.get('SMS_PROVIDER','mock')==='mock'; return {message:'OTP sent',...(mock&&{code})}; }
  async verifyOtp(dto: VerifyOtpDto) {
    await this.otpService.verify(dto.phone, dto.code);
    let user=await this.userRepo.findOne({where:{phone:dto.phone}}); const isNewUser=!user;
    if(!user) user=await this.userRepo.save({phone:dto.phone,displayName:dto.phone,lastSeen:Date.now()});
    else await this.userRepo.update(user.id,{lastSeen:Date.now()});
    const tokens=await this.generateTokens(user);
    await this.userRepo.update(user.id,{refreshToken:await bcrypt.hash(tokens.refreshToken,10)});
    return {...tokens,isNewUser,user:this.sanitize(user)};
  }
  async refreshTokens(userId: string, rt: string) {
    const user=await this.userRepo.findOne({where:{id:userId}});
    if(!user?.refreshToken) throw new UnauthorizedException();
    if(!(await bcrypt.compare(rt,user.refreshToken))) throw new UnauthorizedException();
    const tokens=await this.generateTokens(user);
    await this.userRepo.update(user.id,{refreshToken:await bcrypt.hash(tokens.refreshToken,10)});
    return tokens;
  }
  async logout(userId: string) { await this.userRepo.update(userId,{refreshToken:null,isOnline:false}); return {message:'Logged out'}; }
  private async generateTokens(user: User) {
    const p={sub:user.id,phone:user.phone};
    const [accessToken,refreshToken]=await Promise.all([this.jwtService.signAsync(p),this.jwtService.signAsync(p,{secret:this.config.get('JWT_REFRESH_SECRET'),expiresIn:this.config.get('JWT_REFRESH_EXPIRES_IN','30d')})]);
    return {accessToken,refreshToken};
  }
  sanitize(user: User) { const {refreshToken,fcmToken,...safe}=user as any; return safe; }
}
