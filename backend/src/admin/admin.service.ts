import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { User } from '../database/entities/user.entity';
import { Message } from '../database/entities/message.entity';
@Injectable()
export class AdminService {
  constructor(@InjectRepository(User) private u:Repository<User>,@InjectRepository(Message) private m:Repository<Message>){}
  async stats(){const[t,a,msg]=await Promise.all([this.u.count(),this.u.count({where:{isActive:true}}),this.m.count()]);return{totalUsers:t,activeUsers:a,totalMessages:msg};}
  async users(page=1,limit=20){const[users,total]=await this.u.findAndCount({order:{createdAt:'DESC'},skip:(page-1)*limit,take:limit,select:['id','phone','displayName','isActive','isOnline','lastSeen','createdAt']});return{users,total,page,totalPages:Math.ceil(total/limit)};}
  async disable(id:string){await this.u.update(id,{isActive:false,refreshToken:null});return{message:'Disabled'};}
  async enable(id:string){await this.u.update(id,{isActive:true});return{message:'Enabled'};}
  async forceLogout(id:string){await this.u.update(id,{refreshToken:null});return{message:'Done'};}
}
