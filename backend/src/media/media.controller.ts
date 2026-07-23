import { Controller, Post, UseGuards, UseInterceptors, UploadedFile, Req, Get, Param } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';
import { FileInterceptor } from '@nestjs/platform-express';
import { MediaService } from './media.service';
@Controller('media') @UseGuards(AuthGuard('jwt'))
export class MediaController {
  constructor(private mediaService:MediaService){}
  @Post('upload') @UseInterceptors(FileInterceptor('file')) upload(@Req() req,@UploadedFile() file:Express.Multer.File){return this.mediaService.saveMedia(req.user.id,file);}
  @Get(':id') get(@Param('id') id:string){return this.mediaService.getMedia(id);}
}
