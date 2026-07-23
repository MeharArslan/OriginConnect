import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, In } from 'typeorm';
import { Contact } from '../database/entities/contact.entity';
import { User } from '../database/entities/user.entity';
import { UsersService } from '../users/users.service';
import { SyncContactsDto } from './dto/sync-contacts.dto';
@Injectable()
export class ContactsService {
  constructor(@InjectRepository(Contact) private contactRepo:Repository<Contact>,@InjectRepository(User) private userRepo:Repository<User>,private usersService:UsersService){}
  async syncContacts(ownerId:string,dto:SyncContactsDto){
    const users=(await this.userRepo.find({where:{phone:In(dto.phones),isActive:true}})).filter(u=>u.id!==ownerId);
    for(const u of users) await this.contactRepo.createQueryBuilder().insert().into(Contact).values({ownerId,contactUserId:u.id}).orIgnore().execute();
    return {registeredCount:users.length,totalSynced:dto.phones.length,contacts:await this.getContacts(ownerId)};
  }
  async getContacts(ownerId:string){
    const cs=await this.contactRepo.find({where:{ownerId,isBlocked:false}});
    if(!cs.length) return [];
    const users=await this.userRepo.find({where:{id:In(cs.map(c=>c.contactUserId)),isActive:true}});
    const map=new Map(users.map(u=>[u.id,u]));
    return cs.map(c=>{const u=map.get(c.contactUserId);if(!u)return null;return{contactId:c.id,nickname:c.nickname,user:this.usersService.sanitizeForOther(u,true)};}).filter(Boolean);
  }
  async isContact(ownerId:string,targetId:string){return !!(await this.contactRepo.findOne({where:{ownerId,contactUserId:targetId,isBlocked:false}}));}
  async blockContact(ownerId:string,id:string){await this.contactRepo.update({ownerId,contactUserId:id},{isBlocked:true});return{message:'Blocked'};}
  async unblockContact(ownerId:string,id:string){await this.contactRepo.update({ownerId,contactUserId:id},{isBlocked:false});return{message:'Unblocked'};}
}
