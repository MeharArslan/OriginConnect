import { Repository } from 'typeorm';
import { ConfigService } from '@nestjs/config';
import { Media } from '../database/entities/media.entity';
export declare class MediaService {
    private mediaRepo;
    private config;
    constructor(mediaRepo: Repository<Media>, config: ConfigService);
    saveMedia(uploaderId: string, file: Express.Multer.File): Promise<Media>;
    getMedia(id: string): Promise<Media>;
}
