import { Controller, Get, Put, Body, Param, UseGuards, Req } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';
import { UsersService } from './users.service';
import { UpdateProfileDto } from './dto/update-profile.dto';
@Controller('users') @UseGuards(AuthGuard('jwt'))
export class UsersController {
  constructor(private usersService: UsersService) {}
  @Get('profile') profile(@Req() req) { return this.usersService.getProfile(req.user.id); }
  @Put('profile') update(@Req() req,@Body() dto:UpdateProfileDto) { return this.usersService.updateProfile(req.user.id,dto); }
  @Get(':id') getUser(@Param('id') id:string) { return this.usersService.getUserById(id); }
}
