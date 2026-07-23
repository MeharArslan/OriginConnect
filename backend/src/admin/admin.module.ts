import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { AdminController } from './admin.controller';
import { AdminService } from './admin.service';
import { User } from '../database/entities/user.entity';
import { Message } from '../database/entities/message.entity';
@Module({imports:[TypeOrmModule.forFeature([User,Message])],controllers:[AdminController],providers:[AdminService]})
export class AdminModule {}
