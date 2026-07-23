import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { MessagesController } from './messages.controller';
import { MessagesService } from './messages.service';
import { Message } from '../database/entities/message.entity';
import { Conversation } from '../database/entities/conversation.entity';
@Module({imports:[TypeOrmModule.forFeature([Message,Conversation])],controllers:[MessagesController],providers:[MessagesService],exports:[MessagesService]})
export class MessagesModule {}
