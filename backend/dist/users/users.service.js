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
exports.UsersService = void 0;
const common_1 = require("@nestjs/common");
const typeorm_1 = require("@nestjs/typeorm");
const typeorm_2 = require("typeorm");
const user_entity_1 = require("../database/entities/user.entity");
let UsersService = class UsersService {
    constructor(userRepo) {
        this.userRepo = userRepo;
    }
    async getProfile(id) { const u = await this.userRepo.findOne({ where: { id } }); if (!u)
        throw new common_1.NotFoundException(); return this.sanitize(u); }
    async updateProfile(id, dto) { await this.userRepo.update(id, dto); return this.getProfile(id); }
    async getUsersByPhones(phones) { if (!phones.length)
        return []; return this.userRepo.find({ where: { phone: (0, typeorm_2.In)(phones), isActive: true } }); }
    async getUserById(id) { const u = await this.userRepo.findOne({ where: { id, isActive: true } }); if (!u)
        throw new common_1.NotFoundException(); return u; }
    async setOnlineStatus(id, isOnline) { await this.userRepo.update(id, { isOnline, ...(!isOnline && { lastSeen: Date.now() }) }); }
    sanitize(user) { const { refreshToken, fcmToken, ...safe } = user; return safe; }
    sanitizeForOther(user, isContact) {
        const b = this.sanitize(user);
        if (user.lastSeenPrivacy === 'nobody' || (!isContact && user.lastSeenPrivacy === 'contacts')) {
            b.lastSeen = null;
            b.isOnline = false;
        }
        if (user.photoPrivacy !== 'everyone' && !isContact)
            b.photoUrl = null;
        if (user.aboutPrivacy !== 'everyone' && !isContact)
            b.about = null;
        return b;
    }
};
exports.UsersService = UsersService;
exports.UsersService = UsersService = __decorate([
    (0, common_1.Injectable)(),
    __param(0, (0, typeorm_1.InjectRepository)(user_entity_1.User)),
    __metadata("design:paramtypes", [typeorm_2.Repository])
], UsersService);
//# sourceMappingURL=users.service.js.map