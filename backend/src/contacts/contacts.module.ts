import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ContactsController } from './contacts.controller';
import { ContactsService } from './contacts.service';
import { Contact } from '../database/entities/contact.entity';
import { User } from '../database/entities/user.entity';
import { UsersModule } from '../users/users.module';
@Module({imports:[TypeOrmModule.forFeature([Contact,User]),UsersModule],controllers:[ContactsController],providers:[ContactsService],exports:[ContactsService]})
export class ContactsModule {}
