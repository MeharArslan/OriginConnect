import { Controller, Get, Put, Body, Param, UseGuards, Req } from "@nestjs/common";
import { AuthGuard } from "@nestjs/passport";
import { UsersService } from "./users.service";
import { UpdateProfileDto } from "./dto/update-profile.dto";

@Controller("users")
@UseGuards(AuthGuard("jwt"))
export class UsersController {
  constructor(private usersService: UsersService) {}

  @Get("profile")
  getProfile(@Req() req) { return this.usersService.getProfile(req.user.id); }

  @Put("profile")
  updateProfile(@Req() req, @Body() dto: UpdateProfileDto) {
    return this.usersService.updateProfile(req.user.id, dto);
  }

  @Put("photo")
  updatePhoto(@Req() req, @Body("photoUrl") photoUrl: string) {
    return this.usersService.updatePhoto(req.user.id, photoUrl);
  }

  @Get(":id")
  getUser(@Param("id") id: string) { return this.usersService.getUserById(id); }
}
