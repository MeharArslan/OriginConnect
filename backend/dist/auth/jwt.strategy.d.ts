import { Strategy } from 'passport-jwt';
import { Repository } from 'typeorm';
import { ConfigService } from '@nestjs/config';
import { User } from '../database/entities/user.entity';
declare const JwtStrategy_base: new (...args: any[]) => Strategy;
export declare class JwtStrategy extends JwtStrategy_base {
    private userRepo;
    constructor(userRepo: Repository<User>, config: ConfigService);
    validate(payload: {
        sub: string;
    }): Promise<User>;
}
export {};
