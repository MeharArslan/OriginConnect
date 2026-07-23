"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.MediaModule = void 0;
const common_1 = require("@nestjs/common");
const typeorm_1 = require("@nestjs/typeorm");
const platform_express_1 = require("@nestjs/platform-express");
const config_1 = require("@nestjs/config");
const multer_1 = require("multer");
const path_1 = require("path");
const uuid_1 = require("uuid");
const media_controller_1 = require("./media.controller");
const media_service_1 = require("./media.service");
const media_entity_1 = require("../database/entities/media.entity");
let MediaModule = class MediaModule {
};
exports.MediaModule = MediaModule;
exports.MediaModule = MediaModule = __decorate([
    (0, common_1.Module)({
        imports: [typeorm_1.TypeOrmModule.forFeature([media_entity_1.Media]), platform_express_1.MulterModule.registerAsync({ imports: [config_1.ConfigModule], inject: [config_1.ConfigService], useFactory: (c) => ({ storage: (0, multer_1.diskStorage)({ destination: c.get('UPLOAD_PATH', './uploads'), filename: (_, f, cb) => cb(null, (0, uuid_1.v4)() + (0, path_1.extname)(f.originalname)) }), limits: { fileSize: c.get('MAX_FILE_SIZE', 50) * 1024 * 1024 } }) })],
        controllers: [media_controller_1.MediaController], providers: [media_service_1.MediaService], exports: [media_service_1.MediaService]
    })
], MediaModule);
//# sourceMappingURL=media.module.js.map