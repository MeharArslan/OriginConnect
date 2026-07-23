import { Repository } from 'typeorm';
import { User } from '../database/entities/user.entity';
import { Message } from '../database/entities/message.entity';
export declare class AdminService {
    private u;
    private m;
    constructor(u: Repository<User>, m: Repository<Message>);
    stats(): Promise<{
        totalUsers: number;
        activeUsers: number;
        totalMessages: number;
    }>;
    users(page?: number, limit?: number): Promise<{
        users: User[];
        total: number;
        page: number;
        totalPages: number;
    }>;
    disable(id: string): Promise<{
        message: string;
    }>;
    enable(id: string): Promise<{
        message: string;
    }>;
    forceLogout(id: string): Promise<{
        message: string;
    }>;
}
