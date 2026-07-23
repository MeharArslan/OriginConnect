import { Repository } from 'typeorm';
import { ConfigService } from '@nestjs/config';
import { Otp } from '../database/entities/otp.entity';
export declare class OtpService {
    private otpRepo;
    private config;
    constructor(otpRepo: Repository<Otp>, config: ConfigService);
    generate(phone: string): Promise<string>;
    verify(phone: string, code: string): Promise<boolean>;
}
