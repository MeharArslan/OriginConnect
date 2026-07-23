import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, In } from 'typeorm';
import { User } from '../database/entities/user.entity';
import { UpdateProfileDto } from './dto/update-profile.dto';
@Injectable()
export class UsersService {
  constructor(@InjectRepository(User) private userRepo: Repository<User>) {}
  async getProfile(id: string) { const u=await this.userRepo.findOne({where:{id}}); if(!u) throw new NotFoundException(); return this.sanitize(u); }
  async updateProfile(id: string, dto: UpdateProfileDto) { await this.userRepo.update(id,dto); return this.getProfile(id); }
  async getUsersByPhones(phones: string[]) { if(!phones.length) return []; return this.userRepo.find({where:{phone:In(phones),isActive:true}}); }
  async getUserById(id: string) { const u=await this.userRepo.findOne({where:{id,isActive:true}}); if(!u) throw new NotFoundException(); return u; }
  async setOnlineStatus(id: string, isOnline: boolean) { await this.userRepo.update(id,{isOnline,...(!isOnline&&{lastSeen:Date.now()})}); }
  sanitize(user: User) { const {refreshToken,fcmToken,...safe}=user as any; return safe; }
  sanitizeForOther(user: User, isContact: boolean) {
    const b=this.sanitize(user) as any;
    if(user.lastSeenPrivacy==='nobody'||(!isContact&&user.lastSeenPrivacy==='contacts')){b.lastSeen=null;b.isOnline=false;}
    if(user.photoPrivacy!=='everyone'&&!isContact) b.photoUrl=null;
    if(user.aboutPrivacy!=='everyone'&&!isContact) b.about=null;
    return b;
  }
}
