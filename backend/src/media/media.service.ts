import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { ConfigService } from '@nestjs/config';
import * as path from 'path';
import { Media } from '../database/entities/media.entity';
@Injectable()
export class MediaService {
  constructor(@InjectRepository(Media) private mediaRepo:Repository<Media>,private config:ConfigService){}
  async saveMedia(uploaderId:string,file:Express.Multer.File):Promise<Media>{
    const base=this.config.get('BASE_URL','http://localhost:3000');
    const url=base+'/uploads/'+file.filename;
    let thumbnailUrl:string,width:number,height:number;
    if(file.mimetype.startsWith('image/')){try{const sharp=require('sharp');const tn='thumb_'+file.filename;await sharp(file.path).resize(400,400,{fit:'inside'}).toFile(path.join(path.dirname(file.path),tn));thumbnailUrl=base+'/uploads/'+tn;const m=await sharp(file.path).metadata();width=m.width;height=m.height;}catch(e){}}
    return this.mediaRepo.save({uploaderId,filename:file.filename,originalName:file.originalname,mimeType:file.mimetype,size:file.size,url,thumbnailUrl,width,height});
  }
  getMedia(id:string){return this.mediaRepo.findOne({where:{id}});}
}
