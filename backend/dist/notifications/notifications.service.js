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
Object.defineProperty(exports, "__esModule", { value: true });
exports.NotificationsService = void 0;
const common_1 = require("@nestjs/common");
const config_1 = require("@nestjs/config");
let NotificationsService = class NotificationsService {
    constructor(config) {
        this.config = config;
        this.logger = new common_1.Logger('FCM');
        this.init();
    }
    init() { try {
        const pid = this.config.get('FCM_PROJECT_ID');
        if (!pid) {
            this.logger.warn('FCM not configured');
            return;
        }
        const admin = require('firebase-admin');
        if (!admin.apps.length)
            this.app = admin.initializeApp({ credential: admin.credential.cert({ projectId: pid, privateKey: this.config.get('FCM_PRIVATE_KEY')?.replace(/\\n/g, '\n'), clientEmail: this.config.get('FCM_CLIENT_EMAIL') }) });
        else
            this.app = admin.apps[0];
    }
    catch (e) {
        this.logger.error(e.message);
    } }
    async sendMessage(token, d) {
        if (!this.app || !token)
            return;
        try {
            await require('firebase-admin').messaging().send({ token, notification: { title: d.senderName, body: d.content || ('[' + d.messageType + ']'), imageUrl: d.senderPhoto }, data: { type: 'new_message', senderId: d.senderId, conversationId: d.conversationId, messageId: d.messageId }, android: { priority: 'high', notification: { channelId: 'originconnect_messages', sound: 'default' } } });
        }
        catch (e) {
            this.logger.error(e.message);
        }
    }
};
exports.NotificationsService = NotificationsService;
exports.NotificationsService = NotificationsService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [config_1.ConfigService])
], NotificationsService);
//# sourceMappingURL=notifications.service.js.map