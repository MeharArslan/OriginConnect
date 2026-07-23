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
exports.ContactsService = void 0;
const common_1 = require("@nestjs/common");
const typeorm_1 = require("@nestjs/typeorm");
const typeorm_2 = require("typeorm");
const contact_entity_1 = require("../database/entities/contact.entity");
const user_entity_1 = require("../database/entities/user.entity");
const users_service_1 = require("../users/users.service");
let ContactsService = class ContactsService {
    constructor(contactRepo, userRepo, usersService) {
        this.contactRepo = contactRepo;
        this.userRepo = userRepo;
        this.usersService = usersService;
    }
    async syncContacts(ownerId, dto) {
        const users = (await this.userRepo.find({ where: { phone: (0, typeorm_2.In)(dto.phones), isActive: true } })).filter(u => u.id !== ownerId);
        for (const u of users)
            await this.contactRepo.createQueryBuilder().insert().into(contact_entity_1.Contact).values({ ownerId, contactUserId: u.id }).orIgnore().execute();
        return { registeredCount: users.length, totalSynced: dto.phones.length, contacts: await this.getContacts(ownerId) };
    }
    async getContacts(ownerId) {
        const cs = await this.contactRepo.find({ where: { ownerId, isBlocked: false } });
        if (!cs.length)
            return [];
        const users = await this.userRepo.find({ where: { id: (0, typeorm_2.In)(cs.map(c => c.contactUserId)), isActive: true } });
        const map = new Map(users.map(u => [u.id, u]));
        return cs.map(c => { const u = map.get(c.contactUserId); if (!u)
            return null; return { contactId: c.id, nickname: c.nickname, user: this.usersService.sanitizeForOther(u, true) }; }).filter(Boolean);
    }
    async isContact(ownerId, targetId) { return !!(await this.contactRepo.findOne({ where: { ownerId, contactUserId: targetId, isBlocked: false } })); }
    async blockContact(ownerId, id) { await this.contactRepo.update({ ownerId, contactUserId: id }, { isBlocked: true }); return { message: 'Blocked' }; }
    async unblockContact(ownerId, id) { await this.contactRepo.update({ ownerId, contactUserId: id }, { isBlocked: false }); return { message: 'Unblocked' }; }
};
exports.ContactsService = ContactsService;
exports.ContactsService = ContactsService = __decorate([
    (0, common_1.Injectable)(),
    __param(0, (0, typeorm_1.InjectRepository)(contact_entity_1.Contact)),
    __param(1, (0, typeorm_1.InjectRepository)(user_entity_1.User)),
    __metadata("design:paramtypes", [typeorm_2.Repository, typeorm_2.Repository, users_service_1.UsersService])
], ContactsService);
//# sourceMappingURL=contacts.service.js.map