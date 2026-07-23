"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.AppModule = void 0;
require("reflect-metadata");
const common_1 = require("@nestjs/common");
const config_1 = require("@nestjs/config");
const typeorm_1 = require("@nestjs/typeorm");
const throttler_1 = require("@nestjs/throttler");
const auth_module_1 = require("./auth/auth.module");
const users_module_1 = require("./users/users.module");
const contacts_module_1 = require("./contacts/contacts.module");
const messages_module_1 = require("./messages/messages.module");
const presence_module_1 = require("./presence/presence.module");
const media_module_1 = require("./media/media.module");
const notifications_module_1 = require("./notifications/notifications.module");
const admin_module_1 = require("./admin/admin.module");
let AppModule = class AppModule {
};
exports.AppModule = AppModule;
exports.AppModule = AppModule = __decorate([
    (0, common_1.Module)({ imports: [
            config_1.ConfigModule.forRoot({ isGlobal: true }),
            throttler_1.ThrottlerModule.forRoot([{ ttl: 60000, limit: 100 }]),
            typeorm_1.TypeOrmModule.forRootAsync({ imports: [config_1.ConfigModule], inject: [config_1.ConfigService], useFactory: (c) => ({ type: 'postgres', host: c.get('DB_HOST', 'localhost'), port: c.get('DB_PORT', 5432), username: c.get('DB_USERNAME', 'originconnect'), password: c.get('DB_PASSWORD', ''), database: c.get('DB_NAME', 'originconnect'), entities: [__dirname + '/database/entities/*.entity{.ts,.js}'], synchronize: true, logging: c.get('NODE_ENV') !== 'production', ssl: c.get('DB_SSL') === 'true' ? { rejectUnauthorized: false } : false }) }),
            auth_module_1.AuthModule, users_module_1.UsersModule, contacts_module_1.ContactsModule, messages_module_1.MessagesModule, presence_module_1.PresenceModule, media_module_1.MediaModule, notifications_module_1.NotificationsModule, admin_module_1.AdminModule
        ] })
], AppModule);
//# sourceMappingURL=app.module.js.map