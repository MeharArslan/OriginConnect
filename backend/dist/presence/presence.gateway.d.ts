import { OnGatewayConnection, OnGatewayDisconnect } from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';
import { JwtService } from '@nestjs/jwt';
import { ConfigService } from '@nestjs/config';
export declare class PresenceGateway implements OnGatewayConnection, OnGatewayDisconnect {
    private jwtService;
    private config;
    server: Server;
    private userSockets;
    private socketUsers;
    constructor(jwtService: JwtService, config: ConfigService);
    handleConnection(client: Socket): Promise<void>;
    handleDisconnect(client: Socket): Promise<void>;
    join(c: Socket, d: {
        conversationId: string;
    }): void;
    leave(c: Socket, d: {
        conversationId: string;
    }): void;
    typing(c: Socket, d: {
        conversationId: string;
        isTyping: boolean;
    }): void;
    emitNewMessage(receiverId: string, msg: any): void;
    emitStatus(userId: string, data: any): void;
    isOnline(userId: string): boolean;
}
