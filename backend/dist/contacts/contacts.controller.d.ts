import { ContactsService } from './contacts.service';
import { SyncContactsDto } from './dto/sync-contacts.dto';
export declare class ContactsController {
    private contactsService;
    constructor(contactsService: ContactsService);
    sync(req: any, dto: SyncContactsDto): Promise<{
        registeredCount: number;
        totalSynced: number;
        contacts: {
            contactId: string;
            nickname: string;
            user: any;
        }[];
    }>;
    list(req: any): Promise<{
        contactId: string;
        nickname: string;
        user: any;
    }[]>;
    block(req: any, id: string): Promise<{
        message: string;
    }>;
    unblock(req: any, id: string): Promise<{
        message: string;
    }>;
}
