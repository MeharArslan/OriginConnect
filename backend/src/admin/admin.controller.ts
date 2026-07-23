import { Controller, Get, Post, Delete, Param, Query, UseGuards } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';
import { AdminService } from './admin.service';
@Controller('admin') @UseGuards(AuthGuard('jwt'))
export class AdminController {
  constructor(private adminService:AdminService){}
  @Get('stats') stats(){return this.adminService.stats();}
  @Get('users') users(@Query('page') p='1',@Query('limit') l='20'){return this.adminService.users(+p,+l);}
  @Delete('users/:id') disable(@Param('id') id:string){return this.adminService.disable(id);}
  @Post('users/:id/enable') enable(@Param('id') id:string){return this.adminService.enable(id);}
  @Post('users/:id/force-logout') fl(@Param('id') id:string){return this.adminService.forceLogout(id);}
}
