export declare enum MessageType {
    TEXT = "text",
    IMAGE = "image",
    VIDEO = "video",
    AUDIO = "audio",
    DOCUMENT = "document",
    LOCATION = "location"
}
export declare enum MessageStatus {
    SENDING = "sending",
    SENT = "sent",
    DELIVERED = "delivered",
    READ = "read",
    FAILED = "failed"
}
export declare class Message {
    id: string;
    conversationId: string;
    senderId: string;
    receiverId: string;
    type: MessageType;
    content: string;
    mediaUrl: string;
    mediaThumbnail: string;
    mediaSize: number;
    mimeType: string;
    latitude: number;
    longitude: number;
    replyToId: string;
    reactions: Record<string, string[]>;
    status: MessageStatus;
    isDeleted: boolean;
    isDeletedForEveryone: boolean;
    isEdited: boolean;
    isStarred: boolean;
    deliveredAt: number;
    readAt: number;
    editedAt: number;
    createdAt: Date;
}
