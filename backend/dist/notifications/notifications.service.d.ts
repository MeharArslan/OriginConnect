import { ConfigService } from '@nestjs/config';
export declare class NotificationsService {
    private config;
    private readonly logger;
    private app;
    constructor(config: ConfigService);
    private init;
    sendMessage(token: string, d: {
        senderId: string;
        senderName: string;
        senderPhoto?: string;
        conversationId: string;
        messageId: string;
        messageType: string;
        content?: string;
    }): Promise<void>;
}
