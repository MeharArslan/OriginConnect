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
exports.OtpService = void 0;
const common_1 = require("@nestjs/common");
const typeorm_1 = require("@nestjs/typeorm");
const typeorm_2 = require("typeorm");
const config_1 = require("@nestjs/config");
const otp_entity_1 = require("../database/entities/otp.entity");
let OtpService = class OtpService {
    constructor(otpRepo, config) {
        this.otpRepo = otpRepo;
        this.config = config;
    }
    async generate(phone) {
        await this.otpRepo.update({ phone, isUsed: false }, { isUsed: true });
        const code = Math.floor(100000 + Math.random() * 900000).toString();
        await this.otpRepo.save({ phone, code, expiresAt: Date.now() + 600000 });
        if (this.config.get('SMS_PROVIDER', 'mock') === 'mock')
            console.log('[OTP] ' + phone + ' -> ' + code);
        return code;
    }
    async verify(phone, code) {
        const otp = await this.otpRepo.findOne({ where: { phone, code, isUsed: false }, order: { createdAt: 'DESC' } });
        if (!otp)
            throw new common_1.BadRequestException('Invalid OTP');
        if (Date.now() > Number(otp.expiresAt))
            throw new common_1.BadRequestException('OTP expired');
        await this.otpRepo.update(otp.id, { isUsed: true });
        return true;
    }
};
exports.OtpService = OtpService;
exports.OtpService = OtpService = __decorate([
    (0, common_1.Injectable)(),
    __param(0, (0, typeorm_1.InjectRepository)(otp_entity_1.Otp)),
    __metadata("design:paramtypes", [typeorm_2.Repository, config_1.ConfigService])
], OtpService);
//# sourceMappingURL=otp.service.js.map