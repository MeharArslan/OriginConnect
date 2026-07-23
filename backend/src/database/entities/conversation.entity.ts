import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, UpdateDateColumn } from 'typeorm';
@Entity('conversations')
export class Conversation {
  @PrimaryGeneratedColumn('uuid') id: string;
  @Column({ unique: true }) participantKey: string;
  @Column('uuid') participant1Id: string;
  @Column('uuid') participant2Id: string;
  @Column({ nullable: true }) lastMessageId: string;
  @Column({ type: 'text', nullable: true }) lastMessageContent: string;
  @Column({ type: 'bigint', nullable: true }) lastMessageAt: number;
  @Column({ default: 0 }) unreadCount1: number;
  @Column({ default: 0 }) unreadCount2: number;
  @Column({ default: false }) isBlocked: boolean;
  @Column({ nullable: true }) blockedBy: string;
  @CreateDateColumn() createdAt: Date;
  @UpdateDateColumn() updatedAt: Date;
}
