"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __param = (this && this.__param) || function (paramIndex, decorator) {
    return function (target, key) { decorator(target, key, paramIndex); }
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.PresenceGateway = void 0;
const websockets_1 = require("@nestjs/websockets");
const socket_io_1 = require("socket.io");
const jwt_1 = require("@nestjs/jwt");
const config_1 = require("@nestjs/config");
const common_1 = require("@nestjs/common");
let PresenceGateway = class PresenceGateway {
    constructor(jwtService, config) {
        this.jwtService = jwtService;
        this.config = config;
        this.userSockets = new Map();
        this.socketUsers = new Map();
    }
    async handleConnection(client) {
        try {
            const token = client.handshake.auth.token || client.handshake.headers.authorization?.split(' ')[1];
            const p = this.jwtService.verify(token, { secret: this.config.get('JWT_SECRET') });
            client.data.userId = p.sub;
            this.socketUsers.set(client.id, p.sub);
            if (!this.userSockets.has(p.sub))
                this.userSockets.set(p.sub, new Set());
            this.userSockets.get(p.sub).add(client.id);
            client.join('user:' + p.sub);
            this.server.emit('presence:' + p.sub, { userId: p.sub, isOnline: true });
        }
        catch (e) {
            client.disconnect(true);
        }
    }
    async handleDisconnect(client) {
        const userId = this.socketUsers.get(client.id);
        if (!userId)
            return;
        this.socketUsers.delete(client.id);
        const sockets = this.userSockets.get(userId);
        if (sockets) {
            sockets.delete(client.id);
            if (sockets.size === 0) {
                this.userSockets.delete(userId);
                this.server.emit('presence:' + userId, { userId, isOnline: false, lastSeen: Date.now() });
            }
        }
    }
    join(c, d) { c.join('conv:' + d.conversationId); }
    leave(c, d) { c.leave('conv:' + d.conversationId); }
    typing(c, d) { c.to('conv:' + d.conversationId).emit('typing', { userId: c.data.userId, ...d }); }
    emitNewMessage(receiverId, msg) { this.server.to('user:' + receiverId).emit('new_message', msg); }
    emitStatus(userId, data) { this.server.to('user:' + userId).emit('message_status', data); }
    isOnline(userId) { return (this.userSockets.get(userId)?.size ?? 0) > 0; }
};
exports.PresenceGateway = PresenceGateway;
__decorate([
    (0, websockets_1.WebSocketServer)(),
    __metadata("design:type", socket_io_1.Server)
], PresenceGateway.prototype, "server", void 0);
__decorate([
    (0, websockets_1.SubscribeMessage)('join_conversation'),
    __param(0, (0, websockets_1.ConnectedSocket)()),
    __param(1, (0, websockets_1.MessageBody)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [socket_io_1.Socket, Object]),
    __metadata("design:returntype", void 0)
], PresenceGateway.prototype, "join", null);
__decorate([
    (0, websockets_1.SubscribeMessage)('leave_conversation'),
    __param(0, (0, websockets_1.ConnectedSocket)()),
    __param(1, (0, websockets_1.MessageBody)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [socket_io_1.Socket, Object]),
    __metadata("design:returntype", void 0)
], PresenceGateway.prototype, "leave", null);
__decorate([
    (0, websockets_1.SubscribeMessage)('typing'),
    __param(0, (0, websockets_1.ConnectedSocket)()),
    __param(1, (0, websockets_1.MessageBody)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [socket_io_1.Socket, Object]),
    __metadata("design:returntype", void 0)
], PresenceGateway.prototype, "typing", null);
exports.PresenceGateway = PresenceGateway = __decorate([
    (0, common_1.Injectable)(),
    (0, websockets_1.WebSocketGateway)({ cors: { origin: '*' }, namespace: '/connect' }),
    __metadata("design:paramtypes", [jwt_1.JwtService, config_1.ConfigService])
], PresenceGateway);
//# sourceMappingURL=presence.gateway.js.map