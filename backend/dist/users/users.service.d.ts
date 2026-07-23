import { Repository } from 'typeorm';
import { User } from '../database/entities/user.entity';
import { UpdateProfileDto } from './dto/update-profile.dto';
export declare class UsersService {
    private userRepo;
    constructor(userRepo: Repository<User>);
    getProfile(id: string): Promise<any>;
    updateProfile(id: string, dto: UpdateProfileDto): Promise<any>;
    getUsersByPhones(phones: string[]): Promise<User[]>;
    getUserById(id: string): Promise<User>;
    setOnlineStatus(id: string, isOnline: boolean): Promise<void>;
    sanitize(user: User): any;
    sanitizeForOther(user: User, isContact: boolean): any;
}
