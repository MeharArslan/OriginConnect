import 'reflect-metadata';
import { Module } from '@nestjs/common';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ThrottlerModule } from '@nestjs/throttler';
import { AuthModule } from './auth/auth.module';
import { UsersModule } from './users/users.module';
import { ContactsModule } from './contacts/contacts.module';
import { MessagesModule } from './messages/messages.module';
import { PresenceModule } from './presence/presence.module';
import { MediaModule } from './media/media.module';
import { NotificationsModule } from './notifications/notifications.module';
import { AdminModule } from './admin/admin.module';
@Module({imports:[
  ConfigModule.forRoot({isGlobal:true}),
  ThrottlerModule.forRoot([{ttl:60000,limit:100}]),
  TypeOrmModule.forRootAsync({imports:[ConfigModule],inject:[ConfigService],useFactory:(c:ConfigService)=>({type:'postgres',host:c.get('DB_HOST','localhost'),port:c.get<number>('DB_PORT',5432),username:c.get('DB_USERNAME','originconnect'),password:c.get('DB_PASSWORD',''),database:c.get('DB_NAME','originconnect'),entities:[__dirname+'/database/entities/*.entity{.ts,.js}'],synchronize:true,logging:c.get('NODE_ENV')!=='production',ssl:c.get('DB_SSL')==='true'?{rejectUnauthorized:false}:false})}),
  AuthModule,UsersModule,ContactsModule,MessagesModule,PresenceModule,MediaModule,NotificationsModule,AdminModule
]})
export class AppModule {}
