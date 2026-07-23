import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn } from 'typeorm';
@Entity('otps')
export class Otp {
  @PrimaryGeneratedColumn('uuid') id: string;
  @Column({ length: 20 }) phone: string;
  @Column({ length: 6 }) code: string;
  @Column({ type: 'bigint' }) expiresAt: number;
  @Column({ default: false }) isUsed: boolean;
  @Column({ default: 0 }) attempts: number;
  @CreateDateColumn() createdAt: Date;
}
