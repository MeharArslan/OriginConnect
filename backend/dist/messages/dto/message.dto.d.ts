import { MessageType } from '../../database/entities/message.entity';
export declare class SendMessageDto {
    receiverId: string;
    type: MessageType;
    content?: string;
    mediaUrl?: string;
    mediaThumbnail?: string;
    mediaSize?: number;
    mimeType?: string;
    latitude?: number;
    longitude?: number;
    replyToId?: string;
}
export declare class EditMessageDto {
    content: string;
}
export declare class ReactMessageDto {
    emoji: string;
}
