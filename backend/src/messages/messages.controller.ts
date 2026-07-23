import { Controller, Get, Post, Put, Delete, Body, Param, Query, UseGuards, Req, HttpCode } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';
import { MessagesService } from './messages.service';
import { SendMessageDto, EditMessageDto, ReactMessageDto } from './dto/message.dto';
@Controller() @UseGuards(AuthGuard('jwt'))
export class MessagesController {
  constructor(private messagesService:MessagesService){}
  @Get('conversations') convs(@Req() req){return this.messagesService.getConversations(req.user.id);}
  @Post('messages') send(@Req() req,@Body() dto:SendMessageDto){return this.messagesService.sendMessage(req.user.id,dto);}
  @Get('conversations/:id/messages') msgs(@Req() req,@Param('id') id:string,@Query('before') b?:string){return this.messagesService.getMessages(req.user.id,id,b);}
  @Post('conversations/:id/read') @HttpCode(200) read(@Req() req,@Param('id') id:string){return this.messagesService.markRead(req.user.id,id);}
  @Put('messages/:id') edit(@Req() req,@Param('id') id:string,@Body() dto:EditMessageDto){return this.messagesService.editMessage(req.user.id,id,dto);}
  @Delete('messages/:id') del(@Req() req,@Param('id') id:string,@Query('forEveryone') fe:string){return this.messagesService.deleteMessage(req.user.id,id,fe==='true');}
  @Post('messages/:id/react') @HttpCode(200) react(@Req() req,@Param('id') id:string,@Body() dto:ReactMessageDto){return this.messagesService.reactMessage(req.user.id,id,dto);}
  @Post('messages/:id/star') @HttpCode(200) star(@Req() req,@Param('id') id:string,@Body('star') s:boolean){return this.messagesService.starMessage(req.user.id,id,s);}
}
