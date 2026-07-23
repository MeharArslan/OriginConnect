import { Repository } from 'typeorm';
import { Contact } from '../database/entities/contact.entity';
import { User } from '../database/entities/user.entity';
import { UsersService } from '../users/users.service';
import { SyncContactsDto } from './dto/sync-contacts.dto';
export declare class ContactsService {
    private contactRepo;
    private userRepo;
    private usersService;
    constructor(contactRepo: Repository<Contact>, userRepo: Repository<User>, usersService: UsersService);
    syncContacts(ownerId: string, dto: SyncContactsDto): Promise<{
        registeredCount: number;
        totalSynced: number;
        contacts: {
            contactId: string;
            nickname: string;
            user: any;
        }[];
    }>;
    getContacts(ownerId: string): Promise<{
        contactId: string;
        nickname: string;
        user: any;
    }[]>;
    isContact(ownerId: string, targetId: string): Promise<boolean>;
    blockContact(ownerId: string, id: string): Promise<{
        message: string;
    }>;
    unblockContact(ownerId: string, id: string): Promise<{
        message: string;
    }>;
}
