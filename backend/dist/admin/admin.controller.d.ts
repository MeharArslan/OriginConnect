import { AdminService } from './admin.service';
export declare class AdminController {
    private adminService;
    constructor(adminService: AdminService);
    stats(): Promise<{
        totalUsers: number;
        activeUsers: number;
        totalMessages: number;
    }>;
    users(p?: string, l?: string): Promise<{
        users: import("../database/entities/user.entity").User[];
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
    fl(id: string): Promise<{
        message: string;
    }>;
}
