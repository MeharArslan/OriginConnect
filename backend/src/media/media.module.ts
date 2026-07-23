import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { MulterModule } from '@nestjs/platform-express';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { diskStorage } from 'multer';
import { extname } from 'path';
import { v4 as uuid } from 'uuid';
import { MediaController } from './media.controller';
import { MediaService } from './media.service';
import { Media } from '../database/entities/media.entity';
@Module({
  imports:[TypeOrmModule.forFeature([Media]),MulterModule.registerAsync({imports:[ConfigModule],inject:[ConfigService],useFactory:(c:ConfigService)=>({storage:diskStorage({destination:c.get('UPLOAD_PATH','./uploads'),filename:(_,f,cb)=>cb(null,uuid()+extname(f.originalname))}),limits:{fileSize:c.get<number>('MAX_FILE_SIZE',50)*1024*1024}})})],
  controllers:[MediaController],providers:[MediaService],exports:[MediaService]
})
export class MediaModule {}
