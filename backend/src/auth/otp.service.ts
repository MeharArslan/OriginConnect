import { Injectable, BadRequestException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { ConfigService } from '@nestjs/config';
import { Otp } from '../database/entities/otp.entity';
@Injectable()
export class OtpService {
  constructor(@InjectRepository(Otp) private otpRepo: Repository<Otp>, private config: ConfigService) {}
  async generate(phone: string): Promise<string> {
    await this.otpRepo.update({ phone, isUsed: false }, { isUsed: true });
    const code = Math.floor(100000 + Math.random() * 900000).toString();
    await this.otpRepo.save({ phone, code, expiresAt: Date.now() + 600000 });
    if (this.config.get('SMS_PROVIDER','mock')==='mock') console.log('[OTP] '+phone+' -> '+code);
    return code;
  }
  async verify(phone: string, code: string): Promise<boolean> {
    const otp = await this.otpRepo.findOne({ where:{phone,code,isUsed:false}, order:{createdAt:'DESC'} });
    if (!otp) throw new BadRequestException('Invalid OTP');
    if (Date.now() > Number(otp.expiresAt)) throw new BadRequestException('OTP expired');
    await this.otpRepo.update(otp.id, { isUsed: true });
    return true;
  }
}
