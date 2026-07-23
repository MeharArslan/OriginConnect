import { Repository } from 'typeorm';
import { Message, MessageStatus } from '../database/entities/message.entity';
import { Conversation } from '../database/entities/conversation.entity';
import { SendMessageDto, EditMessageDto, ReactMessageDto } from './dto/message.dto';
export declare class MessagesService {
    private msgRepo;
    private convRepo;
    constructor(msgRepo: Repository<Message>, convRepo: Repository<Conversation>);
    private key;
    getOrCreateConv(p1: string, p2: string): Promise<Conversation>;
    sendMessage(senderId: string, dto: SendMessageDto): Promise<{
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
        status: MessageStatus.SENT;
    } & Message>;
    getConversations(userId: string): Promise<{
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
    getMessages(userId: string, conversationId: string, before?: string, limit?: number): Promise<Message[]>;
    markRead(userId: string, conversationId: string): Promise<{
        conversationId: string;
    }>;
    editMessage(userId: string, msgId: string, dto: EditMessageDto): Promise<Message>;
    deleteMessage(userId: string, msgId: string, forEveryone: boolean): Promise<{
        deleted: boolean;
        forEveryone: boolean;
    }>;
    reactMessage(userId: string, msgId: string, dto: ReactMessageDto): Promise<Record<string, string[]>>;
    starMessage(userId: string, msgId: string, star: boolean): Promise<{
        starred: boolean;
    }>;
}
