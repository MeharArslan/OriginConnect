import { MediaService } from './media.service';
export declare class MediaController {
    private mediaService;
    constructor(mediaService: MediaService);
    upload(req: any, file: Express.Multer.File): Promise<import("../database/entities/media.entity").Media>;
    get(id: string): Promise<import("../database/entities/media.entity").Media>;
}
