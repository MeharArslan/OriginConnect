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
exports.MessagesService = void 0;
const common_1 = require("@nestjs/common");
const typeorm_1 = require("@nestjs/typeorm");
const typeorm_2 = require("typeorm");
const message_entity_1 = require("../database/entities/message.entity");
const conversation_entity_1 = require("../database/entities/conversation.entity");
let MessagesService = class MessagesService {
    constructor(msgRepo, convRepo) {
        this.msgRepo = msgRepo;
        this.convRepo = convRepo;
    }
    key(a, b) { return [a, b].sort().join('_'); }
    async getOrCreateConv(p1, p2) {
        const k = this.key(p1, p2);
        let c = await this.convRepo.findOne({ where: { participantKey: k } });
        if (!c) {
            const [id1, id2] = [p1, p2].sort();
            c = await this.convRepo.save({ participantKey: k, participant1Id: id1, participant2Id: id2 });
        }
        return c;
    }
    async sendMessage(senderId, dto) {
        const conv = await this.getOrCreateConv(senderId, dto.receiverId);
        const msg = await this.msgRepo.save({ conversationId: conv.id, senderId, receiverId: dto.receiverId, type: dto.type, content: dto.content, mediaUrl: dto.mediaUrl, mediaThumbnail: dto.mediaThumbnail, mediaSize: dto.mediaSize, mimeType: dto.mimeType, latitude: dto.latitude, longitude: dto.longitude, replyToId: dto.replyToId, status: message_entity_1.MessageStatus.SENT });
        const isP1 = conv.participant1Id === dto.receiverId;
        await this.convRepo.update(conv.id, { lastMessageId: msg.id, lastMessageContent: (dto.content || ('[' + dto.type + ']')).substring(0, 100), lastMessageAt: Date.now(), unreadCount1: isP1 ? conv.unreadCount1 + 1 : conv.unreadCount1, unreadCount2: !isP1 ? conv.unreadCount2 + 1 : conv.unreadCount2 });
        return msg;
    }
    async getConversations(userId) {
        const cs = await this.convRepo.createQueryBuilder('c').where('c.participant1Id=:id OR c.participant2Id=:id', { id: userId }).orderBy('c.lastMessageAt', 'DESC').getMany();
        return cs.map(c => ({ ...c, unreadCount: c.participant1Id === userId ? c.unreadCount1 : c.unreadCount2, otherUserId: c.participant1Id === userId ? c.participant2Id : c.participant1Id }));
    }
    async getMessages(userId, conversationId, before, limit = 30) {
        const conv = await this.convRepo.findOne({ where: { id: conversationId } });
        if (!conv)
            throw new common_1.NotFoundException();
        if (conv.participant1Id !== userId && conv.participant2Id !== userId)
            throw new common_1.ForbiddenException();
        const q = this.msgRepo.createQueryBuilder('m').where('m.conversationId=:cid', { cid: conversationId }).andWhere('m.isDeletedForEveryone=false').orderBy('m.createdAt', 'DESC').take(limit);
        if (before) {
            const ref = await this.msgRepo.findOne({ where: { id: before } });
            if (ref)
                q.andWhere('m.createdAt<:d', { d: ref.createdAt });
        }
        return (await q.getMany()).reverse();
    }
    async markRead(userId, conversationId) {
        const conv = await this.convRepo.findOne({ where: { id: conversationId } });
        if (!conv)
            return;
        await this.msgRepo.createQueryBuilder().update(message_entity_1.Message).set({ status: message_entity_1.MessageStatus.READ, readAt: Date.now() }).where('conversationId=:cid AND receiverId=:uid AND status!=:s', { cid: conversationId, uid: userId, s: message_entity_1.MessageStatus.READ }).execute();
        const isP1 = conv.participant1Id === userId;
        await this.convRepo.update(conversationId, { unreadCount1: isP1 ? 0 : conv.unreadCount1, unreadCount2: !isP1 ? 0 : conv.unreadCount2 });
        return { conversationId };
    }
    async editMessage(userId, msgId, dto) {
        const msg = await this.msgRepo.findOne({ where: { id: msgId } });
        if (!msg)
            throw new common_1.NotFoundException();
        if (msg.senderId !== userId)
            throw new common_1.ForbiddenException();
        await this.msgRepo.update(msgId, { content: dto.content, isEdited: true, editedAt: Date.now() });
        return this.msgRepo.findOne({ where: { id: msgId } });
    }
    async deleteMessage(userId, msgId, forEveryone) {
        const msg = await this.msgRepo.findOne({ where: { id: msgId } });
        if (!msg)
            throw new common_1.NotFoundException();
        if (forEveryone && msg.senderId !== userId)
            throw new common_1.ForbiddenException();
        if (forEveryone)
            await this.msgRepo.update(msgId, { isDeletedForEveryone: true, content: null });
        else
            await this.msgRepo.update(msgId, { isDeleted: true });
        return { deleted: true, forEveryone };
    }
    async reactMessage(userId, msgId, dto) {
        const msg = await this.msgRepo.findOne({ where: { id: msgId } });
        if (!msg)
            throw new common_1.NotFoundException();
        const r = msg.reactions || {};
        const users = r[dto.emoji] || [];
        if (users.includes(userId)) {
            r[dto.emoji] = users.filter((u) => u !== userId);
            if (!r[dto.emoji].length)
                delete r[dto.emoji];
        }
        else
            r[dto.emoji] = [...users, userId];
        await this.msgRepo.update(msgId, { reactions: r });
        return r;
    }
    async starMessage(userId, msgId, star) { const msg = await this.msgRepo.findOne({ where: { id: msgId } }); if (!msg)
        throw new common_1.NotFoundException(); await this.msgRepo.update(msgId, { isStarred: star }); return { starred: star }; }
};
exports.MessagesService = MessagesService;
exports.MessagesService = MessagesService = __decorate([
    (0, common_1.Injectable)(),
    __param(0, (0, typeorm_1.InjectRepository)(message_entity_1.Message)),
    __param(1, (0, typeorm_1.InjectRepository)(conversation_entity_1.Conversation)),
    __metadata("design:paramtypes", [typeorm_2.Repository, typeorm_2.Repository])
], MessagesService);
//# sourceMappingURL=messages.service.js.map