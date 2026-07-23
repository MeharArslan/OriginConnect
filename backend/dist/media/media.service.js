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
exports.MediaService = void 0;
const common_1 = require("@nestjs/common");
const typeorm_1 = require("@nestjs/typeorm");
const typeorm_2 = require("typeorm");
const config_1 = require("@nestjs/config");
const path = require("path");
const media_entity_1 = require("../database/entities/media.entity");
let MediaService = class MediaService {
    constructor(mediaRepo, config) {
        this.mediaRepo = mediaRepo;
        this.config = config;
    }
    async saveMedia(uploaderId, file) {
        const base = this.config.get('BASE_URL', 'http://localhost:3000');
        const url = base + '/uploads/' + file.filename;
        let thumbnailUrl, width, height;
        if (file.mimetype.startsWith('image/')) {
            try {
                const sharp = require('sharp');
                const tn = 'thumb_' + file.filename;
                await sharp(file.path).resize(400, 400, { fit: 'inside' }).toFile(path.join(path.dirname(file.path), tn));
                thumbnailUrl = base + '/uploads/' + tn;
                const m = await sharp(file.path).metadata();
                width = m.width;
                height = m.height;
            }
            catch (e) { }
        }
        return this.mediaRepo.save({ uploaderId, filename: file.filename, originalName: file.originalname, mimeType: file.mimetype, size: file.size, url, thumbnailUrl, width, height });
    }
    getMedia(id) { return this.mediaRepo.findOne({ where: { id } }); }
};
exports.MediaService = MediaService;
exports.MediaService = MediaService = __decorate([
    (0, common_1.Injectable)(),
    __param(0, (0, typeorm_1.InjectRepository)(media_entity_1.Media)),
    __metadata("design:paramtypes", [typeorm_2.Repository, config_1.ConfigService])
], MediaService);
//# sourceMappingURL=media.service.js.map