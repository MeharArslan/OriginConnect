import { UsersService } from './users.service';
import { UpdateProfileDto } from './dto/update-profile.dto';
export declare class UsersController {
    private usersService;
    constructor(usersService: UsersService);
    profile(req: any): Promise<any>;
    update(req: any, dto: UpdateProfileDto): Promise<any>;
    getUser(id: string): Promise<import("../database/entities/user.entity").User>;
}
