import { Controller, Get, Post, Body, Param, UseGuards, Req, Delete } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';
import { ContactsService } from './contacts.service';
import { SyncContactsDto } from './dto/sync-contacts.dto';
@Controller('contacts') @UseGuards(AuthGuard('jwt'))
export class ContactsController {
  constructor(private contactsService:ContactsService){}
  @Post('sync') sync(@Req() req,@Body() dto:SyncContactsDto){return this.contactsService.syncContacts(req.user.id,dto);}
  @Get() list(@Req() req){return this.contactsService.getContacts(req.user.id);}
  @Post(':id/block') block(@Req() req,@Param('id') id:string){return this.contactsService.blockContact(req.user.id,id);}
  @Delete(':id/block') unblock(@Req() req,@Param('id') id:string){return this.contactsService.unblockContact(req.user.id,id);}
}
