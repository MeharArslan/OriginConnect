import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, UpdateDateColumn } from 'typeorm';
@Entity('users')
export class User {
  @PrimaryGeneratedColumn('uuid') id: string;
  @Column({ unique: true, length: 20 }) phone: string;
  @Column({ length: 100, nullable: true }) displayName: string;
  @Column({ length: 500, nullable: true }) about: string;
  @Column({ nullable: true }) photoUrl: string;
  @Column({ default: false }) isOnline: boolean;
  @Column({ type: 'bigint', nullable: true }) lastSeen: number;
  @Column({ default: true }) isActive: boolean;
  @Column({ nullable: true }) fcmToken: string;
  @Column({ nullable: true }) refreshToken: string;
  @Column({ default: 'everyone' }) lastSeenPrivacy: string;
  @Column({ default: 'everyone' }) photoPrivacy: string;
  @Column({ default: 'everyone' }) aboutPrivacy: string;
  @CreateDateColumn() createdAt: Date;
  @UpdateDateColumn() updatedAt: Date;
}
