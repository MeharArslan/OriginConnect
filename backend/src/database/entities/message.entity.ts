import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn } from 'typeorm';
export enum MessageType { TEXT='text', IMAGE='image', VIDEO='video', AUDIO='audio', DOCUMENT='document', LOCATION='location' }
export enum MessageStatus { SENDING='sending', SENT='sent', DELIVERED='delivered', READ='read', FAILED='failed' }
@Entity('messages')
export class Message {
  @PrimaryGeneratedColumn('uuid') id: string;
  @Column('uuid') conversationId: string;
  @Column('uuid') senderId: string;
  @Column('uuid') receiverId: string;
  @Column({ type: 'enum', enum: MessageType, default: MessageType.TEXT }) type: MessageType;
  @Column({ type: 'text', nullable: true }) content: string;
  @Column({ nullable: true }) mediaUrl: string;
  @Column({ nullable: true }) mediaThumbnail: string;
  @Column({ type: 'bigint', nullable: true }) mediaSize: number;
  @Column({ nullable: true }) mimeType: string;
  @Column({ type: 'float', nullable: true }) latitude: number;
  @Column({ type: 'float', nullable: true }) longitude: number;
  @Column({ nullable: true }) replyToId: string;
  @Column({ type: 'jsonb', nullable: true }) reactions: Record<string, string[]>;
  @Column({ type: 'enum', enum: MessageStatus, default: MessageStatus.SENDING }) status: MessageStatus;
  @Column({ default: false }) isDeleted: boolean;
  @Column({ default: false }) isDeletedForEveryone: boolean;
  @Column({ default: false }) isEdited: boolean;
  @Column({ default: false }) isStarred: boolean;
  @Column({ type: 'bigint', nullable: true }) deliveredAt: number;
  @Column({ type: 'bigint', nullable: true }) readAt: number;
  @Column({ type: 'bigint', nullable: true }) editedAt: number;
  @CreateDateColumn() createdAt: Date;
}
