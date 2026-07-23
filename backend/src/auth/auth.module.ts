import { Module } from '@nestjs/common';
import { JwtModule } from '@nestjs/jwt';
import { PassportModule } from '@nestjs/passport';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { AuthController } from './auth.controller';
import { AuthService } from './auth.service';
import { OtpService } from './otp.service';
import { JwtStrategy } from './jwt.strategy';
import { User } from '../database/entities/user.entity';
import { Otp } from '../database/entities/otp.entity';
@Module({
  imports:[TypeOrmModule.forFeature([User,Otp]),PassportModule.register({defaultStrategy:'jwt'}),JwtModule.registerAsync({imports:[ConfigModule],inject:[ConfigService],useFactory:(c:ConfigService)=>({secret:c.get('JWT_SECRET'),signOptions:{expiresIn:c.get('JWT_EXPIRES_IN','15m')}})})],
  controllers:[AuthController],providers:[AuthService,OtpService,JwtStrategy],exports:[AuthService,JwtModule]
})
export class AuthModule {}
