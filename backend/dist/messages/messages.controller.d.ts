import { MessagesService } from './messages.service';
import { SendMessageDto, EditMessageDto, ReactMessageDto } from './dto/message.dto';
export declare class MessagesController {
    private messagesService;
    constructor(messagesService: MessagesService);
    convs(req: any): Promise<{
        unreadCount: number;
        otherUserId: string;
        id: string;
        participantKey: string;
        participant1Id: string;
        participant2Id: string;
        lastMessageId: string;
        lastMessageContent: string;
        lastMessageAt: number;
        unreadCount1: number;
        unreadCount2: number;
        isBlocked: boolean;
        blockedBy: string;
        createdAt: Date;
        updatedAt: Date;
    }[]>;
    send(req: any, dto: SendMessageDto): Promise<{
        conversationId: string;
        senderId: string;
        receiverId: string;
        type: import("../database/entities/message.entity").MessageType;
        content: string;
        mediaUrl: string;
        mediaThumbnail: string;
        mediaSize: number;
        mimeType: string;
        latitude: number;
        longitude: number;
        replyToId: string;
        status: import("../database/entities/message.entity").MessageStatus.SENT;
    } & import("../database/entities/message.entity").Message>;
    msgs(req: any, id: string, b?: string): Promise<import("../database/entities/message.entity").Message[]>;
    read(req: any, id: string): Promise<{
        conversationId: string;
    }>;
    edit(req: any, id: string, dto: EditMessageDto): Promise<import("../database/entities/message.entity").Message>;
    del(req: any, id: string, fe: string): Promise<{
        deleted: boolean;
        forEveryone: boolean;
    }>;
    react(req: any, id: string, dto: ReactMessageDto): Promise<Record<string, string[]>>;
    star(req: any, id: string, s: boolean): Promise<{
        starred: boolean;
    }>;
}
