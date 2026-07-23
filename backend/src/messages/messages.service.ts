import { Injectable, NotFoundException, ForbiddenException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Message, MessageStatus } from '../database/entities/message.entity';
import { Conversation } from '../database/entities/conversation.entity';
import { SendMessageDto, EditMessageDto, ReactMessageDto } from './dto/message.dto';
@Injectable()
export class MessagesService {
  constructor(@InjectRepository(Message) private msgRepo:Repository<Message>,@InjectRepository(Conversation) private convRepo:Repository<Conversation>){}
  private key(a:string,b:string){return [a,b].sort().join('_');}
  async getOrCreateConv(p1:string,p2:string){
    const k=this.key(p1,p2);let c=await this.convRepo.findOne({where:{participantKey:k}});
    if(!c){const [id1,id2]=[p1,p2].sort();c=await this.convRepo.save({participantKey:k,participant1Id:id1,participant2Id:id2});}
    return c;
  }
  async sendMessage(senderId:string,dto:SendMessageDto){
    const conv=await this.getOrCreateConv(senderId,dto.receiverId);
    const msg=await this.msgRepo.save({conversationId:conv.id,senderId,receiverId:dto.receiverId,type:dto.type,content:dto.content,mediaUrl:dto.mediaUrl,mediaThumbnail:dto.mediaThumbnail,mediaSize:dto.mediaSize,mimeType:dto.mimeType,latitude:dto.latitude,longitude:dto.longitude,replyToId:dto.replyToId,status:MessageStatus.SENT});
    const isP1=conv.participant1Id===dto.receiverId;
    await this.convRepo.update(conv.id,{lastMessageId:msg.id,lastMessageContent:(dto.content||('['+dto.type+']')).substring(0,100),lastMessageAt:Date.now(),unreadCount1:isP1?conv.unreadCount1+1:conv.unreadCount1,unreadCount2:!isP1?conv.unreadCount2+1:conv.unreadCount2});
    return msg;
  }
  async getConversations(userId:string){
    const cs=await this.convRepo.createQueryBuilder('c').where('c.participant1Id=:id OR c.participant2Id=:id',{id:userId}).orderBy('c.lastMessageAt','DESC').getMany();
    return cs.map(c=>({...c,unreadCount:c.participant1Id===userId?c.unreadCount1:c.unreadCount2,otherUserId:c.participant1Id===userId?c.participant2Id:c.participant1Id}));
  }
  async getMessages(userId:string,conversationId:string,before?:string,limit=30){
    const conv=await this.convRepo.findOne({where:{id:conversationId}});
    if(!conv) throw new NotFoundException();
    if(conv.participant1Id!==userId&&conv.participant2Id!==userId) throw new ForbiddenException();
    const q=this.msgRepo.createQueryBuilder('m').where('m.conversationId=:cid',{cid:conversationId}).andWhere('m.isDeletedForEveryone=false').orderBy('m.createdAt','DESC').take(limit);
    if(before){const ref=await this.msgRepo.findOne({where:{id:before}});if(ref)q.andWhere('m.createdAt<:d',{d:ref.createdAt});}
    return (await q.getMany()).reverse();
  }
  async markRead(userId:string,conversationId:string){
    const conv=await this.convRepo.findOne({where:{id:conversationId}});if(!conv)return;
    await this.msgRepo.createQueryBuilder().update(Message).set({status:MessageStatus.READ,readAt:Date.now()}).where('conversationId=:cid AND receiverId=:uid AND status!=:s',{cid:conversationId,uid:userId,s:MessageStatus.READ}).execute();
    const isP1=conv.participant1Id===userId;
    await this.convRepo.update(conversationId,{unreadCount1:isP1?0:conv.unreadCount1,unreadCount2:!isP1?0:conv.unreadCount2});
    return {conversationId};
  }
  async editMessage(userId:string,msgId:string,dto:EditMessageDto){
    const msg=await this.msgRepo.findOne({where:{id:msgId}});if(!msg)throw new NotFoundException();if(msg.senderId!==userId)throw new ForbiddenException();
    await this.msgRepo.update(msgId,{content:dto.content,isEdited:true,editedAt:Date.now()});return this.msgRepo.findOne({where:{id:msgId}});
  }
  async deleteMessage(userId:string,msgId:string,forEveryone:boolean){
    const msg=await this.msgRepo.findOne({where:{id:msgId}});if(!msg)throw new NotFoundException();if(forEveryone&&msg.senderId!==userId)throw new ForbiddenException();
    if(forEveryone)await this.msgRepo.update(msgId,{isDeletedForEveryone:true,content:null});else await this.msgRepo.update(msgId,{isDeleted:true});
    return {deleted:true,forEveryone};
  }
  async reactMessage(userId:string,msgId:string,dto:ReactMessageDto){
    const msg=await this.msgRepo.findOne({where:{id:msgId}});if(!msg)throw new NotFoundException();
    const r=msg.reactions||{};const users=r[dto.emoji]||[];
    if(users.includes(userId)){r[dto.emoji]=users.filter((u:string)=>u!==userId);if(!r[dto.emoji].length)delete r[dto.emoji];}else r[dto.emoji]=[...users,userId];
    await this.msgRepo.update(msgId,{reactions:r});return r;
  }
  async starMessage(userId:string,msgId:string,star:boolean){const msg=await this.msgRepo.findOne({where:{id:msgId}});if(!msg)throw new NotFoundException();await this.msgRepo.update(msgId,{isStarred:star});return{starred:star};}
}
