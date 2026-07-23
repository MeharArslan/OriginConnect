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
exports.AuthService = void 0;
const common_1 = require("@nestjs/common");
const jwt_1 = require("@nestjs/jwt");
const typeorm_1 = require("@nestjs/typeorm");
const typeorm_2 = require("typeorm");
const config_1 = require("@nestjs/config");
const bcrypt = require("bcryptjs");
const user_entity_1 = require("../database/entities/user.entity");
const otp_service_1 = require("./otp.service");
let AuthService = class AuthService {
    constructor(userRepo, jwtService, otpService, config) {
        this.userRepo = userRepo;
        this.jwtService = jwtService;
        this.otpService = otpService;
        this.config = config;
    }
    async sendOtp(dto) { const code = await this.otpService.generate(dto.phone); const mock = this.config.get('SMS_PROVIDER', 'mock') === 'mock'; return { message: 'OTP sent', ...(mock && { code }) }; }
    async verifyOtp(dto) {
        await this.otpService.verify(dto.phone, dto.code);
        let user = await this.userRepo.findOne({ where: { phone: dto.phone } });
        const isNewUser = !user;
        if (!user)
            user = await this.userRepo.save({ phone: dto.phone, displayName: dto.phone, lastSeen: Date.now() });
        else
            await this.userRepo.update(user.id, { lastSeen: Date.now() });
        const tokens = await this.generateTokens(user);
        await this.userRepo.update(user.id, { refreshToken: await bcrypt.hash(tokens.refreshToken, 10) });
        return { ...tokens, isNewUser, user: this.sanitize(user) };
    }
    async refreshTokens(userId, rt) {
        const user = await this.userRepo.findOne({ where: { id: userId } });
        if (!user?.refreshToken)
            throw new common_1.UnauthorizedException();
        if (!(await bcrypt.compare(rt, user.refreshToken)))
            throw new common_1.UnauthorizedException();
        const tokens = await this.generateTokens(user);
        await this.userRepo.update(user.id, { refreshToken: await bcrypt.hash(tokens.refreshToken, 10) });
        return tokens;
    }
    async logout(userId) { await this.userRepo.update(userId, { refreshToken: null, isOnline: false }); return { message: 'Logged out' }; }
    async generateTokens(user) {
        const p = { sub: user.id, phone: user.phone };
        const [accessToken, refreshToken] = await Promise.all([this.jwtService.signAsync(p), this.jwtService.signAsync(p, { secret: this.config.get('JWT_REFRESH_SECRET'), expiresIn: this.config.get('JWT_REFRESH_EXPIRES_IN', '30d') })]);
        return { accessToken, refreshToken };
    }
    sanitize(user) { const { refreshToken, fcmToken, ...safe } = user; return safe; }
};
exports.AuthService = AuthService;
exports.AuthService = AuthService = __decorate([
    (0, common_1.Injectable)(),
    __param(0, (0, typeorm_1.InjectRepository)(user_entity_1.User)),
    __metadata("design:paramtypes", [typeorm_2.Repository, jwt_1.JwtService, otp_service_1.OtpService, config_1.ConfigService])
], AuthService);
//# sourceMappingURL=auth.service.js.map