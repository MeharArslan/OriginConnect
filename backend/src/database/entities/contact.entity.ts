import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, Unique } from 'typeorm';
@Entity('contacts') @Unique(['ownerId', 'contactUserId'])
export class Contact {
  @PrimaryGeneratedColumn('uuid') id: string;
  @Column('uuid') ownerId: string;
  @Column('uuid') contactUserId: string;
  @Column({ nullable: true, length: 100 }) nickname: string;
  @Column({ default: false }) isBlocked: boolean;
  @CreateDateColumn() createdAt: Date;
}
