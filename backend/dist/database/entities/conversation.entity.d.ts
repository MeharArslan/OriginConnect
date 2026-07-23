export declare class Conversation {
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
}
