import { IsString, IsEnum, IsOptional, IsNumber, IsUUID } from 'class-validator';
import { MessageType } from '../../database/entities/message.entity';
export class SendMessageDto {
  @IsUUID() receiverId:string; @IsEnum(MessageType) type:MessageType;
  @IsOptional() @IsString() content?:string; @IsOptional() @IsString() mediaUrl?:string;
  @IsOptional() @IsString() mediaThumbnail?:string; @IsOptional() @IsNumber() mediaSize?:number;
  @IsOptional() @IsString() mimeType?:string; @IsOptional() @IsNumber() latitude?:number;
  @IsOptional() @IsNumber() longitude?:number; @IsOptional() @IsUUID() replyToId?:string;
}
export class EditMessageDto { @IsString() content:string; }
export class ReactMessageDto { @IsString() emoji:string; }
